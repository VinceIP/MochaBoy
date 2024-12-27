package org.mochaboy;

import org.mochaboy.opcode.*;
import org.mochaboy.registers.Interrupt;
import org.mochaboy.registers.Registers;
import org.mochaboy.registers.Timer;

import java.io.IOException;
import java.util.Map;

public class CPU extends Thread {
    private PPU ppu;
    private Memory memory;
    private Registers registers;
    private Timer timer;
    private Stack stack;
    private Interrupt interrupt;
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
    private long elapsedEmulatedTimeNs;
    private Map<String, Integer> map;

    public CPU(PPU ppu, Memory memory) throws IOException {
        this.ppu = ppu;
        this.memory = memory;
        registers = new Registers();
        timer = new Timer(this.memory);
        stack = new Stack(this);
        interrupt = new Interrupt(this, this.memory);
        opcodeLoader = new OpcodeLoader();
        opcodeWrapper = opcodeLoader.getOpcodeWrapper();
        opcodeHandler = new OpcodeHandler(opcodeWrapper);
        map = memory.getMemoryMap();
    }

    @Override
    public void run() {
        running = true;
        long lastDivUpdateCheck = 0;
        long frameStartTime = System.nanoTime();
        int tacFreq = timer.getTacFreq();
        int lastTacUpdateCheck = 0;
        while (running) {
            OpcodeInfo opcode = fetch();
            int pc = registers.getPC();
//            if (pc == 0x233) {
//                printDebugLog(opcode);
//            }

//            if(pc >= 0xFF80 && pc <= 0xFFFE){
//                printHRAM();
//            }

//            if (opcode.getOpcode() == 0xE2 || opcode.getOpcode() == 0xF2) {
//                printHRAM();
//            }
            if (pc > 0x02F0) {
                printDebugLog(opcode);
            }

            int cycles = execute(opcode);
            elapsedEmulatedTimeNs += (long) (cycles * 238.4);
            int isPrefix = opcode.isPrefixed() ? 1 : 0;
            if (!didJump) registers.setPC(pc + opcode.getBytes() + isPrefix);
            else didJump = false;
            //handle pending IME switch
            if (isPendingInterruptSwitch()) {
                setIME(true);
                setPendingInterruptSwitch(false);
            }
            //handle HALT
            ppu.step(cycles);
            totalCycles += cycles;


            //Update timers
            if (elapsedEmulatedTimeNs - lastDivUpdateCheck >= Timer.DIV_INC_TIME_NS) {
                timer.incDiv();
                lastDivUpdateCheck = elapsedEmulatedTimeNs;
            }

            //Handle interrupts
            if (isIME()) {
                int IE = memory.readByte(map.get("IE"));
                int IF = memory.readByte(map.get("IF"));
                int triggered = IE & IF; //Get interrupts currently pending & interrupts enabled
                for (int i = 0; i < 5; i++) { //Check bits of IF in order of interrupt priority
                    if (((triggered >> i) & 1) == 1) {
                        IF &= ~(1 << i); //Clear the bit if set
                        memory.writeByte(map.get("IF"), IF);
                        setIME(false);
                        switch (i) {
                            case 0:
                                interrupt.handleInterrupt(Interrupt.INTERRUPT.VBLANK);
                                break;
                            case 1:
                                interrupt.handleInterrupt(Interrupt.INTERRUPT.STAT);
                                break;
                            case 2:
                                interrupt.handleInterrupt(Interrupt.INTERRUPT.TIMER);
                                break;
                            case 3:
                                interrupt.handleInterrupt(Interrupt.INTERRUPT.SERIAL);
                                break;
                            case 4:
                                interrupt.handleInterrupt(Interrupt.INTERRUPT.JOYPAD);
                                break;
                        }
                        break;
                    }

                }
            }

            //CPU cycle timing
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

    private void printDebugLog(OpcodeInfo opcode) {
        int pc = getRegisters().getPC();
        int rawOpcode = memory.readByte(pc) & 0xFF;
        Operand[] ops = opcode.getOperands();
        StringBuilder sb = new StringBuilder();
        // We'll track where to read immediate values from
        int immOffset = 1; // start from pc+1

        for (int i = 0; i < ops.length; i++) {
            String name = ops[i].getName();
            switch (name) {
                case "n8", "d8", "a8", "e8": {
                    int val = memory.readByte(pc + immOffset) & 0xFF;
                    sb.append(String.format("0x%02X", val));
                    immOffset++;
                    break;
                }
                case "n16", "d16", "a16": {
                    int low = memory.readByte(pc + immOffset) & 0xFF;
                    int high = memory.readByte(pc + immOffset + 1) & 0xFF;
                    int val = (high << 8) | low;
                    sb.append(String.format("0x%04X", val));
                    immOffset += 2;
                    break;
                }
                default:
                    sb.append(name);
                    break;
            }
            if (i < ops.length - 1) {
                sb.append(", ");
            }
        }

        System.out.printf("PC=%04X OPCODE=%02X (%s) %s\n",
                pc, rawOpcode, opcode.getMnemonic(), sb.toString());
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

    public Interrupt getInterrupt() {
        return interrupt;
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

    public long getElapsedEmulatedTimeNs() {
        return elapsedEmulatedTimeNs;
    }

    public void printHRAM() {
        System.out.println("-------------------- HRAM (0xFF80 - 0xFFFE) --------------------");
        for (int row = 0xFF80; row < 0x10000; row += 16) {
            System.out.printf("0x%04X: ", row);
            for (int col = 0; col < 16 && (row + col) < 0x10000; col++) {
                int address = row + col;
                int value = memory.readByte(address);
                System.out.printf("%02X ", value);
            }
            System.out.println();
        }
        System.out.println("-----------------------------------------------------------------");
    }

}
