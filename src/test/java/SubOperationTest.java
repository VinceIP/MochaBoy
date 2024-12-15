import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mochaboy.*;

import java.io.File;
import java.io.IOException;

import static org.junit.jupiter.api.Assertions.*;

class SubOperationTest {

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

    // Test SUB r8
    @Test
    void testSUB_r8_noCarry() {
        OpcodeInfo opcodeInfo = opcodeWrapper.getUnprefixed().get("0x90"); // Assume this opcode represents SUB B

        cpu.getRegisters().setA(0x50);
        cpu.getRegisters().setB(0x20);

        opcodeHandler.execute(cpu, opcodeInfo);

        assertEquals(0x30, cpu.getRegisters().getA()); // 0x50 - 0x20 = 0x30
        assertFalse(cpu.getRegisters().isFlagSet(Registers.FLAG_CARRY)); // No carry since no underflow occurred
        assertFalse(cpu.getRegisters().isFlagSet(Registers.FLAG_ZERO)); // Result is non-zero
        assertTrue(cpu.getRegisters().isFlagSet(Registers.FLAG_SUBTRACT)); // SUB always sets N
    }

    @Test
    void testSUB_r8_withCarry() {
        OpcodeInfo opcodeInfo = opcodeWrapper.getUnprefixed().get("0x90"); // Assume this opcode represents SUB B

        cpu.getRegisters().setA(0x20);
        cpu.getRegisters().setB(0x30);

        opcodeHandler.execute(cpu, opcodeInfo);

        assertEquals(0xF0, cpu.getRegisters().getA()); // 0x20 - 0x30 = 0xF0 (underflow, 2's complement result)
        assertTrue(cpu.getRegisters().isFlagSet(Registers.FLAG_CARRY)); // Carry since underflow occurred
        assertFalse(cpu.getRegisters().isFlagSet(Registers.FLAG_ZERO)); // Result is non-zero
        assertTrue(cpu.getRegisters().isFlagSet(Registers.FLAG_SUBTRACT)); // SUB always sets N
    }

    @Test
    void testSUB_r8_zeroResult() {
        OpcodeInfo opcodeInfo = opcodeWrapper.getUnprefixed().get("0x90"); // Assume this opcode represents SUB B

        cpu.getRegisters().setA(0x20);
        cpu.getRegisters().setB(0x20);

        opcodeHandler.execute(cpu, opcodeInfo);

        assertEquals(0x00, cpu.getRegisters().getA()); // 0x20 - 0x20 = 0x00
        assertFalse(cpu.getRegisters().isFlagSet(Registers.FLAG_CARRY)); // No carry since no underflow occurred
        assertTrue(cpu.getRegisters().isFlagSet(Registers.FLAG_ZERO)); // Result is zero
        assertTrue(cpu.getRegisters().isFlagSet(Registers.FLAG_SUBTRACT)); // SUB always sets N
    }

    // Test SUB [HL]
    @Test
    void testSUB_HL_noCarry() {
        OpcodeInfo opcodeInfo = opcodeWrapper.getUnprefixed().get("0x96"); // Assume this opcode represents SUB [HL]

        cpu.getRegisters().setA(0x50);
        cpu.getRegisters().setH(0x20);
        cpu.getRegisters().setL(0x00);
        cpu.getMemory().writeByte(cpu.getRegisters().getHL(), 0x20);

        opcodeHandler.execute(cpu, opcodeInfo);

        assertEquals(0x30, cpu.getRegisters().getA()); // 0x50 - 0x20 = 0x30
        assertFalse(cpu.getRegisters().isFlagSet(Registers.FLAG_CARRY)); // No carry since no underflow occurred
        assertFalse(cpu.getRegisters().isFlagSet(Registers.FLAG_ZERO)); // Result is non-zero
        assertTrue(cpu.getRegisters().isFlagSet(Registers.FLAG_SUBTRACT)); // SUB always sets N
    }

    @Test
    void testSUB_HL_withCarry() {
        OpcodeInfo opcodeInfo = opcodeWrapper.getUnprefixed().get("0x96"); // Assume this opcode represents SUB [HL]

        cpu.getRegisters().setA(0x20);
        cpu.getRegisters().setH(0x20);
        cpu.getRegisters().setL(0x00);
        cpu.getMemory().writeByte(cpu.getRegisters().getHL(), 0x30);

        opcodeHandler.execute(cpu, opcodeInfo);

        assertEquals(0xF0, cpu.getRegisters().getA()); // 0x20 - 0x30 = 0xF0 (underflow, 2's complement result)
        assertTrue(cpu.getRegisters().isFlagSet(Registers.FLAG_CARRY)); // Carry since underflow occurred
        assertFalse(cpu.getRegisters().isFlagSet(Registers.FLAG_ZERO)); // Result is non-zero
        assertTrue(cpu.getRegisters().isFlagSet(Registers.FLAG_SUBTRACT)); // SUB always sets N
    }

    @Test
    void testSUB_HL_zeroResult() {
        OpcodeInfo opcodeInfo = opcodeWrapper.getUnprefixed().get("0x96"); // Assume this opcode represents SUB [HL]

        cpu.getRegisters().setA(0x20);
        cpu.getRegisters().setH(0x20);
        cpu.getRegisters().setL(0x00);
        cpu.getMemory().writeByte(cpu.getRegisters().getHL(), 0x20);

        opcodeHandler.execute(cpu, opcodeInfo);

        assertEquals(0x00, cpu.getRegisters().getA()); // 0x20 - 0x20 = 0x00
        assertFalse(cpu.getRegisters().isFlagSet(Registers.FLAG_CARRY)); // No carry since no underflow occurred
        assertTrue(cpu.getRegisters().isFlagSet(Registers.FLAG_ZERO)); // Result is zero
        assertTrue(cpu.getRegisters().isFlagSet(Registers.FLAG_SUBTRACT)); // SUB always sets N
    }
}
