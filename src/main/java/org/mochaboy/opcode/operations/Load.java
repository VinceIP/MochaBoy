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
            //If ss is a register but not immediate, or explicitly "a8", "n8", "n16", we expect a source value
            source = opcode.getSourceValue();
        }

        //Store in destination
        if (Registers.isValidRegister(cpu, ds) && opcode.getDestinationOperand().isImmediate()) { //If destination is a register
            source = (ds.length() > 1) ? source & 0xFFFF : source & 0xFF; //Mask for 16 or 8 bits
            r.setByName(ds, source);
        } else {
            memory.writeByte(opcode.getDestinationValue(), source & 0xFF);
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
