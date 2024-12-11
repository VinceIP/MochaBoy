package org.mochaboy;

import java.util.HashMap;
import java.util.Map;
import java.util.function.BiConsumer;

public class OpcodeHandler {
    private Map<String, OpcodeInfo> opcodeMapUnprefixed;
    private Map<String, OpcodeInfo> opcodeMapPrefixed;
    private Map<String, BiConsumer<CPU, OpcodeInfo>> mnemonicMap;

    public OpcodeHandler(OpcodeWrapper opcodeWrapper) {
        this.opcodeMapUnprefixed = opcodeWrapper.getUnprefixed();
        this.opcodeMapPrefixed = opcodeWrapper.getCbprefixed();
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
    }

    /**
     * Add carry - Add value of y plus carry flag to x
     *
     * @param cpu
     * @param opcodeInfo
     */
    private void ADC(CPU cpu, OpcodeInfo opcodeInfo) {
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

        //Set flags per instruction
        cpu.getRegisters().setFlag(Registers.FLAG_ZERO, (result & 0xFF) == 0);
        cpu.getRegisters().setFlag(Registers.FLAG_HALF_CARRY, ((xVal & 0xF) + (yVal & 0xF) + carry) > 0xF);
        cpu.getRegisters().setFlag(Registers.FLAG_CARRY, ((((xVal & 0xFF) + (yVal & 0xFF)) + carry) & 0x100) != 0);
        cpu.getRegisters().setFlag(Registers.FLAG_SUBTRACT, false);

        cpu.getRegisters().setA(result & 0xFF);
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

        processFlags(cpu, opcodeInfo, xVal, yVal);
        int result = xVal + yVal;
        applyResult(cpu, xOpr.getName(), result);

    }

    private void processFlags(CPU cpu, OpcodeInfo opcodeInfo, int xVal, int yVal) {
        Flags flags = opcodeInfo.getFlags();
        String mnemonic = opcodeInfo.getMnemonic();
        Operand xOpr = opcodeInfo.getOperands()[0];
        Operand yOpr = opcodeInfo.getOperands()[1];

        FlagConditions conditions = getFlagConditions(mnemonic, xOpr, yOpr, xVal, yVal);

        switch (flags.getZ()) {
            case "Z" -> cpu.getRegisters().setFlag(Registers.FLAG_ZERO, conditions.isZero);
            case "0" -> cpu.getRegisters().clearFlag(Registers.FLAG_ZERO);
            case "1" -> cpu.getRegisters().setFlag(Registers.FLAG_ZERO);
        }

        switch (flags.getN()) {
            case "N" -> cpu.getRegisters().setFlag(Registers.FLAG_SUBTRACT, conditions.isSubtract);
            case "0" -> cpu.getRegisters().clearFlag(Registers.FLAG_SUBTRACT);
            case "1" -> cpu.getRegisters().setFlag(Registers.FLAG_SUBTRACT);
        }

        switch (flags.getH()) {
            case "H" -> cpu.getRegisters().setFlag(Registers.FLAG_SUBTRACT, conditions.isHalfCarry);
            case "0" -> cpu.getRegisters().clearFlag(Registers.FLAG_HALF_CARRY);
            case "1" -> cpu.getRegisters().setFlag(Registers.FLAG_HALF_CARRY);
        }

        switch (flags.getC()) {
            case "C" -> cpu.getRegisters().setFlag(Registers.FLAG_CARRY, conditions.isCarry);
            case "0" -> cpu.getRegisters().clearFlag(Registers.FLAG_CARRY);
            case "1" -> cpu.getRegisters().setFlag(Registers.FLAG_CARRY);
        }
    }

    private FlagConditions getFlagConditions(String mnemonic, Operand xOpr, Operand yOpr, int xVal, int yVal) {
        FlagConditions conditions = new FlagConditions();

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

