package org.mochaboy;

import java.io.IOException;

public class CPU extends Thread {
    private PPU ppu;
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
    private int totalCycles;
    private static final int CYCLES_PER_FRAME = 70224;
    private static final double FRAME_TIME_MS = 1000.0 / 59.7275;
    private boolean runOnce = false;

    public CPU(PPU ppu, Memory memory) throws IOException {
        this.ppu = ppu;
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
        long frameStartTime = System.nanoTime();
        while (running) {
            OpcodeInfo opcode = fetch();
            int pc = registers.getPC();
            //System.out.printf("PC: 0x%04X\n", pc);

//            if (pc == 0x0048) {
//                ppu.printVRAM();
//                System.out.println(String.format("9910: %04X", memory.readByte(0x9910)));
//            }

            int cycles = execute(opcode);

            if (!didJump) {
                //getRegisters().incrementPC();
                registers.setPC(pc + opcode.getBytes());
            } else didJump = false;
            //handle pending IME switch
            //handle HALT
            ppu.step(cycles);
            totalCycles += cycles;
            if (totalCycles >= CYCLES_PER_FRAME) {
                long frameEndTime = System.nanoTime();
                double frameTimeMs = (frameEndTime - frameStartTime) / 1_000_000.0;
                double sleepTime = FRAME_TIME_MS - frameTimeMs;
                if (sleepTime > 0) {
                    try {
                        Thread.sleep((long) sleepTime);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
                totalCycles -= CYCLES_PER_FRAME;
                frameStartTime = System.nanoTime();
            }
        }

    }

    public void stopCPU() {
        running = false;
    }


    public OpcodeInfo fetch() {
        int opcode = memory.readByte(registers.getPC()) & 0xFF;
        OpcodeInfo opcodeInfo;
        String hexString;
        if (opcode == 0xCB) {
            //getRegisters().incrementPC();
            opcode = memory.readByte(registers.getPC() + 1) & 0xFF;
            hexString = String.format("0x%02X", opcode);
            opcodeInfo = opcodeWrapper.getCbprefixed().get(hexString);
        } else {
            hexString = String.format("0x%02X", opcode);
            opcodeInfo = opcodeWrapper.getUnprefixed().get(hexString);
        }
        return opcodeInfo;
    }

    public int execute(OpcodeInfo opcodeInfo) {
        if (opcodeInfo == null) return 0;
        else
            return opcodeHandler.execute(this, opcodeInfo);
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
