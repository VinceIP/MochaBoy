package org.mochaboy;

import org.mochaboy.opcode.*;
import org.mochaboy.opcode.operations.MicroOperation;
import org.mochaboy.opcode.operations.ReadImmediate8bit;
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
    private Input input;
    private Opcode currentOpcodeObject;
    private OpcodeLoader opcodeLoader;
    private OpcodeBuilder opcodeBuilder;
    private OpcodeWrapper opcodeWrapper;
    //private OpcodeHandler opcodeHandler;
    private State state;
    private long tStateCounter;
    private boolean built = false;
    private boolean IME;
    private boolean pendingInterruptSwitch;
    private boolean halt;
    private boolean lowPowerMode;
    private boolean stopMode;
    private boolean didJump;
    private boolean running;
    private boolean fetchedCb;
    private int totalCycles;
    private int opcode;
    private int fetchedAt;
    private static final int CYCLES_PER_FRAME = 70224;
    private static final double NS_PER_CYCLE = 238.4;
    private static final double FRAME_TIME_MS = 1000.0 / 59.7275;
    private boolean runOnce = false;
    private long elapsedEmulatedTimeNs;
    private Map<String, Integer> map;

    public CPU(PPU ppu, Memory memory) throws IOException {
        this.ppu = ppu;
        this.memory = memory;
        this.memory.setPpu(this.ppu);
        registers = new Registers();
        interrupt = new Interrupt(this, this.memory);
        timer = new Timer(this.memory, interrupt);
        stack = new Stack(this);
        input = new Input(memory);
        opcodeLoader = new OpcodeLoader();
        opcodeWrapper = opcodeLoader.getOpcodeWrapper();
        //opcodeHandler = new OpcodeHandler(opcodeWrapper);
        opcodeBuilder = new OpcodeBuilder(this, opcodeWrapper);
        map = memory.getMemoryMap();
    }

    @Override
    public void run() {
        running = true;
        long lastDivUpdateCheck = 0;
        long lastTimaUpdateCheck = 0;
        long frameStartTime = System.nanoTime();
        int divCount = 0;
        boolean didPostBoot = false;
        state = State.FETCH;
        while (running) {
            step();
            try {
                sleep(50);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
            /*
            OpcodeInfo opcode = fetch();
            int pc = registers.getPC();

            if (pc == 0x100 && !didPostBoot) {
                postBootInit();
                didPostBoot = true;
            }


            int cycles = 0;
            if (!isHalt()) {

                if (isPendingInterruptSwitch()) {
                    setIME(true);
                    setPendingInterruptSwitch(false);
                }

                cycles = execute(opcode);

                elapsedEmulatedTimeNs += (long) (cycles * NS_PER_CYCLE);
                int isPrefix = opcode.isPrefixed() ? 1 : 0;
                if (!didJump) registers.setPC(pc + opcode.getBytes() + isPrefix);
                else didJump = false;
                //handle pending IME switch

            }


            //Update timers
            if (elapsedEmulatedTimeNs - lastDivUpdateCheck >= Timer.DIV_INC_TIME_NS) {
                timer.incDiv();
                lastDivUpdateCheck = elapsedEmulatedTimeNs;
            }


            if (timer.isTacEnabled()) {
                //System.out.println("tac");
                long timaTickRate = (long) (timer.getTacFreq() * NS_PER_CYCLE);
                if (elapsedEmulatedTimeNs - lastTimaUpdateCheck >= timaTickRate) {
                    timer.incTima();
                    lastTimaUpdateCheck = elapsedEmulatedTimeNs;
                }
            }


            //Handle interrupts
            if (isIME()) {
                int IE = memory.readByte(map.get("IE"));
                int IF = memory.readByte(map.get("IF"));
                //System.out.printf("\nIE: %02X\nIF: %02X\n", IE, IF);
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


            CPU cycle timing
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

            ppu.step(cycles);
            totalCycles += cycles;
        }
*/
        }
    }


    public void step() {
        switch (state) {
            case FETCH:
                if (!fetchedCb) fetchedAt = registers.getPC();
                fetch();
                if (opcode != 0xCB) {
                    state = State.DECODE_AND_EXECUTE;
                } else fetchedCb = true;
                break;
            case DECODE_AND_EXECUTE:
                if (!built) {
                    currentOpcodeObject = opcodeBuilder.build(fetchedAt, opcode, fetchedCb);
                    fetchedCb = false;
                    built = true;
                    //Skip over opcode and force PC to correct location if this isn't implemented yet
                    if (currentOpcodeObject.isUnimplError()) {
                        registers.setPC(fetchedAt + currentOpcodeObject.getOpcodeInfo().getBytes());
                        currentOpcodeObject.setOperationsRemaining(false);
                    }
                }
                if(registers.getPC() == 0x14){
                    System.out.printf("");
                }
                if (currentOpcodeObject.hasOperationsRemaining()) { //If this opcode still has work to do
                    boolean done = false;
                    while (!done) { //Make sure we continuously execute any operations that don't consume cycles
                        currentOpcodeObject.execute(this, memory);
                        MicroOperation nextOp = currentOpcodeObject.getMicroOps().peek();
                        if (nextOp == null) {
                            done = true;
                        } else if (nextOp.getCycles() != 0) {
                            done = true;
                        }
                    }
                } else {
                    System.out.println(currentOpcodeObject.toString(true));
                    built = false;
                    state = State.FETCH;
                }
                break;
        }
    }

    public void stopCPU() {
        running = false;
    }

    private void fetch() {
        new ReadImmediate8bit(this::setOpcode).execute(this, memory);
    }

//    public OpcodeInfo fetch() {
//        int opcode = memory.readByte(registers.getPC()) & 0xFF;
//        OpcodeInfo opcodeInfo;
//        String hexString;
//        if (opcode == 0xCB) {
//            //getRegisters().incrementPC();
//            opcode = memory.readByte(registers.getPC() + 1) & 0xFF;
//            hexString = String.format("0x%02X", opcode);
//            opcodeInfo = opcodeWrapper.getCbprefixed().get(hexString);
//        } else {
//            hexString = String.format("0x%02X", opcode);
//            opcodeInfo = opcodeWrapper.getUnprefixed().get(hexString);
//        }
//        return opcodeInfo;
//    }

//    public int execute(OpcodeInfo opcodeInfo) {
//        if (opcodeInfo == null) return 0;
//        else
//            return opcodeHandler.execute(this, opcodeInfo);
//    }

    private void postBootInit() {
        Registers reg = getRegisters();
        reg.setAF(0x01B0);
        reg.setBC(0x0013);
        reg.setDE(0x00D8);
        reg.setHL(0x014D);
        reg.setSP(0xFFFE);
    }

    public enum State {
        FETCH,
        DECODE_AND_EXECUTE
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
        //setIME(true); //GET RID OF THIS LATER
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

    public boolean isHalt() {
        return halt;
    }

    public void setHalt(boolean halt) {
        this.halt = halt;
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

    private void printDebugLog(OpcodeInfo opcode) {
        int pc = getRegisters().getPC();
        int rawOpcode = memory.readByte(pc) & 0xFF;
        Operand[] ops = opcode.getOperands();
        StringBuilder sb = new StringBuilder();
        int immOffset = 1; // For reading immediate bytes after this opcode

        // Print the opcode operands as usual
        for (int i = 0; i < ops.length; i++) {
            String name = ops[i].getName();
            switch (name) {
                case "n8", "d8", "a8", "e8" -> {
                    int val = memory.readByte(pc + immOffset) & 0xFF;
                    sb.append(String.format("0x%02X", val));
                    immOffset++;
                }
                case "n16", "d16", "a16" -> {
                    int low = memory.readByte(pc + immOffset) & 0xFF;
                    int high = memory.readByte(pc + immOffset + 1) & 0xFF;
                    int val = (high << 8) | low;
                    sb.append(String.format("0x%04X", val));
                    immOffset += 2;
                }
                default -> sb.append(name);
            }
            if (i < ops.length - 1) {
                sb.append(", ");
            }
        }

        // Build a small string to append for RET instructions.
        String extra = "";
        String mnemonic = opcode.getMnemonic().toUpperCase();

        // Check if the opcode is RET, RETI, or conditional RETs (RET Z, RET NZ, RET C, etc.)
        // If so, peek at the stack to see where we *would* return if the condition passes.
        if (mnemonic.startsWith("RET")) {
            int sp = getRegisters().getSP();
            // Peek at the 2 bytes on top of the stack:
            int low = memory.readByte(sp) & 0xFF;
            int high = memory.readByte(sp + 1) & 0xFF;
            int returnAddr = (high << 8) | low;
            extra = String.format(" -> returns to 0x%04X", returnAddr);
        }

        System.out.printf("PC=%04X OPCODE=%02X (%s) %s%s\n",
                pc, rawOpcode, opcode.getMnemonic(), sb.toString(), extra);
    }

    private void setOpcode(int opcode) {
        this.opcode = opcode;
    }


}
