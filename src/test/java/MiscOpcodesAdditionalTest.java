import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.mochaboy.CPU;
import org.mochaboy.Memory;
import org.mochaboy.registers.Registers;

import java.io.IOException;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.*;
import static org.junit.jupiter.params.provider.Arguments.arguments;

class MiscOpcodesAdditionalTest {
    private Memory memory;
    private CPU cpu;

    @BeforeEach
    void setUp() throws IOException {
        memory = new Memory();
        cpu = new CPU(null, memory);
        memory.setCpu(cpu);
        cpu.setTestStepComplete(false);
        cpu.setTestMode(true);
        cpu.setCpuState(CPU.CPUState.FETCH);
        cpu.getRegisters().setPC(0);
    }

    @Test
    void testNop() {
        cpu.getRegisters().setA(0x12);
        memory.writeByteUnrestricted(0, 0x00); // NOP
        while (!cpu.isTestStepComplete()) cpu.step();
        assertEquals(0x12, cpu.getRegisters().getA());
        assertEquals(1, cpu.getRegisters().getPC());
    }

    @Test
    void testDi() {
        cpu.setIME(true);
        memory.writeByteUnrestricted(0, 0xF3); // DI
        while (!cpu.isTestStepComplete()) cpu.step();
        assertFalse(cpu.isIME());
    }

    @Test
    void testEi() {
        cpu.setIME(false);
        memory.writeByteUnrestricted(0, 0xFB); // EI
        memory.writeByteUnrestricted(1, 0x00); // NOP to trigger IME enable
        while (!cpu.isTestStepComplete()) cpu.step();
        assertTrue(cpu.isIME());
        assertFalse(cpu.isPendingImeEnable());

        cpu.setTestStepComplete(false);
        cpu.getRegisters().setPC(1);
        while (!cpu.isTestStepComplete()) cpu.step();

        assertTrue(cpu.isIME());
    }

    static Stream<Arguments> flagOps() {
        return Stream.of(
                arguments("SCF", 0x37, false, true),
                arguments("CCF", 0x3F, true, false)
        );
    }

    @ParameterizedTest(name = "{0}")
    @MethodSource("flagOps")
    void testFlagOps(String name, int opcode, boolean carryIn, boolean carryOut) {
        cpu.getRegisters().setFlag(Registers.FLAG_ZERO, true); // Z unaffected
        if (carryIn) cpu.getRegisters().setFlag(Registers.FLAG_CARRY);
        else cpu.getRegisters().clearFlag(Registers.FLAG_CARRY);
        memory.writeByteUnrestricted(0, opcode);
        while (!cpu.isTestStepComplete()) cpu.step();
        assertEquals(carryOut, cpu.getRegisters().isFlagSet(Registers.FLAG_CARRY));
        assertFalse(cpu.getRegisters().isFlagSet(Registers.FLAG_SUBTRACT));
        assertFalse(cpu.getRegisters().isFlagSet(Registers.FLAG_HALF_CARRY));
        assertTrue(cpu.getRegisters().isFlagSet(Registers.FLAG_ZERO));
    }

    static Stream<Arguments> daaProvider() {
        return Stream.of(
                arguments("DAA no adjust", 0x45, false, false, false, 0x45, false, false),
                arguments("DAA high adjust", 0x9A, false, false, false, 0x00, true, true),
                arguments("DAA sub adjust", 0x15, true, true, false, 0x0F, false, false),
                arguments("DAA sub carry", 0xA0, true, false, true, 0x40, false, true)
        );
    }

    @ParameterizedTest(name = "{0}")
    @MethodSource("daaProvider")
    void testDaa(String name, int aInit, boolean n, boolean h, boolean c,
                 int expected, boolean z, boolean cOut) {
        Registers r = cpu.getRegisters();
        r.setA(aInit);
        if (n) r.setFlag(Registers.FLAG_SUBTRACT); else r.clearFlag(Registers.FLAG_SUBTRACT);
        if (h) r.setFlag(Registers.FLAG_HALF_CARRY); else r.clearFlag(Registers.FLAG_HALF_CARRY);
        if (c) r.setFlag(Registers.FLAG_CARRY); else r.clearFlag(Registers.FLAG_CARRY);
        memory.writeByteUnrestricted(0, 0x27); // DAA
        while (!cpu.isTestStepComplete()) cpu.step();
        assertEquals(expected, r.getA());
        assertEquals(z, r.isFlagSet(Registers.FLAG_ZERO));
        assertEquals(cOut, r.isFlagSet(Registers.FLAG_CARRY));
        assertFalse(r.isFlagSet(Registers.FLAG_HALF_CARRY));
        assertEquals(n, r.isFlagSet(Registers.FLAG_SUBTRACT));
    }
}