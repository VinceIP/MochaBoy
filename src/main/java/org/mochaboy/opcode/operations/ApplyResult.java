package org.mochaboy.opcode.operations;

import org.mochaboy.CPU;
import org.mochaboy.DataType;
import org.mochaboy.Memory;
import org.mochaboy.registers.Registers;

import java.util.function.Supplier;

public class ApplyResult implements MicroOperation {
    private final Supplier<Integer> resultSupplier;
    private final DataType type;
    private final String target;
    private int result;

    public ApplyResult(Supplier<Integer> resultSupplier, DataType type, String target) {
        this.resultSupplier = resultSupplier;
        this.type = type;
        this.target = target;
    }

    private void applyResult(CPU cpu, Memory memory) {
        result = resultSupplier.get();
        Registers r = cpu.getRegisters();
        switch (type) {
            case R8:
                r.setByName(target, (result & 0xFF));
                break;
            case R16:
                r.setByName(target, (result & 0xFFFF));
                break;
            case MEMORY:
                int address = r.getByName(target);
                memory.writeByte(address, result);
                break;
        }
    }

    @Override
    public MicroOperation execute(CPU cpu, Memory memory) {
        applyResult(cpu, memory);
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

//    public enum Type {
//        REGISTER8BIT,
//        REGISTER16BIT,
//        MEMORY
//    }


}
