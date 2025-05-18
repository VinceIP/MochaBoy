import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.mochaboy.CPU;
import org.mochaboy.Memory;
import org.mochaboy.registers.Registers;

import java.io.IOException;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.params.provider.Arguments.arguments;

class ArithmeticOpcodesTest {
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

    // ADC A,r8, ADC A,n8, ADC A,(HL)
    static Stream<Arguments> adcProvider() {
        return Stream.of(
                // name,                       opcode, srcReg, A_init, srcVal, carry?, result, Z,     N,     H,     C
                arguments("ADC A,B (no carry)", 0x88, "B", 0x14, 0x22, false, 0x36, false, false, false, false),
                arguments("ADC A,B (with carry)", 0x88, "B", 0x80, 0x80, true, 0x01, false, false, false, true),
                arguments("ADC A,n8 (no carry)", 0xCE, null, 0x10, 0xF0, false, 0x00, true, false, false, true),
                arguments("ADC A,(HL) (with carry)", 0x8E, "HL", 0x05, 0xFB, true, 0x01, false, false, true, true)
        );
    }

    @ParameterizedTest(name = "{0}")
    @MethodSource("adcProvider")
    void testAdc(String name, int opcode, String srcReg,
                 int aInit, int srcVal, boolean initialCarry,
                 int expected,
                 boolean z, boolean n, boolean h, boolean c) {
        // Set A and carry-in
        cpu.getRegisters().setA(aInit);
        if (initialCarry) cpu.getRegisters().setFlag(Registers.FLAG_CARRY, true);
        else cpu.getRegisters().clearFlag(Registers.FLAG_CARRY);

        int hl = 0x1234;
        if ("HL".equals(srcReg)) {
            cpu.getRegisters().setHL(hl);
            memory.writeByteUnrestricted(hl, srcVal);
        } else if (srcReg != null) {
            cpu.getRegisters().setByName(srcReg, srcVal);
        }

        memory.writeByteUnrestricted(0, opcode);
        if (opcode == 0xCE) memory.writeByteUnrestricted(1, (byte) srcVal);

        while (!cpu.isTestStepComplete()) cpu.step();

        assertEquals(expected, cpu.getRegisters().getA(), "A result");
        assertEquals(z, cpu.getRegisters().isFlagSet(Registers.FLAG_ZERO), "Z flag");
        assertEquals(n, cpu.getRegisters().isFlagSet(Registers.FLAG_SUBTRACT), "N flag");
        assertEquals(h, cpu.getRegisters().isFlagSet(Registers.FLAG_HALF_CARRY), "H flag");
        assertEquals(c, cpu.getRegisters().isFlagSet(Registers.FLAG_CARRY), "C flag");
    }

    // ADD A,r8, ADD A,n8, ADD A,(HL)
    static Stream<Arguments> addProvider() {
        return Stream.of(
                arguments("ADD SP,E8", 0xE8, "SP", 0x10, 0x10, 0x20, false, false, false, false),
                arguments("ADD A,C", 0x81, "C", 0x10, 0x10, 0x20, false, false, false, false),
                arguments("ADD A,n8", 0xC6, null, 0xFF, 0x01, 0x00, true, false, true, true),
                arguments("ADD A,(HL)", 0x86, "HL", 0x0F, 0x01, 0x10, false, false, true, false)
        );
    }

    @ParameterizedTest(name = "{0}")
    @MethodSource("addProvider")
    void testAdd(String name, int opcode, String srcReg,
                 int aInit, int srcVal, int expected,
                 boolean z, boolean n, boolean h, boolean c) {
        cpu.getRegisters().setA(aInit);
        int hl = 0x2000;
        if ("HL".equals(srcReg)) {
            cpu.getRegisters().setHL(hl);
            memory.writeByteUnrestricted(hl, srcVal);
        } else if (srcReg != null) {
            cpu.getRegisters().setByName(srcReg, srcVal);
        }


        memory.writeByteUnrestricted(0, opcode);
        if (opcode == 0xC6 || opcode == 0xE8) {
            memory.writeByteUnrestricted(1, (byte) srcVal);
        }

        while (!cpu.isTestStepComplete()) cpu.step();

        if (opcode == 0xE8) {
            assertEquals(expected, cpu.getRegisters().getSP(), "SP result");
        } else {
            assertEquals(expected, cpu.getRegisters().getA(), "A result");
        }
        assertEquals(z, cpu.getRegisters().isFlagSet(Registers.FLAG_ZERO), "Z flag");
        assertEquals(n, cpu.getRegisters().isFlagSet(Registers.FLAG_SUBTRACT), "N flag");
        assertEquals(h, cpu.getRegisters().isFlagSet(Registers.FLAG_HALF_CARRY), "H flag");
        assertEquals(c, cpu.getRegisters().isFlagSet(Registers.FLAG_CARRY), "C flag");
    }

