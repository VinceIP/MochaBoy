import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mochaboy.*;

import java.io.File;
import java.io.IOException;

import static org.junit.jupiter.api.Assertions.*;

class SbcOperationTest {

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

    // 8-bit SBC A, r8 tests
    @Test
    void testSBC_A_r8_noBorrow() {
        OpcodeInfo opcodeInfo = opcodeWrapper.getUnprefixed().get("0x98"); // SBC A, B

        cpu.getRegisters().setA(0x30);
        cpu.getRegisters().setB(0x10);

        opcodeHandler.execute(cpu, opcodeInfo);

        assertEquals(0x20, cpu.getRegisters().getA());
        assertFalse(cpu.getRegisters().isFlagSet(Registers.FLAG_ZERO));
        assertFalse(cpu.getRegisters().isFlagSet(Registers.FLAG_CARRY));
        assertFalse(cpu.getRegisters().isFlagSet(Registers.FLAG_HALF_CARRY));
        assertTrue(cpu.getRegisters().isFlagSet(Registers.FLAG_SUBTRACT));
    }

    @Test
    void testSBC_A_r8_withBorrow() {
        OpcodeInfo opcodeInfo = opcodeWrapper.getUnprefixed().get("0x98"); // SBC A, B

        cpu.getRegisters().setA(0x10);
        cpu.getRegisters().setB(0x20);
        cpu.getRegisters().setFlag(Registers.FLAG_CARRY, true);

        opcodeHandler.execute(cpu, opcodeInfo);

        assertEquals(0xEF, cpu.getRegisters().getA()); // Two's complement subtraction wraps around
        assertFalse(cpu.getRegisters().isFlagSet(Registers.FLAG_ZERO));
        assertTrue(cpu.getRegisters().isFlagSet(Registers.FLAG_CARRY));
        assertTrue(cpu.getRegisters().isFlagSet(Registers.FLAG_HALF_CARRY));
        assertTrue(cpu.getRegisters().isFlagSet(Registers.FLAG_SUBTRACT));
    }

    @Test
    void testSBC_A_r8_resultZero() {
        OpcodeInfo opcodeInfo = opcodeWrapper.getUnprefixed().get("0x98"); // SBC A, B

        cpu.getRegisters().setA(0x10);
        cpu.getRegisters().setB(0x0F);
        cpu.getRegisters().setFlag(Registers.FLAG_CARRY, true);

        opcodeHandler.execute(cpu, opcodeInfo);

        assertEquals(0x00, cpu.getRegisters().getA());
        assertTrue(cpu.getRegisters().isFlagSet(Registers.FLAG_ZERO));
        assertFalse(cpu.getRegisters().isFlagSet(Registers.FLAG_CARRY));
        assertTrue(cpu.getRegisters().isFlagSet(Registers.FLAG_HALF_CARRY));
        assertTrue(cpu.getRegisters().isFlagSet(Registers.FLAG_SUBTRACT));
    }

    // SBC A, n8 tests
    @Test
    void testSBC_A_n8() {
        OpcodeInfo opcodeInfo = opcodeWrapper.getUnprefixed().get("0xDE"); // SBC A, n8

        cpu.getRegisters().setA(0x50);
        cpu.getRegisters().setPC(0x1000);
        cpu.getMemory().writeByte(0x1000, 0x20);

        opcodeHandler.execute(cpu, opcodeInfo);

        assertEquals(0x30, cpu.getRegisters().getA());
        assertEquals(0x1001, cpu.getRegisters().getPC());
        assertFalse(cpu.getRegisters().isFlagSet(Registers.FLAG_ZERO));
        assertFalse(cpu.getRegisters().isFlagSet(Registers.FLAG_CARRY));
        assertFalse(cpu.getRegisters().isFlagSet(Registers.FLAG_HALF_CARRY));
        assertTrue(cpu.getRegisters().isFlagSet(Registers.FLAG_SUBTRACT));
    }

    // SBC A, [HL] tests
    @Test
    void testSBC_A_HL() {
        OpcodeInfo opcodeInfo = opcodeWrapper.getUnprefixed().get("0x9E"); // SBC A, [HL]

        cpu.getRegisters().setA(0x40);
        cpu.getRegisters().setH(0x20);
        cpu.getRegisters().setL(0x00);
        cpu.getMemory().writeByte(cpu.getRegisters().getHL(), 0x10);

        opcodeHandler.execute(cpu, opcodeInfo);

        assertEquals(0x30, cpu.getRegisters().getA());
        assertFalse(cpu.getRegisters().isFlagSet(Registers.FLAG_ZERO));
        assertFalse(cpu.getRegisters().isFlagSet(Registers.FLAG_CARRY));
        assertFalse(cpu.getRegisters().isFlagSet(Registers.FLAG_HALF_CARRY));
        assertTrue(cpu.getRegisters().isFlagSet(Registers.FLAG_SUBTRACT));
    }

    @Test
    void testSBC_A_HL_withBorrow() {
        OpcodeInfo opcodeInfo = opcodeWrapper.getUnprefixed().get("0x9E"); // SBC A, [HL]

        cpu.getRegisters().setA(0x20);
        cpu.getRegisters().setH(0x20);
        cpu.getRegisters().setL(0x00);
        cpu.getMemory().writeByte(cpu.getRegisters().getHL(), 0x30);
        cpu.getRegisters().setFlag(Registers.FLAG_CARRY, true);

        opcodeHandler.execute(cpu, opcodeInfo);

        assertEquals(0xEF, cpu.getRegisters().getA()); // Two's complement wrap
        assertFalse(cpu.getRegisters().isFlagSet(Registers.FLAG_ZERO));
        assertTrue(cpu.getRegisters().isFlagSet(Registers.FLAG_CARRY));
        assertTrue(cpu.getRegisters().isFlagSet(Registers.FLAG_HALF_CARRY));
        assertTrue(cpu.getRegisters().isFlagSet(Registers.FLAG_SUBTRACT));
    }
}
