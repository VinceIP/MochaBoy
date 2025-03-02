package org.mochaboy.opcode.operations;

import org.mochaboy.CPU;
import org.mochaboy.DataType;
import org.mochaboy.Memory;
import org.mochaboy.registers.Registers;

import java.util.function.Supplier;


public class AluOperation implements MicroOperation {
    private final Supplier<Integer> destinationValue;
    private Supplier<Integer> sourceValue;
    private final Type type;
    private int result;
    private boolean is16BitOperation;

    public AluOperation(Type type, Supplier<Integer> destinationValue) {
        this.type = type;
        this.destinationValue = destinationValue;
    }

    public AluOperation(Type type, Supplier<Integer> destinationValue, Supplier<Integer> sourceValue) {
        this.type = type;
        this.destinationValue = destinationValue;
        this.sourceValue = sourceValue;
    }

    public AluOperation(Type type, Supplier<Integer> destinationValue, Supplier<Integer> sourceValue, boolean is16BitOperation) {
        this.type = type;
        this.destinationValue = destinationValue;
        this.sourceValue = sourceValue;
        this.is16BitOperation = is16BitOperation;
    }


    @Override
    public MicroOperation execute(CPU cpu, Memory memory) {
        int x = 0;
        int y = 0;
        if (destinationValue != null) x = destinationValue.get();
        if (sourceValue != null) y = sourceValue.get();
        switch (type) {
            case ADC -> result = adc(cpu, x, y);
            case ADD -> result = add(x, y);
            case CP -> result = 0; //FlagCalculator handles CP
            case DEC -> result = dec(x);
            case INC -> result = inc(x);
            case SBC -> result = sbc(cpu, x, y);
            case SUB -> result = sub(x, y);
        }
        applyResult(cpu);
        return this;
    }

    private int adc(CPU cpu, int x, int y) {
        int c = cpu.getRegisters().isFlagSet(Registers.FLAG_CARRY) ? 1 : 0;
        return ((x + y) + c) & 0xFF;
    }

    private int add(int x, int y) {
        return (x + y) & (is16BitOperation ? 0xFFFF : 0xFF);
    }

    private int inc(int x) {
        x = (x + 1) & (is16BitOperation ? 0xFFFF : 0xFF);
        return x;
    }

    private int dec(int x) {
        x = (x - 1) & (is16BitOperation ? 0xFFFF : 0xFF);
        return x;
    }

    private int sbc(CPU cpu, int x, int y) {
        int c = cpu.getRegisters().isFlagSet(Registers.FLAG_CARRY) ? 1 : 0;
        return ((x - y) - c) & 0xFF;
    }

    private int sub(int x, int y) {
        return (x - y) & 0xFF;
    }

    private void applyResult(CPU cpu) {
        Registers r = cpu.getRegisters();
        if (is16BitOperation) {
            r.setByName("HL", result); //Only for HL post inc/dec
        } else {
            r.setByName("A", result);
        }
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
