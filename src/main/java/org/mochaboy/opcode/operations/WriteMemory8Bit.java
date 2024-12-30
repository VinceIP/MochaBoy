package org.mochaboy.opcode.operations;

import org.mochaboy.CPU;
import org.mochaboy.Memory;

import java.util.function.Supplier;

public class WriteMemory8Bit implements MicroOperation{
    private final Supplier<Integer> addressSupplier;
    private final Supplier<Integer> dataSupplier;

    public WriteMemory8Bit(Supplier<Integer> addressSupplier, Supplier<Integer> dataSupplier) {
        this.addressSupplier = addressSupplier;
        this.dataSupplier = dataSupplier;
    }


    @Override
    public MicroOperation execute(CPU cpu, Memory memory) {
        int addr = addressSupplier.get() & 0xFFFF;
        int data = dataSupplier.get() & 0xFF;
        memory.writeByte(addr, data);
        return this;
    }

    @Override
    public int getCycles() {
        return 2;
    }

    @Override
    public int getResult() {
        return 0;
    }
}
