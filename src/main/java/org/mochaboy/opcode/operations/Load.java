package org.mochaboy.opcode.operations;

import org.mochaboy.CPU;
import org.mochaboy.DataType;
import org.mochaboy.Memory;
import org.mochaboy.opcode.Opcode;
import org.mochaboy.registers.Registers;

public class Load implements MicroOperation {
    private final Opcode opcode;

    public Load(Opcode opcode) {
        this.opcode = opcode;
    }

    @Override
    public MicroOperation execute(CPU cpu, Memory memory) {
        Registers r = cpu.getRegisters();
        String ss = opcode.getSourceOperandString();
        String ds = opcode.getDestinationOperandString();
        DataType destinationType = opcode.getDestinationType();
        DataType sourceType = opcode.getSourceType();
        int source;

        //Get the value to be copied
        if (sourceType == DataType.R8 || sourceType == DataType.R16) {
            source = r.getByName(ss); //Pull the source value from a register
        } else {
            //If ss is a register but not immediate, or explicitly "a8", "n8", "n16", we expect a source value to be in the opcode
            source = opcode.getSourceValue();
        }
        //Store in destination
        switch (destinationType) {
            //Writes to registers
            case R8, R16 -> {
                //Special case LD HL, SP+e8
                if (opcode.getOpcodeInfo().getOpcode() == 0xF8) {
                    //Get e8, extend it, then add to source (SP) and mask to 16 bits
                    int e8 = opcode.getExtraValue();
                    e8 = (e8 << 24) >> 24;
                    source = ((source + e8) & 0xFFFF);
                }
                if (Registers.isValidRegister(cpu, ds)) r.setByName(ds, source);
            }
            //Writes to memory
            case A8 -> {
                int addr = 0xFF00 | opcode.getDestinationValue();
                memory.writeByte(addr, source);
            }
            case N16 -> {
                int addr = opcode.getDestinationValue();
                memory.writeByte(addr, source & 0xFF);
                if (sourceType == DataType.R16)
                    memory.writeByte(addr + 1, (source >> 8) & 0xFF);
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
}
