import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mochaboy.*;

import java.io.File;
import java.io.IOException;

import static org.junit.jupiter.api.Assertions.*;

class DecOperationTest {

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
    // 8-bit DEC r8 Tests (A, B, C, D, E, H, L)
    // ---------------------------------------

    @Test
    void testDEC_A_simpleDecrement() {
        OpcodeInfo opcodeInfo = opcodeWrapper.getUnprefixed().get("0x3D"); // DEC A
        cpu.getRegisters().setA(0x10);
        opcodeHandler.execute(cpu, opcodeInfo);
        assertEquals(0x0F, cpu.getRegisters().getA());
    }

    @Test
    void testDEC_B_underflow() {
        OpcodeInfo opcodeInfo = opcodeWrapper.getUnprefixed().get("0x05"); // DEC B
        cpu.getRegisters().setB(0x00); // Decrement 0x00
        opcodeHandler.execute(cpu, opcodeInfo);
        assertEquals(0xFF, cpu.getRegisters().getB(), "0x00 - 1 = 0xFF (wraps around)");
    }

    @Test
    void testDEC_C_halfBorrow() {
        OpcodeInfo opcodeInfo = opcodeWrapper.getUnprefixed().get("0x0D"); // DEC C
        cpu.getRegisters().setC(0x10);
        opcodeHandler.execute(cpu, opcodeInfo);
        assertEquals(0x0F, cpu.getRegisters().getC(), "0x10 - 1 = 0x0F");
        assertTrue(cpu.getRegisters().isFlagSet(Registers.FLAG_HALF_CARRY), "Half-carry flag should be set when borrowing from bit 4");
    }

    @Test
    void testDEC_E_zeroFlag() {
        OpcodeInfo opcodeInfo = opcodeWrapper.getUnprefixed().get("0x1D"); // DEC E
        cpu.getRegisters().setE(0x01);
        opcodeHandler.execute(cpu, opcodeInfo);
        assertEquals(0x00, cpu.getRegisters().getE(), "0x01 - 1 = 0x00");
        assertTrue(cpu.getRegisters().isFlagSet(Registers.FLAG_ZERO), "Zero flag should be set when result is 0");
    }

    @Test
    void testDEC_L_subtractFlag() {
        OpcodeInfo opcodeInfo = opcodeWrapper.getUnprefixed().get("0x2D"); // DEC L
        cpu.getRegisters().setL(0xF0);
        opcodeHandler.execute(cpu, opcodeInfo);
        assertTrue(cpu.getRegisters().isFlagSet(Registers.FLAG_SUBTRACT), "Subtract flag should be set after DEC operation");
    }

    // ---------------------------------------
    // 8-bit DEC [HL] Test
    // ---------------------------------------

    @Test
    void testDEC_HL_asMemoryAddress() {
        OpcodeInfo opcodeInfo = opcodeWrapper.getUnprefixed().get("0x35"); // DEC [HL]
        cpu.getRegisters().setH(0x20);
        cpu.getRegisters().setL(0x10);
        cpu.getMemory().writeByte(cpu.getRegisters().getHL(), 0x10); // Value at [HL] is 0x10
        opcodeHandler.execute(cpu, opcodeInfo);
        assertEquals(0x0F, cpu.getMemory().readByte(cpu.getRegisters().getHL()), "Memory at [HL] should be decremented from 0x10 to 0x0F");
    }

    // ---------------------------------------
    // 16-bit DEC r16 Tests (HL, BC, DE)
    // ---------------------------------------

    @Test
    void testDEC_HL_as16BitRegister() {
        OpcodeInfo opcodeInfo = opcodeWrapper.getUnprefixed().get("0x2B"); // DEC HL
        cpu.getRegisters().setHL(0x8000);
        opcodeHandler.execute(cpu, opcodeInfo);
        assertEquals(0x7FFF, cpu.getRegisters().getHL(), "HL should be decremented from 0x8000 to 0x7FFF");
    }

    @Test
    void testDEC_BC_as16BitRegister() {
        OpcodeInfo opcodeInfo = opcodeWrapper.getUnprefixed().get("0x0B"); // DEC BC
        cpu.getRegisters().setBC(0x0001);
        opcodeHandler.execute(cpu, opcodeInfo);
        assertEquals(0x0000, cpu.getRegisters().getBC(), "BC should be decremented from 0x0001 to 0x0000");
    }

    @Test
    void testDEC_DE_as16BitRegister() {
        OpcodeInfo opcodeInfo = opcodeWrapper.getUnprefixed().get("0x1B"); // DEC DE
        cpu.getRegisters().setDE(0x1234);
        opcodeHandler.execute(cpu, opcodeInfo);
        assertEquals(0x1233, cpu.getRegisters().getDE(), "DE should be decremented from 0x1234 to 0x1233");
    }

    // ---------------------------------------
    // 16-bit DEC SP Test
    // ---------------------------------------

    @Test
    void testDEC_SP_as16BitRegister() {
        OpcodeInfo opcodeInfo = opcodeWrapper.getUnprefixed().get("0x3B"); // DEC SP
        cpu.getRegisters().setSP(0x1000);
        opcodeHandler.execute(cpu, opcodeInfo);
        assertEquals(0x0FFF, cpu.getRegisters().getSP(), "SP should be decremented from 0x1000 to 0x0FFF");
    }

    // ---------------------------------------
    // Edge Cases
    // ---------------------------------------

    @Test
    void testDEC_HL_memoryUnderflow() {
        OpcodeInfo opcodeInfo = opcodeWrapper.getUnprefixed().get("0x35"); // DEC [HL]
        cpu.getRegisters().setH(0x20);
        cpu.getRegisters().setL(0x10);
        cpu.getMemory().writeByte(cpu.getRegisters().getHL(), 0x00);
        opcodeHandler.execute(cpu, opcodeInfo);
        assertEquals(0xFF, cpu.getMemory().readByte(cpu.getRegisters().getHL()), "Memory at [HL] should wrap from 0x00 to 0xFF");
    }

    @Test
    void testDEC_HL_memoryHalfBorrow() {
        OpcodeInfo opcodeInfo = opcodeWrapper.getUnprefixed().get("0x35"); // DEC [HL]
        cpu.getRegisters().setH(0x20);
        cpu.getRegisters().setL(0x10);
        cpu.getMemory().writeByte(cpu.getRegisters().getHL(), 0x10);
        opcodeHandler.execute(cpu, opcodeInfo);
        assertTrue(cpu.getRegisters().isFlagSet(Registers.FLAG_HALF_CARRY), "Half-carry flag should be set for borrow from bit 4");
    }

    @Test
    void testDEC_HL_memoryZeroFlag() {
        OpcodeInfo opcodeInfo = opcodeWrapper.getUnprefixed().get("0x35"); // DEC [HL]
        cpu.getRegisters().setH(0x20);
        cpu.getRegisters().setL(0x10);
        cpu.getMemory().writeByte(cpu.getRegisters().getHL(), 0x01);
        opcodeHandler.execute(cpu, opcodeInfo);
        assertTrue(cpu.getRegisters().isFlagSet(Registers.FLAG_ZERO), "Zero flag should be set when [HL] decrements to 0");
    }
}
