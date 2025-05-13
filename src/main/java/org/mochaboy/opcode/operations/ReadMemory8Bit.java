package org.mochaboy.opcode.operations;

import org.mochaboy.CPU;
import org.mochaboy.Memory;

import java.util.function.Consumer;
import java.util.function.Supplier;

public class ReadMemory8Bit implements MicroOperation {
    private final Consumer<Integer> consumer;
    private final Supplier<Integer> address;
    private int result;

    public ReadMemory8Bit(Consumer<Integer> consumer, Supplier<Integer> address) {
        this.consumer = consumer;
        this.address = address;
    }

    @Override
    public MicroOperation execute(CPU cpu, Memory memory) {
        int addr = 0;
        if (address.get() != null) addr = address.get();
        result = memory.readByte(addr);
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
