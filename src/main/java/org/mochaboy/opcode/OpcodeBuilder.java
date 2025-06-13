package org.mochaboy.opcode;

import org.mochaboy.CPU;
import org.mochaboy.DataType;
import org.mochaboy.opcode.operations.*;

public class OpcodeBuilder {
    private final CPU cpu;
    private final OpcodeWrapper opcodeWrapper;
    private final FlagCalculator flagCalculator;
    private boolean checkIncDec;

    public OpcodeBuilder(CPU cpu, OpcodeWrapper opcodeWrapper) {
        this.cpu = cpu;
        this.opcodeWrapper = opcodeWrapper;
        this.flagCalculator = new FlagCalculator();
    }

    public Opcode build(int predefinedOpcode, boolean isPrefixed) {
        return build(
                0x00, predefinedOpcode, isPrefixed
        );
    }

    public Opcode build(int fetchedAt, int opcode, boolean isPrefixed) {
        if (fetchedAt == 0x0A) {
            //System.out.println();
        }
        Opcode opcodeObject = new Opcode();
        opcodeObject.setFetchedAt(fetchedAt);
        String hexKey = String.format("0x%02X", opcode);
        opcodeObject.setOpcodeHex(hexKey);
        OpcodeInfo opcodeInfo = isPrefixed ?
                opcodeWrapper.getCbprefixed().get(hexKey) : opcodeWrapper.getUnprefixed().get(hexKey);
        opcodeObject.setOpcodeInfo(opcodeInfo);


        //Build opcode
        buildMicroOpsFromOperands(opcodeObject, opcodeInfo);
        buildOpsFromMnemonics(opcodeObject, opcodeInfo);

        //Queue up a flag process op if needed
        Flags f = opcodeInfo.getFlags();
        if (!f.getC().equals("-") || !f.getH().equals("-") || !f.getN().equals("-") || !f.getZ().equals("-")) {
            if (!opcodeObject.getOpcodeInfo().getMnemonic().equals("DAA")) {
                opcodeObject.addOp(new HandleFlags(flagCalculator, cpu, opcodeInfo,
                        opcodeObject::getDestinationValue, opcodeObject::getSourceValue));
            }

        }

        //Determine if this is LD [HL-/+]
        if (checkIncDec) {
            handlePostIncDec(opcodeObject, opcodeInfo);
        }

        //
        //opcodeObject.setCyclesConsumed(calculateCycles(opcodeObject));
        opcodeObject.addOp(
                new CalculateCycles(opcodeObject)
        );

        return opcodeObject;
    }

    //Builds an opcode object, setting up micro operations
    //TODO: noteworthy/odd opcodes to consider:
    /*

    LD [n16],SP
    Copy SP & $FF at address n16 and SP >> 8 at address n16 + 1.
    Cycles: 5
    Bytes: 3
    Flags: None affected.

     */
    private void buildMicroOpsFromOperands(Opcode opcodeObject, OpcodeInfo opcodeInfo) {
        Operand[] operands = opcodeInfo.getOperands();
        Operand d = operands.length > 0 ? operands[0] : null;
        Operand s = operands.length > 1 ? operands[1] : null;
        Operand x = operands.length > 2 ? operands[2] : null;
        int result;

        //Handle destination operand
        if (d != null) {
            handleDestinationOperand(opcodeObject, d);
        }

        //Handle source operand
        if (s != null) {
            handleSourceOperand(opcodeObject, s, opcodeInfo);
        }

        if (operands.length > 2) {
            //This must be LD HL, SP+e8
            opcodeObject.addOp(
                    new ReadImmediate8bit(opcodeObject::setExtraValue)
            );
            opcodeObject.addOp(
                    new EmptyCycle() //Because this opcode needs 3 cycles
            );
        }

    }

