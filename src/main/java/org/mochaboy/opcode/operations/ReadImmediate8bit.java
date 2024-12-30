package org.mochaboy.opcode.operations;

import org.mochaboy.CPU;
import org.mochaboy.Memory;
import org.mochaboy.opcode.Opcode;

import java.util.function.Consumer;

public class ReadImmediate8bit implements MicroOperation {
    private Consumer<Integer> consumer;
    private int result;

    public ReadImmediate8bit(Consumer<Integer> consumer) {
        this.consumer = consumer;
    }

    @Override
    public MicroOperation execute(CPU cpu, Memory memory) {
        result = memory.readByte(cpu.getRegisters().getPC()) & 0xFF;
        cpu.getRegisters().incrementPC();
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
