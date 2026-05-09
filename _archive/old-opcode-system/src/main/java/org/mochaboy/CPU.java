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
    private CPUState state;

    private static final int CYCLES_PER_FRAME = 70224;
    private static final double NS_PER_CYCLE = 238.42;
    private static final double FRAME_TIME_MS = 1000.0 / 59.7275;

    private long elapsedNs;
    private int totalCycles;
    private int divCycleAcc;
    private int timaCycleAcc;

    private long tStateCounter;
    private boolean built = false;
    private boolean IME;
    private boolean pendingImeEnable;
    private boolean halt;
    private boolean running;
    private boolean lowPowerMode;
    private boolean stopMode;

    private boolean didJump;
    private boolean fetchedCb;
    private int opcode;
    private int fetchedAt;
    private boolean runOnce = false;
    private Map<String, Integer> map;
    private RegSnap regBefore;

    private boolean testStepComplete;
    private boolean testMode;

    private boolean breaker = false;
    private int breakerAddress = 0x27A3;

    private int FF;
    private int FFold;

    private boolean debugMode = false;

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
        opcodeBuilder = new OpcodeBuilder(this, opcodeWrapper);
        map = memory.getMemoryMap();
    }

    @Override
    public void run() {

        running = true;
        long frameStart = System.nanoTime();
        state = CPUState.FETCH;

        while (running) {
            int cycles = halt ? 1 : step(); //Step if not in HALT

            for (int i = 0; i < cycles; i++) {
                tickTimers(1);
                if (IME) serviceInterrupts();
                ppu.step(1);
                totalCycles++;
                elapsedNs += (long) NS_PER_CYCLE;

                if (totalCycles >= CYCLES_PER_FRAME) {
                    long frameEnd = System.nanoTime();
                    double frameMs = (frameEnd - frameStart) / 1_000_000.0;
                    double sleepMs = FRAME_TIME_MS - frameMs;

                    if (sleepMs > 0) try {
                        Thread.sleep((long) sleepMs);
                    } catch (InterruptedException ignored) {
                    }
                    totalCycles -= CYCLES_PER_FRAME;
                    frameStart = System.nanoTime();
                }
            }
        }
    }

    private void tickTimers(int cycles) {
        //DIV - 256 cycles per increment
        divCycleAcc += cycles;
        while (divCycleAcc >= 256) {
            timer.incDiv();
            divCycleAcc -= 256;
        }

        //TIMA
        if (timer.isTacEnabled()) {
            int freq = timer.getTacRate();
            timaCycleAcc += cycles;
            while (timaCycleAcc >= freq) {
                boolean overflow = timer.incTima();
                if (overflow) {
                    interrupt.setInterrupt(Interrupt.INTERRUPT.TIMER);
                    timaCycleAcc = 0;
                    break;
                }
                timaCycleAcc -= freq;
            }
        } else {
            timaCycleAcc = 0;
        }
    }

    private void serviceInterrupts() {
        int IE = memory.readByte(map.get("IE"));
        int IF = memory.readByte(map.get("IF"));
        int pending = IE & IF;
        if (pending == 0) return;

        for (int i = 0; i < 5; i++) {
            if ((pending & (1 << i)) != 0) {
                interrupt.handleInterrupt(Interrupt.INTERRUPT.values()[i]);
                memory.writeByte(map.get("IF"), IF & ~(1 << i));
                break;
            }
        }
    }


    public int step() {
        switch (state) {
            case FETCH -> {
                testStepComplete = false;
                if (!fetchedCb) fetchedAt = registers.getPC();
                fetch();
                if (opcode != 0xCB || fetchedCb) {
                    state = CPUState.DECODE_AND_EXECUTE;
                } else {
                    fetchedCb = true;
                }
                return 1;
            }
            case DECODE_AND_EXECUTE -> {
                if (!built) {
                    if (testMode) {
                        currentOpcodeObject = opcodeBuilder.build(opcode, fetchedCb);
                    } else {
                        currentOpcodeObject = opcodeBuilder.build(fetchedAt, opcode, fetchedCb);
                    }

                    fetchedCb = false;
                    built = true;

                    //Skip over opcode and force PC to correct location if this isn't implemented yet
                    if (currentOpcodeObject.isUnimplError()) {
                        registers.setPC(fetchedAt + currentOpcodeObject.getOpcodeInfo().getBytes());
                        currentOpcodeObject.setOperationsRemaining(false);
                    }
                }

                //handle pending IME switch
                if (isPendingImeEnable()) {
                    setIME(true);
                    setPendingImeEnable(false);
                }

                if (currentOpcodeObject.hasOperationsRemaining()) {//If this opcode still has work to do
                    regBefore = new RegSnap(registers); //for debug

                    while (currentOpcodeObject.hasOperationsRemaining()) {
                        MicroOperation executed = currentOpcodeObject.execute(this, memory);
                        int opCycles = executed.getCycles();
                        if (opCycles > 0) return opCycles;
                    }
                } else {

                    if (debugMode) {
                        if (!memory.isBootRomEnabled()) {
                            System.out.printf("%s\n", currentOpcodeObject.toString());
                            System.out.printf("%04X - SP is at %04X\n", currentOpcodeObject.getFetchedAt(), registers.getSP());
                        }

                        if (currentOpcodeObject.getFetchedAt() == 0x2838) {
                            System.out.println();
                        }
                    }

                    built = false;
                    state = CPUState.FETCH;
                    testStepComplete = true;

                    int tima = timer.getTima();
                    int tac = timer.getTac();
                    int div = timer.getDiv();
                    String interrupts = interrupt.getInterruptsAsString();

                }
            }
        }
        return 0;
    }

    private void fetch() {
        new ReadImmediate8bit(this::setOpcode).execute(this, memory);
    }

    public void kill() {
        running = false;
    }

    private void printDebug() {
        RegSnap after = new RegSnap(registers);

        String mnemonic = formatMnemonic(currentOpcodeObject);
        String delta = regBefore.diff(after);

        //  memory write? -> Memory class remembers the last write (address,value)
        String memWrite = "";
        Memory.LastWrite lastWrite = memory.getLastWrite(); // returns immutable record or null
        if (lastWrite != null) {

            int address = lastWrite.getAddress();
            int value = lastWrite.getValue();
            int cycleMarker = lastWrite.getCycleMarker();
            if (cycleMarker == fetchedAt) { // wrote during THIS opcode
                memWrite = String.format(" [WR %04X=%02X]", address, value);
            }

            System.out.printf("%04X  0x%02X  %-12s %s%s\n",
                    fetchedAt,
                    memory.readByte(fetchedAt) & 0xFF,
                    mnemonic,
                    delta,
                    memWrite);
        }
    }

    private String formatMnemonic(Opcode op) {
        Operand[] ops = op.getOpcodeInfo().getOperands();
        if (ops.length == 0) return op.getOpcodeInfo().getMnemonic();

        StringBuilder sb = new StringBuilder(op.getOpcodeInfo().getMnemonic()).append(" ");
        int pc = fetchedAt + 1; // immediate bytes start after opcode byte (prefix already eaten)
        for (int i = 0; i < ops.length; i++) {
            Operand o = ops[i];
            if (!o.isImmediate()) {                       // register / condition / [HL]
                sb.append(o.getName());
            } else {
                // immediate: read from memory directly so we print *exact* encoded constant
                int val;
                if (o.getName().endsWith("8")) {
                    val = memory.readByte(pc) & 0xFF;
                    sb.append(String.format("0x%02X", val));
                    pc += 1;
                } else { // 16‑bit
                    int lo = memory.readByte(pc) & 0xFF;
                    int hi = memory.readByte(pc + 1) & 0xFF;
                    val = (hi << 8) | lo;
                    sb.append(String.format("0x%04X", val));
                    pc += 2;
                }
            }
            if (i != ops.length - 1) sb.append(", ");
        }
        return sb.toString();
    }

    public void stopCPU() {
        running = false;
    }


    public void postBootInit() {
        Registers reg = getRegisters();
        reg.setAF(0x01B0);
        reg.setBC(0x0013);
        reg.setDE(0x00D8);
        reg.setHL(0x014D);
        reg.setSP(0xFFFE);
    }

    public enum CPUState {
        FETCH,
        DECODE_AND_EXECUTE
    }

    public CPUState getCpuState() {
        return state;
    }

    public void setCpuState(CPUState cpuState) {
        this.state = cpuState;
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

    public boolean isPendingImeEnable() {
        return pendingImeEnable;
    }

    public void setPendingImeEnable(boolean pendingImeEnable) {
        this.pendingImeEnable = pendingImeEnable;
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

    public Opcode getCurrentOpcodeObject() {
        return currentOpcodeObject;
    }

    public boolean isTestStepComplete() {
        return testStepComplete;
    }

    public void setTestStepComplete(boolean testStepComplete) {
        this.testStepComplete = testStepComplete;
    }

    public boolean isTestMode() {
        return testMode;
    }

    public void setTestMode(boolean testMode) {
        this.testMode = testMode;
    }

    public boolean isBuilt() {
        return built;
    }

    public void setBuilt(boolean built) {
        this.built = built;
    }

    public void resetDivCycleAcc() {
        this.divCycleAcc = 0;
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


    private static final class RegSnap {
        final int A, B, C, D, E, H, L, F, SP, PC;

        RegSnap(Registers r) {
            A = r.getA();
            B = r.getB();
            C = r.getC();
            D = r.getD();
            E = r.getE();
            H = r.getH();
            L = r.getL();
            F = r.getF();
            SP = r.getSP();
            PC = r.getPC();
        }

        String diff(RegSnap n) {
            StringBuilder sb = new StringBuilder("{");
            add(sb, "A", A, n.A, 0xFF);
            add(sb, "F", F, n.F, 0xFF);
            add(sb, "B", B, n.B, 0xFF);
            add(sb, "C", C, n.C, 0xFF);
            add(sb, "D", D, n.D, 0xFF);
            add(sb, "E", E, n.E, 0xFF);
            add(sb, "H", H, n.H, 0xFF);
            add(sb, "L", L, n.L, 0xFF);
            add(sb, "SP", SP, n.SP, 0xFFFF);
            add(sb, "PC", PC, n.PC, 0xFFFF);
            if (sb.length() == 1) sb.append("no‑change");
            sb.append('}');
            return sb.toString();
        }

        private static void add(StringBuilder sb, String n, int a, int b, int mask) {
            if ((a & mask) != (b & mask))
                sb.append(String.format(" %s:%0" + (mask == 0xFF ? "2" : "4") + "X->%0" + (mask == 0xFF ? "2" : "4") + "X", n, a & mask, b & mask));
        }
    }
}

