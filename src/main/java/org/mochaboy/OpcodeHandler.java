package org.mochaboy;

import java.util.HashMap;
import java.util.Map;
import java.util.function.BiConsumer;

public class OpcodeHandler {
    private Map<String, OpcodeInfo> opcodeMapUnprefixed;
    private Map<String, OpcodeInfo> opcodeMapPrefixed;
    private Map<String, BiConsumer<CPU, OpcodeInfo>> mnemonicMap;
    private FlagCalculator flagCalculator;

    public OpcodeHandler(OpcodeWrapper opcodeWrapper) {
        this.opcodeMapUnprefixed = opcodeWrapper.getUnprefixed();
        this.opcodeMapPrefixed = opcodeWrapper.getCbprefixed();
        flagCalculator = new FlagCalculator();
        mapMnemonics();
    }

    public void execute(CPU cpu, OpcodeInfo opcodeInfo) {
        //do logic on cpu
        mnemonicMap.get(opcodeInfo.getMnemonic()).accept(cpu, opcodeInfo);
        //inc PC based on opcode info
        //add to cpu timer based on opcode info
        //consider increasing t states during specific operations (reading and writing) for accuracy instead?
    }

    private void mapMnemonics() {
        this.mnemonicMap = new HashMap<>();
        mnemonicMap.put("ADC", this::ADC);
        mnemonicMap.put("ADD", this::ADD);
        mnemonicMap.put("AND", this::AND);
        mnemonicMap.put("BIT", this::BIT);
        mnemonicMap.put("CALL", this::CALL);
        mnemonicMap.put("CCF", this::CCF);
        mnemonicMap.put("CP", this::CP);
        mnemonicMap.put("CPL", this::CPL);
        mnemonicMap.put("DAA", this::DAA);
        mnemonicMap.put("DEC", this::DEC);
        mnemonicMap.put("DI", this::DI);
        mnemonicMap.put("EI", this::EI);
        mnemonicMap.put("HALT", this::HALT);
        mnemonicMap.put("INC", this::INC);
        mnemonicMap.put("JP", this::JP);
        mnemonicMap.put("LD", this::LD);
    }

    /**
     * Add carry - Add value of y plus carry flag to x
     *
     * @param cpu
     * @param opcodeInfo
     */
    private void ADC(CPU cpu, OpcodeInfo opcodeInfo) {
        Operand xOpr = opcodeInfo.getOperands()[0];
        Operand yOpr = opcodeInfo.getOperands()[1];
        int xVal = cpu.getRegisters().getA() & 0xFF;
        int yVal;

        if (yOpr.getName().equals("n8")) {
            //Get immediate 8-bit value
            yVal = cpu.getMemory().readByte(cpu.getRegisters().getPC());
            cpu.getRegisters().incrementPC();
        } else if (yOpr.getName().equals("HL")) {
            //get 16-bit value in [HL]
            yVal = cpu.getMemory().readByte(cpu.getRegisters().getHL());
        } else {
            //Otherwise, get the 8-bit registered specified in the operand
            yVal = cpu.getRegisters().getByName(yOpr.getName()) & 0xFF;
        }

        //Add result plus the current carry bit
        int carry = cpu.getRegisters().isFlagSet(Registers.FLAG_CARRY) ? 1 : 0;
        int result = xVal + yVal + carry;

        processFlags(cpu, opcodeInfo, xVal, yVal);
        applyResult(cpu, xOpr.getName(), result);
    }

    /**
     * Add
     *
     * @param cpu
     * @param opcodeInfo
     */
    private void ADD(CPU cpu, OpcodeInfo opcodeInfo) {
        Operand xOpr = opcodeInfo.getOperands()[0];
        Operand yOpr = opcodeInfo.getOperands()[1];

        int xVal = cpu.getRegisters().getByName(xOpr.getName());
        int yVal;
        switch (yOpr.getName()) {
            case "n8":
                yVal = cpu.getMemory().readByte(cpu.getRegisters().getPC());
                cpu.getRegisters().incrementPC();
                break;
            case "e8":
                yVal = cpu.getMemory().readUnsignedByte(cpu.getRegisters().getPC());
                cpu.getRegisters().incrementPC();
                break;
            case "HL":
                yVal = cpu.getMemory().readByte(cpu.getRegisters().getHL());
                break;
            default:
                yVal = cpu.getRegisters().getByName(yOpr.getName());
                break;
        }
        int result = xVal + yVal;
        processFlags(cpu, opcodeInfo, xVal, yVal);
        applyResult(cpu, xOpr.getName(), result);

    }

