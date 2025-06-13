//import org.junit.jupiter.api.BeforeEach;
//import org.junit.jupiter.api.Test;
//import org.junit.jupiter.params.ParameterizedTest;
//import org.junit.jupiter.params.provider.Arguments;
//import org.junit.jupiter.params.provider.MethodSource;
//import org.mochaboy.CPU;
//import org.mochaboy.Memory;
//
//import java.io.IOException;
//import java.util.stream.Stream;
//
//import static org.junit.jupiter.api.Assertions.assertEquals;
//import static org.junit.jupiter.params.provider.Arguments.arguments;
//
//class LdOpcodesTest2 {
//    private Memory memory;
//    private CPU cpu;
//
//    @BeforeEach
//    void setUp() throws IOException {
//        // Use Tetris.gb in project root as cartridge for testing
//        //Cartridge cart = new Cartridge(Paths.get("Tetris.gb"));
//        memory = new Memory();
//        cpu = new CPU(null, memory);
//        memory.setCpu(cpu);
//        cpu.setTestStepComplete(false);
//        cpu.setTestMode(true);
//        cpu.setCpuState(CPU.CPUState.FETCH);
//        cpu.getRegisters().setPC(0);
//    }
//
//    // Register-to-register LD r,r'
//    static Stream<Arguments> regToRegProvider() {
//        return Stream.of(
//                arguments("LD B,B", 0x40, "B", "B"),
//                // ... existing entries omitted for brevity ...
//                arguments("LD A,A", 0x7F, "A", "A")
//        );
//    }
//
//    @ParameterizedTest(name = "{0}")
//    @MethodSource("regToRegProvider")
//    void testRegisterToRegister(String name, int opcode, String destReg, String srcReg) {
//        cpu.getRegisters().setByName(srcReg, 0x5A);
//        memory.writeByteUnrestricted(0, opcode);
//        while (!cpu.isTestStepComplete()) {
//            cpu.step();
//        }
//        int result = cpu.getRegisters().getByName(destReg);
//        assertEquals(0x5A, result, name + " should load value from " + srcReg + " into " + destReg);
//    }
//
//    // ... other existing tests unchanged ...
//
//    @Test
//    void testLdMemoryA16Sp() {
//        cpu.getRegisters().setSP(0xAA55);
//        memory.writeByteUnrestricted(0, 0x08);
//        memory.writeByteUnrestricted(1, 0x00);
//        memory.writeByteUnrestricted(2, (byte) 0xFF);
//        while (!cpu.isTestStepComplete()) {
//            cpu.step();
//        }
//        assertEquals(0x55, memory.readByteUnrestricted(0xFF00));
//        assertEquals(0xAA, memory.readByteUnrestricted(0xFF01));
//    }
//
//    // LD (BC),A and LD (DE),A
//    static Stream<Arguments> regPairToMemProvider() {
//        return Stream.of(
//                arguments("LD (BC),A", 0x02),
//                arguments("LD (DE),A", 0x12)
//        );
//    }
//
//    @ParameterizedTest(name = "{0}")
//    @MethodSource("regPairToMemProvider")
//    void testRegisterPairToMemory(String name, int opcode) {
//        // set base addresses for BC and DE
//        cpu.getRegisters().setBC(0x1000);
//        cpu.getRegisters().setDE(0x2000);
//        cpu.getRegisters().setByName("A", 0xAB);
//        memory.writeByteUnrestricted(0, opcode);
//        while (!cpu.isTestStepComplete()) {
//            cpu.step();
//        }
//        int addr = (opcode == 0x02 ? 0x1000 : 0x2000);
//        assertEquals(0xAB, memory.readByteUnrestricted(addr), name + " should store A into memory[r16]");
//    }
//
//    // LD A,(BC) and LD A,(DE)
//    static Stream<Arguments> memRegPairToRegProvider() {
//        return Stream.of(
//                arguments("LD A,(BC)", 0x0A, "BC"),
//                arguments("LD A,(DE)", 0x1A, "DE")
//        );
//    }
//
//    @ParameterizedTest(name = "{0}")
//    @MethodSource("memRegPairToRegProvider")
//    void testMemoryRegPairToRegister(String name, int opcode, String regPair) {
//        int addr;
//        if (regPair.equals("BC")) {
//            cpu.getRegisters().setBC(0x3000);
//            addr = 0x3000;
//        } else {
//            cpu.getRegisters().setDE(0x4000);
//            addr = 0x4000;
//        }
//        memory.writeByteUnrestricted(addr, 0xCD);
//        memory.writeByteUnrestricted(0, opcode);
//        while (!cpu.isTestStepComplete()) {
//            cpu.step();
//        }
//        assertEquals(0xCD, cpu.getRegisters().getByName("A"), name + " should load memory[r16] into A");
//    }
//
//    // LDI/LDD (HL),A and LDI/LDD A,(HL)
//    static Stream<Arguments> ldIncDecMemProvider() {
//        return Stream.of(
//                arguments("LDI (HL),A", 0x22),
//                arguments("LDD (HL),A", 0x32)
//        );
//    }
//
//    @ParameterizedTest(name = "{0}")
//    @MethodSource("ldIncDecMemProvider")
//    void testLdIncDecToMemoryHl(String name, int opcode) {
//        int start = 0x5000;
//        cpu.getRegisters().setHL(start);
//        cpu.getRegisters().setByName("A", 0xAA);
//        memory.writeByteUnrestricted(0, opcode);
//        while (!cpu.isTestStepComplete()) {
//            cpu.step();
//        }
//        assertEquals(0xAA, memory.readByte(start));
//        int expected = name.contains("LDI") ? start + 1 : start - 1;
//        assertEquals(expected, cpu.getRegisters().getHL(), name + " should adjust HL");
//    }
//
//    static Stream<Arguments> ldIncDecRegProvider() {
//        return Stream.of(
//                arguments("LDI A,(HL)", 0x2A),
//                arguments("LDD A,(HL)", 0x3A)
//        );
//    }
//
//    @ParameterizedTest(name = "{0}")
//    @MethodSource("ldIncDecRegProvider")
//    void testLdIncDecFromMemoryHl(String name, int opcode) {
//        int start = 0x6000;
//        cpu.getRegisters().setHL(start);
//        memory.writeByteUnrestricted(start, 0xBB);
//        memory.writeByteUnrestricted(0, opcode);
//        while (!cpu.isTestStepComplete()) {
//            cpu.step();
//        }
//        assertEquals(0xBB, cpu.getRegisters().getByName("A"));
//        int expected = name.contains("LDI") ? start + 1 : start - 1;
//        assertEquals(expected, cpu.getRegisters().getHL(), name + " should adjust HL");
//    }
//
//    // LDH (C),A and LDH A,(C)
//    static Stream<Arguments> ldHCProvider() {
//        return Stream.of(
//                arguments("LDH (C),A", 0xE2),
//                arguments("LDH A,(C)", 0xF2)
//        );
//    }
//
//    @ParameterizedTest(name = "{0}")
//    @MethodSource("ldHCProvider")
//    void testLdHC(String name, int opcode) {
//        int offset = 0x10;
//        cpu.getRegisters().setByName("C", offset);
//        if (opcode == 0xE2) {
//            cpu.getRegisters().setByName("A", 0xCD);
//            memory.writeByteUnrestricted(0, opcode);
//            while (!cpu.isTestStepComplete()) cpu.step();
//            assertEquals(0xCD, memory.readByte(0xFF00 | offset));
//        } else {
//            memory.writeByteUnrestricted(0xFF00 | offset, 0xEF);
//            memory.writeByteUnrestricted(0, opcode);
//            while (!cpu.isTestStepComplete()) cpu.step();
//            assertEquals(0xEF, cpu.getRegisters().getByName("A"), name + " should load from [C]");
//        }
//    }
//}
