import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mochaboy.*;

import java.io.File;
import java.io.IOException;

import static org.junit.jupiter.api.Assertions.*;

class CpOperationTest {

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

    // CP A, r8 tests
    @Test
    void testCP_A_r8_equal() {  // No changes needed
        OpcodeInfo opcodeInfo = opcodeWrapper.getUnprefixed().get("0xB8"); // CP A, B
        cpu.getRegisters().setA(0x20);
        cpu.getRegisters().setB(0x20);
        opcodeHandler.execute(cpu, opcodeInfo);
        assertEquals(0x20, cpu.getRegisters().getA());
        assertTrue(cpu.getRegisters().isFlagSet(Registers.FLAG_ZERO));
        assertFalse(cpu.getRegisters().isFlagSet(Registers.FLAG_CARRY));
        assertFalse(cpu.getRegisters().isFlagSet(Registers.FLAG_HALF_CARRY)); // No borrow needed in lower nibble
        assertTrue(cpu.getRegisters().isFlagSet(Registers.FLAG_SUBTRACT));
    }

    @Test
    void testCP_A_r8_greater() {  // No changes needed
        OpcodeInfo opcodeInfo = opcodeWrapper.getUnprefixed().get("0xB8");
        cpu.getRegisters().setA(0x30);
        cpu.getRegisters().setB(0x20);
        opcodeHandler.execute(cpu, opcodeInfo);
        assertEquals(0x30, cpu.getRegisters().getA());
        assertFalse(cpu.getRegisters().isFlagSet(Registers.FLAG_ZERO));
        assertFalse(cpu.getRegisters().isFlagSet(Registers.FLAG_CARRY));
        assertFalse(cpu.getRegisters().isFlagSet(Registers.FLAG_HALF_CARRY)); // No borrow needed in lower nibble
        assertTrue(cpu.getRegisters().isFlagSet(Registers.FLAG_SUBTRACT));
    }

    @Test
    void testCP_A_r8_less() {  // Changed H flag expectation
        OpcodeInfo opcodeInfo = opcodeWrapper.getUnprefixed().get("0xB8");
        cpu.getRegisters().setA(0x10);
        cpu.getRegisters().setB(0x20);
        opcodeHandler.execute(cpu, opcodeInfo);
        assertEquals(0x10, cpu.getRegisters().getA());
        assertFalse(cpu.getRegisters().isFlagSet(Registers.FLAG_ZERO));
        assertTrue(cpu.getRegisters().isFlagSet(Registers.FLAG_CARRY));
        assertTrue(cpu.getRegisters().isFlagSet(Registers.FLAG_HALF_CARRY)); // Changed: Need to borrow from bit 4 (0 < 2 in lower nibble)
        assertTrue(cpu.getRegisters().isFlagSet(Registers.FLAG_SUBTRACT));
    }

    @Test
    void testCP_A_HL() {  // No changes needed
        OpcodeInfo opcodeInfo = opcodeWrapper.getUnprefixed().get("0xBE");
        cpu.getRegisters().setA(0x30);
        cpu.getRegisters().setH(0x20);
        cpu.getRegisters().setL(0x00);
        cpu.getMemory().writeByte(cpu.getRegisters().getHL(), 0x30);
        opcodeHandler.execute(cpu, opcodeInfo);
        assertEquals(0x30, cpu.getRegisters().getA());
        assertTrue(cpu.getRegisters().isFlagSet(Registers.FLAG_ZERO));
        assertFalse(cpu.getRegisters().isFlagSet(Registers.FLAG_CARRY));
        assertFalse(cpu.getRegisters().isFlagSet(Registers.FLAG_HALF_CARRY)); // No borrow needed in lower nibble
        assertTrue(cpu.getRegisters().isFlagSet(Registers.FLAG_SUBTRACT));
    }

