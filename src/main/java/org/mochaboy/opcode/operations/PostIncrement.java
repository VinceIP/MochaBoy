package org.mochaboy.opcode.operations;

import org.mochaboy.CPU;
import org.mochaboy.Memory;

public class PostIncrement implements MicroOperation{
    @Override
    public MicroOperation execute(CPU cpu, Memory memory) {
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
}
