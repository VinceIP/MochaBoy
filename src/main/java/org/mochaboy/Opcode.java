package org.mochaboy;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Consumer;

public class Opcode {

    //ALl opcodes stored in a Runnable array
    public static final Consumer<CPU>[] instructions = new Consumer[256];
    public static final Map<Integer, String> opcodeDescriptions = new HashMap<>();

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

        opcodeDescriptions.put(0x00, "NOP");
        instructions[0x00] = (cpu) -> cpu.getRegisters().incrementPC();

        opcodeDescriptions.put(0x01, "LD BC, d16");
        instructions[0x01] = (cpu) -> {
            int d16 = cpu.getMemory().readWord(cpu.getRegisters().getPC()); //Read 2 bytes from PC
            cpu.getRegisters().setBC(d16); //Set BC to the 16-bit value
            cpu.getRegisters().incrementPC(2);
        };

    }

    public static void execute(CPU cpu, int opcode) {
        System.out.printf("\nExecuting: 0x%02X%n", opcode);
        instructions[opcode].accept(cpu);
    }

}
