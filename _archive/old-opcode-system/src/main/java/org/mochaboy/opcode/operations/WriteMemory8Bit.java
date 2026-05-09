package org.mochaboy.opcode.operations;

import org.mochaboy.CPU;
import org.mochaboy.Memory;

import java.util.function.Supplier;

public class WriteMemory8Bit implements MicroOperation{
    private final Supplier<Integer> addressSupplier;
    private final Supplier<Integer> sourceSupplier;

    public WriteMemory8Bit(Supplier<Integer> addressSupplier, Supplier<Integer> dataSupplier) {
        this.addressSupplier = addressSupplier;
        this.sourceSupplier = dataSupplier;
    }


    @Override
    public MicroOperation execute(CPU cpu, Memory memory) {
        int addr = addressSupplier.get() & 0xFFFF;
        int data = sourceSupplier.get() & 0xFF;
        memory.writeByte(addr, data);
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