    private void handleDestinationOperand(Opcode opcodeObject, Operand d) {
        opcodeObject.setDestinationOperandString(d.getName());
        opcodeObject.setDestinationOperand(d);
        switch (d.getName()) {
            case "e8" -> {
                //Should only appear in JR n8 (aka JR n16)
                //Will end in imaginary ADD PC, e8? so setup source value here
                opcodeObject.setSourceType(DataType.E8);
                opcodeObject.addOp(new ReadImmediate8bit(opcodeObject::setSourceValue));
            }
            case "a8" -> {
                //Only occurs in LDH
                opcodeObject.setDestinationType(DataType.A8);
                opcodeObject.addOp(
                        new ReadImmediate8bit(opcodeObject::setDestinationValue, true) //Adds offset of FF00
                );
            }
            case "a16" -> {
                //This is an address
                //2 cycles to read 16-bit address
                //Gets the low and high bytes, stores them temporarily in the opcode's dest and source values,
                //then merges them together to be stored in dest value. Source val is overwritten later.
                opcodeObject.setDestinationType(DataType.N16);
                opcodeObject.addOp(new ReadImmediate8bit(opcodeObject::setDestinationValue));
                opcodeObject.addOp(new ReadImmediate8bit(opcodeObject::setSourceValue));
                opcodeObject.addOp(
                        new MergeOperands(
                                opcodeObject::getDestinationValue, opcodeObject::getSourceValue,
                                opcodeObject::setDestinationValue)
                );
            }
            case "A", "B", "C", "D", "E", "H", "L" -> {
                //Operand C could refer to register C, or flag condition cc, depending on the mnemonic
                //Explicitly set a condition if so and break
                OpcodeInfo o = opcodeObject.getOpcodeInfo();
                if (o.getMnemonic().equals("CALL")
                        || o.getMnemonic().equals("JP")
                        || o.getMnemonic().equals("JR")
                        || o.getMnemonic().equals("RET")) {
                    opcodeObject.setCc("C");
                    break;
                }

                if (!d.isImmediate()) {
                    //If it's an 8b register and not immediate, it must be LD [C], A
                    opcodeObject.setDestinationType(DataType.A8);
                } else {
                    opcodeObject.setDestinationType(DataType.R8);
                }
                opcodeObject.addOp(
                        new ReadRegister8Bit(opcodeObject::setDestinationValue, d.getName())
                );
            }
            case "AF", "BC", "DE", "HL", "SP", "PC" -> {
                //Signal for post increment/decrement as in LD [HL-]/[HL+] A
                if (d.isIncrement() || d.isDecrement()) checkIncDec = true;

                //If not an immediate value, read the address held in a 16-bit register
                if (!d.isImmediate()) {
                    opcodeObject.setDestinationType(DataType.N16);
                    opcodeObject.addOp(new ReadRegister16Bit(opcodeObject::setDestinationValue, d.getName()));
                } else {
                    //This is a register. Set opcodeObject.destinationValue to the value held in that register
                    opcodeObject.setDestinationType(DataType.R16);
                    opcodeObject.addOp(
                            new ReadRegister16Bit(opcodeObject::setDestinationValue, d.getName()));
                }
            }
            //RES - set bit u3 to 0 in r8 or [HL]
            //BIT - test bit u3 in r8
            case "0", "1", "2", "3", "4", "5", "6", "7", "8", "9" -> {
                opcodeObject.setDestinationValue(Integer.parseInt(d.getName()));
            }
            case "Z", "NZ", "NC" -> {
                opcodeObject.setCc(d.getName()); //Other flag conditions
            }
            case "$00", "$08", "$10", "$18", "$20", "$28", "$30", "$38" -> {
            }
        }

    }

