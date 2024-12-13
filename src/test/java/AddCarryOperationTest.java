import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mochaboy.*;

import java.io.File;
import java.io.IOException;

import static org.junit.jupiter.api.Assertions.*;

class AddCarryOperationTest {

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

    // 8-bit ADC A, r8 tests
    @Test
    void testADC_A_r8_noCarry() {
        OpcodeInfo opcodeInfo = opcodeWrapper.getUnprefixed().get("0x88"); // ADC A, B

        cpu.getRegisters().setA(0x10);
        cpu.getRegisters().setB(0x20);
        cpu.getRegisters().clearFlag(Registers.FLAG_CARRY); // No carry from previous operation

        opcodeHandler.execute(cpu, opcodeInfo);

        assertEquals(0x30, cpu.getRegisters().getA());
        assertFalse(cpu.getRegisters().isFlagSet(Registers.FLAG_ZERO));
        assertFalse(cpu.getRegisters().isFlagSet(Registers.FLAG_CARRY));
        assertFalse(cpu.getRegisters().isFlagSet(Registers.FLAG_HALF_CARRY));
        assertFalse(cpu.getRegisters().isFlagSet(Registers.FLAG_SUBTRACT));
    }

    @Test
    void testADC_A_r8_withCarry() {
        OpcodeInfo opcodeInfo = opcodeWrapper.getUnprefixed().get("0x88"); // ADC A, B

        cpu.getRegisters().setA(0xFF);
        cpu.getRegisters().setB(0x01);
        cpu.getRegisters().setFlag(Registers.FLAG_CARRY); // Carry from previous operation

        opcodeHandler.execute(cpu, opcodeInfo);

        assertEquals(0x01, cpu.getRegisters().getA()); // 0xFF + 0x01 + 1 = 0x100, wraps to 0x00 with carry
        assertFalse(cpu.getRegisters().isFlagSet(Registers.FLAG_ZERO));
        assertTrue(cpu.getRegisters().isFlagSet(Registers.FLAG_CARRY));
        assertTrue(cpu.getRegisters().isFlagSet(Registers.FLAG_HALF_CARRY));
        assertFalse(cpu.getRegisters().isFlagSet(Registers.FLAG_SUBTRACT));
    }

    @Test
    void testADC_A_r8_halfCarry() {
        OpcodeInfo opcodeInfo = opcodeWrapper.getUnprefixed().get("0x88"); // ADC A, B

        cpu.getRegisters().setA(0x0F);
        cpu.getRegisters().setB(0x01);
        cpu.getRegisters().setFlag(Registers.FLAG_CARRY); // Carry from previous operation

        opcodeHandler.execute(cpu, opcodeInfo);

        assertEquals(0x11, cpu.getRegisters().getA());
        assertFalse(cpu.getRegisters().isFlagSet(Registers.FLAG_ZERO));
        assertFalse(cpu.getRegisters().isFlagSet(Registers.FLAG_CARRY));
        assertTrue(cpu.getRegisters().isFlagSet(Registers.FLAG_HALF_CARRY));
        assertFalse(cpu.getRegisters().isFlagSet(Registers.FLAG_SUBTRACT));
    }

    // ADC A, [HL] tests
    @Test
    void testADC_A_HL() {
        OpcodeInfo opcodeInfo = opcodeWrapper.getUnprefixed().get("0x8E"); // ADC A, [HL]

        cpu.getRegisters().setA(0x10);
        cpu.getRegisters().setH(0x20);
        cpu.getRegisters().setL(0x00);
        cpu.getMemory().writeByte(cpu.getRegisters().getHL(), 0x20);
        cpu.getRegisters().setFlag(Registers.FLAG_CARRY); // Carry from previous operation

        opcodeHandler.execute(cpu, opcodeInfo);

        assertEquals(0x31, cpu.getRegisters().getA()); // 0x10 + 0x20 + 1 = 0x31
        assertFalse(cpu.getRegisters().isFlagSet(Registers.FLAG_ZERO));
        assertFalse(cpu.getRegisters().isFlagSet(Registers.FLAG_CARRY));
        assertFalse(cpu.getRegisters().isFlagSet(Registers.FLAG_HALF_CARRY));
        assertFalse(cpu.getRegisters().isFlagSet(Registers.FLAG_SUBTRACT));
    }

