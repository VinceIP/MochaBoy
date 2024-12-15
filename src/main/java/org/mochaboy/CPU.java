package org.mochaboy;

public class CPU {

    private Memory memory;
    private Registers registers;
    private Stack stack;
    private long tStateCounter;
    private boolean IME;
    private boolean pendingInterruptSwitch;
    private boolean lowPowerMode;
    private boolean stopMode;

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
                //handle pending IME switch
                //handle HALT
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

    public boolean isIME() {
        return IME;
    }

    public void setIME(boolean IME) {
        this.IME = IME;
    }

    public boolean isPendingInterruptSwitch() {
        return pendingInterruptSwitch;
    }

    public void setPendingInterruptSwitch(boolean pendingInterruptSwitch) {
        this.pendingInterruptSwitch = pendingInterruptSwitch;
        setIME(true); //GET RID OF THIS LATER
    }

    public boolean isLowPowerMode() {
        return lowPowerMode;
    }

    public void setLowPowerMode(boolean lowPowerMode) {
        this.lowPowerMode = lowPowerMode;
    }

    public boolean isStopMode() {
        return stopMode;
    }

    public void setStopMode(boolean stopMode) {
        this.stopMode = stopMode;
    }
}