    private void handleSourceOperand(Opcode opcodeObject, Operand s, OpcodeInfo opcodeInfo) {
        opcodeObject.setSourceOperandString(s.getName());
        opcodeObject.setSourceOperand(s);
        switch (s.getName()) {
            case "n8":
                opcodeObject.setSourceType(DataType.N8);
                opcodeObject.addOp(
                        new ReadImmediate8bit(opcodeObject::setSourceValue)
                );
                break;
            case "a8":
                opcodeObject.setSourceType(DataType.A8);
                opcodeObject.addOp(
                        new ReadImmediate8bit(opcodeObject::setSourceValue, true)
                );
                //opcodeObject.addOp(new ReadMemory8Bit(opcodeObject::setSourceValue, opcodeObject::getSourceValue));
                break;
            case "n16":
                //Read integer constant value, then merge, set as operand 2
                opcodeObject.setSourceType(DataType.N16);
                opcodeObject.addOp(
                        new ReadImmediate8bit(opcodeObject::setSourceValue)
                );
                opcodeObject.addOp(
                        new ReadImmediate8bit(opcodeObject::setExtraValue)
                );
                opcodeObject.addOp(
                        new FlipBytes(
                                opcodeObject::getSourceValue, opcodeObject::getExtraValue, opcodeObject::setSourceValue)
                );
                break;

            case "a16":
                opcodeObject.setSourceType(DataType.N16);
                opcodeObject.addOp(
                        new ReadImmediate8bit(opcodeObject::setDestinationValue)
                );
                opcodeObject.addOp(
                        new ReadImmediate8bit(opcodeObject::setSourceValue)
                );

                //Change where MergeOperands result is stored depending on whether of not this is a call
                //Trust me bro
                OpcodeInfo o = opcodeObject.getOpcodeInfo();
                if (o.getMnemonic().equals("CALL")
                        || o.getMnemonic().equals("JP")
                        || o.getMnemonic().equals("JR")
                        || o.getMnemonic().equals("RET")) {
                    opcodeObject.addOp(
                            new MergeOperands(
                                    opcodeObject::getDestinationValue, opcodeObject::getSourceValue, opcodeObject::setDestinationValue)
                    );
                    break;
                } else {
                    opcodeObject.addOp(
                            new MergeOperands(
                                    opcodeObject::getDestinationValue, opcodeObject::getSourceValue, opcodeObject::setSourceValue)
                    );
                }

                opcodeObject.addOp(new ReadMemory8Bit(
                        opcodeObject::setSourceValue, opcodeObject::getSourceValue
                ));
                break;
            case "e8":
                opcodeObject.setSourceType(DataType.E8);
                opcodeObject.addOp(new ReadImmediate8bit(opcodeObject::setSourceValue));
                opcodeObject.addOp(new EmptyCycle()); //To make sure ADD SP, e8 takes 4 cycles
                break;
            case "A":
            case "B":
            case "C":
            case "D":
            case "E":
            case "H":
            case "L":
                if (opcodeObject.getSourceOperand().isImmediate()) {
                    opcodeObject.setSourceType(DataType.R8);
                    String m = opcodeInfo.getMnemonic();
                    //Stupid check to make sure INC and DEC get handled correctly. Trust me
                    if (m.equals("INC") || m.equals("DEC")) {
                        opcodeObject.addOp(new ReadRegister8Bit(opcodeObject::setDestinationValue, s.getName()));
                        break;
                    } else opcodeObject.addOp(new ReadRegister8Bit(opcodeObject::setSourceValue, s.getName()));
                    break;
                } else {
                    opcodeObject.setSourceType(DataType.A8);
                    opcodeObject.addOp(new ReadRegister8Bit(opcodeObject::setSourceValue, s.getName()));
                    //opcodeObject.addOp(new ReadMemory8Bit(opcodeObject::setSourceValue, opcodeObject::getSourceValue));
                }
                break;
            case "AF":
            case "BC":
            case "DE":
            case "HL":
            case "SP":
            case "PC":
                if (s.isIncrement() || s.isDecrement()) checkIncDec = true;
                if (opcodeObject.getSourceOperand().isImmediate()) {
                    opcodeObject.setSourceType(DataType.R16);
                    opcodeObject.addOp(
                            new ReadRegister16Bit(opcodeObject::setSourceValue, s.getName())
                    );
                    break;
                } else {
                    opcodeObject.setSourceType(DataType.N16);
                    opcodeObject.addOp(
                            new ReadRegister16Bit(opcodeObject::setSourceValue, s.getName())
                    );
                    opcodeObject.addOp(
                            new ReadMemory8Bit(opcodeObject::setSourceValue, opcodeObject::getSourceValue)
                    );
                    break;
                }
        }
    }

