package org.mochaboy.opcode.operations;

import org.mochaboy.CPU;
import org.mochaboy.DataType;
import org.mochaboy.Memory;
import org.mochaboy.opcode.Opcode;
import org.mochaboy.opcode.OpcodeInfo;
import org.mochaboy.registers.Registers;

import java.util.function.Supplier;


public class AluOperation implements MicroOperation {
    private final Supplier<Integer> destinationValue;
    private Supplier<Integer> sourceValue;
    private final Type type;
    private int result;
    private boolean is16BitOperation;
    private Opcode opcode;

    public AluOperation(Type type, Supplier<Integer> destinationValue) {
        this.type = type;
        this.destinationValue = destinationValue;
    }

    public AluOperation(Type type, Opcode opcode, Supplier<Integer> destinationValue, Supplier<Integer> sourceValue) {
        this.type = type;
        this.opcode = opcode;
        this.destinationValue = destinationValue;
        this.sourceValue = sourceValue;
        is16BitOperation = (opcode.getDestinationType().equals(DataType.R16) || opcode.getDestinationType().equals(DataType.N16));
    }


    @Override
    public MicroOperation execute(CPU cpu, Memory memory) {
        int x = 0;
        int y = 0;
        if (destinationValue != null) x = destinationValue.get();
        if (sourceValue != null) y = sourceValue.get();
        switch (type) {
            case ADC -> result = adc(cpu, x, y);
            case ADD -> result = add(x, y, cpu);
            case CP -> {
                return this; //FlagCalculator handles CP
            }
            case DEC -> result = dec(x);
            case INC -> result = inc(x);
            case SBC -> result = sbc(cpu, x, y);
            case SUB -> result = sub(x, y);
            case POST_DEC -> {
                Registers r = cpu.getRegisters();
                int val = (r.getHL() - 1) & 0xFFFF;
                r.setByName("HL", val);
                return this; //Exit early
            }
            case POST_INC -> {
                Registers r = cpu.getRegisters();
                int val = (r.getHL() + 1) & 0xFFFF;
                r.setByName("HL", val);
                return this; //Exit early
            }
        }
        applyResult(cpu);
        return this;
    }

    private int adc(CPU cpu, int x, int y) {
        int c = cpu.getRegisters().isFlagSet(Registers.FLAG_CARRY) ? 1 : 0;
        return ((x + y) + c) & 0xFF;
    }

    private int add(int x, int y, CPU cpu) {
        OpcodeInfo o = opcode.getOpcodeInfo();
        if ((o.getOperands().length > 1 && o.getOperands()[1].getName().equals("e8"))
                || opcode.getOpcodeInfo().getMnemonic().equals("JR")) { //If this is ADD SP, e8 or a JR;
            //System.out.printf("\n%04X", cpu.getRegisters().getPC());
            int signedDisp = (y << 24) >> 24; //Extend e8 to 32 bits
            return (x + signedDisp);
        } else {
            return (x + y) & (is16BitOperation ? 0xFFFF : 0xFF);

        }
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

    @Override
    public int getCycles() {
        return 0;
    }

    @Override
    public int getResult() {
        return result;
    }

    public void applyResult(CPU cpu) {
        DataType dt = opcode.getDestinationType();
        String destinationRegister = opcode.getDestinationOperandString();
        if (dt == DataType.N16) {
            //Writes result to memory for INC [HL], DEC [HL]
            int addr = opcode.getDestinationValue();
            cpu.getMemory().writeByte(addr, result & 0xFF);
            cpu.getMemory().writeByte(addr + 1, (result >> 8) & 0xFF);
        } else {
            if (Registers.isValidRegister(cpu, destinationRegister)) {
                cpu.getRegisters().setByName(destinationRegister, result);
            }
        }
    }

    public enum Type {
        ADC,
        ADD,
        CP,
        DEC,
        INC,
        SBC,
        SUB,
        POST_INC,
        POST_DEC
    }
}
