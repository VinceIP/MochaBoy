package org.mochaboy;

public class CPU {

    private Memory memory;
    private Registers registers;
    private Stack stack;
    private long tStateCounter;

    public CPU(Memory memory) {
        this.memory = memory;
        this.registers = new Registers();
        stack = new Stack(this);
    }

    private void init() {

    }

    public void run() {
        try {
            while (registers.getPC() < memory.getMemoryLength()) {
                int opcode = (fetch() & 0xFF);
                execute(opcode);
            }
        } catch (OutOfMemoryError e) {
            System.out.println("Out of mem error: " + e.getMessage());
        }

    }

    public byte fetch() {
        return (byte) (memory.readByte(registers.getPC()) & 0xFF);
    }

    public void execute(int opcode) {
        //OpcodeHandler.execute(this, opcode);
    }

    public Memory getMemory() {
        return memory;
    }

    public Registers getRegisters() {
        return registers;
    }

    public long getTStateCounter() {
        return tStateCounter;
    }

    public void settStateCounter(long value) {
        this.tStateCounter = value;
    }

    public void incrementTStateCounter(long value) {
        this.tStateCounter += value;
    }

    public Stack getStack() {
        return stack;
    }
}
