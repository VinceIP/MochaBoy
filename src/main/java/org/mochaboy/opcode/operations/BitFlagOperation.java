package org.mochaboy.opcode.operations;

import org.mochaboy.CPU;
import org.mochaboy.Memory;
import org.mochaboy.opcode.Opcode;

import java.util.function.Supplier;

public class BitFlagOperation implements MicroOperation {
    private final Type type;
    private final Supplier<Integer> bitIndex;
    private final Supplier<Integer> targetValue;
    private final Opcode opcode;
    private int result;

    //FYI - BIT is not handled here because it changes no values, it's handled in FlagCalculator

    public BitFlagOperation(Type type, Opcode opcode, Supplier<Integer> bitIndex, Supplier<Integer> targetValue) {
        this.type = type;
        this.opcode = opcode;
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
        applyResult(cpu);
        return this;
    }

    public void applyResult(CPU cpu) {
        if (opcode.getSourceOperand().isRegister()) {
            cpu.getRegisters().setByName(opcode.getSourceOperandString(), result);
        }
        else cpu.getMemory().writeByte(opcode.getSourceValue(), result);
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
        RES,
        SET
    }
}
