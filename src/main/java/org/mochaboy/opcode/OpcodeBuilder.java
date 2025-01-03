package org.mochaboy.opcode;

import org.mochaboy.opcode.operations.*;

public class OpcodeBuilder {
    OpcodeWrapper opcodeWrapper;

    public OpcodeBuilder(OpcodeWrapper opcodeWrapper) {
        this.opcodeWrapper = opcodeWrapper;
    }

    public Opcode build(int fetchedAt, int opcode, boolean isPrefixed) {
        Opcode opcodeObject = new Opcode();
        opcodeObject.setFetchedAt(fetchedAt);
        String hexKey = String.format("0x%02X", opcode);
        opcodeObject.setOpcodeHex(hexKey);
        OpcodeInfo opcodeInfo = isPrefixed ? opcodeWrapper.getCbprefixed().get(hexKey) :
                opcodeWrapper.getUnprefixed().get(hexKey);
        opcodeObject.setOpcodeInfo(opcodeInfo);
        buildMicroOpsFromOperands(opcodeObject, opcodeInfo);
        buildOpsFromMnemonics(opcodeObject, opcodeInfo);
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

        //Handle destination operand
        if (d != null) {
            handleDestinationOperand(opcodeObject, d);
        }

        //Handle source operand
        if (s != null) {
            handleSourceOperand(opcodeObject, s);
        }

    }

    private void handleDestinationOperand(Opcode opcodeObject, Operand d) {
        opcodeObject.setDestinationOperandString(d.getName());
        opcodeObject.setDestinationOperand(d);
        switch (d.getName()) {
            case "a8":
                //Only occurs in LDH
                opcodeObject.addOp(
                        new ReadImmediate8bit(opcodeObject::setDestinationValue, true) //Adds offset of FF00
                );
                break;
            case "a16":
                //This is an address
                //2 cycles to read 16-bit address
                opcodeObject.addOp(new ReadImmediate8bit(opcodeObject::setDestinationValue));
                opcodeObject.addOp(new ReadImmediate8bit(opcodeObject::setSourceValue));
                opcodeObject.addOp(
                        new MergeOperands(
                                opcodeObject::getDestinationValue, opcodeObject::getSourceValue,
                                opcodeObject::setSourceValue)
                );
                break;
            case "A":
            case "B":
            case "C":
                opcodeObject.setCc("C"); //Carry condition, or register C
            case "D":
            case "E":
            case "H":
            case "L":
            case "AF":
            case "BC":
            case "DE":
            case "HL":
                //Signal for post increment/decrement as in LD [HL-]/[HL+] A
                if (d.isIncrement()) opcodeObject.setIncrementOperand(1);
                else if (d.isDecrement()) opcodeObject.setDecrementOperand(1);
            case "SP":
            case "PC":
                //If not an immediate value, read the address held in a 16-bit register
                if (!d.isImmediate()) {
                    opcodeObject.addOp(new ReadRegister16Bit(opcodeObject::setDestinationValue, d.getName()));
                } //Otherwise, do nothing(?)
                break;
            //RES - set bit u3 to 0 in r8 or [HL], so make this operand2 (source)
            //BIT
            case "0", "1", "2", "3", "4", "5", "6", "7":
                //opcodeObject.setSource(Integer.parseInt(d.getName()));
                break;
            case "Z":
            case "NZ":
            case "NC":
                opcodeObject.setCc(d.getName()); //Other flag conditions
                break;
        }
    }

    private void handleSourceOperand(Opcode opcodeObject, Operand s) {
        opcodeObject.setSourceOperandString(s.getName());
        opcodeObject.setSourceOperand(s);
        switch (s.getName()) {
            case "n8":
                opcodeObject.addOp(
                        new ReadImmediate8bit(opcodeObject::setSourceValue)
                );
                break;

            case "n16":
                //Read address, then merge, set as operand 2
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

            case "a8":
                //Only occurs in LDH
                opcodeObject.addOp(
                        new ReadImmediate8bit(opcodeObject::setSourceValue, true) //Adds offset of FF00
                );
                break;
            case "A":
            case "B":
            case "C":
            case "D":
            case "E":
            case "H":
            case "L":
            case "AF":
            case "BC":
            case "DE":
            case "HL":
                //Signal for post increment/decrement as in LD [HL-]/[HL+] A
                if (s.isIncrement()) opcodeObject.setIncrementOperand(2);
                else if (s.isDecrement()) opcodeObject.setDecrementOperand(2);
            case "SP":
            case "PC":
                //If not an immediate value, read the address held in a 16-bit register
                if (!s.isImmediate()) {
                    opcodeObject.addOp(new ReadRegister16Bit(opcodeObject::setSourceValue, s.getName()));
                }
                break;
        }
    }

    private void buildOpsFromMnemonics(Opcode opcodeObject, OpcodeInfo opcodeInfo) {
        String m = opcodeInfo.getMnemonic();
        switch (m) {
            //LD
            case "LD", "LDH":
                //TODO: inc or dec
                opcodeObject.addOp(
                        new Load(opcodeObject)
                );
                break;
            default:
                opcodeObject.setUnimplError(true);
                break;

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
