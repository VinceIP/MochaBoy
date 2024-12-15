import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mochaboy.*;

import java.io.File;
import java.io.IOException;

import static org.junit.jupiter.api.Assertions.*;

class SwapOperationTest {

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

    // Test SWAP r8
    @Test
    void testSWAP_r8() {
        OpcodeInfo opcodeInfo = opcodeWrapper.getCbprefixed().get("0x30"); // Assume this opcode represents SWAP B

        cpu.getRegisters().setB(0b10110001);

        opcodeHandler.execute(cpu, opcodeInfo);

        assertEquals(0b00011011, cpu.getRegisters().getB()); // Swap of 0b10110001 gives 0b00011011
        assertFalse(cpu.getRegisters().isFlagSet(Registers.FLAG_CARRY)); // C is always cleared
        assertFalse(cpu.getRegisters().isFlagSet(Registers.FLAG_SUBTRACT)); // N is always cleared
        assertFalse(cpu.getRegisters().isFlagSet(Registers.FLAG_HALF_CARRY)); // H is always cleared
        assertFalse(cpu.getRegisters().isFlagSet(Registers.FLAG_ZERO)); // Result is non-zero
    }

    @Test
    void testSWAP_r8_zeroResult() {
        OpcodeInfo opcodeInfo = opcodeWrapper.getCbprefixed().get("0x30"); // Assume this opcode represents SWAP B

        cpu.getRegisters().setB(0b00000000);

        opcodeHandler.execute(cpu, opcodeInfo);

        assertEquals(0b00000000, cpu.getRegisters().getB()); // Swap of 0b00000000 gives 0b00000000
        assertTrue(cpu.getRegisters().isFlagSet(Registers.FLAG_ZERO)); // Z is set since result is zero
        assertFalse(cpu.getRegisters().isFlagSet(Registers.FLAG_CARRY)); // C is always cleared
        assertFalse(cpu.getRegisters().isFlagSet(Registers.FLAG_SUBTRACT)); // N is always cleared
        assertFalse(cpu.getRegisters().isFlagSet(Registers.FLAG_HALF_CARRY)); // H is always cleared
    }

    // Test SWAP [HL]
    @Test
    void testSWAP_HL() {
        OpcodeInfo opcodeInfo = opcodeWrapper.getCbprefixed().get("0x36"); // Assume this opcode represents SWAP [HL]

        cpu.getRegisters().setH(0x20);
        cpu.getRegisters().setL(0x00);
        cpu.getMemory().writeByte(cpu.getRegisters().getHL(), 0b11110000);

        opcodeHandler.execute(cpu, opcodeInfo);

        assertEquals(0b00001111, cpu.getMemory().readByte(cpu.getRegisters().getHL())); // Swap of 0b11110000 gives 0b00001111
        assertFalse(cpu.getRegisters().isFlagSet(Registers.FLAG_CARRY)); // C is always cleared
        assertFalse(cpu.getRegisters().isFlagSet(Registers.FLAG_SUBTRACT)); // N is always cleared
        assertFalse(cpu.getRegisters().isFlagSet(Registers.FLAG_HALF_CARRY)); // H is always cleared
        assertFalse(cpu.getRegisters().isFlagSet(Registers.FLAG_ZERO)); // Result is non-zero
    }

    @Test
    void testSWAP_HL_zeroResult() {
        OpcodeInfo opcodeInfo = opcodeWrapper.getCbprefixed().get("0x36"); // Assume this opcode represents SWAP [HL]

        cpu.getRegisters().setH(0x20);
        cpu.getRegisters().setL(0x00);
        cpu.getMemory().writeByte(cpu.getRegisters().getHL(), 0b00000000);

        opcodeHandler.execute(cpu, opcodeInfo);

        assertEquals(0b00000000, cpu.getMemory().readByte(cpu.getRegisters().getHL())); // Swap of 0b00000000 gives 0b00000000
        assertTrue(cpu.getRegisters().isFlagSet(Registers.FLAG_ZERO)); // Z is set since result is zero
        assertFalse(cpu.getRegisters().isFlagSet(Registers.FLAG_CARRY)); // C is always cleared
        assertFalse(cpu.getRegisters().isFlagSet(Registers.FLAG_SUBTRACT)); // N is always cleared
        assertFalse(cpu.getRegisters().isFlagSet(Registers.FLAG_HALF_CARRY)); // H is always cleared
    }
}
