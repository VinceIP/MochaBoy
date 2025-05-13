package org.mochaboy.opcode.operations;

import org.mochaboy.CPU;
import org.mochaboy.Memory;
import org.mochaboy.opcode.Opcode;
import org.mochaboy.registers.Registers;

import java.util.function.Supplier;

public class StackOperation implements MicroOperation {
    private final Type type;
    private final Opcode opcode;
    private final int value;

    public StackOperation(Type type, Opcode opcode, Supplier<Integer> valueSupplier) {
        this.type = type;
        this.opcode = opcode;
        this.value = valueSupplier.get();
    }

    @Override
    public MicroOperation execute(CPU cpu, Memory memory) {
        Registers r = cpu.getRegisters();
        switch (type) {
            case POP -> {
                if (opcode.getDestinationOperandString().equals("AF")) {
                    r.setByName("AF", ((value & 0xFF00) | (value & 0x00F0)));
                }
                r.setByName(opcode.getDestinationOperandString(), value);
                r.setSP((r.getSP() + 2) & 0xFFFF);
            }
            
            case PUSH -> {

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
        PUSH,
        POP
    }
}
