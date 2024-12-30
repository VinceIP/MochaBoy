package org.mochaboy.opcode;

import org.mochaboy.opcode.operations.MergeOperands;
import org.mochaboy.opcode.operations.MicroOperation;
import org.mochaboy.opcode.operations.ReadImmediate8bit;

public class OpcodeBuilder {
    OpcodeWrapper opcodeWrapper;

    public OpcodeBuilder(OpcodeWrapper opcodeWrapper) {
        this.opcodeWrapper = opcodeWrapper;
    }

    public Opcode build(int opcode, boolean isPrefixed) {
        Opcode opcodeObject = new Opcode();
        String hexKey = String.format("0x%02X", opcode);
        OpcodeInfo opcodeInfo = isPrefixed ? opcodeWrapper.getCbprefixed().get(hexKey) :
                opcodeWrapper.getUnprefixed().get(hexKey);
        buildMicroOpsFromOperands(opcodeObject, opcodeInfo);
        buildOpsFromMnemonics(opcodeObject, opcodeInfo);
        return opcodeObject;
    }

    private void buildMicroOpsFromOperands(Opcode opcodeObject, OpcodeInfo opcodeInfo) {
        MicroOperation op;
        MicroOperation mo;

        Operand d = null, s = null, x = null;
        if (opcodeInfo.getOperands().length > 0) {
            d = opcodeInfo.getOperands()[0];
            if (opcodeInfo.getOperands().length > 1) {
                s = opcodeInfo.getOperands()[1];
                if (opcodeInfo.getOperands().length > 2) {
                    x = opcodeInfo.getOperands()[2];
                }
            }
        }

        if (d != null) {
            switch (d.getName()) {
                //TODO: left operands can be u3 ("1", "2", etc)
                //TODO: left operand can be "Z", "NZ", etc for conditional call
                case "n16":
                    //Read n16 at PC - target address for data operation
                    MicroOperation mo1 = new ReadImmediate8bit(opcodeObject::setOperand1);
                    MicroOperation mo2 = new ReadImmediate8bit(opcodeObject::setOperand2);
                    MicroOperation mo3 = new MergeOperands(opcodeObject::getOperand1, opcodeObject::getOperand2, opcodeObject::setOperand1);
                    opcodeObject.setOperand2(0); //Merge 2 read bytes, set as operand1, clear out old operand2
                    //MicroOperation mo3 = new FlipBytes(opcodeObject::getOperand1, opcodeObject::getOperand2, opcodeObject::setOperand1);
                    opcodeObject.addOp(mo1);
                    opcodeObject.addOp(mo2);
                    //opcodeObject.addOp(mo3);
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
                case "SP":
                case "PC":
                    if()
                    opcodeObject.setDestinationRegister(d.getName());
                    break;
                //RES - set bit u3 to 1 in r8 or [HL], so make this operand2 (source)
                case "0", "1", "2", "3", "4", "5", "6", "7":
                    opcodeObject.setOperand2(Integer.parseInt(d.getName()));


            }
        }

    }

    private void buildOpsFromMnemonics(Opcode opcodeObject, OpcodeInfo opcodeInfo) {

    }

}
