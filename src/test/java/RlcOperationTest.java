import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mochaboy.*;

import java.io.File;
import java.io.IOException;

import static org.junit.jupiter.api.Assertions.*;

class RlcOperationTest {

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

    @Test
    void testRLC_A_MSBSet() {
        OpcodeInfo opcodeInfo = opcodeWrapper.getCbprefixed().get("0x07"); // RLC A

        cpu.getRegisters().setA(0xB5); // 10110101

        opcodeHandler.execute(cpu, opcodeInfo);

        assertEquals(0x6B, cpu.getRegisters().getA()); // 10110101 rotated becomes 01101011 (0x6B)
        assertTrue(cpu.getRegisters().isFlagSet(Registers.FLAG_CARRY)); // Original b7 = 1
        assertFalse(cpu.getRegisters().isFlagSet(Registers.FLAG_ZERO)); // Result is not zero
    }

    @Test
    void testRLC_B_MSBNotSet() {
        OpcodeInfo opcodeInfo = opcodeWrapper.getCbprefixed().get("0x00"); // RLC B

        cpu.getRegisters().setB(0x35); // 00110101

        opcodeHandler.execute(cpu, opcodeInfo);

        assertEquals(0x6A, cpu.getRegisters().getB()); // 00110101 rotated becomes 01101010 (0x6A)
        assertFalse(cpu.getRegisters().isFlagSet(Registers.FLAG_CARRY)); // Original b7 = 0
        assertFalse(cpu.getRegisters().isFlagSet(Registers.FLAG_ZERO)); // Result is not zero
    }

    @Test
    void testRLC_C_ResultIsZero() {
        OpcodeInfo opcodeInfo = opcodeWrapper.getCbprefixed().get("0x01"); // RLC C

        cpu.getRegisters().setC(0x00); // 00000000

        opcodeHandler.execute(cpu, opcodeInfo);

        assertEquals(0x00, cpu.getRegisters().getC()); // Rotating 0 gives 0
        assertFalse(cpu.getRegisters().isFlagSet(Registers.FLAG_CARRY)); // Original b7 = 0
        assertTrue(cpu.getRegisters().isFlagSet(Registers.FLAG_ZERO)); // Result is zero
    }

    @Test
    void testRLC_D_AllOnes() {
        OpcodeInfo opcodeInfo = opcodeWrapper.getCbprefixed().get("0x02"); // RLC D

        cpu.getRegisters().setD(0xFF); // 11111111

        opcodeHandler.execute(cpu, opcodeInfo);

        assertEquals(0xFF, cpu.getRegisters().getD()); // Rotating 11111111 gives 11111111
        assertTrue(cpu.getRegisters().isFlagSet(Registers.FLAG_CARRY)); // Original b7 = 1
        assertFalse(cpu.getRegisters().isFlagSet(Registers.FLAG_ZERO)); // Result is not zero
    }

    @Test
    void testRLC_HL_Memory() {
        OpcodeInfo opcodeInfo = opcodeWrapper.getCbprefixed().get("0x06"); // RLC (HL)

        cpu.getRegisters().setH(0x20);
        cpu.getRegisters().setL(0x10);
        cpu.getMemory().writeByte(cpu.getRegisters().getHL(), 0x80); // 10000000

        opcodeHandler.execute(cpu, opcodeInfo);

        int memoryValue = cpu.getMemory().readByte(cpu.getRegisters().getHL());
        assertEquals(0x01, memoryValue); // 10000000 rotated becomes 00000001 (0x01)
        assertTrue(cpu.getRegisters().isFlagSet(Registers.FLAG_CARRY)); // Original b7 = 1
        assertFalse(cpu.getRegisters().isFlagSet(Registers.FLAG_ZERO)); // Result is not zero
    }

    @Test
    void testRLC_H_ResultZeroWithCarry() {
        OpcodeInfo opcodeInfo = opcodeWrapper.getCbprefixed().get("0x04"); // RLC H

        cpu.getRegisters().setH(0x80); // 10000000

        opcodeHandler.execute(cpu, opcodeInfo);

        assertEquals(0x01, cpu.getRegisters().getH()); // 10000000 rotated becomes 00000001 (0x01)
        assertTrue(cpu.getRegisters().isFlagSet(Registers.FLAG_CARRY)); // Original b7 = 1
        assertFalse(cpu.getRegisters().isFlagSet(Registers.FLAG_ZERO)); // Result is not zero
    }
}