    private void AND(CPU cpu, OpcodeInfo opcodeInfo) {
        Operand xOpr = opcodeInfo.getOperands()[0];
        Operand yOpr = opcodeInfo.getOperands()[1];

        int xVal = cpu.getRegisters().getByName(xOpr.getName());
        int yVal;

        switch (yOpr.getName()) {
            case "n8":
                yVal = cpu.getMemory().readByte(cpu.getRegisters().getPC());
                cpu.getRegisters().incrementPC();
                break;
            case "HL":
                yVal = cpu.getMemory().readByte(cpu.getRegisters().getHL());
                break;
            default:
                yVal = cpu.getRegisters().getByName(yOpr.getName());
                break;
        }
        int result = (xVal & yVal) & 0xFF;
        processFlags(cpu, opcodeInfo, xVal, yVal);
        applyResult(cpu, xOpr.getName(), result);
    }

    private void BIT(CPU cpu, OpcodeInfo opcodeInfo) {
        Operand xOpr = opcodeInfo.getOperands()[0];
        Operand yOpr = opcodeInfo.getOperands()[1];
        //Get u3 from the opcode
        int xVal = (opcodeInfo.getOpcode() >> 3) & 0x07;
        cpu.getRegisters().incrementPC();
        int yVal;
        if (yOpr.getName().equals("HL")) yVal = cpu.getMemory().readByte(cpu.getRegisters().getHL());
        else yVal = cpu.getRegisters().getByName(yOpr.getName());

        processFlags(cpu, opcodeInfo, xVal, yVal);
    }

    private void CALL(CPU cpu, OpcodeInfo opcodeInfo) {
        // Read the address to jump to (16-bit) from PC+1 and PC+2
        int callAddress = cpu.getMemory().readWord(cpu.getRegisters().getPC() + 1);
        int returnAddress = cpu.getRegisters().getPC() + 3; // Next instruction address

        boolean shouldCall = true;

        if (opcodeInfo.getOperands().length > 1) {
            Operand xOpr = opcodeInfo.getOperands()[0];
            String condition = xOpr.getName(); // "Z", "NZ", "C", "NC"

            switch (condition) {
                case "Z":
                    shouldCall = cpu.getRegisters().isFlagSet(Registers.FLAG_ZERO);
                    break;
                case "NZ":
                    shouldCall = !cpu.getRegisters().isFlagSet(Registers.FLAG_ZERO);
                    break;
                case "C":
                    shouldCall = cpu.getRegisters().isFlagSet(Registers.FLAG_CARRY);
                    break;
                case "NC":
                    shouldCall = !cpu.getRegisters().isFlagSet(Registers.FLAG_CARRY);
                    break;
            }
        }

        if (shouldCall) {
            cpu.getStack().push(returnAddress);
            cpu.getRegisters().setPC(callAddress);
        } else {
            cpu.getRegisters().setPC(cpu.getRegisters().getPC() + 3);
        }
    }

    private void CCF(CPU cpu, OpcodeInfo opcodeInfo) {
        System.out.println("CCF unimpl");
    }

    /**
     * Subtract value in yOpr from xOpr and set flags accordingly, but don't store the result. For comparing values.
     *
     * @param cpu
     * @param opcodeInfo
     */
    private void CP(CPU cpu, OpcodeInfo opcodeInfo) {
        Operand xOpr = opcodeInfo.getOperands()[0];
        Operand yOpr = opcodeInfo.getOperands()[1];

        int xVal = cpu.getRegisters().getByName(xOpr.getName());
        int yVal;
        switch (yOpr.getName()) {
            case "n8":
                yVal = cpu.getMemory().readByte(cpu.getRegisters().getPC());
                cpu.getRegisters().incrementPC();
                break;
            case "HL":
                yVal = cpu.getMemory().readByte(cpu.getRegisters().getHL());
                break;
            default:
                yVal = cpu.getRegisters().getByName(yOpr.getName());
                break;
        }

        processFlags(cpu, opcodeInfo, xVal, yVal);

    }

    private void CPL(CPU cpu, OpcodeInfo opcodeInfo) {
        System.out.println("CPL unimpl");
    }

    private void DAA(CPU cpu, OpcodeInfo opcodeInfo) {
        System.out.println("DAA unimpl");
    }

