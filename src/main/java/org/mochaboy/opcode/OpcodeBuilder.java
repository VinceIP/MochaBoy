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

    public Opcode build(int fetchedAt, int opcode, boolean isPrefixed) {
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

        //Queue up a flag process op
        opcodeObject.addOp(new HandleFlags(flagCalculator, cpu, opcodeInfo,
                opcodeObject::getDestinationValue, opcodeObject::getSourceValue));

        //Determine if this is LD [HL-/+]
        if (checkIncDec) {
            handlePostIncDec(opcodeObject, opcodeInfo);
        }

        //
        opcodeObject.setCyclesConsumed(1 + calculateCycles(opcodeObject));

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
            case "a8":
                //Only occurs in LDH
                opcodeObject.setDestinationType(DataType.A8);
                opcodeObject.addOp(
                        new ReadImmediate8bit(opcodeObject::setDestinationValue, true) //Adds offset of FF00
                );
                break;
            case "a16":
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
                break;
            case "A":
            case "B":
            case "C":
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
            case "D":
            case "E":
            case "H":
            case "L":
                if (!d.isImmediate()) {
                    //If it's an 8b register and not immediate, it must be LD [C], A
                    opcodeObject.setDestinationType(DataType.A8);
                } else {
                    opcodeObject.setDestinationType(DataType.R8);
                }
                opcodeObject.addOp(
                        new ReadRegister8Bit(opcodeObject::setDestinationValue, d.getName())
                );
                break;
            case "AF":
            case "BC":
            case "DE":
            case "HL":
                //Signal for post increment/decrement as in LD [HL-]/[HL+] A
                if (d.isIncrement() || d.isDecrement()) checkIncDec = true;
            case "SP":
            case "PC":
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
                break;
            //RES - set bit u3 to 0 in r8 or [HL]
            //BIT - test bit u3 in r8
            case "0", "1", "2", "3", "4", "5", "6", "7", "8", "9":
                opcodeObject.setDestinationValue(Integer.parseInt(d.getName()));
                break;
            case "Z":
            case "NZ":
            case "NC":
                opcodeObject.setCc(d.getName()); //Other flag conditions
                break;
        }

    }

    private void handleSourceOperand(Opcode opcodeObject, Operand s, OpcodeInfo opcodeInfo) {
        opcodeObject.setSourceOperandString(s.getName());
        opcodeObject.setSourceOperand(s);
        switch (s.getName()) {
            case "n8":
                opcodeObject.setSourceType(DataType.A8);
                opcodeObject.addOp(
                        new ReadImmediate8bit(opcodeObject::setSourceValue)
                );
                break;
            case "a8":
                //Only occurs in LDH
                opcodeObject.setSourceType(DataType.A8);
                opcodeObject.addOp(
                        new ReadImmediate8bit(opcodeObject::setSourceValue, true) //Adds offset of FF00
                );
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
                //Same as above, but a16 only appears in JUMP/CALL, so set it as operand 1
                opcodeObject.setDestinationType(DataType.N16);
                opcodeObject.addOp(
                        new ReadImmediate8bit(opcodeObject::setDestinationValue)
                );
                opcodeObject.addOp(
                        new ReadImmediate8bit(opcodeObject::setSourceValue)
                );
                opcodeObject.addOp(
                        new MergeOperands(
                                opcodeObject::getDestinationValue, opcodeObject::getSourceValue, opcodeObject::setDestinationValue)
                );
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
                opcodeObject.setSourceType(DataType.R8);
                String m = opcodeInfo.getMnemonic();
                //Stupid check to make sure INC and DEC get handled correctly. Trust me
                if (m.equals("INC") || m.equals("DEC")) {
                    opcodeObject.addOp(new ReadRegister8Bit(opcodeObject::setDestinationValue, s.getName()));
                } else opcodeObject.addOp(new ReadRegister8Bit(opcodeObject::setSourceValue, s.getName()));
                break;
            case "AF":
            case "BC":
            case "DE":
            case "HL":
                if (s.isIncrement() || s.isDecrement()) checkIncDec = true;
            case "SP":
            case "PC":
                opcodeObject.setSourceType(DataType.R16);
                opcodeObject.addOp(
                        new ReadRegister16Bit(opcodeObject::setSourceValue, s.getName())
                );
                break;
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
            case "DEC" -> opcodeObject.addOp(
                    new AluOperation(AluOperation.Type.DEC, opcodeObject, opcodeObject::getDestinationValue, opcodeObject::getSourceValue)
            );
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
                    new BitwiseOperation(BitwiseOperation.Type.AND, opcodeObject::getSourceValue)
            );
            case "CPL" -> opcodeObject.addOp(
                    new BitwiseOperation(BitwiseOperation.Type.CPL, opcodeObject::getSourceValue)
            );
            case "OR" -> opcodeObject.addOp(
                    new BitwiseOperation(BitwiseOperation.Type.OR, opcodeObject::getSourceValue)
            );
            case "XOR" -> opcodeObject.addOp(
                    new BitwiseOperation(BitwiseOperation.Type.XOR, opcodeObject::getSourceValue)
            );

            //Bit flag operations
            case "BIT" -> {
                System.out.printf("");
            } //Changes no values, is implied in FlagCalculator
            case "RES" -> opcodeObject.addOp(
                    new BitFlagOperation(BitFlagOperation.Type.RES, opcodeObject, opcodeObject::getDestinationValue, opcodeObject::getSourceValue)
            );
            case "SET" -> opcodeObject.addOp(
                    new BitFlagOperation(BitFlagOperation.Type.SET, opcodeObject, opcodeObject::getDestinationValue, opcodeObject::getSourceValue)
            );

            //Bit shift operations
            case "RL" -> {
                new BitShiftOperation(BitShiftOperation.Type.RL, opcodeObject, opcodeObject::getDestinationValue);
            }
            case "RLA" -> {
                new BitShiftOperation(BitShiftOperation.Type.RLA, opcodeObject, opcodeObject::getDestinationValue);
            }
            case "RLC" -> {
                new BitShiftOperation(BitShiftOperation.Type.RLC, opcodeObject, opcodeObject::getDestinationValue);
            }
            case "RLCA" -> {
                new BitShiftOperation(BitShiftOperation.Type.RLCA, opcodeObject, opcodeObject::getDestinationValue);
            }
            case "RR" -> {
                new BitShiftOperation(BitShiftOperation.Type.RR, opcodeObject, opcodeObject::getDestinationValue);
            }
            case "RRA" -> {
                new BitShiftOperation(BitShiftOperation.Type.RRA, opcodeObject, opcodeObject::getDestinationValue);
            }
            case "RRC" -> {
                new BitShiftOperation(BitShiftOperation.Type.RRC, opcodeObject, opcodeObject::getDestinationValue);
            }
            case "RRCA" -> {
                new BitShiftOperation(BitShiftOperation.Type.RRCA, opcodeObject, opcodeObject::getDestinationValue);
            }
            case "SLA" -> {
                new BitShiftOperation(BitShiftOperation.Type.SLA, opcodeObject, opcodeObject::getDestinationValue);
            }
            case "SRA" -> {
                new BitShiftOperation(BitShiftOperation.Type.SRA, opcodeObject, opcodeObject::getDestinationValue);
            }
            case "SRL" -> {
                new BitShiftOperation(BitShiftOperation.Type.SRL, opcodeObject, opcodeObject::getDestinationValue);
            }
            case "SWAP" -> {
                new BitShiftOperation(BitShiftOperation.Type.SWAP, opcodeObject, opcodeObject::getDestinationValue);
            }

            //Subroutine instructions

            //Carry flag instructions
            case "CCF" -> {
                //Pure flag calculation
            }
            case "SCF" -> {
                //Pure flag calculation
            }

            //Stack instructions


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
