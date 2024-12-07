package org.mochaboy;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Consumer;

public class Opcode {

    //ALl opcodes stored in a Runnable array
    public static final Consumer<CPU>[] instructions = new Consumer[256];
    public static final Map<Integer, OpcodeInfo> opcodeInfoMap = new HashMap<>();

    static {

        //Unimplemented opcode Consumer
        Consumer<CPU> defaultConsumer = (cpu) -> {
            int pc = cpu.getRegisters().getPC();
            int op = cpu.getMemory().readByte(pc) & 0xFF;
            System.out.printf("\nOpcode unimplemented: 0x%02X%n", op);
            cpu.getRegisters().incrementPC();
        };
        //Check for an invalid index and assign to defaultConsumer if one is found
        for (int i = 0; i < instructions.length; i++) {
            if (instructions[i] == null) {
                instructions[i] = defaultConsumer;
            }

        }

        //Opcodes go here
        opcodeInfoMap.put(0x00, new OpcodeInfo("NOP", 1, 4));
        instructions[0x00] = (cpu) -> {
        };

        opcodeInfoMap.put(0x01, new OpcodeInfo("LD BC, d16", 3, 12));
        instructions[0x01] = (cpu) -> {
            int d16 = cpu.getMemory().readWord(cpu.getRegisters().getPC()); //Read 2 bytes from PC
            cpu.getRegisters().setBC(d16); //Set BC to the 16-bit value
        };

        opcodeInfoMap.put(0x02, new OpcodeInfo("LD[BC], A", 1, 8));
        instructions[0x02] = (cpu) -> cpu.getMemory().writeByte(cpu.getRegisters().getBC() & 0xFFFF, cpu.getRegisters().getA());

        opcodeInfoMap.put(0x03, new OpcodeInfo("INC BC", 1, 8));
        instructions[0x03] = (cpu) -> cpu.getRegisters().setBC((cpu.getRegisters().getBC() + 1) & 0xFFFF);

        opcodeInfoMap.put(0x04, new OpcodeInfo("INC B", 1, 4));
        instructions[0x04] = (cpu) -> {
            int result = (cpu.getRegisters().getB() + 1) & 0xFF;
            if (result == 0) cpu.getRegisters().setFlag(Registers.FLAG_ZERO);
            else cpu.getRegisters().clearFlag(Registers.FLAG_ZERO);
            cpu.getRegisters().clearFlag(Registers.FLAG_SUBTRACT);
            //If lower nibble overflowed...
            if (((cpu.getRegisters().getB() & 0x0F) + 1) > 0x0F) {
                cpu.getRegisters().setFlag(Registers.FLAG_HALF_CARRY);
            }
            cpu.getRegisters().setB(result);
        };

        opcodeInfoMap.put(0x05, new OpcodeInfo("DEC B", 1, 4));
        instructions[0x05] = (cpu) -> {
            int result = (cpu.getRegisters().getB() - 1) & 0xFF;
            if (result == 0) cpu.getRegisters().setFlag(Registers.FLAG_ZERO);
            else cpu.getRegisters().clearFlag(Registers.FLAG_ZERO);
            cpu.getRegisters().setFlag(Registers.FLAG_SUBTRACT);
            //Set half-carry if a borrow occurred when subtracting
            if ((cpu.getRegisters().getB() & 0x0F) == 0) cpu.getRegisters().setFlag(Registers.FLAG_HALF_CARRY);
            else cpu.getRegisters().clearFlag(Registers.FLAG_HALF_CARRY);
            cpu.getRegisters().setB(result);
        };

        opcodeInfoMap.put(0x06, new OpcodeInfo("LD B, n8", 2, 8));
        instructions[0x06] = (cpu) -> {
            int n8 = cpu.getMemory().readByte(cpu.getRegisters().getPC() + 1) & 0xFF;
            cpu.getRegisters().setB(n8);
        };

        opcodeInfoMap.put(0x07, new OpcodeInfo("RLCA", 1, 4));
        instructions[0x07] = (cpu) -> {
            int carry = (cpu.getRegisters().getA() & 0x80) >> 7;
            if (carry == 1) cpu.getRegisters().setFlag(Registers.FLAG_CARRY);
            else cpu.getRegisters().clearFlag(Registers.FLAG_CARRY);
            cpu.getRegisters().clearFlag(Registers.FLAG_ZERO);
            cpu.getRegisters().clearFlag(Registers.FLAG_SUBTRACT);
            cpu.getRegisters().clearFlag(Registers.FLAG_HALF_CARRY);
            cpu.getRegisters().setA((cpu.getRegisters().getA() << 1 | carry) & 0xFF);
        };

        opcodeInfoMap.put(0x08, new OpcodeInfo("LD [a16] SP", 3, 20));
        instructions[0x08] = (cpu) -> {

        }


    }

    public static void execute(CPU cpu, int opcode) {
        OpcodeInfo info = opcodeInfoMap.get(opcode);
        instructions[opcode].accept(cpu);
        cpu.incrementTStateCounter(info.gettStates());
        cpu.getRegisters().incrementPC(info.getLengthInBytes());
    }

}

