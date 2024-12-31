package org.mochaboy.opcode.operations;

import org.mochaboy.CPU;
import org.mochaboy.Memory;

import java.util.function.Consumer;
import java.util.function.Supplier;

public class Load implements MicroOperation {
    private Supplier<Integer> source;
    private Consumer<Integer> destination;
    private int result;

    public Load(Supplier<Integer> source, Consumer<Integer> destination) {
        this.source = source;
        this.destination = destination;
    }

    @Override
    public MicroOperation execute(CPU cpu, Memory memory) {
        result = source.get(); //Get val from source
        destination.accept(result); //Write val to destination
        return this;
    }

    @Override
    public int getCycles() {
        return 0;
    }

    @Override
    public int getResult() {
        return result;
    }
}