    private void buildOpsFromMnemonics(Opcode opcodeObject, OpcodeInfo opcodeInfo) {
        String m = opcodeInfo.getMnemonic();
        switch (m) {
            //ALU operations
            case "ADC" -> opcodeObject.addOp(
                    new AluOperation(AluOperation.Type.ADC, opcodeObject, opcodeObject::getDestinationValue, opcodeObject::getSourceValue)
            );
            case "ADD" -> {
                opcodeObject.addOp(
                        new AluOperation(AluOperation.Type.ADD, opcodeObject, opcodeObject::getDestinationValue, opcodeObject::getSourceValue)
                );
            }
            case "CP" -> opcodeObject.addOp(
                    new AluOperation(AluOperation.Type.CP, opcodeObject, opcodeObject::getDestinationValue, opcodeObject::getSourceValue)
            );
            case "DEC" -> {
                opcodeObject.addOp(
                        new AluOperation(AluOperation.Type.DEC, opcodeObject, opcodeObject::getDestinationValue, opcodeObject::getSourceValue)
                );
            }
            case "INC" -> opcodeObject.addOp(
                    new AluOperation(AluOperation.Type.INC, opcodeObject, opcodeObject::getDestinationValue, opcodeObject::getSourceValue)
            );
            case "SBC" -> opcodeObject.addOp(
                    new AluOperation(AluOperation.Type.SBC, opcodeObject, opcodeObject::getDestinationValue, opcodeObject::getSourceValue)
            );
            case "SUB" -> opcodeObject.addOp(
                    new AluOperation(AluOperation.Type.SUB, opcodeObject, opcodeObject::getDestinationValue, opcodeObject::getSourceValue)
            );


            //LD Operations
            case "LD", "LDH" -> {
                // special‑case opcode 0x08 :  LD (a16),SP
                if (opcodeObject.getOpcodeInfo().getOpcode() == 0x08) {
                    //Write a 16 bits at address a16, one byte at a time per cycle
                    opcodeObject.addOp(
                            new WriteMemory8Bit(
                                    opcodeObject::getDestinationValue,
                                    () -> opcodeObject.getSourceValue() & 0x00FF
                            )
                    );
                    opcodeObject.addOp(
                            new WriteMemory8Bit(
                                    () -> (opcodeObject.getDestinationValue() + 1) & 0xFFFF,
                                    () -> (opcodeObject.getSourceValue() >>> 8) & 0x00FF
                            )
                    );
                } else {
                    opcodeObject.addOp(new Load(opcodeObject));
                }
            }

            //Bitwise operations
            case "AND" -> opcodeObject.addOp(
                    new BitwiseOperation(BitwiseOperation.Type.AND, opcodeObject, opcodeObject::getSourceValue)
            );
            case "CPL" -> opcodeObject.addOp(
                    new BitwiseOperation(BitwiseOperation.Type.CPL, opcodeObject, opcodeObject::getSourceValue)
            );
            case "OR" -> opcodeObject.addOp(
                    new BitwiseOperation(BitwiseOperation.Type.OR, opcodeObject, opcodeObject::getSourceValue)
            );
            case "XOR" -> opcodeObject.addOp(
                    new BitwiseOperation(BitwiseOperation.Type.XOR, opcodeObject, opcodeObject::getSourceValue)
            );

            //Bit flag operations
            case "BIT" -> {
            } //Changes no values, is implied in FlagCalculator
            case "RES" -> opcodeObject.addOp(
                    new BitFlagOperation(BitFlagOperation.Type.RES, opcodeObject, opcodeObject::getDestinationValue, opcodeObject::getSourceValue)
            );
            case "SET" -> opcodeObject.addOp(
                    new BitFlagOperation(BitFlagOperation.Type.SET, opcodeObject, opcodeObject::getDestinationValue, opcodeObject::getSourceValue)
            );

            //Bit shift operations
            case "RL" -> {
                opcodeObject.addOp(
                        new BitShiftOperation(BitShiftOperation.Type.RL, opcodeObject, opcodeObject::getDestinationValue)
                );
            }
            case "RLA" -> {
                opcodeObject.addOp(new ReadRegister8Bit(opcodeObject::setDestinationValue, "A"));
                opcodeObject.addOp(
                        new BitShiftOperation(BitShiftOperation.Type.RLA, opcodeObject, opcodeObject::getDestinationValue)
                );
            }
            case "RLC" -> {
                opcodeObject.addOp(
                        new BitShiftOperation(BitShiftOperation.Type.RLC, opcodeObject, opcodeObject::getDestinationValue)
                );
            }
            case "RLCA" -> {
                opcodeObject.addOp(new ReadRegister8Bit(opcodeObject::setDestinationValue, "A"));
                opcodeObject.addOp(
                        new BitShiftOperation(BitShiftOperation.Type.RLCA, opcodeObject, opcodeObject::getDestinationValue)
                );
            }
            case "RR" -> {
                opcodeObject.addOp(
                        new BitShiftOperation(BitShiftOperation.Type.RR, opcodeObject, opcodeObject::getDestinationValue)
                );
            }
            case "RRA" -> {
                opcodeObject.addOp(new ReadRegister8Bit(opcodeObject::setDestinationValue, "A"));
                opcodeObject.addOp(
                        new BitShiftOperation(BitShiftOperation.Type.RRA, opcodeObject, opcodeObject::getDestinationValue)
                );
            }
            case "RRC" -> {
                opcodeObject.addOp(
                        new BitShiftOperation(BitShiftOperation.Type.RRC, opcodeObject, opcodeObject::getDestinationValue)
                );
            }
            case "RRCA" -> {
                opcodeObject.addOp(new ReadRegister8Bit(opcodeObject::setDestinationValue, "A"));
                opcodeObject.addOp(
                        new BitShiftOperation(BitShiftOperation.Type.RRCA, opcodeObject, opcodeObject::getDestinationValue)
                );
            }
            case "SLA" -> {
                opcodeObject.addOp(
                        new BitShiftOperation(BitShiftOperation.Type.SLA, opcodeObject, opcodeObject::getDestinationValue)
                );
            }
            case "SRA" -> {
                opcodeObject.addOp(
                        new BitShiftOperation(BitShiftOperation.Type.SRA, opcodeObject, opcodeObject::getDestinationValue)
                );
            }
            case "SRL" -> {
                opcodeObject.addOp(
                        new BitShiftOperation(BitShiftOperation.Type.SRL, opcodeObject, opcodeObject::getDestinationValue)
                );
            }
            case "SWAP" -> {
                opcodeObject.addOp(
                        new BitShiftOperation(BitShiftOperation.Type.SWAP, opcodeObject, opcodeObject::getDestinationValue)
                );
            }

            //Subroutine instructions
            case "CALL" -> {
                //Read address target from immediate, merge, store into sourceValue of opcode
//                opcodeObject.addOp(
//                        new ReadImmediate8bit(opcodeObject::setDestinationValue));
//                opcodeObject.addOp(
//                        new ReadImmediate8bit(opcodeObject::setSourceValue));
//                opcodeObject.addOp(
//                        new MergeOperands(
//                            opcodeObject::getDestinationValue,
//                            opcodeObject::getSourceValue,
//                            opcodeObject::setSourceValue));

                //Check conditions cc
                if (opcodeObject.getCc() != null) {
                    switch (opcodeObject.getCc()) {
                        case "Z" -> opcodeObject.addOp(new CheckCC(CheckCC.Type.Z, opcodeObject));
                        case "NZ" -> opcodeObject.addOp(new CheckCC(CheckCC.Type.NZ, opcodeObject));
                        case "C" -> opcodeObject.addOp(new CheckCC(CheckCC.Type.C, opcodeObject));
                        case "NC" -> opcodeObject.addOp(new CheckCC(CheckCC.Type.NC, opcodeObject));
                    }
                }

                opcodeObject.addOp(new EmptyCycle());

                //Push current PC onto stack
                opcodeObject.addOp(
                        new StackOperation(StackOperation.Type.PUSH_HIGH,
                                opcodeObject,
                                () -> cpu.getRegisters().getPC())
                );
                opcodeObject.addOp(
                        new StackOperation(StackOperation.Type.PUSH_LOW,
                                opcodeObject,
                                () -> cpu.getRegisters().getPC())
                );
                //Set PC to target stored in sourceValue by doing LD PC, n16
                opcodeObject.setDestinationOperandString("PC");
                opcodeObject.setDestinationType(DataType.R16);
                opcodeObject.setSourceOperandString("n16");
                opcodeObject.setSourceType(DataType.N16);
                opcodeObject.addOp(new FlipOperands(opcodeObject));
                opcodeObject.addOp(
                        new Load(opcodeObject)
                );
            }

            case "JP" -> {
                String ds = opcodeObject.getDestinationOperandString();
                if (ds.equals("HL")) {
                    //JP HL, do imaginary LD PC HL
                    opcodeObject.setDestinationOperandString("PC");
                    opcodeObject.setSourceType(DataType.R16);
                    opcodeObject.setSourceOperandString("HL");
                    opcodeObject.addOp(new ReadRegister16Bit(opcodeObject::setSourceValue, "HL"));
                    opcodeObject.addOp(new Load(opcodeObject));
                } else if (ds.equals("n16") || ds.equals("a16") || ds.equals("Z") || ds.equals("NZ") || ds.equals("C") || ds.equals("NC")) {
                    //JP n16/JP cc,n16, read n16 then do imaginary LD PC n16
//                    opcodeObject.addOp(new ReadImmediate8bit(opcodeObject::setDestinationValue));
//                    opcodeObject.addOp(new ReadImmediate8bit(opcodeObject::setSourceValue));
//                    opcodeObject.addOp(new MergeOperands(opcodeObject::getDestinationValue, opcodeObject::getSourceValue,
//                            opcodeObject::setSourceValue));
                    opcodeObject.setDestinationOperandString("PC");
                    opcodeObject.setDestinationType(DataType.R16);
                    opcodeObject.setSourceOperandString("n16");
                    opcodeObject.setSourceType(DataType.N16);
                    if (opcodeObject.getCc() != null) {
                        switch (opcodeObject.getCc()) {
                            case "Z" -> opcodeObject.addOp(new CheckCC(CheckCC.Type.Z, opcodeObject));
                            case "NZ" -> opcodeObject.addOp(new CheckCC(CheckCC.Type.NZ, opcodeObject));
                            case "C" -> opcodeObject.addOp(new CheckCC(CheckCC.Type.C, opcodeObject));
                            case "NC" -> opcodeObject.addOp(new CheckCC(CheckCC.Type.NC, opcodeObject));
                        }
                    }
                    opcodeObject.addOp(new FlipOperands(opcodeObject));
                    opcodeObject.addOp(new Load(opcodeObject));
                }
            }

            case "JR" -> {
                //Read 8-bit offset, check for conditions
                //opcodeObject.addOp(new ReadImmediate8bit(opcodeObject::setSourceValue));
                if (opcodeObject.getCc() != null) {
                    switch (opcodeObject.getCc()) {
                        case "Z" -> opcodeObject.addOp(new CheckCC(CheckCC.Type.Z, opcodeObject));
                        case "NZ" -> opcodeObject.addOp(new CheckCC(CheckCC.Type.NZ, opcodeObject));
                        case "C" -> opcodeObject.addOp(new CheckCC(CheckCC.Type.C, opcodeObject));
                        case "NC" -> opcodeObject.addOp(new CheckCC(CheckCC.Type.NC, opcodeObject));
                    }
                }
                //If no cc or conditions satisfied, idle once and add the offset to PC
                opcodeObject.addOp(new EmptyCycle());
                opcodeObject.setDestinationOperandString("PC");
                opcodeObject.setDestinationType(DataType.R16);
                //opcodeObject.addOp(new FlipOperands(opcodeObject));
                opcodeObject.addOp(new AluOperation(
                        AluOperation.Type.ADD,
                        opcodeObject,
                        () -> cpu.getRegisters().getPC(),
                        opcodeObject::getSourceValue
                ));

            }

            case "RET", "RETI" -> {
                if (opcodeObject.getCc() != null) {
                    opcodeObject.addOp(new EmptyCycle());
                    switch (opcodeObject.getCc()) {
                        case "Z" -> opcodeObject.addOp(new CheckCC(CheckCC.Type.Z, opcodeObject));
                        case "NZ" -> opcodeObject.addOp(new CheckCC(CheckCC.Type.NZ, opcodeObject));
                        case "C" -> opcodeObject.addOp(new CheckCC(CheckCC.Type.C, opcodeObject));
                        case "NC" -> opcodeObject.addOp(new CheckCC(CheckCC.Type.NC, opcodeObject));
                    }
                }
                opcodeObject.addOp(new ReadMemory8Bit(opcodeObject::setDestinationValue, () -> cpu.getRegisters().getSP()));
                opcodeObject.addOp(new ReadMemory8Bit(opcodeObject::setSourceValue, () -> (cpu.getRegisters().getSP() + 1) & 0xFFFF));
                opcodeObject.addOp(
                        new MergeOperands(opcodeObject::getDestinationValue, opcodeObject::getSourceValue, opcodeObject::setSourceValue)
                );
                opcodeObject.setDestinationOperandString("PC");

                opcodeObject.addOp(
                        new StackOperation(StackOperation.Type.POP, opcodeObject, opcodeObject::getSourceValue)
                );

                if (opcodeObject.getOpcodeInfo().getMnemonic().equals("RETI")) {
                    //SET IME HERE
                }
            }

            case "RST" -> {
                opcodeObject.addOp(new EmptyCycle());
                String ds = opcodeObject.getDestinationOperandString();
                switch (ds) {
                    case "$00" -> opcodeObject.setSourceValue(0x0000);
                    case "$08" -> opcodeObject.setSourceValue(0x0008);
                    case "$10" -> opcodeObject.setSourceValue(0x0010);
                    case "$18" -> opcodeObject.setSourceValue(0x0018);
                    case "$20" -> opcodeObject.setSourceValue(0x0020);
                    case "$28" -> opcodeObject.setSourceValue(0x0028);
                    case "$30" -> opcodeObject.setSourceValue(0x0030);
                    case "$38" -> opcodeObject.setSourceValue(0x0038);
                }

                //Push current PC onto stack
                opcodeObject.addOp(
                        new StackOperation(StackOperation.Type.PUSH_HIGH,
                                opcodeObject,
                                () -> cpu.getRegisters().getPC())
                );
                opcodeObject.addOp(
                        new StackOperation(StackOperation.Type.PUSH_LOW,
                                opcodeObject,
                                () -> cpu.getRegisters().getPC())
                );

                opcodeObject.setDestinationOperandString("PC");
                opcodeObject.setDestinationType(DataType.R16);
                opcodeObject.setSourceType(DataType.N16);
                opcodeObject.setSourceOperandString("n16");
                opcodeObject.addOp(new Load(opcodeObject));
            }

            //Carry flag instructions
            case "CCF" -> {
                //Pure flag calculation
            }
            case "SCF" -> {
                //Pure flag calculation
            }

            //Stack instructions
            case "POP" -> {
                opcodeObject.addOp(new ReadMemory8Bit(opcodeObject::setDestinationValue, () -> cpu.getRegisters().getSP()));
                opcodeObject.addOp(new ReadMemory8Bit(opcodeObject::setSourceValue, () -> (cpu.getRegisters().getSP() + 1) & 0xFFFF));
                opcodeObject.addOp(
                        new MergeOperands(opcodeObject::getDestinationValue, opcodeObject::getSourceValue, opcodeObject::setDestinationValue)
                );
                opcodeObject.addOp(
                        new StackOperation(StackOperation.Type.POP, opcodeObject, opcodeObject::getDestinationValue)
                );
            }

            case "PUSH" -> {
                opcodeObject.addOp(new EmptyCycle());
                opcodeObject.addOp(new StackOperation(
                        StackOperation.Type.PUSH_HIGH, opcodeObject,
                        () -> cpu.getRegisters().getByName(opcodeObject.getDestinationOperandString())
                ));
                opcodeObject.addOp(new StackOperation(
                        StackOperation.Type.PUSH_LOW, opcodeObject,
                        () -> cpu.getRegisters().getByName(opcodeObject.getDestinationOperandString())
                ));
            }

            //Interrupt instructions
            case "DI" -> {
                opcodeObject.addOp(
                        new InterruptOperation(InterruptOperation.Type.DI, cpu));
            }
            case "EI" -> {
                opcodeObject.addOp(
                        new InterruptOperation(InterruptOperation.Type.EI, cpu));
            }
            case "HALT" -> {

            }

            //Misc instructions
            case "DAA" -> {
                opcodeObject.addOp(new AluOperation(AluOperation.Type.DAA, opcodeObject, opcodeObject::getDestinationValue, opcodeObject::getSourceValue));
            }
            case "NOP" -> {
                opcodeObject.addOp(new EmptyCycle());
            }

            default -> {
                opcodeObject.setUnimplError(true);
            }
        }
    }

