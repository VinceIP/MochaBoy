import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mochaboy.*;

import java.io.File;
import java.io.IOException;

import static org.junit.jupiter.api.Assertions.*;

class AddOperationTest {

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

    // 8-bit ADD A, r8 tests
    @Test
    void testADD_A_r8_noCarry() {
        OpcodeInfo opcodeInfo = opcodeWrapper.getUnprefixed().get("0x80"); // ADD A, B

        cpu.getRegisters().setA(0x10);
        cpu.getRegisters().setB(0x20);

        opcodeHandler.execute(cpu, opcodeInfo);

        assertEquals(0x30, cpu.getRegisters().getA());
        assertFalse(cpu.getRegisters().isFlagSet(Registers.FLAG_ZERO));
        assertFalse(cpu.getRegisters().isFlagSet(Registers.FLAG_CARRY));
        assertFalse(cpu.getRegisters().isFlagSet(Registers.FLAG_HALF_CARRY));
        assertFalse(cpu.getRegisters().isFlagSet(Registers.FLAG_SUBTRACT));
    }

    @Test
    void testADD_A_r8_withCarry() {
        OpcodeInfo opcodeInfo = opcodeWrapper.getUnprefixed().get("0x80"); // ADD A, B

        cpu.getRegisters().setA(0xFF);
        cpu.getRegisters().setB(0x01);

        opcodeHandler.execute(cpu, opcodeInfo);

        assertEquals(0x00, cpu.getRegisters().getA());
        assertTrue(cpu.getRegisters().isFlagSet(Registers.FLAG_ZERO));
        assertTrue(cpu.getRegisters().isFlagSet(Registers.FLAG_CARRY));
        assertTrue(cpu.getRegisters().isFlagSet(Registers.FLAG_HALF_CARRY));
        assertFalse(cpu.getRegisters().isFlagSet(Registers.FLAG_SUBTRACT));
    }

    @Test
    void testADD_A_r8_halfCarry() {
        OpcodeInfo opcodeInfo = opcodeWrapper.getUnprefixed().get("0x80"); // ADD A, B

        cpu.getRegisters().setA(0x0F);
        cpu.getRegisters().setB(0x01);

        opcodeHandler.execute(cpu, opcodeInfo);

        assertEquals(0x10, cpu.getRegisters().getA());
        assertFalse(cpu.getRegisters().isFlagSet(Registers.FLAG_ZERO));
        assertFalse(cpu.getRegisters().isFlagSet(Registers.FLAG_CARRY));
        assertTrue(cpu.getRegisters().isFlagSet(Registers.FLAG_HALF_CARRY));
        assertFalse(cpu.getRegisters().isFlagSet(Registers.FLAG_SUBTRACT));
    }

    // ADD A, [HL] tests
    @Test
    void testADD_A_HL() {
        OpcodeInfo opcodeInfo = opcodeWrapper.getUnprefixed().get("0x86"); // ADD A, [HL]

        cpu.getRegisters().setA(0x10);
        cpu.getRegisters().setH(0x20);
        cpu.getRegisters().setL(0x00);
        cpu.getMemory().writeByte(cpu.getRegisters().getHL(), 0x20);

        opcodeHandler.execute(cpu, opcodeInfo);

        assertEquals(0x30, cpu.getRegisters().getA());
        assertFalse(cpu.getRegisters().isFlagSet(Registers.FLAG_ZERO));
        assertFalse(cpu.getRegisters().isFlagSet(Registers.FLAG_CARRY));
        assertFalse(cpu.getRegisters().isFlagSet(Registers.FLAG_HALF_CARRY));
        assertFalse(cpu.getRegisters().isFlagSet(Registers.FLAG_SUBTRACT));
    }

