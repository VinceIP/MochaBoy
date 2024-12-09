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
    }

    /**
     * Add carry - Add value of y plus carry flag to x
     *
     * @param cpu
     * @param opcodeInfo
     */
    private void ADC(CPU cpu, OpcodeInfo opcodeInfo) {
        //No need to get an xOpr - ADC's x operand is always A, so we alays know where to get the value
        Operand yOpr = opcodeInfo.getOperands()[1];
        int xVal = cpu.getRegisters().getA() & 0xFF;
        int yVal;
        //If getting value in memory
        if (yOpr.getName().equals("n8")) {
            yVal = cpu.getMemory().readByte(cpu.getRegisters().getPC());
            cpu.getRegisters().incrementPC();
            //Otherwise, we're getting a value from a register or immediate
        } else yVal = cpu.getRegisters().getByName(yOpr.getName()) & 0xFF;

        int carry = cpu.getRegisters().isFlagSet(Registers.FLAG_CARRY) ? 1 : 0;
        int result = yVal + xVal + carry;

        if ((result & 0xFF) == 0) cpu.getRegisters().setFlag(Registers.FLAG_ZERO);
        else cpu.getRegisters().clearFlag(Registers.FLAG_ZERO);

        if ((xVal + yVal + carry) > 0xFF) cpu.getRegisters().setFlag(Registers.FLAG_CARRY);
        else cpu.getRegisters().clearFlag(Registers.FLAG_CARRY);

        if (((xVal & 0xF) + (yVal & 0xF) + carry) > 0xF) cpu.getRegisters().setFlag(Registers.FLAG_HALF_CARRY);
        else cpu.getRegisters().clearFlag(Registers.FLAG_HALF_CARRY);

        cpu.getRegisters().clearFlag(Registers.FLAG_SUBTRACT);
        cpu.getRegisters().setA(result & 0xFF);
    }
}

