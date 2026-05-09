package org.mochaboy.opcode.operations;

import org.mochaboy.CPU;
import org.mochaboy.Memory;
import org.mochaboy.opcode.Opcode;

public class CalculateCycles implements MicroOperation {
    private int[] cycleChoices;
    private int realCycles;
    private final Opcode opcode;

    public CalculateCycles(Opcode opcode) {
        this.opcode = opcode;
    }

    @Override
    public MicroOperation execute(CPU cpu, Memory memory) {
        cycleChoices = opcode.getOpcodeInfo().getCycles();
        if (cycleChoices.length == 1) {
            realCycles = cycleChoices[0];
        } else {
            boolean taken = opcode.isTaken();
            realCycles = cycleChoices[taken ? 0 : 1];
        }
        opcode.setRealCycles(realCycles);
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
