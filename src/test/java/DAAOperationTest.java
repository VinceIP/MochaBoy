import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mochaboy.*;

import java.io.File;
import java.io.IOException;

import static org.junit.jupiter.api.Assertions.*;

class DAAOperationTest {

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
    void testDAA_noAdjustmentNeeded_addition() {
        // A = 0x35, no half-carry, no carry, after addition
        // Since A <= 0x99 and (A & 0x0F) <= 9, no adjustment is needed
        // Result: A=0x35, C=0, Z=0, H=0
        OpcodeInfo opcodeInfo = opcodeWrapper.getUnprefixed().get("0x27");

        cpu.getRegisters().setA(0x35);
        cpu.getRegisters().clearFlag(Registers.FLAG_SUBTRACT);
        cpu.getRegisters().clearFlag(Registers.FLAG_HALF_CARRY);
        cpu.getRegisters().clearFlag(Registers.FLAG_CARRY);

        opcodeHandler.execute(cpu, opcodeInfo);

        assertEquals(0x35, cpu.getRegisters().getA());
        assertFalse(cpu.getRegisters().isFlagSet(Registers.FLAG_CARRY));
        assertFalse(cpu.getRegisters().isFlagSet(Registers.FLAG_ZERO));
        assertFalse(cpu.getRegisters().isFlagSet(Registers.FLAG_HALF_CARRY));
    }

    @Test
    void testDAA_adjustForHalfCarry_addition() {
        // A=0x3D after addition, half-carry set
        // Conditions: (A & 0x0F)=0x0D >9, add 0x06
        // Result: A=0x43, no carry set
        OpcodeInfo opcodeInfo = opcodeWrapper.getUnprefixed().get("0x27");

        cpu.getRegisters().setA(0x3D);
        cpu.getRegisters().clearFlag(Registers.FLAG_SUBTRACT);
        cpu.getRegisters().setFlag(Registers.FLAG_HALF_CARRY);
        cpu.getRegisters().clearFlag(Registers.FLAG_CARRY);

        opcodeHandler.execute(cpu, opcodeInfo);

        assertEquals(0x43, cpu.getRegisters().getA());
        assertFalse(cpu.getRegisters().isFlagSet(Registers.FLAG_CARRY));
        assertFalse(cpu.getRegisters().isFlagSet(Registers.FLAG_ZERO));
        assertFalse(cpu.getRegisters().isFlagSet(Registers.FLAG_HALF_CARRY));
    }

    @Test
    void testDAA_adjustForCarry_addition() {
        // A=0x9A after addition, no half-carry, no carry initially
        // Conditions: A>0x99, add 0x60 and set carry
        // After adding 0x60: A=0xFA
        // (A & 0x0F)=0x0A >9 as well, so add 0x06 => A=0x100, but since A is 8-bit, A=0x00
        // In official DAA, you must apply both steps. Both conditions met means both adjustments done.
        // Final result: A=0x00, carry=1, zero=1, half-carry=0
        OpcodeInfo opcodeInfo = opcodeWrapper.getUnprefixed().get("0x27");

        cpu.getRegisters().setA(0x9A);
        cpu.getRegisters().clearFlag(Registers.FLAG_SUBTRACT);
        cpu.getRegisters().clearFlag(Registers.FLAG_HALF_CARRY);
        cpu.getRegisters().clearFlag(Registers.FLAG_CARRY);

        opcodeHandler.execute(cpu, opcodeInfo);

        assertEquals(0x00, cpu.getRegisters().getA(), "A should wrap around after both adjustments");
        assertTrue(cpu.getRegisters().isFlagSet(Registers.FLAG_CARRY), "Carry should be set");
        assertTrue(cpu.getRegisters().isFlagSet(Registers.FLAG_ZERO), "Zero should be set since A=0x00");
        assertFalse(cpu.getRegisters().isFlagSet(Registers.FLAG_HALF_CARRY), "Half-carry always cleared");
    }

    @Test
    void testDAA_afterSubtraction_noAdjust() {
        // A=0x45 after subtraction, no carry, no half-carry
        // No adjustments: A=0x45 unchanged, C=0, Z=0, H=0
        OpcodeInfo opcodeInfo = opcodeWrapper.getUnprefixed().get("0x27");

        cpu.getRegisters().setA(0x45);
        cpu.getRegisters().setFlag(Registers.FLAG_SUBTRACT);
        cpu.getRegisters().clearFlag(Registers.FLAG_HALF_CARRY);
        cpu.getRegisters().clearFlag(Registers.FLAG_CARRY);

        opcodeHandler.execute(cpu, opcodeInfo);

        assertEquals(0x45, cpu.getRegisters().getA());
        assertFalse(cpu.getRegisters().isFlagSet(Registers.FLAG_CARRY));
        assertFalse(cpu.getRegisters().isFlagSet(Registers.FLAG_ZERO));
        assertFalse(cpu.getRegisters().isFlagSet(Registers.FLAG_HALF_CARRY));
    }

    @Test
    void testDAA_afterSubtraction_withAdjustments() {
        // A=0x00 after subtraction, carry and half-carry set
        // Conditions: Carry => A-=0x60 => A=0xA0 (0x00-0x60 = 0xA0 wrapping 8-bit)
        // Half-carry => A-=0x06 => A=0x9A (0xA0-0x06=0x9A)
        // Result: A=0x9A, C=1, Z=0, H=0
        OpcodeInfo opcodeInfo = opcodeWrapper.getUnprefixed().get("0x27");

        cpu.getRegisters().setA(0x00);
        cpu.getRegisters().setFlag(Registers.FLAG_SUBTRACT);
        cpu.getRegisters().setFlag(Registers.FLAG_HALF_CARRY);
        cpu.getRegisters().setFlag(Registers.FLAG_CARRY);

        opcodeHandler.execute(cpu, opcodeInfo);

        assertEquals(0x9A, cpu.getRegisters().getA());
        assertTrue(cpu.getRegisters().isFlagSet(Registers.FLAG_CARRY), "Carry remains set if it was set");
        assertFalse(cpu.getRegisters().isFlagSet(Registers.FLAG_ZERO));
        assertFalse(cpu.getRegisters().isFlagSet(Registers.FLAG_HALF_CARRY));
    }

    @Test
    void testDAA_zeroResult() {
        // A=0x00, no half-carry, no carry, addition
        // No adjustments, A stays 0x00, zero=1
        OpcodeInfo opcodeInfo = opcodeWrapper.getUnprefixed().get("0x27");

        cpu.getRegisters().setA(0x00);
        cpu.getRegisters().clearFlag(Registers.FLAG_SUBTRACT);
        cpu.getRegisters().clearFlag(Registers.FLAG_HALF_CARRY);
        cpu.getRegisters().clearFlag(Registers.FLAG_CARRY);

        opcodeHandler.execute(cpu, opcodeInfo);

        assertEquals(0x00, cpu.getRegisters().getA());
        assertTrue(cpu.getRegisters().isFlagSet(Registers.FLAG_ZERO));
        assertFalse(cpu.getRegisters().isFlagSet(Registers.FLAG_CARRY));
        assertFalse(cpu.getRegisters().isFlagSet(Registers.FLAG_HALF_CARRY));
    }
}
