import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mochaboy.*;

import java.io.File;
import java.io.IOException;

import static org.junit.jupiter.api.Assertions.*;

class SlaOperationTest {

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

    // Test SLA r8
    @Test
    void testSLA_r8_noCarry() {
        OpcodeInfo opcodeInfo = opcodeWrapper.getCbprefixed().get("0x20"); // Assume this opcode represents SLA B

        cpu.getRegisters().setB(0b00101100);

        opcodeHandler.execute(cpu, opcodeInfo);

        assertEquals(0b01011000, cpu.getRegisters().getB()); // Left shift of 0b00101100 gives 0b01011000
        assertFalse(cpu.getRegisters().isFlagSet(Registers.FLAG_CARRY)); // No carry since MSB was 0
        assertFalse(cpu.getRegisters().isFlagSet(Registers.FLAG_ZERO)); // Result is non-zero
    }

    @Test
    void testSLA_r8_withCarry() {
        OpcodeInfo opcodeInfo = opcodeWrapper.getCbprefixed().get("0x27"); // Assume this opcode represents SLA A

        cpu.getRegisters().setA(0b11111111);

        opcodeHandler.execute(cpu, opcodeInfo);

        assertEquals(0b11111110, cpu.getRegisters().getA()); // Left shift of 0b11111111 gives 0b11111110
        assertTrue(cpu.getRegisters().isFlagSet(Registers.FLAG_CARRY)); // Carry since MSB was 1
        assertFalse(cpu.getRegisters().isFlagSet(Registers.FLAG_ZERO)); // Result is non-zero
    }

    @Test
    void testSLA_r8_zeroResult() {
        OpcodeInfo opcodeInfo = opcodeWrapper.getCbprefixed().get("0x20"); // Assume this opcode represents SLA B

        cpu.getRegisters().setB(0b10000000);

        opcodeHandler.execute(cpu, opcodeInfo);

        assertEquals(0b00000000, cpu.getRegisters().getB()); // Left shift of 0b10000000 gives 0b00000000
        assertTrue(cpu.getRegisters().isFlagSet(Registers.FLAG_CARRY)); // Carry since MSB was 1
        assertTrue(cpu.getRegisters().isFlagSet(Registers.FLAG_ZERO)); // Result is zero
    }

    // Test SLA [HL]
    @Test
    void testSLA_HL_noCarry() {
        OpcodeInfo opcodeInfo = opcodeWrapper.getCbprefixed().get("0x26"); // Assume this opcode represents SLA [HL]

        cpu.getRegisters().setH(0x20);
        cpu.getRegisters().setL(0x00);
        cpu.getMemory().writeByte(cpu.getRegisters().getHL(), 0b00101100);

        opcodeHandler.execute(cpu, opcodeInfo);

        assertEquals(0b01011000, cpu.getMemory().readByte(cpu.getRegisters().getHL())); // Left shift of 0b00101100 gives 0b01011000
        assertFalse(cpu.getRegisters().isFlagSet(Registers.FLAG_CARRY)); // No carry since MSB was 0
        assertFalse(cpu.getRegisters().isFlagSet(Registers.FLAG_ZERO)); // Result is non-zero
    }

    @Test
    void testSLA_HL_withCarry() {
        OpcodeInfo opcodeInfo = opcodeWrapper.getCbprefixed().get("0x26"); // Assume this opcode represents SLA [HL]

        cpu.getRegisters().setH(0x20);
        cpu.getRegisters().setL(0x00);
        cpu.getMemory().writeByte(cpu.getRegisters().getHL(), 0b11111111);

        opcodeHandler.execute(cpu, opcodeInfo);

        assertEquals(0b11111110, cpu.getMemory().readByte(cpu.getRegisters().getHL())); // Left shift of 0b11111111 gives 0b11111110
        assertTrue(cpu.getRegisters().isFlagSet(Registers.FLAG_CARRY)); // Carry since MSB was 1
        assertFalse(cpu.getRegisters().isFlagSet(Registers.FLAG_ZERO)); // Result is non-zero
    }

    @Test
    void testSLA_HL_zeroResult() {
        OpcodeInfo opcodeInfo = opcodeWrapper.getCbprefixed().get("0x26"); // Assume this opcode represents SLA [HL]

        cpu.getRegisters().setH(0x20);
        cpu.getRegisters().setL(0x00);
        cpu.getMemory().writeByte(cpu.getRegisters().getHL(), 0b10000000);

        opcodeHandler.execute(cpu, opcodeInfo);

        assertEquals(0b00000000, cpu.getMemory().readByte(cpu.getRegisters().getHL())); // Left shift of 0b10000000 gives 0b00000000
        assertTrue(cpu.getRegisters().isFlagSet(Registers.FLAG_CARRY)); // Carry since MSB was 1
        assertTrue(cpu.getRegisters().isFlagSet(Registers.FLAG_ZERO)); // Result is zero
    }
}
