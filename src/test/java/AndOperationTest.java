import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mochaboy.*;

import java.io.File;
import java.io.IOException;

import static org.junit.jupiter.api.Assertions.*;

class AndOperationTest {

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

    // AND A, r8 tests
    @Test
    void testAND_A_r8_simple() {
        OpcodeInfo opcodeInfo = opcodeWrapper.getUnprefixed().get("0xA0"); // AND A, B

        cpu.getRegisters().setA(0xF0);
        cpu.getRegisters().setB(0x0F);

        opcodeHandler.execute(cpu, opcodeInfo);

        assertEquals(0x00, cpu.getRegisters().getA()); // 0xF0 & 0x0F = 0x00
        assertTrue(cpu.getRegisters().isFlagSet(Registers.FLAG_ZERO)); // Result is zero, so Z flag is set
        assertFalse(cpu.getRegisters().isFlagSet(Registers.FLAG_CARRY)); // Carry is always cleared for AND
        assertTrue(cpu.getRegisters().isFlagSet(Registers.FLAG_HALF_CARRY)); // H flag is always set for AND
        assertFalse(cpu.getRegisters().isFlagSet(Registers.FLAG_SUBTRACT)); // N flag is always cleared for AND
    }

    @Test
    void testAND_A_r8_noZero() {
        OpcodeInfo opcodeInfo = opcodeWrapper.getUnprefixed().get("0xA0"); // AND A, B

        cpu.getRegisters().setA(0xF0);
        cpu.getRegisters().setB(0xF0);

        opcodeHandler.execute(cpu, opcodeInfo);

        assertEquals(0xF0, cpu.getRegisters().getA()); // 0xF0 & 0xF0 = 0xF0
        assertFalse(cpu.getRegisters().isFlagSet(Registers.FLAG_ZERO)); // Result is not zero, so Z flag is cleared
        assertFalse(cpu.getRegisters().isFlagSet(Registers.FLAG_CARRY)); // Carry is always cleared for AND
        assertTrue(cpu.getRegisters().isFlagSet(Registers.FLAG_HALF_CARRY)); // H flag is always set for AND
        assertFalse(cpu.getRegisters().isFlagSet(Registers.FLAG_SUBTRACT)); // N flag is always cleared for AND
    }

    @Test
    void testAND_A_r8_withZeroResult() {
        OpcodeInfo opcodeInfo = opcodeWrapper.getUnprefixed().get("0xA0"); // AND A, B

        cpu.getRegisters().setA(0x00);
        cpu.getRegisters().setB(0xFF);

        opcodeHandler.execute(cpu, opcodeInfo);

        assertEquals(0x00, cpu.getRegisters().getA()); // 0x00 & 0xFF = 0x00
        assertTrue(cpu.getRegisters().isFlagSet(Registers.FLAG_ZERO)); // Result is zero, so Z flag is set
        assertFalse(cpu.getRegisters().isFlagSet(Registers.FLAG_CARRY)); // Carry is always cleared for AND
        assertTrue(cpu.getRegisters().isFlagSet(Registers.FLAG_HALF_CARRY)); // H flag is always set for AND
        assertFalse(cpu.getRegisters().isFlagSet(Registers.FLAG_SUBTRACT)); // N flag is always cleared for AND
    }

    // AND A, [HL] tests
    @Test
    void testAND_A_HL() {
        OpcodeInfo opcodeInfo = opcodeWrapper.getUnprefixed().get("0xA6"); // AND A, [HL]

        cpu.getRegisters().setA(0xF0);
        cpu.getRegisters().setH(0x20);
        cpu.getRegisters().setL(0x00);
        cpu.getMemory().writeByte(cpu.getRegisters().getHL(), 0x0F);

        opcodeHandler.execute(cpu, opcodeInfo);

        assertEquals(0x00, cpu.getRegisters().getA()); // 0xF0 & 0x0F = 0x00
        assertTrue(cpu.getRegisters().isFlagSet(Registers.FLAG_ZERO)); // Result is zero, so Z flag is set
        assertFalse(cpu.getRegisters().isFlagSet(Registers.FLAG_CARRY)); // Carry is always cleared for AND
        assertTrue(cpu.getRegisters().isFlagSet(Registers.FLAG_HALF_CARRY)); // H flag is always set for AND
        assertFalse(cpu.getRegisters().isFlagSet(Registers.FLAG_SUBTRACT)); // N flag is always cleared for AND
    }

    // AND A, n8 tests
    @Test
    void testAND_A_n8() {
        OpcodeInfo opcodeInfo = opcodeWrapper.getUnprefixed().get("0xE6"); // AND A, n8

        cpu.getRegisters().setA(0xF0);
        cpu.getRegisters().setPC(0x1000);
        cpu.getMemory().writeByte(0x1000, 0x0F);

        opcodeHandler.execute(cpu, opcodeInfo);

        assertEquals(0x00, cpu.getRegisters().getA()); // 0xF0 & 0x0F = 0x00
        assertEquals(0x1001, cpu.getRegisters().getPC()); // PC should increment by 1
        assertTrue(cpu.getRegisters().isFlagSet(Registers.FLAG_ZERO)); // Result is zero, so Z flag is set
        assertFalse(cpu.getRegisters().isFlagSet(Registers.FLAG_CARRY)); // Carry is always cleared for AND
        assertTrue(cpu.getRegisters().isFlagSet(Registers.FLAG_HALF_CARRY)); // H flag is always set for AND
        assertFalse(cpu.getRegisters().isFlagSet(Registers.FLAG_SUBTRACT)); // N flag is always cleared for AND
    }

    @Test
    void testAND_A_n8_noZero() {
        OpcodeInfo opcodeInfo = opcodeWrapper.getUnprefixed().get("0xE6"); // AND A, n8

        cpu.getRegisters().setA(0xFF);
        cpu.getRegisters().setPC(0x1000);
        cpu.getMemory().writeByte(0x1000, 0xF0);

        opcodeHandler.execute(cpu, opcodeInfo);

        assertEquals(0xF0, cpu.getRegisters().getA()); // 0xFF & 0xF0 = 0xF0
        assertEquals(0x1001, cpu.getRegisters().getPC()); // PC should increment by 1
        assertFalse(cpu.getRegisters().isFlagSet(Registers.FLAG_ZERO)); // Result is not zero, so Z flag is cleared
        assertFalse(cpu.getRegisters().isFlagSet(Registers.FLAG_CARRY)); // Carry is always cleared for AND
        assertTrue(cpu.getRegisters().isFlagSet(Registers.FLAG_HALF_CARRY)); // H flag is always set for AND
        assertFalse(cpu.getRegisters().isFlagSet(Registers.FLAG_SUBTRACT)); // N flag is always cleared for AND
    }
}
