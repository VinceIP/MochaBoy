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
    private int x;
    private int y;
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
        if (opcode.getDestinationType() != null)
            is16BitOperation = (opcode.getDestinationType().equals(DataType.R16) || opcode.getDestinationType().equals(DataType.N16));
        else is16BitOperation = false;
    }


    @Override
    public MicroOperation execute(CPU cpu, Memory memory) {
        if (destinationValue != null) x = destinationValue.get();
        if (sourceValue != null) y = sourceValue.get();
        switch (type) {
            case ADC -> result = adc(cpu, x, y);
            case ADD -> result = add(x, y, cpu);
            case CP -> {
                opcode.setSourceValue(y);
                opcode.setDestinationValue(x);
                return this; //FlagCalculator handles CP
            }
            case DAA -> {
                daa(cpu, opcode);
                return this;
            }
            case DEC -> {
                result = dec(x, memory,cpu);
            }
            case INC -> {
                result = inc(x, memory,cpu);
            }
            case SBC -> result = sbc(cpu, x, y);
            case SUB -> result = sub(x, y);
            case POST_DEC -> {
                Registers r = cpu.getRegisters();
                String targetReg = "";
                if (opcode.getSourceOperand().isIncrement() || opcode.getSourceOperand().isDecrement()) {
                    targetReg = opcode.getSourceOperand().getName();
                } else if (opcode.getDestinationOperand().isIncrement() || opcode.getDestinationOperand().isDecrement()) {
                    targetReg = opcode.getDestinationOperand().getName();
                }
                int val = (r.getByName(targetReg) - 1) & 0xFFFF;
                r.setByName(targetReg, val);
                return this; //Exit early
            }
            case POST_INC -> {
                Registers r = cpu.getRegisters();
                String targetReg = "";
                if (opcode.getSourceOperand().isIncrement() || opcode.getSourceOperand().isDecrement()) {
                    targetReg = opcode.getSourceOperand().getName();
                } else if (opcode.getDestinationOperand().isIncrement() || opcode.getDestinationOperand().isDecrement()) {
                    targetReg = opcode.getDestinationOperand().getName();
                }
                int val = (r.getByName(targetReg) + 1) & 0xFFFF;
                r.setByName(targetReg, val);
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
            return ((x + signedDisp) & 0xFFFF);
        } else {
            return (x + y) & (is16BitOperation ? 0xFFFF : 0xFF);

        }
    }

    private int dec(int x, Memory memory, CPU cpu) {
        if (!opcode.getDestinationOperand().isImmediate()) {
            x = memory.readByte(cpu.getRegisters().getHL());
            opcode.setDestinationValue(x);
        }
        return (x - 1) & (is16BitOperation ? 0xFFFF : 0xFF);
    }

    private int inc(int x, Memory memory, CPU cpu) {
        if (!opcode.getDestinationOperand().isImmediate()) {
            x = memory.readByte(cpu.getRegisters().getHL());
            opcode.setDestinationValue(x);
        }
        return (x + 1) & (is16BitOperation ? 0xFFFF : 0xFF);
    }

    private int sbc(CPU cpu, int x, int y) {
        int c = cpu.getRegisters().isFlagSet(Registers.FLAG_CARRY) ? 1 : 0;
        return ((x - y) - c) & 0xFF;
    }

    private int sub(int x, int y) {
        return (x - y) & 0xFF;
    }

    private void daa(CPU cpu, Opcode opcode) {
        Registers r = cpu.getRegisters();
        int a = r.getA();
        boolean n = r.isFlagSet(Registers.FLAG_SUBTRACT);
        boolean h = r.isFlagSet(Registers.FLAG_HALF_CARRY);
        boolean c = r.isFlagSet(Registers.FLAG_CARRY);
        int result = a;

        if (!n) {
            if (h || (result & 0xF) > 9) result += 0x06;
            if (c || (result > 0x9F)) result += 0x60;
        } else {
            if (h) result = (result - 6) & 0xFF;
            if (c) result -= 0x60;
        }

        r.clearFlag(Registers.FLAG_HALF_CARRY);
        if ((result & 0x100) != 0) r.setFlag(Registers.FLAG_CARRY);

        result &= 0xFF;
        if (result == 0) r.setFlag(Registers.FLAG_ZERO);
        else r.clearFlag(Registers.FLAG_ZERO);
        r.setA(result);
    }


    private int pullXFromMem(Memory memory, int address) {
        return memory.readByte(address);
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
            int addr = cpu.getRegisters().getHL();
            cpu.getMemory().writeByte(addr, result & 0xFF);
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
        POST_DEC,
        DAA
    }
}
