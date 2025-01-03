package org.mochaboy.opcode.operations;

import org.mochaboy.CPU;
import org.mochaboy.Memory;

import java.util.function.Consumer;
import java.util.function.Supplier;

public class AluOperation implements MicroOperation {
    private final Consumer<Integer> consumer;
    private final Supplier<Integer> supplier;
    private final Type type;
    private int result;

    public AluOperation(Type type, Supplier<Integer> supplier, Consumer<Integer> consumer) {
        this.type = type;
        this.supplier = supplier;
        this.consumer = consumer;
    }

    @Override
    public MicroOperation execute(CPU cpu, Memory memory) {
        return null;
    }

    private void add() {
    }


    @Override
    public int getCycles() {
        return 0;
    }

    @Override
    public int getResult() {
        return result;
    }

    public enum Type {
        ADC,
        ADD,
        CP,
        DEC,
        INC,
        SBC,
        SUB
    }
}
