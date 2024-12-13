import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mochaboy.*;

import java.io.File;
import java.io.IOException;

import static org.junit.jupiter.api.Assertions.*;

class IncOperationTest {

    private Cartridge cartridge;
    private Memory memory;
    private CPU cpu;
    private OpcodeLoader opcodeLoader;
    private OpcodeHandler opcodeHandler;
    private OpcodeWrapper opcodeWrapper;

    @BeforeEach
    void setUp() throws IOException {
        cartridge = new Cartridge(new File("./././Tetris.gb").toPath());
        memory = new Memory(cartridge);
        cpu = new CPU(memory);
        opcodeLoader = new OpcodeLoader();
        opcodeWrapper = opcodeLoader.getOpcodeWrapper();
        opcodeHandler = new OpcodeHandler(opcodeWrapper);
    }

    // ---------------------------------------
    // 8-bit INC r8 Tests (A, B, C, D, E, H, L)
    // ---------------------------------------

    @Test
    void testINC_A_simpleIncrement() {
        OpcodeInfo opcodeInfo = opcodeWrapper.getUnprefixed().get("0x3C"); // INC A
        cpu.getRegisters().setA(0x10);
        opcodeHandler.execute(cpu, opcodeInfo);
        assertEquals(0x11, cpu.getRegisters().getA());
    }

    @Test
    void testINC_B_overflow() {
        OpcodeInfo opcodeInfo = opcodeWrapper.getUnprefixed().get("0x04"); // INC B
        cpu.getRegisters().setB(0xFF);
        opcodeHandler.execute(cpu, opcodeInfo);
        assertEquals(0x00, cpu.getRegisters().getB(), "0xFF + 1 = 0x00 (wraps around)");
    }

    @Test
    void testINC_C_halfCarry() {
        OpcodeInfo opcodeInfo = opcodeWrapper.getUnprefixed().get("0x0C"); // INC C
        cpu.getRegisters().setC(0x0F);
        opcodeHandler.execute(cpu, opcodeInfo);
        assertEquals(0x10, cpu.getRegisters().getC(), "0x0F + 1 = 0x10");
        assertTrue(cpu.getRegisters().isFlagSet(Registers.FLAG_HALF_CARRY), "Half-carry flag should be set for carry from bit 3 to 4");
    }

    @Test
    void testINC_D_zeroFlag() {
        OpcodeInfo opcodeInfo = opcodeWrapper.getUnprefixed().get("0x14"); // INC D
        cpu.getRegisters().setD(0xFF);
        opcodeHandler.execute(cpu, opcodeInfo);
        assertEquals(0x00, cpu.getRegisters().getD(), "0xFF + 1 = 0x00");
        assertTrue(cpu.getRegisters().isFlagSet(Registers.FLAG_ZERO), "Zero flag should be set when result is 0");
    }

    @Test
    void testINC_H_noSubtractFlag() {
        OpcodeInfo opcodeInfo = opcodeWrapper.getUnprefixed().get("0x24"); // INC H
        cpu.getRegisters().setH(0xF0);
        opcodeHandler.execute(cpu, opcodeInfo);
        assertFalse(cpu.getRegisters().isFlagSet(Registers.FLAG_SUBTRACT), "Subtract flag should be cleared after INC operation");
    }

    // ---------------------------------------
    // 8-bit INC [HL] Test
    // ---------------------------------------

    @Test
    void testINC_HL_asMemoryAddress() {
        OpcodeInfo opcodeInfo = opcodeWrapper.getUnprefixed().get("0x34"); // INC [HL]
        cpu.getRegisters().setH(0x20);
        cpu.getRegisters().setL(0x10);
        cpu.getMemory().writeByte(cpu.getRegisters().getHL(), 0x10);
        opcodeHandler.execute(cpu, opcodeInfo);
        assertEquals(0x11, cpu.getMemory().readByte(cpu.getRegisters().getHL()), "Memory at [HL] should be incremented from 0x10 to 0x11");
    }