    // ADC A, n8 tests
    @Test
    void testADC_A_n8() {
        OpcodeInfo opcodeInfo = opcodeWrapper.getUnprefixed().get("0xCE"); // ADC A, n8

        cpu.getRegisters().setA(0x10);
        cpu.getRegisters().setPC(0x1000);
        cpu.getMemory().writeByte(0x1000, 0x20);
        cpu.getRegisters().clearFlag(Registers.FLAG_CARRY); // No carry from previous operation

        opcodeHandler.execute(cpu, opcodeInfo);

        assertEquals(0x30, cpu.getRegisters().getA());
        assertEquals(0x1001, cpu.getRegisters().getPC());
        assertFalse(cpu.getRegisters().isFlagSet(Registers.FLAG_ZERO));
        assertFalse(cpu.getRegisters().isFlagSet(Registers.FLAG_CARRY));
        assertFalse(cpu.getRegisters().isFlagSet(Registers.FLAG_HALF_CARRY));
        assertFalse(cpu.getRegisters().isFlagSet(Registers.FLAG_SUBTRACT));
    }

    @Test
    void testADC_A_n8_withCarry() {
        OpcodeInfo opcodeInfo = opcodeWrapper.getUnprefixed().get("0xCE"); // ADC A, n8

        cpu.getRegisters().setA(0xFF);
        cpu.getRegisters().setPC(0x1000);
        cpu.getMemory().writeByte(0x1000, 0x01);
        cpu.getRegisters().setFlag(Registers.FLAG_CARRY); // Carry from previous operation

        opcodeHandler.execute(cpu, opcodeInfo);

        assertEquals(0x01, cpu.getRegisters().getA()); // 0xFF + 0x01 + 1 = 0x100, wraps to 0x00 with carry
        assertFalse(cpu.getRegisters().isFlagSet(Registers.FLAG_ZERO));
        assertTrue(cpu.getRegisters().isFlagSet(Registers.FLAG_CARRY));
        assertTrue(cpu.getRegisters().isFlagSet(Registers.FLAG_HALF_CARRY));
        assertFalse(cpu.getRegisters().isFlagSet(Registers.FLAG_SUBTRACT));
    }

    // Edge case for zero result
    @Test
    void testADC_A_r8_zeroResult() {
        OpcodeInfo opcodeInfo = opcodeWrapper.getUnprefixed().get("0x88"); // ADC A, B

        cpu.getRegisters().setA(0x00);
        cpu.getRegisters().setB(0x00);
        cpu.getRegisters().clearFlag(Registers.FLAG_CARRY); // No carry from previous operation

        opcodeHandler.execute(cpu, opcodeInfo);

        assertEquals(0x00, cpu.getRegisters().getA());
        assertTrue(cpu.getRegisters().isFlagSet(Registers.FLAG_ZERO)); // The result is 0, so the Z flag should be set
        assertFalse(cpu.getRegisters().isFlagSet(Registers.FLAG_CARRY));
        assertFalse(cpu.getRegisters().isFlagSet(Registers.FLAG_HALF_CARRY));
        assertFalse(cpu.getRegisters().isFlagSet(Registers.FLAG_SUBTRACT));
    }

    @Test
    void testADC_A_r8_carryInZeroOut() {
        OpcodeInfo opcodeInfo = opcodeWrapper.getUnprefixed().get("0x88"); // ADC A, B

        cpu.getRegisters().setA(0xFF);
        cpu.getRegisters().setB(0x00);
        cpu.getRegisters().setFlag(Registers.FLAG_CARRY); // Carry from previous operation

        opcodeHandler.execute(cpu, opcodeInfo);

        assertEquals(0x00, cpu.getRegisters().getA());
        assertTrue(cpu.getRegisters().isFlagSet(Registers.FLAG_ZERO)); // Result is 0
        assertTrue(cpu.getRegisters().isFlagSet(Registers.FLAG_CARRY)); // Carry from 0xFF + 0x00 + 1
        assertTrue(cpu.getRegisters().isFlagSet(Registers.FLAG_HALF_CARRY)); // Carry from bit 3 to 4
        assertFalse(cpu.getRegisters().isFlagSet(Registers.FLAG_SUBTRACT));
    }
}
