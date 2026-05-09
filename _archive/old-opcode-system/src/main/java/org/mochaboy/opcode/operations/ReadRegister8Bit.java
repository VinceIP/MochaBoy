package org.mochaboy.opcode.operations;

import org.mochaboy.CPU;
import org.mochaboy.Memory;

import java.util.function.Consumer;

public class ReadRegister8Bit implements MicroOperation {
    private Consumer<Integer> consumer;
    private String name;
    private int result;

    public ReadRegister8Bit(Consumer<Integer> consumer, String name) {
        this.consumer = consumer;
        this.name = name;
    }

    @Override
    public MicroOperation execute(CPU cpu, Memory memory) {
        result = cpu.getRegisters().getByName(name) & 0xFF;
        consumer.accept(result);
        return this;
    }

    @Override
    public int getCycles() {
        return 0;
    }

    @Override
    public int getResult() {
        return result;
    }
}
