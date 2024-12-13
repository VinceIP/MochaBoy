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
        mnemonicMap.put("CP", this::CP);
        mnemonicMap.put("CALL", this::CALL);
        mnemonicMap.put("JP", this::JP);
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

    private void CALL(CPU cpu, OpcodeInfo opcodeInfo) {

        int address = cpu.getMemory().readWord(cpu.getRegisters().getPC());
        cpu.getRegisters().incrementPC(2);
        cpu.getStack().push(address);

        JP(cpu, opcodeInfo);
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

    private boolean is8BitRegister(String register) {
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