    @Test
    void testCP_A_n8() {  // No changes needed
        OpcodeInfo opcodeInfo = opcodeWrapper.getUnprefixed().get("0xFE");
        cpu.getRegisters().setA(0x20);
        cpu.getRegisters().setPC(0x1000);
        cpu.getMemory().writeByte(0x1000, 0x10);
        opcodeHandler.execute(cpu, opcodeInfo);
        assertEquals(0x20, cpu.getRegisters().getA());
        assertFalse(cpu.getRegisters().isFlagSet(Registers.FLAG_ZERO));
        assertFalse(cpu.getRegisters().isFlagSet(Registers.FLAG_CARRY));
        assertFalse(cpu.getRegisters().isFlagSet(Registers.FLAG_HALF_CARRY)); // No borrow needed in lower nibble
        assertTrue(cpu.getRegisters().isFlagSet(Registers.FLAG_SUBTRACT));
    }

    @Test
    void testCP_A_n8_zeroResult() {  // No changes needed
        OpcodeInfo opcodeInfo = opcodeWrapper.getUnprefixed().get("0xFE");
        cpu.getRegisters().setA(0x10);
        cpu.getRegisters().setPC(0x1000);
        cpu.getMemory().writeByte(0x1000, 0x10);
        opcodeHandler.execute(cpu, opcodeInfo);
        assertEquals(0x10, cpu.getRegisters().getA());
        assertTrue(cpu.getRegisters().isFlagSet(Registers.FLAG_ZERO));
        assertFalse(cpu.getRegisters().isFlagSet(Registers.FLAG_CARRY));
        assertFalse(cpu.getRegisters().isFlagSet(Registers.FLAG_HALF_CARRY)); // No borrow needed in lower nibble
        assertTrue(cpu.getRegisters().isFlagSet(Registers.FLAG_SUBTRACT));
    }

    @Test
    void testCP_A_n8_borrow() {  // Changed H flag expectation
        OpcodeInfo opcodeInfo = opcodeWrapper.getUnprefixed().get("0xFE");
        cpu.getRegisters().setA(0x10);
        cpu.getRegisters().setPC(0x1000);
        cpu.getMemory().writeByte(0x1000, 0x20);
        opcodeHandler.execute(cpu, opcodeInfo);
        assertEquals(0x10, cpu.getRegisters().getA());
        assertFalse(cpu.getRegisters().isFlagSet(Registers.FLAG_ZERO));
        assertTrue(cpu.getRegisters().isFlagSet(Registers.FLAG_CARRY));
        assertTrue(cpu.getRegisters().isFlagSet(Registers.FLAG_HALF_CARRY)); // Changed: Need to borrow from bit 4 (0 < 2 in lower nibble)
        assertTrue(cpu.getRegisters().isFlagSet(Registers.FLAG_SUBTRACT));
    }

    @Test
    void testCP_A_halfCarry_borrow() {
        OpcodeInfo opcodeInfo = opcodeWrapper.getUnprefixed().get("0xB8"); // CP A, B
        cpu.getRegisters().setA(0x0F); // Lower nibble of A is 0xF
        cpu.getRegisters().setB(0x01); // Lower nibble of B is 0x1
        opcodeHandler.execute(cpu, opcodeInfo);
        assertEquals(0x0F, cpu.getRegisters().getA());
        assertFalse(cpu.getRegisters().isFlagSet(Registers.FLAG_ZERO));
        assertFalse(cpu.getRegisters().isFlagSet(Registers.FLAG_CARRY));
        assertFalse(cpu.getRegisters().isFlagSet(Registers.FLAG_HALF_CARRY)); // No borrow occurred in nibble
        assertTrue(cpu.getRegisters().isFlagSet(Registers.FLAG_SUBTRACT));
    }

    @Test
    void testCP_A_full_borrow() {
        OpcodeInfo opcodeInfo = opcodeWrapper.getUnprefixed().get("0xB8");
        cpu.getRegisters().setA(0x00);
        cpu.getRegisters().setB(0x01);
        opcodeHandler.execute(cpu, opcodeInfo);
        assertEquals(0x00, cpu.getRegisters().getA());
        assertFalse(cpu.getRegisters().isFlagSet(Registers.FLAG_ZERO));
        assertTrue(cpu.getRegisters().isFlagSet(Registers.FLAG_CARRY));
        assertTrue(cpu.getRegisters().isFlagSet(Registers.FLAG_HALF_CARRY));
        assertTrue(cpu.getRegisters().isFlagSet(Registers.FLAG_SUBTRACT));
    }


}