    // CP A,r8, CP A,n8, CP A,(HL)
    static Stream<Arguments> cpProvider() {
        return Stream.of(

                arguments("CP A,D", 0xBA, "D", 0x20, 0x10, false, true, false, false),
                arguments("CP A,n8", 0xFE, null, 0x00, 0x00, true, true, false, false),
                arguments("CP A,(HL)", 0xBE, "HL", 0x05, 0x10, false, true, false, true)
        );
    }

    @ParameterizedTest(name = "{0}")
    @MethodSource("cpProvider")
    void testCp(String name, int opcode, String srcReg,
                int aInit, int srcVal,
                boolean z, boolean n, boolean h, boolean c) {
        cpu.getRegisters().setA(aInit);
        int hl = 0x3000;
        if ("HL".equals(srcReg)) {
            cpu.getRegisters().setHL(hl);
            memory.writeByteUnrestricted(hl, srcVal);
        } else if (srcReg != null) {
            cpu.getRegisters().setByName(srcReg, srcVal);
        }

        memory.writeByteUnrestricted(0, opcode);
        if (opcode == 0xFE) memory.writeByteUnrestricted(1, (byte) srcVal);

        while (!cpu.isTestStepComplete()) cpu.step();

        assertEquals(z, cpu.getRegisters().isFlagSet(Registers.FLAG_ZERO), "Z flag");
        assertTrue(cpu.getRegisters().isFlagSet(Registers.FLAG_SUBTRACT), "N flag must be set");
        assertEquals(h, cpu.getRegisters().isFlagSet(Registers.FLAG_HALF_CARRY), "H flag");
        assertEquals(c, cpu.getRegisters().isFlagSet(Registers.FLAG_CARRY), "C flag");
    }

    // INC/DEC r8
    static Stream<Arguments> incDecRegProvider() {
        return Stream.of(
                // result wraps 0xFF → 0x00, sets Z and H, clears N
                arguments("INC B", 0x04, "B", 0xFF, 0x00, true,  false, true),
                // 0x01 → 0x00 sets Z, sets N, clears H
                arguments("DEC C", 0x0D, "C", 0x01, 0x00, true,  true,  false)
        );
    }


    @ParameterizedTest(name = "{0}")
    @MethodSource("incDecRegProvider")
    void testIncDecReg(String name, int opcode, String reg,
                       int init, int expected,
                       boolean z, boolean n, boolean h) {
        cpu.getRegisters().setByName(reg, init);
        memory.writeByteUnrestricted(0, opcode);
        while (!cpu.isTestStepComplete()) cpu.step();
        assertEquals(expected, cpu.getRegisters().getByName(reg));
        assertEquals(z, cpu.getRegisters().isFlagSet(Registers.FLAG_ZERO));
        assertEquals(n, cpu.getRegisters().isFlagSet(Registers.FLAG_SUBTRACT));
        assertEquals(h, cpu.getRegisters().isFlagSet(Registers.FLAG_HALF_CARRY));
    }

    // INC/DEC (HL)
    static Stream<Arguments> incDecMemProvider() {
        return Stream.of(
                arguments("INC (HL)", 0x34, 0x10, 0x11, false, false, false),
                arguments("DEC (HL)", 0x35, 0x20, 0x1F, false, true, true)
        );
    }

    @ParameterizedTest(name = "{0}")
    @MethodSource("incDecMemProvider")
    void testIncDecMem(String name, int opcode, int addrVal, int expected,
                       boolean z, boolean n, boolean h) {
        int hl = 0x4000;
        cpu.getRegisters().setHL(hl);
        memory.writeByteUnrestricted(hl, addrVal);
        memory.writeByteUnrestricted(0, opcode);
        while (!cpu.isTestStepComplete()) cpu.step();
        assertEquals(expected, memory.readByteUnrestricted(hl));
        assertEquals(z, cpu.getRegisters().isFlagSet(Registers.FLAG_ZERO));
        assertEquals(n, cpu.getRegisters().isFlagSet(Registers.FLAG_SUBTRACT));
        assertEquals(h, cpu.getRegisters().isFlagSet(Registers.FLAG_HALF_CARRY));
    }

    // SBC A,r8, SBC A,n8, SBC A,(HL)
    static Stream<Arguments> sbcProvider() {
        return Stream.of(
                arguments("SBC A,E", 0x9B, "E", 0x10, 0x01, 0x0E, false, true, true, false),
                arguments("SBC A,n8", 0xDE, null, 0x00, 0x00, 0xFF, false, true, true, true),
                arguments("SBC A,(HL)", 0x9E, "HL", 0x00, 0x01, 0xFE, false, true, true, true)
        );
    }

