package org.mochaboy.opcode.operations;

import org.mochaboy.CPU;
import org.mochaboy.Memory;

import java.util.function.Supplier;

public class BitFlagOperation implements MicroOperation {
    private final Type type;
    private final Supplier<Integer> bitIndex;
    private final Supplier<Integer> targetValue;
    private int result;

    //FYI - BIT is not handled here because it changes no values, it's handled in FlagCalculator

    public BitFlagOperation(Type type, Supplier<Integer> bitIndex, Supplier<Integer> targetValue, Type type1, Supplier<Integer> bitIndex1, Supplier<Integer> targetValue1) {
        this.type = type;
        this.bitIndex = bitIndex;
        this.targetValue = targetValue;
    }

    @Override
    public MicroOperation execute(CPU cpu, Memory memory) {
        int b = 0;
        int t = 0;
        result = 0;
        if (bitIndex != null) b = bitIndex.get();
        if (targetValue != null) t = targetValue.get();
        switch (type) {
            case RES -> result = (t & ~(1 << b));
            case SET -> result = t | (1 << b);
        }
        return null;
    }

    @Override
    public int getCycles() {
        return 0;
    }

    @Override
    public int getResult() {
        return 0;
    }

    public enum Type {
        RES,
        SET
    }
}