    // ADD A, n8 tests
    @Test
    void testADD_A_n8() {
        OpcodeInfo opcodeInfo = opcodeWrapper.getUnprefixed().get("0xC6"); // ADD A, n8

        cpu.getRegisters().setA(0x10);
        cpu.getRegisters().setPC(0x1000);
        cpu.getMemory().writeByte(0x1000, 0x20);

        opcodeHandler.execute(cpu, opcodeInfo);

        assertEquals(0x30, cpu.getRegisters().getA());
        assertEquals(0x1001, cpu.getRegisters().getPC());
        assertFalse(cpu.getRegisters().isFlagSet(Registers.FLAG_ZERO));
        assertFalse(cpu.getRegisters().isFlagSet(Registers.FLAG_CARRY));
        assertFalse(cpu.getRegisters().isFlagSet(Registers.FLAG_HALF_CARRY));
        assertFalse(cpu.getRegisters().isFlagSet(Registers.FLAG_SUBTRACT));
    }

    // 16-bit ADD HL, r16 tests
    @Test
    void testADD_HL_r16_noCarry() {
        OpcodeInfo opcodeInfo = opcodeWrapper.getUnprefixed().get("0x09"); // ADD HL, BC

        cpu.getRegisters().setHL(0x1000);
        cpu.getRegisters().setBC(0x0200);

        opcodeHandler.execute(cpu, opcodeInfo);

        assertEquals(0x1200, cpu.getRegisters().getHL());
        assertFalse(cpu.getRegisters().isFlagSet(Registers.FLAG_CARRY));
        assertFalse(cpu.getRegisters().isFlagSet(Registers.FLAG_HALF_CARRY));
        assertFalse(cpu.getRegisters().isFlagSet(Registers.FLAG_SUBTRACT));
    }

    @Test
    void testADD_HL_r16_withCarry() {
        OpcodeInfo opcodeInfo = opcodeWrapper.getUnprefixed().get("0x09"); // ADD HL, BC

        cpu.getRegisters().setHL(0xF000);
        cpu.getRegisters().setBC(0x2000);

        opcodeHandler.execute(cpu, opcodeInfo);

        assertEquals(0x1000, cpu.getRegisters().getHL());
        assertTrue(cpu.getRegisters().isFlagSet(Registers.FLAG_CARRY));
        assertFalse(cpu.getRegisters().isFlagSet(Registers.FLAG_HALF_CARRY));
        assertFalse(cpu.getRegisters().isFlagSet(Registers.FLAG_SUBTRACT));
    }

    // ADD SP, e8 tests
    @Test
    void testADD_SP_e8_positive() {
        OpcodeInfo opcodeInfo = opcodeWrapper.getUnprefixed().get("0xE8"); // ADD SP, e8

        cpu.getRegisters().setSP(0x1000);
        cpu.getRegisters().setPC(0x2000);
        cpu.getMemory().writeByte(0x2000, 0x02); // Adding 2

        opcodeHandler.execute(cpu, opcodeInfo);

        assertEquals(0x1002, cpu.getRegisters().getSP());
        assertEquals(0x2001, cpu.getRegisters().getPC());
        assertFalse(cpu.getRegisters().isFlagSet(Registers.FLAG_ZERO));
        assertFalse(cpu.getRegisters().isFlagSet(Registers.FLAG_CARRY));
        assertFalse(cpu.getRegisters().isFlagSet(Registers.FLAG_HALF_CARRY));
        assertFalse(cpu.getRegisters().isFlagSet(Registers.FLAG_SUBTRACT));
    }

    @Test
    void testADD_SP_e8_negative() {
        OpcodeInfo opcodeInfo = opcodeWrapper.getUnprefixed().get("0xE8"); // ADD SP, e8

        cpu.getRegisters().setSP(0x1000);
        cpu.getRegisters().setPC(0x2000);
        cpu.getMemory().writeByte(0x2000, 0xFE); // Adding -2 (two's complement)

        opcodeHandler.execute(cpu, opcodeInfo);

        assertEquals(0x0FFE, cpu.getRegisters().getSP());
        assertEquals(0x2001, cpu.getRegisters().getPC());
        assertFalse(cpu.getRegisters().isFlagSet(Registers.FLAG_ZERO));
        assertFalse(cpu.getRegisters().isFlagSet(Registers.FLAG_CARRY));
        assertFalse(cpu.getRegisters().isFlagSet(Registers.FLAG_HALF_CARRY));
        assertFalse(cpu.getRegisters().isFlagSet(Registers.FLAG_SUBTRACT));
    }
}