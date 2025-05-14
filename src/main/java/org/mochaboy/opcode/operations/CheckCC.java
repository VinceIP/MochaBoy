package org.mochaboy.opcode.operations;

import org.mochaboy.CPU;
import org.mochaboy.Memory;
import org.mochaboy.opcode.Opcode;
import org.mochaboy.registers.Registers;

public class CheckCC implements MicroOperation {
    private final Type type;
    private final Opcode opcode;

    public CheckCC(Type type, Opcode opcode) {
        this.type = type;
        this.opcode = opcode;
    }

    @Override
    public MicroOperation execute(CPU cpu, Memory memory) {
        Registers r = cpu.getRegisters();
        switch (type) {
            case Z -> {
                if (!r.isFlagSet(Registers.FLAG_ZERO)) {
                    opcode.setKillRemainingOps(true);
                }
            }
            case NZ -> {
                if (r.isFlagSet(Registers.FLAG_ZERO)) {
                    opcode.setKillRemainingOps(true);
                }
            }
            case C -> {
                if (!r.isFlagSet(Registers.FLAG_CARRY)) {
                    opcode.setKillRemainingOps(true);
                }
            }
            case NC -> {
                if (r.isFlagSet(Registers.FLAG_CARRY)) {
                    opcode.setKillRemainingOps(true);
                }
            }
        }
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

    public enum Type {
        Z,
        NZ,
        C,
        NC
    }
}
