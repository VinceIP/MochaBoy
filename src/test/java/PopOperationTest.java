import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mochaboy.*;

import java.io.File;
import java.io.IOException;

import static org.junit.jupiter.api.Assertions.*;

class PopOperationTest {

    private Cartridge cartridge;
    private Memory memory;
    private CPU cpu;
    private OpcodeLoader opcodeLoader;
    private OpcodeHandler opcodeHandler;
    private OpcodeWrapper opcodeWrapper;
    private Stack stack;

    @BeforeEach
    void setUp() throws IOException {
        cartridge = new Cartridge(new File("./././Tetris.gb").toPath());
        memory = new Memory(cartridge);
        cpu = new CPU(memory);
        opcodeLoader = new OpcodeLoader();
        opcodeWrapper = opcodeLoader.getOpcodeWrapper();
        opcodeHandler = new OpcodeHandler(opcodeWrapper);
        stack = new Stack(cpu);
    }

    // POP AF test
    @Test
    void testPOP_AF() {
        OpcodeInfo opcodeInfo = opcodeWrapper.getUnprefixed().get("0xF1"); // POP AF

        stack.push(0x12F4);
        System.out.printf("A: 0x%02X, F: 0x%02X%n", cpu.getRegisters().getA(), cpu.getRegisters().getF());

        opcodeHandler.execute(cpu, opcodeInfo);
        System.out.printf("A: 0x%02X, F: 0x%02X%n", cpu.getRegisters().getA(), cpu.getRegisters().getF());
        System.out.printf("F: 0b%8s%n", Integer.toBinaryString(cpu.getRegisters().getF() & 0xFF).replace(' ', '0'));

        assertEquals(0x12, cpu.getRegisters().getA()); // Upper byte to A
        assertEquals(0xF0, cpu.getRegisters().getF()); // lower nibble is always 0        assertTrue(cpu.getRegisters().isFlagSet(Registers.FLAG_ZERO)); // Check bit 7 of F (0xF4 = 11110100)
        assertTrue(cpu.getRegisters().isFlagSet(Registers.FLAG_SUBTRACT)); // Check bit 6 of F (0xF4 = 11110100)
        assertTrue(cpu.getRegisters().isFlagSet(Registers.FLAG_HALF_CARRY)); // Check bit 5 of F
        assertTrue(cpu.getRegisters().isFlagSet(Registers.FLAG_CARRY)); // Check bit 4 of F
    }

    // POP BC test
    @Test
    void testPOP_BC() {
        OpcodeInfo opcodeInfo = opcodeWrapper.getUnprefixed().get("0xC1"); // POP BC

        stack.push(0x1234);

        opcodeHandler.execute(cpu, opcodeInfo);

        assertEquals(0x12, cpu.getRegisters().getB()); // Upper byte to B
        assertEquals(0x34, cpu.getRegisters().getC()); // Lower byte to C
    }

    // POP DE test
    @Test
    void testPOP_DE() {
        OpcodeInfo opcodeInfo = opcodeWrapper.getUnprefixed().get("0xD1"); // POP DE

        stack.push(0x5678);

        opcodeHandler.execute(cpu, opcodeInfo);

        assertEquals(0x56, cpu.getRegisters().getD()); // Upper byte to D
        assertEquals(0x78, cpu.getRegisters().getE()); // Lower byte to E
    }

    // POP HL test
    @Test
    void testPOP_HL() {
        OpcodeInfo opcodeInfo = opcodeWrapper.getUnprefixed().get("0xE1"); // POP HL

        stack.push(0x9ABC);

        opcodeHandler.execute(cpu, opcodeInfo);

        assertEquals(0x9A, cpu.getRegisters().getH()); // Upper byte to H
        assertEquals(0xBC, cpu.getRegisters().getL()); // Lower byte to L
    }

    // Edge case for POP AF where F is 0x00 (no flags set)
    @Test
    void testPOP_AF_NoFlagsSet() {
        OpcodeInfo opcodeInfo = opcodeWrapper.getUnprefixed().get("0xF1"); // POP AF

        stack.push(0x1200);

        opcodeHandler.execute(cpu, opcodeInfo);

        assertEquals(0x12, cpu.getRegisters().getA()); // Upper byte to A
        assertEquals(0x00, cpu.getRegisters().getF()); // Lower byte to F
        assertFalse(cpu.getRegisters().isFlagSet(Registers.FLAG_ZERO));
        assertFalse(cpu.getRegisters().isFlagSet(Registers.FLAG_SUBTRACT));
        assertFalse(cpu.getRegisters().isFlagSet(Registers.FLAG_HALF_CARRY));
        assertFalse(cpu.getRegisters().isFlagSet(Registers.FLAG_CARRY));
    }

    // Edge case for POP where SP is at the maximum boundary of the stack
    @Test
    void testPOP_SPBoundary() {
        OpcodeInfo opcodeInfo = opcodeWrapper.getUnprefixed().get("0xF1"); // POP AF

        cpu.getRegisters().setSP(0xFFFE);
        stack.push(0xAABB);

        opcodeHandler.execute(cpu, opcodeInfo);

        assertEquals(0xAA, cpu.getRegisters().getA()); // Upper byte to A
        assertEquals(0xB0, cpu.getRegisters().getF());
    }
}
