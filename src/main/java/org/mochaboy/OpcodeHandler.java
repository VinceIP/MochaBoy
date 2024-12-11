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
        if (yOpr.getName().equals("HL")) {
            yVal = cpu.getMemory().readByte(cpu.getRegisters().getHL());
        } else if (yOpr.getName().equals("n8")) {
            yVal = cpu.getMemory().readByte(cpu.getRegisters().getPC());
            cpu.getRegisters().incrementPC();
        } else if (yOpr.getName().equals("e8")) {
            yVal = cpu.getMemory().readByte(cpu.getRegisters().getPC());
            cpu.getRegisters().incrementPC();
        } else yVal = cpu.getRegisters().getByName(yOpr.getName());

        int result = xVal + yVal;

        cpu.getRegisters().clearFlag(Registers.FLAG_SUBTRACT);
        if (yOpr.getName().equals("e8")) cpu.getRegisters().clearFlag(Registers.FLAG_ZERO);
        else
            cpu.getRegisters().setFlag(Registers.FLAG_ZERO, (result & 0xFF) == 0);
        if (yOpr.getName().equals("r16") || yOpr.getName().equals("SP")) {
            cpu.getRegisters().setFlag(Registers.FLAG_HALF_CARRY, (xVal & 0xFFF) + (yVal & 0xFFF) > 0xFFF);
            cpu.getRegisters().setFlag(Registers.FLAG_CARRY, ((xVal & 0xFFFF) + (yVal & 0xFFFF)) > 0xFFFF);
        } else if (yOpr.getName().equals("e8")) {
            int lowerByte = xVal & 0xFF;
            int e8 = (byte)yVal;
            cpu.getRegisters().setFlag(Registers.FLAG_HALF_CARRY,
                    (lowerByte & 0xF) + (e8 & 0xF) > 0xF);
            cpu.getRegisters().setFlag(Registers.FLAG_CARRY,
                    lowerByte + e8 > 0xFF);
            System.out.println(String.format("lowerByte: 0x%02X", lowerByte));
            System.out.println(String.format("e8: 0x%02X", e8));
            System.out.println(String.format("halfCarry: %b", (lowerByte & 0xF) + (e8 & 0xF) > 0xF));
        } else {
            cpu.getRegisters().setFlag(Registers.FLAG_HALF_CARRY, (xVal & 0xF) + (yVal & 0xF) > 0xF);
            cpu.getRegisters().setFlag(Registers.FLAG_CARRY, ((xVal & 0xFF) + (yVal & 0xFF) & 0x100) != 0);
        }

        if (yOpr.getName().equals("e8")) {
            cpu.getRegisters().setSP(xVal + yVal);
            return;
        }
        if (xOpr.getName().equals("HL")) cpu.getRegisters().setHL(result);
        else cpu.getRegisters().setA(result & 0xFF);
    }

}

