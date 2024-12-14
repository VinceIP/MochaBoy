import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mochaboy.*;

import java.io.File;
import java.io.IOException;

import static org.junit.jupiter.api.Assertions.*;

class OrOperationTest {

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

    // 8-bit OR A, r8 tests
    @Test
    void testOR_A_r8() {
        OpcodeInfo opcodeInfo = opcodeWrapper.getUnprefixed().get("0xB0"); // OR A, B

        cpu.getRegisters().setA(0x55);
        cpu.getRegisters().setB(0xAA);

        opcodeHandler.execute(cpu, opcodeInfo);

        assertEquals(0xFF, cpu.getRegisters().getA()); // 0x55 | 0xAA = 0xFF
        assertFalse(cpu.getRegisters().isFlagSet(Registers.FLAG_ZERO));
        assertFalse(cpu.getRegisters().isFlagSet(Registers.FLAG_CARRY));
        assertFalse(cpu.getRegisters().isFlagSet(Registers.FLAG_HALF_CARRY));
        assertFalse(cpu.getRegisters().isFlagSet(Registers.FLAG_SUBTRACT));
    }

    @Test
    void testOR_A_r8_zeroResult() {
        OpcodeInfo opcodeInfo = opcodeWrapper.getUnprefixed().get("0xB0"); // OR A, B

        cpu.getRegisters().setA(0x00);
        cpu.getRegisters().setB(0x00);

        opcodeHandler.execute(cpu, opcodeInfo);

        assertEquals(0x00, cpu.getRegisters().getA());
        assertTrue(cpu.getRegisters().isFlagSet(Registers.FLAG_ZERO)); // The result is 0, so the Z flag should be set
        assertFalse(cpu.getRegisters().isFlagSet(Registers.FLAG_CARRY));
        assertFalse(cpu.getRegisters().isFlagSet(Registers.FLAG_HALF_CARRY));
        assertFalse(cpu.getRegisters().isFlagSet(Registers.FLAG_SUBTRACT));
    }

    // OR A, [HL] tests
    @Test
    void testOR_A_HL() {
        OpcodeInfo opcodeInfo = opcodeWrapper.getUnprefixed().get("0xB6"); // OR A, [HL]

        cpu.getRegisters().setA(0xF0);
        cpu.getRegisters().setH(0x20);
        cpu.getRegisters().setL(0x00);
        cpu.getMemory().writeByte(cpu.getRegisters().getHL(), 0x0F);

        opcodeHandler.execute(cpu, opcodeInfo);

        assertEquals(0xFF, cpu.getRegisters().getA()); // 0xF0 | 0x0F = 0xFF
        assertFalse(cpu.getRegisters().isFlagSet(Registers.FLAG_ZERO));
        assertFalse(cpu.getRegisters().isFlagSet(Registers.FLAG_CARRY));
        assertFalse(cpu.getRegisters().isFlagSet(Registers.FLAG_HALF_CARRY));
        assertFalse(cpu.getRegisters().isFlagSet(Registers.FLAG_SUBTRACT));
    }

    @Test
    void testOR_A_HL_zeroResult() {
        OpcodeInfo opcodeInfo = opcodeWrapper.getUnprefixed().get("0xB6"); // OR A, [HL]

        cpu.getRegisters().setA(0x00);
        cpu.getRegisters().setH(0x20);
        cpu.getRegisters().setL(0x00);
        cpu.getMemory().writeByte(cpu.getRegisters().getHL(), 0x00);

        opcodeHandler.execute(cpu, opcodeInfo);

        assertEquals(0x00, cpu.getRegisters().getA());
        assertTrue(cpu.getRegisters().isFlagSet(Registers.FLAG_ZERO)); // The result is 0, so the Z flag should be set
        assertFalse(cpu.getRegisters().isFlagSet(Registers.FLAG_CARRY));
        assertFalse(cpu.getRegisters().isFlagSet(Registers.FLAG_HALF_CARRY));
        assertFalse(cpu.getRegisters().isFlagSet(Registers.FLAG_SUBTRACT));
    }

    // OR A, n8 tests
    @Test
    void testOR_A_n8() {
        OpcodeInfo opcodeInfo = opcodeWrapper.getUnprefixed().get("0xF6"); // OR A, n8

        cpu.getRegisters().setA(0x0F);
        cpu.getRegisters().setPC(0x1000);
        cpu.getMemory().writeByte(0x1000, 0xF0);

        opcodeHandler.execute(cpu, opcodeInfo);

        assertEquals(0xFF, cpu.getRegisters().getA()); // 0x0F | 0xF0 = 0xFF
        assertEquals(0x1001, cpu.getRegisters().getPC());
        assertFalse(cpu.getRegisters().isFlagSet(Registers.FLAG_ZERO));
        assertFalse(cpu.getRegisters().isFlagSet(Registers.FLAG_CARRY));
        assertFalse(cpu.getRegisters().isFlagSet(Registers.FLAG_HALF_CARRY));
        assertFalse(cpu.getRegisters().isFlagSet(Registers.FLAG_SUBTRACT));
    }

    @Test
    void testOR_A_n8_zeroResult() {
        OpcodeInfo opcodeInfo = opcodeWrapper.getUnprefixed().get("0xF6"); // OR A, n8

        cpu.getRegisters().setA(0x00);
        cpu.getRegisters().setPC(0x1000);
        cpu.getMemory().writeByte(0x1000, 0x00);

        opcodeHandler.execute(cpu, opcodeInfo);

        assertEquals(0x00, cpu.getRegisters().getA());
        assertEquals(0x1001, cpu.getRegisters().getPC());
        assertTrue(cpu.getRegisters().isFlagSet(Registers.FLAG_ZERO)); // The result is 0, so the Z flag should be set
        assertFalse(cpu.getRegisters().isFlagSet(Registers.FLAG_CARRY));
        assertFalse(cpu.getRegisters().isFlagSet(Registers.FLAG_HALF_CARRY));
        assertFalse(cpu.getRegisters().isFlagSet(Registers.FLAG_SUBTRACT));
    }
}
