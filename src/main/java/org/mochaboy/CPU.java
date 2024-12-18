package org.mochaboy;

import java.io.IOException;

public class CPU extends Thread {

    private Memory memory;
    private Registers registers;
    private Stack stack;
    private OpcodeLoader opcodeLoader;
    private OpcodeWrapper opcodeWrapper;
    private OpcodeHandler opcodeHandler;
    private long tStateCounter;
    private boolean IME;
    private boolean pendingInterruptSwitch;
    private boolean lowPowerMode;
    private boolean stopMode;
    private boolean didJump;
    private boolean running;

    public CPU(Memory memory) throws IOException {
        this.memory = memory;
        this.registers = new Registers();
        stack = new Stack(this);
        opcodeLoader = new OpcodeLoader();
        opcodeWrapper = opcodeLoader.getOpcodeWrapper();
        opcodeHandler = new OpcodeHandler(opcodeWrapper);
    }

    @Override
    public void run() {
        running = true;
        while (running) {
            System.out.printf("0x%04X\n", getRegisters().getPC());
            OpcodeInfo opcode = fetch();
            execute(opcode);
            if (!didJump) {
                getRegisters().incrementPC();
            } else didJump = false;
            //handle pending IME switch
            //handle HALT
            try {
                Thread.sleep(1);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }

    }

    public void stopCPU(){
        running = false;
    }


    public OpcodeInfo fetch() {
        int opcode = memory.readByte(registers.getPC()) & 0xFF;
        OpcodeInfo opcodeInfo;
        String hexString;
        if (opcode == 0xCB) {
            getRegisters().incrementPC();
            opcode = memory.readByte(registers.getPC()) & 0xFF;
            hexString = String.format("0x%02X", opcode);
            opcodeInfo = opcodeWrapper.getCbprefixed().get(hexString);
        } else {
            hexString = String.format("0x%02X", opcode);
            opcodeInfo = opcodeWrapper.getUnprefixed().get(hexString);
        }
        return opcodeInfo;
    }

    public void execute(OpcodeInfo opcodeInfo) {
        opcodeHandler.execute(this, opcodeInfo);
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

    public boolean isDidJump() {
        return didJump;
    }

    public void setDidJump(boolean didJump) {
        this.didJump = didJump;
    }
}
