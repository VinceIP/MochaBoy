package org.mochaboy.opcode.operations;

import org.mochaboy.CPU;
import org.mochaboy.Memory;

import java.util.function.Consumer;
import java.util.function.Supplier;

public class MergeOperands implements MicroOperation {
    private final Supplier<Integer> leftGetter;
    private final Supplier<Integer> rightGetter;
    private final Consumer<Integer> resultSetter;

    public MergeOperands(Supplier<Integer> leftGetter, Supplier<Integer> rightGetter, Consumer<Integer> resultSetter) {
        this.leftGetter = leftGetter;
        this.rightGetter = rightGetter;
        this.resultSetter = resultSetter;
    }

    @Override
    public MicroOperation execute(CPU cpu, Memory memory) {
        int lo  = leftGetter.get() & 0xFF;
        int hi  = rightGetter.get() & 0xFF;
        int n16 = ((hi << 8) | lo) & 0xFFFF;
        resultSetter.accept(n16);
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
