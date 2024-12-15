import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mochaboy.*;

import java.io.File;
import java.io.IOException;

import static org.junit.jupiter.api.Assertions.*;

class XorOperationTest {

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

    // Test XOR r8
    @Test
    void testXOR_r8_noZero() {
        OpcodeInfo opcodeInfo = opcodeWrapper.getUnprefixed().get("0xA8"); // Assume this opcode represents XOR B

        cpu.getRegisters().setA(0b10101010);
        cpu.getRegisters().setB(0b11001100);

        opcodeHandler.execute(cpu, opcodeInfo);

        assertEquals(0b01100110, cpu.getRegisters().getA()); // 0b10101010 XOR 0b11001100 = 0b01100110
        assertFalse(cpu.getRegisters().isFlagSet(Registers.FLAG_ZERO)); // Result is non-zero
        assertFalse(cpu.getRegisters().isFlagSet(Registers.FLAG_CARRY)); // C is always cleared for XOR
        assertFalse(cpu.getRegisters().isFlagSet(Registers.FLAG_SUBTRACT)); // N is always cleared for XOR
        assertFalse(cpu.getRegisters().isFlagSet(Registers.FLAG_HALF_CARRY)); // H is always cleared for XOR
    }

    @Test
    void testXOR_r8_zeroResult() {
        OpcodeInfo opcodeInfo = opcodeWrapper.getUnprefixed().get("0xA8"); // Assume this opcode represents XOR B

        cpu.getRegisters().setA(0b10101010);
        cpu.getRegisters().setB(0b10101010);

        opcodeHandler.execute(cpu, opcodeInfo);

        assertEquals(0b00000000, cpu.getRegisters().getA()); // 0b10101010 XOR 0b10101010 = 0b00000000
        assertTrue(cpu.getRegisters().isFlagSet(Registers.FLAG_ZERO)); // Z is set since result is zero
        assertFalse(cpu.getRegisters().isFlagSet(Registers.FLAG_CARRY)); // C is always cleared for XOR
        assertFalse(cpu.getRegisters().isFlagSet(Registers.FLAG_SUBTRACT)); // N is always cleared for XOR
        assertFalse(cpu.getRegisters().isFlagSet(Registers.FLAG_HALF_CARRY)); // H is always cleared for XOR
    }

    // Test XOR [HL]
    @Test
    void testXOR_HL_noZero() {
        OpcodeInfo opcodeInfo = opcodeWrapper.getUnprefixed().get("0xAE"); // Assume this opcode represents XOR [HL]

        cpu.getRegisters().setA(0b10101010);
        cpu.getRegisters().setH(0x20);
        cpu.getRegisters().setL(0x00);
        cpu.getMemory().writeByte(cpu.getRegisters().getHL(), 0b11001100);

        opcodeHandler.execute(cpu, opcodeInfo);

        assertEquals(0b01100110, cpu.getRegisters().getA()); // 0b10101010 XOR 0b11001100 = 0b01100110
        assertFalse(cpu.getRegisters().isFlagSet(Registers.FLAG_ZERO)); // Result is non-zero
        assertFalse(cpu.getRegisters().isFlagSet(Registers.FLAG_CARRY)); // C is always cleared for XOR
        assertFalse(cpu.getRegisters().isFlagSet(Registers.FLAG_SUBTRACT)); // N is always cleared for XOR
        assertFalse(cpu.getRegisters().isFlagSet(Registers.FLAG_HALF_CARRY)); // H is always cleared for XOR
    }

    @Test
    void testXOR_HL_zeroResult() {
        OpcodeInfo opcodeInfo = opcodeWrapper.getUnprefixed().get("0xAE"); // Assume this opcode represents XOR [HL]

        cpu.getRegisters().setA(0b10101010);
        cpu.getRegisters().setH(0x20);
        cpu.getRegisters().setL(0x00);
        cpu.getMemory().writeByte(cpu.getRegisters().getHL(), 0b10101010);

        opcodeHandler.execute(cpu, opcodeInfo);

        assertEquals(0b00000000, cpu.getRegisters().getA()); // 0b10101010 XOR 0b10101010 = 0b00000000
        assertTrue(cpu.getRegisters().isFlagSet(Registers.FLAG_ZERO)); // Z is set since result is zero
        assertFalse(cpu.getRegisters().isFlagSet(Registers.FLAG_CARRY)); // C is always cleared for XOR
        assertFalse(cpu.getRegisters().isFlagSet(Registers.FLAG_SUBTRACT)); // N is always cleared for XOR
        assertFalse(cpu.getRegisters().isFlagSet(Registers.FLAG_HALF_CARRY)); // H is always cleared for XOR
    }
}
