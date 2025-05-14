package org.mochaboy.opcode.operations;

import org.mochaboy.CPU;
import org.mochaboy.Memory;

import java.util.function.Consumer;

public class ReadImmediate8bit implements MicroOperation {
    private final Consumer<Integer> consumer;
    private int result;
    private boolean addHRamOffset = false;

    public ReadImmediate8bit(Consumer<Integer> consumer) {
        this.consumer = consumer;
    }

    public ReadImmediate8bit(Consumer<Integer> consumer, boolean addHRamOffset) {
        this.consumer = consumer;
        this.addHRamOffset = addHRamOffset;
    }

    @Override
    public MicroOperation execute(CPU cpu, Memory memory) {
        result = memory.readByte(cpu.getRegisters().getPC()) & 0xFF;
        //System.out.printf("\nReading byte at %04X: %02X", cpu.getRegisters().getPC(), result);
        cpu.getRegisters().incrementPC();
        if (addHRamOffset) result |= 0xFF00;
        consumer.accept(result);
        return this;
    }

    @Override
    public int getCycles() {
        return 1;
    }

    @Override
    public int getResult() {
        return result;
    }

}
