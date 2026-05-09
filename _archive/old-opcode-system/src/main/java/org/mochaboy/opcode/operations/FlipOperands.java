package org.mochaboy.opcode.operations;

import org.mochaboy.CPU;
import org.mochaboy.Memory;
import org.mochaboy.opcode.Opcode;

public class FlipOperands implements MicroOperation {
    private final Opcode opcode;

    public FlipOperands(Opcode opcode) {
        this.opcode = opcode;
    }

    @Override
    public MicroOperation execute(CPU cpu, Memory memory) {
        int destination = opcode.getDestinationValue();
        int source = opcode.getSourceValue();
        opcode.setSourceValue(destination);
        opcode.setDestinationValue(source);
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