    private void DEC(CPU cpu, OpcodeInfo opcodeInfo) {
        Operand xOpr = opcodeInfo.getOperands()[0];
        int value;

        if (!is8BitRegister(xOpr.getName()) && xOpr.isImmediate()) {
            value = cpu.getRegisters().getByName(xOpr.getName());
            int result = (value - 1) & 0xFFFF;
            cpu.getRegisters().setByName(xOpr.getName(), result);
            return; // No need to modify flags for 16-bit DEC
        }
        if (!xOpr.isImmediate()) {
            // probably means DEC [HL])
            value = cpu.getMemory().readByte(cpu.getRegisters().getHL());
        } else {
            // Otherwise, it’s a normal 8-bit register
            value = cpu.getRegisters().getByName(xOpr.getName());
        }

        int result = (value - 1) & 0xFF; // 8-bit subtraction with wrap-around
        processFlags(cpu, opcodeInfo, value, 1);

        if (!xOpr.isImmediate()) {
            // Write back to memory at address [HL]
            cpu.getMemory().writeByte(cpu.getRegisters().getHL(), result);
        } else {
            // Write back to a standard 8-bit register
            cpu.getRegisters().setByName(xOpr.getName(), result);
        }
    }

    private void DI(CPU cpu, OpcodeInfo opcodeInfo) {

    }

    private void EI(CPU cpu, OpcodeInfo opcodeInfo) {

    }

    private void HALT(CPU cpu, OpcodeInfo opcodeInfo) {

    }

    private void INC(CPU cpu, OpcodeInfo opcodeInfo) {
        Operand xOpr = opcodeInfo.getOperands()[0];
        int value;

        //For 16-bit registers
        if (!is8BitRegister(xOpr.getName()) && xOpr.isImmediate()) {
            value = cpu.getRegisters().getByName(xOpr.getName());
            int result = (value + 1) & 0xFFFF;
            cpu.getRegisters().setByName(xOpr.getName(), result);
            return; // No need to modify flags for 16-bit INC
        }
        if (!xOpr.isImmediate()) {
            // probably means INC [HL])
            value = cpu.getMemory().readByte(cpu.getRegisters().getHL());
        } else {
            // Otherwise, it’s a normal 8-bit register
            value = cpu.getRegisters().getByName(xOpr.getName());
        }

        int result = (value + 1) & 0xFF; // 8-bit subtraction with wrap-around
        processFlags(cpu, opcodeInfo, value, 1);

        if (!xOpr.isImmediate()) {
            // Write back to memory at address [HL]
            cpu.getMemory().writeByte(cpu.getRegisters().getHL(), result);
        } else {
            // Write back to a standard 8-bit register
            cpu.getRegisters().setByName(xOpr.getName(), result);
        }
    }

    private void JP(CPU cpu, OpcodeInfo opcodeInfo) {
        int address = cpu.getMemory().readWord(cpu.getRegisters().getPC());
        cpu.getRegisters().incrementPC(2);

        if (opcodeInfo.getOperands().length > 1) {
            Operand xOpr = opcodeInfo.getOperands()[0];
            String condition = xOpr.getName(); // "Z", "NZ", "C", "NC"

            boolean shouldJump = false;
            switch (condition) {
                case "Z":
                    shouldJump = cpu.getRegisters().isFlagSet(Registers.FLAG_ZERO);
                    break;
                case "NZ":
                    shouldJump = !cpu.getRegisters().isFlagSet(Registers.FLAG_ZERO);
                    break;
                case "C":
                    shouldJump = cpu.getRegisters().isFlagSet(Registers.FLAG_CARRY);
                    break;
                case "NC":
                    shouldJump = !cpu.getRegisters().isFlagSet(Registers.FLAG_CARRY);
                    break;
            }

            if (shouldJump) {
                cpu.getRegisters().setPC(address);
            }
        } else {
            cpu.getRegisters().setPC(address);
        }
    }