    private void handlePostIncDec(Opcode opcodeObject, OpcodeInfo opcodeInfo) {
        Operand[] o = opcodeInfo.getOperands();
        checkIncDec = false;
        if (o[0].isIncrement()) {
            opcodeObject.addOp(
                    new AluOperation(AluOperation.Type.POST_INC, opcodeObject, opcodeObject::getDestinationValue, opcodeObject::getSourceValue)
            );
        } else if (o[0].isDecrement()) {
            opcodeObject.addOp(
                    new AluOperation(AluOperation.Type.POST_DEC, opcodeObject, opcodeObject::getDestinationValue, opcodeObject::getSourceValue)
            );
        }
        if (o.length > 1) {
            if (o[1].isIncrement()) {
                opcodeObject.addOp(
                        new AluOperation(AluOperation.Type.POST_INC, opcodeObject, opcodeObject::getDestinationValue, opcodeObject::getSourceValue)
                );
            } else if (o[1].isDecrement()) {
                opcodeObject.addOp(
                        new AluOperation(AluOperation.Type.POST_DEC, opcodeObject, opcodeObject::getDestinationValue, opcodeObject::getSourceValue)
                );
            }
        }
    }

    private int calculateCycles(Opcode opcodeObject) {
        int cycles = 0;
        for (MicroOperation mo : opcodeObject.getMicroOps()) {
            cycles += mo.getCycles();
        }
        return cycles;
    }

}