    // ---------------------------------------
    // 16-bit INC r16 Tests (HL, BC, DE)
    // ---------------------------------------

    @Test
    void testINC_HL_as16BitRegister() {
        OpcodeInfo opcodeInfo = opcodeWrapper.getUnprefixed().get("0x23"); // INC HL
        cpu.getRegisters().setHL(0x7FFF);
        opcodeHandler.execute(cpu, opcodeInfo);
        assertEquals(0x8000, cpu.getRegisters().getHL(), "HL should be incremented from 0x7FFF to 0x8000");
    }

    @Test
    void testINC_BC_as16BitRegister() {
        OpcodeInfo opcodeInfo = opcodeWrapper.getUnprefixed().get("0x03"); // INC BC
        cpu.getRegisters().setBC(0x0000);
        opcodeHandler.execute(cpu, opcodeInfo);
        assertEquals(0x0001, cpu.getRegisters().getBC(), "BC should be incremented from 0x0000 to 0x0001");
    }

    @Test
    void testINC_DE_as16BitRegister() {
        OpcodeInfo opcodeInfo = opcodeWrapper.getUnprefixed().get("0x13"); // INC DE
        cpu.getRegisters().setDE(0x1234);
        opcodeHandler.execute(cpu, opcodeInfo);
        assertEquals(0x1235, cpu.getRegisters().getDE(), "DE should be incremented from 0x1234 to 0x1235");
    }

    // ---------------------------------------
    // 16-bit INC SP Test
    // ---------------------------------------

    @Test
    void testINC_SP_as16BitRegister() {
        OpcodeInfo opcodeInfo = opcodeWrapper.getUnprefixed().get("0x33"); // INC SP
        cpu.getRegisters().setSP(0x1000);
        opcodeHandler.execute(cpu, opcodeInfo);
        assertEquals(0x1001, cpu.getRegisters().getSP(), "SP should be incremented from 0x1000 to 0x1001");
    }

    // ---------------------------------------
    // Edge Cases
    // ---------------------------------------

    @Test
    void testINC_HL_memoryOverflow() {
        OpcodeInfo opcodeInfo = opcodeWrapper.getUnprefixed().get("0x34"); // INC [HL]
        cpu.getRegisters().setH(0x20);
        cpu.getRegisters().setL(0x10);
        cpu.getMemory().writeByte(cpu.getRegisters().getHL(), 0xFF);
        opcodeHandler.execute(cpu, opcodeInfo);
        assertEquals(0x00, cpu.getMemory().readByte(cpu.getRegisters().getHL()), "Memory at [HL] should wrap from 0xFF to 0x00");
    }

    @Test
    void testINC_HL_memoryHalfCarry() {
        OpcodeInfo opcodeInfo = opcodeWrapper.getUnprefixed().get("0x34"); // INC [HL]
        cpu.getRegisters().setH(0x20);
        cpu.getRegisters().setL(0x10);
        cpu.getMemory().writeByte(cpu.getRegisters().getHL(), 0x0F);
        opcodeHandler.execute(cpu, opcodeInfo);
        assertTrue(cpu.getRegisters().isFlagSet(Registers.FLAG_HALF_CARRY), "Half-carry flag should be set for carry from bit 3 to 4");
    }

    @Test
    void testINC_HL_memoryZeroFlag() {
        OpcodeInfo opcodeInfo = opcodeWrapper.getUnprefixed().get("0x34"); // INC [HL]
        cpu.getRegisters().setH(0x20);
        cpu.getRegisters().setL(0x10);
        cpu.getMemory().writeByte(cpu.getRegisters().getHL(), 0xFF);
        opcodeHandler.execute(cpu, opcodeInfo);
        assertTrue(cpu.getRegisters().isFlagSet(Registers.FLAG_ZERO), "Zero flag should be set when [HL] increments to 0x00");
    }
}
