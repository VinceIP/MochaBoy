import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mochaboy.*;

import java.io.File;
import java.io.IOException;

import static org.junit.jupiter.api.Assertions.*;

class RlOperationTest {

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
    void testRL_A_WithCarryAndMSBSet() {
        OpcodeInfo opcodeInfo = opcodeWrapper.getCbprefixed().get("0x17"); // RL A

        cpu.getRegisters().setA(0xB5);
        cpu.getRegisters().setFlag(Registers.FLAG_CARRY, true);

        opcodeHandler.execute(cpu, opcodeInfo);

        assertEquals(0x6B, cpu.getRegisters().getA());
        assertTrue(cpu.getRegisters().isFlagSet(Registers.FLAG_CARRY));
        assertFalse(cpu.getRegisters().isFlagSet(Registers.FLAG_ZERO));
        assertFalse(cpu.getRegisters().isFlagSet(Registers.FLAG_SUBTRACT));
        assertFalse(cpu.getRegisters().isFlagSet(Registers.FLAG_HALF_CARRY));
    }

    @Test
    void testRL_B_NoCarryAndMSBNotSet() {
        OpcodeInfo opcodeInfo = opcodeWrapper.getCbprefixed().get("0x10"); // RL B

        cpu.getRegisters().setB(0x35);
        cpu.getRegisters().clearFlag(Registers.FLAG_CARRY);

        opcodeHandler.execute(cpu, opcodeInfo);

        assertEquals(0x6A, cpu.getRegisters().getB());
        assertFalse(cpu.getRegisters().isFlagSet(Registers.FLAG_CARRY));
        assertFalse(cpu.getRegisters().isFlagSet(Registers.FLAG_ZERO));
        assertFalse(cpu.getRegisters().isFlagSet(Registers.FLAG_SUBTRACT));
        assertFalse(cpu.getRegisters().isFlagSet(Registers.FLAG_HALF_CARRY));
    }

    @Test
    void testRL_C_ResultIsZero() {
        OpcodeInfo opcodeInfo = opcodeWrapper.getCbprefixed().get("0x11"); // RL C

        cpu.getRegisters().setC(0x00);
        cpu.getRegisters().clearFlag(Registers.FLAG_CARRY);

        opcodeHandler.execute(cpu, opcodeInfo);

        assertEquals(0x00, cpu.getRegisters().getC());
        assertTrue(cpu.getRegisters().isFlagSet(Registers.FLAG_ZERO));
        assertFalse(cpu.getRegisters().isFlagSet(Registers.FLAG_CARRY));
        assertFalse(cpu.getRegisters().isFlagSet(Registers.FLAG_SUBTRACT));
        assertFalse(cpu.getRegisters().isFlagSet(Registers.FLAG_HALF_CARRY));
    }

    @Test
    void testRL_E_AllOnes() {
        OpcodeInfo opcodeInfo = opcodeWrapper.getCbprefixed().get("0x13"); // RL E

        cpu.getRegisters().setE(0xFF);
        cpu.getRegisters().clearFlag(Registers.FLAG_CARRY);

        opcodeHandler.execute(cpu, opcodeInfo);

        assertEquals(0xFE, cpu.getRegisters().getE(), "E should become 0xFE, not 0xFF");
        assertTrue(cpu.getRegisters().isFlagSet(Registers.FLAG_CARRY), "Carry should be set because original b7 was 1");
        assertFalse(cpu.getRegisters().isFlagSet(Registers.FLAG_ZERO), "Not zero, so Z=0");
        assertFalse(cpu.getRegisters().isFlagSet(Registers.FLAG_SUBTRACT));
        assertFalse(cpu.getRegisters().isFlagSet(Registers.FLAG_HALF_CARRY));
    }

    @Test
    void testRL_HL_Memory() {
        OpcodeInfo opcodeInfo = opcodeWrapper.getCbprefixed().get("0x16"); // RL (HL)

        cpu.getRegisters().setH(0x20);
        cpu.getRegisters().setL(0x10);
        cpu.getMemory().writeByte(cpu.getRegisters().getHL(), 0x80);
        cpu.getRegisters().setFlag(Registers.FLAG_CARRY, true);

        opcodeHandler.execute(cpu, opcodeInfo);

        int memoryValue = cpu.getMemory().readByte(cpu.getRegisters().getHL());
        assertEquals(0x01, memoryValue);
        assertTrue(cpu.getRegisters().isFlagSet(Registers.FLAG_CARRY));
        assertFalse(cpu.getRegisters().isFlagSet(Registers.FLAG_ZERO));
        assertFalse(cpu.getRegisters().isFlagSet(Registers.FLAG_SUBTRACT));
        assertFalse(cpu.getRegisters().isFlagSet(Registers.FLAG_HALF_CARRY));
    }

    @Test
    void testRL_H_ResultZeroWithCarry() {
        OpcodeInfo opcodeInfo = opcodeWrapper.getCbprefixed().get("0x14"); // RL H

        cpu.getRegisters().setH(0x80);
        cpu.getRegisters().setFlag(Registers.FLAG_CARRY, false);

        opcodeHandler.execute(cpu, opcodeInfo);

        assertEquals(0x00, cpu.getRegisters().getH());
        assertTrue(cpu.getRegisters().isFlagSet(Registers.FLAG_CARRY));
        assertTrue(cpu.getRegisters().isFlagSet(Registers.FLAG_ZERO));
        assertFalse(cpu.getRegisters().isFlagSet(Registers.FLAG_SUBTRACT));
        assertFalse(cpu.getRegisters().isFlagSet(Registers.FLAG_HALF_CARRY));
    }
}
