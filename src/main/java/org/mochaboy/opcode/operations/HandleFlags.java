package org.mochaboy.opcode.operations;

import org.mochaboy.CPU;
import org.mochaboy.Memory;
import org.mochaboy.opcode.FlagCalculator;
import org.mochaboy.opcode.OpcodeInfo;

import java.util.function.Supplier;

public class HandleFlags implements MicroOperation {
    private final FlagCalculator flagCalculator;
    private final CPU cpu;
    private final OpcodeInfo opcodeInfo;
    private final Supplier<Integer> x;
    private final Supplier<Integer> y;

    public HandleFlags(FlagCalculator flagCalculator, CPU cpu, OpcodeInfo opcodeInfo, Supplier<Integer> x, Supplier<Integer> y) {
        this.flagCalculator = flagCalculator;
        this.cpu = cpu;
        this.opcodeInfo = opcodeInfo;
        this.x = x;
        this.y = y;
    }


    @Override
    public MicroOperation execute(CPU cpu, Memory memory) {
        int xVal = x.get();
        int yVal = y.get();
        flagCalculator.processFlags(cpu, opcodeInfo, xVal, yVal);
        return null;
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
