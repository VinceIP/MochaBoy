import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mochaboy.*;

import java.io.File;
import java.io.IOException;

import static org.junit.jupiter.api.Assertions.*;

class SrlOperationTest {

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

    // Test SRL r8
    @Test
    void testSRL_r8_noCarry() {
        OpcodeInfo opcodeInfo = opcodeWrapper.getCbprefixed().get("0x38"); // Assume this opcode represents SRL B

        cpu.getRegisters().setB(0b00101100);

        opcodeHandler.execute(cpu, opcodeInfo);

        assertEquals(0b00010110, cpu.getRegisters().getB()); // Right shift of 0b00101100 gives 0b00010110
        assertFalse(cpu.getRegisters().isFlagSet(Registers.FLAG_CARRY)); // No carry since LSB was 0
        assertFalse(cpu.getRegisters().isFlagSet(Registers.FLAG_ZERO)); // Result is non-zero
    }

    @Test
    void testSRL_r8_withCarry() {
        OpcodeInfo opcodeInfo = opcodeWrapper.getCbprefixed().get("0x3F"); // Assume this opcode represents SRL A

        cpu.getRegisters().setA(0b11111111);

        opcodeHandler.execute(cpu, opcodeInfo);

        assertEquals(0b01111111, cpu.getRegisters().getA()); // Right shift of 0b11111111 gives 0b01111111
        assertTrue(cpu.getRegisters().isFlagSet(Registers.FLAG_CARRY)); // Carry since LSB was 1
        assertFalse(cpu.getRegisters().isFlagSet(Registers.FLAG_ZERO)); // Result is non-zero
    }

    @Test
    void testSRL_r8_zeroResult() {
        OpcodeInfo opcodeInfo = opcodeWrapper.getCbprefixed().get("0x38"); // Assume this opcode represents SRL B

        cpu.getRegisters().setB(0b00000001);

        opcodeHandler.execute(cpu, opcodeInfo);

        assertEquals(0b00000000, cpu.getRegisters().getB()); // Right shift of 0b00000001 gives 0b00000000
        assertTrue(cpu.getRegisters().isFlagSet(Registers.FLAG_CARRY)); // Carry since LSB was 1
        assertTrue(cpu.getRegisters().isFlagSet(Registers.FLAG_ZERO)); // Result is zero
    }

    // Test SRL [HL]
    @Test
    void testSRL_HL_noCarry() {
        OpcodeInfo opcodeInfo = opcodeWrapper.getCbprefixed().get("0x3E"); // Assume this opcode represents SRL [HL]

        cpu.getRegisters().setH(0x20);
        cpu.getRegisters().setL(0x00);
        cpu.getMemory().writeByte(cpu.getRegisters().getHL(), 0b00101100);

        opcodeHandler.execute(cpu, opcodeInfo);

        assertEquals(0b00010110, cpu.getMemory().readByte(cpu.getRegisters().getHL())); // Right shift of 0b00101100 gives 0b00010110
        assertFalse(cpu.getRegisters().isFlagSet(Registers.FLAG_CARRY)); // No carry since LSB was 0
        assertFalse(cpu.getRegisters().isFlagSet(Registers.FLAG_ZERO)); // Result is non-zero
    }

    @Test
    void testSRL_HL_withCarry() {
        OpcodeInfo opcodeInfo = opcodeWrapper.getCbprefixed().get("0x3E"); // Assume this opcode represents SRL [HL]

        cpu.getRegisters().setH(0x20);
        cpu.getRegisters().setL(0x00);
        cpu.getMemory().writeByte(cpu.getRegisters().getHL(), 0b11111111);

        opcodeHandler.execute(cpu, opcodeInfo);

        assertEquals(0b01111111, cpu.getMemory().readByte(cpu.getRegisters().getHL())); // Right shift of 0b11111111 gives 0b01111111
        assertTrue(cpu.getRegisters().isFlagSet(Registers.FLAG_CARRY)); // Carry since LSB was 1
        assertFalse(cpu.getRegisters().isFlagSet(Registers.FLAG_ZERO)); // Result is non-zero
    }

    @Test
    void testSRL_HL_zeroResult() {
        OpcodeInfo opcodeInfo = opcodeWrapper.getCbprefixed().get("0x3E"); // Assume this opcode represents SRL [HL]

        cpu.getRegisters().setH(0x20);
        cpu.getRegisters().setL(0x00);
        cpu.getMemory().writeByte(cpu.getRegisters().getHL(), 0b00000001);

        opcodeHandler.execute(cpu, opcodeInfo);

        assertEquals(0b00000000, cpu.getMemory().readByte(cpu.getRegisters().getHL())); // Right shift of 0b00000001 gives 0b00000000
        assertTrue(cpu.getRegisters().isFlagSet(Registers.FLAG_CARRY)); // Carry since LSB was 1
        assertTrue(cpu.getRegisters().isFlagSet(Registers.FLAG_ZERO)); // Result is zero
    }
}
