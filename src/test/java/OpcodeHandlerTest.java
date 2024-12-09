import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mochaboy.*;

import java.io.File;
import java.io.IOException;

import static org.junit.jupiter.api.Assertions.*;

class OpcodeHandlerTest {

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
    void testADC_A_r8_noCarry_noOverflow() {
        OpcodeInfo opcodeInfo = opcodeWrapper.getUnprefixed().get("0x88"); // ADC A, B example opcode

        cpu.getRegisters().setA(0x10);
        cpu.getRegisters().setB(0x20);
        cpu.getRegisters().clearFlag(Registers.FLAG_CARRY); // No carry

        opcodeHandler.execute(cpu, opcodeInfo);

        assertEquals(0x30, cpu.getRegisters().getA());
        assertFalse(cpu.getRegisters().isFlagSet(Registers.FLAG_ZERO));
        assertFalse(cpu.getRegisters().isFlagSet(Registers.FLAG_CARRY));
        assertFalse(cpu.getRegisters().isFlagSet(Registers.FLAG_HALF_CARRY));
        assertFalse(cpu.getRegisters().isFlagSet(Registers.FLAG_SUBTRACT));
    }

    @Test
    void testADC_A_r8_withCarry() {
        OpcodeInfo opcodeInfo = opcodeWrapper.getUnprefixed().get("0x88"); // ADC A, B example opcode

        cpu.getRegisters().setA(0x0F);
        cpu.getRegisters().setB(0x01);
        cpu.getRegisters().setFlag(Registers.FLAG_CARRY); // Carry set

        opcodeHandler.execute(cpu, opcodeInfo);

        assertEquals(0x11, cpu.getRegisters().getA());
        assertFalse(cpu.getRegisters().isFlagSet(Registers.FLAG_ZERO));
        assertFalse(cpu.getRegisters().isFlagSet(Registers.FLAG_CARRY));
        assertTrue(cpu.getRegisters().isFlagSet(Registers.FLAG_HALF_CARRY));
        assertFalse(cpu.getRegisters().isFlagSet(Registers.FLAG_SUBTRACT));
    }

    @Test
    void testADC_A_HL() {
        OpcodeInfo opcodeInfo = opcodeWrapper.getUnprefixed().get("0x8E"); // ADC A, [HL] example opcode

        cpu.getRegisters().setA(0x10);
        cpu.getRegisters().setH(0x01);
        cpu.getRegisters().setL(0x00);
        cpu.getMemory().writeByte(cpu.getRegisters().getHL(), 0x20);
        cpu.getRegisters().clearFlag(Registers.FLAG_CARRY); // No carry

        opcodeHandler.execute(cpu, opcodeInfo);

        assertEquals(0x30, cpu.getRegisters().getA());
        assertFalse(cpu.getRegisters().isFlagSet(Registers.FLAG_ZERO));
        assertFalse(cpu.getRegisters().isFlagSet(Registers.FLAG_CARRY));
        assertFalse(cpu.getRegisters().isFlagSet(Registers.FLAG_HALF_CARRY));
        assertFalse(cpu.getRegisters().isFlagSet(Registers.FLAG_SUBTRACT));
    }

    @Test
    void testADC_A_HL_withCarry() {
        OpcodeInfo opcodeInfo = opcodeWrapper.getUnprefixed().get("0x8E"); // ADC A, [HL] example opcode

        cpu.getRegisters().setA(0x0F);
        cpu.getRegisters().setH(0x01);
        cpu.getRegisters().setL(0x00);
        cpu.getMemory().writeByte(cpu.getRegisters().getHL(), 0x01);
        cpu.getRegisters().setFlag(Registers.FLAG_CARRY); // Carry set

        opcodeHandler.execute(cpu, opcodeInfo);

        assertEquals(0x11, cpu.getRegisters().getA());
        assertFalse(cpu.getRegisters().isFlagSet(Registers.FLAG_ZERO));
        assertFalse(cpu.getRegisters().isFlagSet(Registers.FLAG_CARRY));
        assertTrue(cpu.getRegisters().isFlagSet(Registers.FLAG_HALF_CARRY));
        assertFalse(cpu.getRegisters().isFlagSet(Registers.FLAG_SUBTRACT));
    }

    @Test
    void testADC_A_n8() {
        OpcodeInfo opcodeInfo = opcodeWrapper.getUnprefixed().get("0xCE"); // ADC A, n8 example opcode

        cpu.getRegisters().setA(0x10);
        cpu.getRegisters().setPC(0x1234); // PC points to n8 value
        cpu.getMemory().writeByte(0x1234, 0x20);
        cpu.getRegisters().clearFlag(Registers.FLAG_CARRY); // No carry

        opcodeHandler.execute(cpu, opcodeInfo);

        assertEquals(0x30, cpu.getRegisters().getA());
        assertFalse(cpu.getRegisters().isFlagSet(Registers.FLAG_ZERO));
        assertFalse(cpu.getRegisters().isFlagSet(Registers.FLAG_CARRY));
        assertFalse(cpu.getRegisters().isFlagSet(Registers.FLAG_HALF_CARRY));
        assertFalse(cpu.getRegisters().isFlagSet(Registers.FLAG_SUBTRACT));
    }

    @Test
    void testADC_A_n8_withCarry() {
        OpcodeInfo opcodeInfo = opcodeWrapper.getUnprefixed().get("0xCE"); // ADC A, n8 example opcode

        cpu.getRegisters().setA(0x0F);
        cpu.getRegisters().setPC(0x1234);
        cpu.getMemory().writeByte(0x1234, 0x01);
        cpu.getRegisters().setFlag(Registers.FLAG_CARRY); // Carry set

        opcodeHandler.execute(cpu, opcodeInfo);

        assertEquals(0x11, cpu.getRegisters().getA());
        assertFalse(cpu.getRegisters().isFlagSet(Registers.FLAG_ZERO));
        assertFalse(cpu.getRegisters().isFlagSet(Registers.FLAG_CARRY));
        assertTrue(cpu.getRegisters().isFlagSet(Registers.FLAG_HALF_CARRY));
        assertFalse(cpu.getRegisters().isFlagSet(Registers.FLAG_SUBTRACT));
    }

    @Test
    void testADC_A_n8_withOverflow() {
        OpcodeInfo opcodeInfo = opcodeWrapper.getUnprefixed().get("0xCE"); // ADC A, n8 example opcode

        cpu.getRegisters().setA(0x80);
        cpu.getRegisters().setPC(0x1234);
        cpu.getMemory().writeByte(0x1234, 0x80);
        cpu.getRegisters().clearFlag(Registers.FLAG_CARRY); // No carry

        opcodeHandler.execute(cpu, opcodeInfo);

        assertEquals(0x00, cpu.getRegisters().getA());
        assertTrue(cpu.getRegisters().isFlagSet(Registers.FLAG_ZERO));
        assertTrue(cpu.getRegisters().isFlagSet(Registers.FLAG_CARRY));
        assertFalse(cpu.getRegisters().isFlagSet(Registers.FLAG_HALF_CARRY));
        assertFalse(cpu.getRegisters().isFlagSet(Registers.FLAG_SUBTRACT));
    }
}