    private void LD(CPU cpu, OpcodeInfo opcodeInfo) {
        Operand xOpr = opcodeInfo.getOperands()[0];
        Operand yOpr = opcodeInfo.getOperands()[1];
        int sourceValue = 0;
        int address = 0;

        //Get value to be copied from Y operand
        switch (yOpr.getName()) {
            case "n8":
                // Load immediate 8-bit value
                sourceValue = cpu.getMemory().readByte(cpu.getRegisters().getPC());
                cpu.getRegisters().incrementPC();
                break;

            case "n16":
                // Load immediate 16-bit value
                sourceValue = cpu.getMemory().readWord(cpu.getRegisters().getPC());
                cpu.getRegisters().incrementPC(2);
                break;

            case "a16":
                // Load from absolute 16-bit address
                address = cpu.getMemory().readWord(cpu.getRegisters().getPC());
                sourceValue = cpu.getMemory().readByte(address);
                cpu.getRegisters().incrementPC(2);
                break;
            //Otherwise, we are getting a value from a register
            default:
                //If the value is not flagged as immediate, we treat this register as a pointer to another address
                if (!yOpr.isImmediate()) {
                    if (is8BitRegister(yOpr.getName()))
                        sourceValue = cpu.getMemory().readByte(cpu.getRegisters().getByName(yOpr.getName()));
                    else sourceValue = cpu.getMemory().readWord(cpu.getRegisters().getByName(yOpr.getName()));
                } else {
                    //Otherwise, pull a value directly from the yOpr's name
                    sourceValue = cpu.getRegisters().getByName(yOpr.getName());
                }
                break;
        }

        switch (xOpr.getName()) {
            case "a16":
                // Store value into absolute 16-bit address
                address = cpu.getMemory().readWord(cpu.getRegisters().getPC());
                cpu.getMemory().writeByte(address, sourceValue);
                cpu.getRegisters().incrementPC(2);
                break;

            default:
                if (!xOpr.isImmediate()) {
                    cpu.getMemory().writeByte(cpu.getRegisters().getByName(xOpr.getName()), sourceValue);
                } else {
                    cpu.getRegisters().setByName(xOpr.getName(), sourceValue);
                }
                break;
        }
    }

    private void NOP(CPU cpu, OpcodeInfo opcodeInfo){
    }


    private void processFlags(CPU cpu, OpcodeInfo opcodeInfo, int xVal, int yVal) {
        FlagConditions conditions = flagCalculator.calculateFlags(
                cpu, opcodeInfo.getMnemonic(), xVal, yVal, opcodeInfo.getOperands());
        applyFlags(cpu, opcodeInfo.getFlags(), conditions);
    }

    /**
     * Apply flags to CPU based on calculated conditions or opcode info.
     * Flag states are set to their expected values when Opcodes.json indicates "0", "1", or "-" for flag states.
     * Otherwise, FlagCalculator determines the set flag.
     *
     * @param flags
     * @param conditions
     */
    private void applyFlags(CPU cpu, Flags flags, FlagConditions conditions) {
        //Zero flag
        switch (flags.getZ()) {
            //OpcodeInfo indicates this flag should be set based on conditions that were set in the flag calculator
            case "Z":
                cpu.getRegisters().setFlag(Registers.FLAG_ZERO, conditions.isZero);
                break;
            //Opcode info indicates this flag should always be cleared
            case "0":
                cpu.getRegisters().clearFlag(Registers.FLAG_ZERO);
                break;
            //Opcode info indicates this flags should always be set
            case "1":
                cpu.getRegisters().setFlag(Registers.FLAG_ZERO);
                break;
            //Opcode info could give "-", indicating nothing should change with this flag
        }

        // Subtract flag
        switch (flags.getN()) {
            case "N":
                cpu.getRegisters().setFlag(Registers.FLAG_SUBTRACT, conditions.isSubtract);
            case "0":
                cpu.getRegisters().clearFlag(Registers.FLAG_SUBTRACT);
                break;
            case "1":
                cpu.getRegisters().setFlag(Registers.FLAG_SUBTRACT);
                break;
            // "-" and other cases - do nothing
        }

        // Half-carry flag
        switch (flags.getH()) {
            case "H":
                cpu.getRegisters().setFlag(Registers.FLAG_HALF_CARRY, conditions.isHalfCarry);
                break;
            case "0":
                cpu.getRegisters().clearFlag(Registers.FLAG_HALF_CARRY);
                break;
            case "1":
                cpu.getRegisters().setFlag(Registers.FLAG_HALF_CARRY);
                break;
            // "-" case - do nothing
        }

        // Carry flag
        switch (flags.getC()) {
            case "C":
                cpu.getRegisters().setFlag(Registers.FLAG_CARRY, conditions.isCarry);
                break;
            case "0":
                cpu.getRegisters().clearFlag(Registers.FLAG_CARRY);
                break;
            case "1":
                cpu.getRegisters().setFlag(Registers.FLAG_CARRY);
                break;
            // "-" case - do nothing
        }
    }

    public static boolean is8BitRegister(String register) {
        return (register.length() == 1 || register.equals("n8"));
    }

    private void applyResult(CPU cpu, String register, int result) {
        //Mask result depending on bit size of register
        cpu.getRegisters().setByName(register,
                (is8BitRegister(register) ? result & 0xFF : result & 0xFFFF)
        );
    }

}

class FlagConditions {
    boolean isZero;
    boolean isHalfCarry;
    boolean isCarry;
    boolean isSubtract;
}

