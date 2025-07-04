package org.mochaboy.opcode.operations;

import org.mochaboy.CPU;
import org.mochaboy.Memory;
import org.mochaboy.opcode.Opcode;
import org.mochaboy.registers.Registers;

import java.util.function.Supplier;

public class StackOperation implements MicroOperation {
    private final Type type;
    private final Opcode opcode;
    private final Supplier<Integer> valueSupplier;

    public StackOperation(Type type, Opcode opcode, Supplier<Integer> valueSupplier) {
        this.type = type;
        this.opcode = opcode;
        this.valueSupplier = valueSupplier;
    }

    @Override
    public MicroOperation execute(CPU cpu, Memory memory) {
        Registers r = cpu.getRegisters();
        int value = valueSupplier.get();
        switch (type) {
            case POP -> {
                opcode.setDestinationValue(value);     // pass to FlagCalculator

                if ("AF".equals(opcode.getDestinationOperandString())) {
                    r.setAF(value);
                } else {
                    r.setByName(opcode.getDestinationOperandString(), value);
                }
                r.setSP((r.getSP() + 2) & 0xFFFF);
            }

            case PUSH_HIGH -> {
                int sp = ((r.getSP()) - 1) & 0xFFFF;
                int high = (value >>> 8) & 0xFF;
                memory.writeByte(sp, high);
                r.setSP(sp);
            }

            case PUSH_LOW -> {
                int sp = (r.getSP() - 1) & 0xFFFF;
                int low = value & 0xFF;
                if ("AF".equals(opcode.getDestinationOperandString())) {
                    low &= 0xF0; //Mask flag nibble to preserve flags
                }
                memory.writeByte(sp, low);
                r.setSP(sp);

            }
        }
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

    public enum Type {
        PUSH_HIGH,
        PUSH_LOW,
        POP
    }
}