    @ParameterizedTest(name = "{0}")
    @MethodSource("sbcProvider")
    void testSbc(String name, int opcode, String srcReg,
                 int aInit, int srcVal,
                 int expected,
                 boolean z, boolean n, boolean h, boolean c) {
        cpu.getRegisters().setA(aInit);
        // seed incoming borrow
        cpu.getRegisters().setFlag(Registers.FLAG_CARRY, true);
        int hl = 0x5000;
        if ("HL".equals(srcReg)) {
            cpu.getRegisters().setHL(hl);
            memory.writeByteUnrestricted(hl, srcVal);
        } else if (srcReg != null) {
            cpu.getRegisters().setByName(srcReg, srcVal);
        }
        memory.writeByteUnrestricted(0, opcode);
        if (opcode == 0xDE) memory.writeByteUnrestricted(1, (byte) srcVal);
        while (!cpu.isTestStepComplete()) cpu.step();
        assertEquals(expected, cpu.getRegisters().getA(), "A result");
        assertEquals(z, cpu.getRegisters().isFlagSet(Registers.FLAG_ZERO));
        assertEquals(n, cpu.getRegisters().isFlagSet(Registers.FLAG_SUBTRACT));
        assertEquals(h, cpu.getRegisters().isFlagSet(Registers.FLAG_HALF_CARRY));
        assertEquals(c, cpu.getRegisters().isFlagSet(Registers.FLAG_CARRY));
    }

    // SUB A,r8, SUB A,n8, SUB A,(HL)
    static Stream<Arguments> subProvider() {
        return Stream.of(
                arguments("SUB A,H", 0x94, "H", 0x20, 0x10, 0x10, false, true, false, false),
                arguments("SUB A,n8", 0xD6, null, 0x00, 0x01, 0xFF, false, true, true, true),
                arguments("SUB A,(HL)", 0x96, "HL", 0x00, 0x00, 0x00, true, true, false, false)
        );
    }

    @ParameterizedTest(name = "{0}")
    @MethodSource("subProvider")
    void testSub(String name, int opcode, String srcReg,
                 int aInit, int srcVal,
                 int expected,
                 boolean z, boolean n, boolean h, boolean c) {
        cpu.getRegisters().setA(aInit);
        int hl = 0x6000;
        if ("HL".equals(srcReg)) {
            cpu.getRegisters().setHL(hl);
            memory.writeByteUnrestricted(hl, srcVal);
        } else if (srcReg != null) {
            cpu.getRegisters().setByName(srcReg, srcVal);
        }
        memory.writeByteUnrestricted(0, opcode);
        if (opcode == 0xD6) memory.writeByteUnrestricted(1, (byte) srcVal);
        while (!cpu.isTestStepComplete()) cpu.step();
        assertEquals(expected, cpu.getRegisters().getA(), "A result");
        assertEquals(z, cpu.getRegisters().isFlagSet(Registers.FLAG_ZERO));
        assertEquals(n, cpu.getRegisters().isFlagSet(Registers.FLAG_SUBTRACT));
        assertEquals(h, cpu.getRegisters().isFlagSet(Registers.FLAG_HALF_CARRY));
        assertEquals(c, cpu.getRegisters().isFlagSet(Registers.FLAG_CARRY));
    }

    // 16-bit: ADD HL,r16, INC r16, DEC r16
    static Stream<Arguments> addHlProvider() {
        return Stream.of(
                arguments("ADD HL,BC", 0x09, "BC", 0x1234, 0x0001, 0x1235, false, false),
                arguments("ADD HL,DE (ovf)", 0x19, "DE", 0xFFFF, 0x0001, 0x0000, true, true)
        );
    }

    @ParameterizedTest(name = "{0}")
    @MethodSource("addHlProvider")
    void testAddHl(String name, int opcode, String reg16,
                   int init, int val,
                   int expected,
                   boolean h, boolean c) {
        cpu.getRegisters().setHL(init);
        cpu.getRegisters().setByName(reg16, val);
        memory.writeByteUnrestricted(0, opcode);
        while (!cpu.isTestStepComplete()) cpu.step();
        assertEquals(expected, cpu.getRegisters().getHL());
        assertEquals(h, cpu.getRegisters().isFlagSet(Registers.FLAG_HALF_CARRY));
        assertEquals(c, cpu.getRegisters().isFlagSet(Registers.FLAG_CARRY));
    }

    static Stream<Arguments> incDec16Provider() {
        return Stream.of(
                arguments("INC DE", 0x13, "DE", 0x00FF, 0x0100),
                arguments("DEC BC", 0x0B, "BC", 0x0000, 0xFFFF)
        );
    }

    @ParameterizedTest(name = "{0}")
    @MethodSource("incDec16Provider")
    void testIncDec16(String name, int opcode, String reg16,
                      int init, int expected) {
        cpu.getRegisters().setByName(reg16, init);
        memory.writeByteUnrestricted(0, opcode);
        while (!cpu.isTestStepComplete()) cpu.step();
        assertEquals(expected, cpu.getRegisters().getByName(reg16));
    }
}
