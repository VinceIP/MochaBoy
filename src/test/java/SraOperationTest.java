import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mochaboy.*;

import java.io.File;
import java.io.IOException;

import static org.junit.jupiter.api.Assertions.*;

class SraOperationTest {

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

    // Test SRA r8
    @Test
    void testSRA_r8_noCarry() {
        OpcodeInfo opcodeInfo = opcodeWrapper.getCbprefixed().get("0x28"); // Assume this opcode represents SRA B

        cpu.getRegisters().setB(0b00101100);

        opcodeHandler.execute(cpu, opcodeInfo);

        assertEquals(0b00010110, cpu.getRegisters().getB()); // Right shift of 0b00101100 gives 0b00010110
        assertFalse(cpu.getRegisters().isFlagSet(Registers.FLAG_CARRY)); // No carry since LSB was 0
        assertFalse(cpu.getRegisters().isFlagSet(Registers.FLAG_ZERO)); // Result is non-zero
    }

    @Test
    void testSRA_r8_withCarry() {
        OpcodeInfo opcodeInfo = opcodeWrapper.getCbprefixed().get("0x2F"); // Assume this opcode represents SRA A

        cpu.getRegisters().setA(0b11111111);

        opcodeHandler.execute(cpu, opcodeInfo);

        assertEquals(0b11111111, cpu.getRegisters().getA()); // Right shift of 0b11111111 preserves MSB (1) and gives 0b11111111
        assertTrue(cpu.getRegisters().isFlagSet(Registers.FLAG_CARRY)); // Carry since LSB was 1
        assertFalse(cpu.getRegisters().isFlagSet(Registers.FLAG_ZERO)); // Result is non-zero
    }

    @Test
    void testSRA_r8_zeroResult() {
        OpcodeInfo opcodeInfo = opcodeWrapper.getCbprefixed().get("0x28"); // Assume this opcode represents SRA B

        cpu.getRegisters().setB(0b00000001);

        opcodeHandler.execute(cpu, opcodeInfo);

        assertEquals(0b00000000, cpu.getRegisters().getB()); // Right shift of 0b00000001 gives 0b00000000
        assertTrue(cpu.getRegisters().isFlagSet(Registers.FLAG_CARRY)); // Carry since LSB was 1
        assertTrue(cpu.getRegisters().isFlagSet(Registers.FLAG_ZERO)); // Result is zero
    }

    // Test SRA [HL]
    @Test
    void testSRA_HL_noCarry() {
        OpcodeInfo opcodeInfo = opcodeWrapper.getCbprefixed().get("0x2E"); // Assume this opcode represents SRA [HL]

        cpu.getRegisters().setH(0x20);
        cpu.getRegisters().setL(0x00);
        cpu.getMemory().writeByte(cpu.getRegisters().getHL(), 0b00101100);

        opcodeHandler.execute(cpu, opcodeInfo);

        assertEquals(0b00010110, cpu.getMemory().readByte(cpu.getRegisters().getHL())); // Right shift of 0b00101100 gives 0b00010110
        assertFalse(cpu.getRegisters().isFlagSet(Registers.FLAG_CARRY)); // No carry since LSB was 0
        assertFalse(cpu.getRegisters().isFlagSet(Registers.FLAG_ZERO)); // Result is non-zero
    }

    @Test
    void testSRA_HL_withCarry() {
        OpcodeInfo opcodeInfo = opcodeWrapper.getCbprefixed().get("0x2E"); // Assume this opcode represents SRA [HL]

        cpu.getRegisters().setH(0x20);
        cpu.getRegisters().setL(0x00);
        cpu.getMemory().writeByte(cpu.getRegisters().getHL(), 0b11111111);

        opcodeHandler.execute(cpu, opcodeInfo);

        assertEquals(0b11111111, cpu.getMemory().readByte(cpu.getRegisters().getHL())); // Right shift of 0b11111111 preserves MSB (1) and gives 0b11111111
        assertTrue(cpu.getRegisters().isFlagSet(Registers.FLAG_CARRY)); // Carry since LSB was 1
        assertFalse(cpu.getRegisters().isFlagSet(Registers.FLAG_ZERO)); // Result is non-zero
    }

    @Test
    void testSRA_HL_zeroResult() {
        OpcodeInfo opcodeInfo = opcodeWrapper.getCbprefixed().get("0x2E"); // Assume this opcode represents SRA [HL]

        cpu.getRegisters().setH(0x20);
        cpu.getRegisters().setL(0x00);
        cpu.getMemory().writeByte(cpu.getRegisters().getHL(), 0b00000001);

        opcodeHandler.execute(cpu, opcodeInfo);

        assertEquals(0b00000000, cpu.getMemory().readByte(cpu.getRegisters().getHL())); // Right shift of 0b00000001 gives 0b00000000
        assertTrue(cpu.getRegisters().isFlagSet(Registers.FLAG_CARRY)); // Carry since LSB was 1
        assertTrue(cpu.getRegisters().isFlagSet(Registers.FLAG_ZERO)); // Result is zero
    }
}
