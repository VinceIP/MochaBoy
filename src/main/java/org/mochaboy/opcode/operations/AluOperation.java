package org.mochaboy.opcode.operations;

import org.mochaboy.CPU;
import org.mochaboy.DataType;
import org.mochaboy.Memory;
import org.mochaboy.registers.Registers;

import java.util.function.Supplier;

public class AluOperation implements MicroOperation {
    private final Supplier<Integer> destinationValue;
    private final Type type;
    private DataType destinationType;
    private int result;

    public AluOperation(Type type, Supplier<Integer> destinationValue) {
        this.type = type;
        this.destinationValue = destinationValue;
    }

    public AluOperation(Type type, DataType destinationType, Supplier<Integer> destinationValue) {
        this.type = type;
        this.destinationValue = destinationValue;
        this.destinationType = destinationType;
    }


    @Override
    public MicroOperation execute(CPU cpu, Memory memory) {
        int x = destinationValue.get();
                switch (type) {
                    case INC -> result = inc(x);
                    case DEC -> result = dec(x);
                }
        applyResult(cpu);
        return this;
    }

    private void add() {
    }

    private int inc(int x) {
        x = (x + 1) & 0xFFFF;
        return x;
    }

    private int dec(int x) {
        x = (x - 1) & 0xFFFF;
        return x;
    }

    private void applyResult(CPU cpu) {
        Registers r = cpu.getRegisters();
        if (destinationType != null && destinationType == DataType.R16) {
            r.setByName("HL", result); //Only for HL post inc/dec
        }
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
        ADC,
        ADD,
        CP,
        DEC,
        INC,
        SBC,
        SUB
    }
}
