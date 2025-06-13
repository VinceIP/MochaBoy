//import org.junit.jupiter.api.BeforeEach;
//import org.junit.jupiter.api.Test;
//import org.junit.jupiter.params.ParameterizedTest;
//import org.junit.jupiter.params.provider.Arguments;
//import org.junit.jupiter.params.provider.MethodSource;
//import org.mochaboy.CPU;
//import org.mochaboy.Memory;
//import org.mochaboy.registers.Registers;
//
//import java.io.IOException;
//import java.util.stream.Stream;
//
//import static org.junit.jupiter.api.Assertions.*;
//import static org.junit.jupiter.params.provider.Arguments.arguments;
//
//class LogicOpcodesTest {
//    private Memory memory;
//    private CPU cpu;
//
//    @BeforeEach
//    void setUp() throws IOException {
//        memory = new Memory();
//        cpu = new CPU(null, memory);
//        memory.setCpu(cpu);
//        cpu.setTestStepComplete(false);
//        cpu.setTestMode(true);
//        cpu.setCpuState(CPU.CPUState.FETCH);
//        cpu.getRegisters().setPC(0);
//    }
//
//    // ----- AND A, r8 -----
//    static Stream<Arguments> andRegProvider() {
//        return Stream.of(
//                arguments("AND A, B", 0xA0, "B"),
//                arguments("AND A, C", 0xA1, "C"),
//                arguments("AND A, D", 0xA2, "D"),
//                arguments("AND A, E", 0xA3, "E"),
//                arguments("AND A, H", 0xA4, "H"),
//                arguments("AND A, L", 0xA5, "L")
//        );
//    }
//
//    @ParameterizedTest(name = "{0}")
//    @MethodSource("andRegProvider")
//    void testAndRegister(String name, int opcode, String srcReg) {
//        // Setup: A=0xF0, src=0x0F => result=0x00
//        cpu.getRegisters().setByName("A", 0xF0);
//        cpu.getRegisters().setByName(srcReg, 0x0F);
//        memory.writeByteUnrestricted(0, opcode);
//        while (!cpu.isTestStepComplete()) cpu.step();
//
//        int result = cpu.getRegisters().getA();
//        assertEquals(0x00, result, name + " result");
//        // Flags: Z=1, N=0, H=1, C=0
//        assertTrue(cpu.getRegisters().isFlagSet(Registers.FLAG_ZERO), "Z should be set");
//        assertFalse(cpu.getRegisters().isFlagSet(Registers.FLAG_SUBTRACT), "N should be cleared");
//        assertTrue(cpu.getRegisters().isFlagSet(Registers.FLAG_HALF_CARRY), "H should be set");
//        assertFalse(cpu.getRegisters().isFlagSet(Registers.FLAG_CARRY), "C should be cleared");
//    }
//
//    // AND A, [HL]
//    @Test
//    void testAndMemoryHl() {
//        cpu.getRegisters().setByName("A", 0xF0);
//        cpu.getRegisters().setHL(0x1234);
//        memory.writeByteUnrestricted(0x1234, 0x0F);
//        memory.writeByteUnrestricted(0, 0xA6);
//        while (!cpu.isTestStepComplete()) cpu.step();
//
//        assertEquals(0x00, cpu.getRegisters().getA(), "AND A,(HL) result");
//        assertTrue(cpu.getRegisters().isFlagSet(Registers.FLAG_ZERO));
//        assertFalse(cpu.getRegisters().isFlagSet(Registers.FLAG_SUBTRACT));
//        assertTrue(cpu.getRegisters().isFlagSet(Registers.FLAG_HALF_CARRY));
//        assertFalse(cpu.getRegisters().isFlagSet(Registers.FLAG_CARRY));
//    }
//
//    // AND A, n8
//    @Test
//    void testAndImmediate() {
//        cpu.getRegisters().setByName("A", 0xF0);
//        memory.writeByteUnrestricted(0, 0xE6);
//        memory.writeByteUnrestricted(1, (byte)0x0F);
//        while (!cpu.isTestStepComplete()) cpu.step();
//
//        assertEquals(0x00, cpu.getRegisters().getA(), "AND A,n8 result");
//        assertTrue(cpu.getRegisters().isFlagSet(Registers.FLAG_ZERO));
//        assertFalse(cpu.getRegisters().isFlagSet(Registers.FLAG_SUBTRACT));
//        assertTrue(cpu.getRegisters().isFlagSet(Registers.FLAG_HALF_CARRY));
//        assertFalse(cpu.getRegisters().isFlagSet(Registers.FLAG_CARRY));
//    }
//
//    // ----- CPL -----
//    @Test
//    void testCpl() {
//        cpu.getRegisters().setByName("A", 0b10101010);
//        // Set some flags to ensure CPL only affects N and H
//        cpu.getRegisters().setFlag(Registers.FLAG_ZERO, true);
//        cpu.getRegisters().setFlag(Registers.FLAG_CARRY, true);
//
//        memory.writeByteUnrestricted(0, 0x2F);
//        while (!cpu.isTestStepComplete()) cpu.step();
//
//        assertEquals(0b01010101, cpu.getRegisters().getA(), "CPL should invert A");
//        assertTrue(cpu.getRegisters().isFlagSet(Registers.FLAG_ZERO), "Z unchanged by CPL");
//        assertTrue(cpu.getRegisters().isFlagSet(Registers.FLAG_SUBTRACT), "N should be set");
//        assertTrue(cpu.getRegisters().isFlagSet(Registers.FLAG_HALF_CARRY), "H should be set");
//        assertTrue(cpu.getRegisters().isFlagSet(Registers.FLAG_CARRY), "C unchanged by CPL");
//    }
//
//    // ----- OR A, r8 -----
//    static Stream<Arguments> orRegProvider() {
//        return Stream.of(
//                arguments("OR A, B", 0xB0, "B"),
//                arguments("OR A, C", 0xB1, "C"),
//                arguments("OR A, D", 0xB2, "D"),
//                arguments("OR A, E", 0xB3, "E"),
//                arguments("OR A, H", 0xB4, "H"),
//                arguments("OR A, L", 0xB5, "L")
//        );
//    }
//
//    @ParameterizedTest(name = "{0}")
//    @MethodSource("orRegProvider")
//    void testOrRegister(String name, int opcode, String srcReg) {
//        // Setup: A=0xF0, src=0x0F => result=0xFF
//        cpu.getRegisters().setByName("A", 0xF0);
//        cpu.getRegisters().setByName(srcReg, 0x0F);
//        memory.writeByteUnrestricted(0, opcode);
//        while (!cpu.isTestStepComplete()) cpu.step();
//
//        assertEquals(0xFF, cpu.getRegisters().getA(), name + " result");
//        // Flags: Z=0, N=0, H=0, C=0
//        assertFalse(cpu.getRegisters().isFlagSet(Registers.FLAG_ZERO));
//        assertFalse(cpu.getRegisters().isFlagSet(Registers.FLAG_SUBTRACT));
//        assertFalse(cpu.getRegisters().isFlagSet(Registers.FLAG_HALF_CARRY));
//        assertFalse(cpu.getRegisters().isFlagSet(Registers.FLAG_CARRY));
//    }
//
//    // OR A, [HL]
//    @Test
//    void testOrMemoryHl() {
//        cpu.getRegisters().setByName("A", 0xF0);
//        cpu.getRegisters().setHL(0x2000);
//        memory.writeByteUnrestricted(0x2000, 0x0F);
//        memory.writeByteUnrestricted(0, 0xB6);
//        while (!cpu.isTestStepComplete()) cpu.step();
//
//        assertEquals(0xFF, cpu.getRegisters().getA());
//        assertFalse(cpu.getRegisters().isFlagSet(Registers.FLAG_ZERO));
//        assertFalse(cpu.getRegisters().isFlagSet(Registers.FLAG_SUBTRACT));
//        assertFalse(cpu.getRegisters().isFlagSet(Registers.FLAG_HALF_CARRY));
//        assertFalse(cpu.getRegisters().isFlagSet(Registers.FLAG_CARRY));
//    }
//
//    // OR A, n8
//    @Test
//    void testOrImmediate() {
//        cpu.getRegisters().setByName("A", 0xF0);
//        memory.writeByteUnrestricted(0, 0xF6);
//        memory.writeByteUnrestricted(1, (byte)0x0F);
//        while (!cpu.isTestStepComplete()) cpu.step();
//
//        assertEquals(0xFF, cpu.getRegisters().getA());
//        assertFalse(cpu.getRegisters().isFlagSet(Registers.FLAG_ZERO));
//        assertFalse(cpu.getRegisters().isFlagSet(Registers.FLAG_SUBTRACT));
//        assertFalse(cpu.getRegisters().isFlagSet(Registers.FLAG_HALF_CARRY));
//        assertFalse(cpu.getRegisters().isFlagSet(Registers.FLAG_CARRY));
//    }
//
//    // ----- XOR A, r8 -----
//    static Stream<Arguments> xorRegProvider() {
//        return Stream.of(
//                arguments("XOR A, B", 0xA8, "B"),
//                arguments("XOR A, C", 0xA9, "C"),
//                arguments("XOR A, D", 0xAA, "D"),
//                arguments("XOR A, E", 0xAB, "E"),
//                arguments("XOR A, H", 0xAC, "H"),
//                arguments("XOR A, L", 0xAD, "L")
//        );
//    }
//
//    @ParameterizedTest(name = "{0}")
//    @MethodSource("xorRegProvider")
//    void testXorRegister(String name, int opcode, String srcReg) {
//        // Setup: A=0xF0, src=0x0F => result=0xFF
//        cpu.getRegisters().setByName("A", 0xF0);
//        cpu.getRegisters().setByName(srcReg, 0x0F);
//        memory.writeByteUnrestricted(0, opcode);
//        while (!cpu.isTestStepComplete()) cpu.step();
//
//        assertEquals(0xFF, cpu.getRegisters().getA(), name + " result");
//        assertFalse(cpu.getRegisters().isFlagSet(Registers.FLAG_ZERO));
//        assertFalse(cpu.getRegisters().isFlagSet(Registers.FLAG_SUBTRACT));
//        assertFalse(cpu.getRegisters().isFlagSet(Registers.FLAG_HALF_CARRY));
//        assertFalse(cpu.getRegisters().isFlagSet(Registers.FLAG_CARRY));
//    }
//
//    // XOR A, [HL]
//    @Test
//    void testXorMemoryHl() {
//        cpu.getRegisters().setByName("A", 0xF0);
//        cpu.getRegisters().setHL(0x3000);
//        memory.writeByteUnrestricted(0x3000, 0x0F);
//        memory.writeByteUnrestricted(0, 0xAE);
//        while (!cpu.isTestStepComplete()) cpu.step();
//
//        assertEquals(0xFF, cpu.getRegisters().getA());
//        assertFalse(cpu.getRegisters().isFlagSet(Registers.FLAG_ZERO));
//        assertFalse(cpu.getRegisters().isFlagSet(Registers.FLAG_SUBTRACT));
//        assertFalse(cpu.getRegisters().isFlagSet(Registers.FLAG_HALF_CARRY));
//        assertFalse(cpu.getRegisters().isFlagSet(Registers.FLAG_CARRY));
//    }
//
//    // XOR A, n8
//    @Test
//    void testXorImmediate() {
//        cpu.getRegisters().setByName("A", 0xF0);
//        memory.writeByteUnrestricted(0, 0xEE);
//        memory.writeByteUnrestricted(1, (byte)0x0F);
//        while (!cpu.isTestStepComplete()) cpu.step();
//
//        assertEquals(0xFF, cpu.getRegisters().getA());
//        assertFalse(cpu.getRegisters().isFlagSet(Registers.FLAG_ZERO));
//        assertFalse(cpu.getRegisters().isFlagSet(Registers.FLAG_SUBTRACT));
//        assertFalse(cpu.getRegisters().isFlagSet(Registers.FLAG_HALF_CARRY));
//        assertFalse(cpu.getRegisters().isFlagSet(Registers.FLAG_CARRY));
//    }
//}
