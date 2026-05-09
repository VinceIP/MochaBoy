package org.mochaboy.opcode.operations;

import org.mochaboy.CPU;
import org.mochaboy.Memory;

public class EmptyCycle implements MicroOperation{
    @Override
    public MicroOperation execute(CPU cpu, Memory memory) {
        return this;
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
