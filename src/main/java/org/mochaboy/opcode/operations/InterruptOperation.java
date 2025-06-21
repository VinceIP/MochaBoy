package org.mochaboy.opcode.operations;

import org.mochaboy.CPU;
import org.mochaboy.Memory;

public class InterruptOperation implements MicroOperation {
    private final Type type;
    private final CPU cpu;

    public InterruptOperation(Type type, CPU cpu) {
        this.type = type;
        this.cpu = cpu;
    }

    @Override
    public MicroOperation execute(CPU cpu, Memory memory) {
        switch (type) {
            case DI -> {
                cpu.setIME(false);
            }
            case EI -> {
                cpu.setPendingImeEnable(true);
            }

            case EI_QUICK -> cpu.setIME(true);

            case HALT -> {
                    cpu.setHalt(true);
            }
        }
        return this;
    }

    @Override
    public int getCycles() {
        if (type != Type.EI_QUICK)
            return 1;
        else return 0;
    }

    @Override
    public int getResult() {
        return 0;
    }

    public enum Type {
        DI,
        EI,
        EI_QUICK,
        HALT
    }
}
