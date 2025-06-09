package org.mochaboy.opcode.operations;

import org.mochaboy.CPU;
import org.mochaboy.Memory;
import org.mochaboy.opcode.Opcode;
import org.mochaboy.registers.Registers;

import java.util.function.Supplier;

public class BitShiftOperation implements MicroOperation {
    private final Type type;
    private final Supplier<Integer> targetValue;
    private final Opcode opcode;
    private int result;
    private int msb;

    public BitShiftOperation(Type type, Opcode opcode, Supplier<Integer> targetValue) {
        this.type = type;
        this.targetValue = targetValue;
        this.opcode = opcode;
    }


    @Override
    public MicroOperation execute(CPU cpu, Memory memory) {
        int v = targetValue != null ? targetValue.get() : 0;
        if (opcode.getOpcodeInfo().getOperands().length > 0 && opcode.getOpcodeInfo().getOperands()[0].getName().equals("HL")) {
            v = memory.readWord(v);
        }
        msb = ((v >> 7) & 1);
        switch (type) {
            case RL, RLA -> {
                int carryIn = cpu.getRegisters().isFlagSet(Registers.FLAG_CARRY) ? 1 : 0;
                result = ((v << 1) | carryIn) & 0xFF;
            }
            case RLC, RLCA -> {
                result = ((v << 1) | msb) & 0xFF;
            }
            case RR, RRA -> {
                int carryIn = cpu.getRegisters().isFlagSet(Registers.FLAG_CARRY) ? 1 : 0;
                int out = v & 0x01;
                result = ((v >>> 1) | (carryIn << 7)) & 0xFF;
                msb = out;
            }
            case RRC, RRCA -> {
                int out = v & 0x01;
                result = ((v >>> 1) | (out << 7)) & 0xFF;
                msb = out;
            }
            case SLA -> {
                result = (v << 1) & 0xFF;
            }
            case SRA -> {
                int old = v & 0xFF;
                int carry = old & 0x01;
                result = (old >> 1 ) | (old & 0x80);
                result &= 0xFF;
                msb = carry;
            }
            case SRL -> {
                msb = v & 1;
                result = (v >> 1) & 0xFF;
            }
            case SWAP -> {
                int upper = (v & 0xF0);
                int lower = (v & 0xF);
                result = ((upper >>> 4) | (lower << 4)) & 0xFF;
            }
        }
        applyResult(cpu);
        return this;
    }

    public void applyResult(CPU cpu) {
        String destRegister = "";
        if (opcode.getDestinationOperand() == null) {
            destRegister = "A";
        } else {
            destRegister = opcode.getDestinationOperand().getName();
            if (!opcode.getDestinationOperand().isImmediate()) {
                cpu.getMemory().writeByte(cpu.getRegisters().getHL(), result);
                return;
            }
        }
        opcode.setDestinationValue(result); //Update value so flag calc can read it - this may be a problem elsewhere too
        opcode.setSourceValue(msb);
        cpu.getRegisters().setByName(destRegister, result);
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
        RL,
        RLA,
        RLC,
        RLCA,
        RR,
        RRA,
        RRC,
        RRCA,
        SLA,
        SRA,
        SRL,
        SWAP
    }
}
