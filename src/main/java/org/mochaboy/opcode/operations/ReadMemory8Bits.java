package org.mochaboy.opcode.operations;

import org.mochaboy.CPU;
import org.mochaboy.Memory;

import java.util.function.Consumer;

public class ReadMemory8Bits implements MicroOperation {
    private Consumer<Integer> consumer;
    private int result;

    public ReadMemory8Bits(Consumer<Integer> consumer) {
        this.consumer = consumer;
    }

    @Override
    public MicroOperation execute(CPU cpu, Memory memory) {
        return null;
    }

    @Override
    public int getCycles() {
        return 1;
    }

    @Override
    public int getResult() {
        return 0;
    }
}
