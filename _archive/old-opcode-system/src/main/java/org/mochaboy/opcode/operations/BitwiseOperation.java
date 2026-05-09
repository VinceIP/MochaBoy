package org.mochaboy.opcode.operations;

import org.mochaboy.CPU;
import org.mochaboy.Memory;
import org.mochaboy.opcode.Opcode;
import org.mochaboy.registers.Registers;

import java.util.function.Supplier;

public class BitwiseOperation implements MicroOperation {
    private final Supplier<Integer> sourceValue;
    private final Type type;
    private final Opcode opcode;
    private int result;

    public BitwiseOperation(Type type, Opcode opcode, Supplier<Integer> sourceValue) {
        this.sourceValue = sourceValue;
        this.type = type;
        this.opcode = opcode;
    }

    @Override
    public MicroOperation execute(CPU cpu, Memory memory) {
        int x = cpu.getRegisters().getA();
        int y = 0;
        if (sourceValue != null) y = sourceValue.get();
        switch (type) {
            case AND -> result = (x & y) & 0xFF;
            case CPL -> result = ~x & 0xFF;
            case OR -> result = (x | y) & 0xFF;
            case XOR -> result = (x ^ y) & 0xFF;
        }
        applyResult(cpu);
        return null;
    }

    private void applyResult(CPU cpu) {
        Registers r = cpu.getRegisters();
        r.setByName("A", result);
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
        AND,
        CPL,
        OR,
        XOR
    }
}
