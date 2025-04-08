package org.mochaboy.opcode.operations;

import org.mochaboy.CPU;
import org.mochaboy.Memory;

import java.util.function.Consumer;

public class ReadMemory8Bits implements MicroOperation {
    private Consumer<Integer> consumer;
    private String register;
    private int result;

    public ReadMemory8Bits(Consumer<Integer> consumer, String register) {
        this.consumer = consumer;
        this.register = register;
    }

    @Override
    public MicroOperation execute(CPU cpu, Memory memory) {
        int address = cpu.getRegisters().getByName(register);
        result = memory.readByte(address);
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
