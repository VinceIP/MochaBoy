package org.mochaboy.opcode.operations;

import org.mochaboy.CPU;
import org.mochaboy.Memory;

import java.util.function.Consumer;
import java.util.function.Supplier;

public class ReadMemory16Bit implements MicroOperation {
    private final Consumer<Integer> consumer;
    private final Supplier<Integer> address;
    private int result;

    public ReadMemory16Bit(Consumer<Integer> consumer, Supplier<Integer> address) {
        this.consumer = consumer;
        this.address = address;
    }

    @Override
    public MicroOperation execute(CPU cpu, Memory memory) {
        result = memory.readWord(address.get());
        consumer.accept(result);
        return this;
    }

    @Override
    public int getCycles() {
        return 0;
    }

    @Override
    public int getResult() {
        return 0;
    }
}
