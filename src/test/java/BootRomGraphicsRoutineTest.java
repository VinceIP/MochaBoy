//package org.mochaboy;
//
//import org.junit.jupiter.api.BeforeEach;
//import org.junit.jupiter.api.Test;
//import org.mochaboy.opcode.OpcodeHandler;
//import org.mochaboy.opcode.OpcodeInfo;
//import org.mochaboy.opcode.OpcodeLoader;
//import org.mochaboy.opcode.OpcodeWrapper;
//import org.mochaboy.registers.Registers;
//
//import java.io.File;
//import java.io.IOException;
//
//import static org.junit.jupiter.api.Assertions.*;
//
//class BootRomGraphicsRoutineTest {
//
//    private Cartridge cartridge;
//    private Memory memory;
//    private CPU cpu;
//    private OpcodeLoader opcodeLoader;
//    private OpcodeHandler opcodeHandler;
//    private OpcodeWrapper opcodeWrapper;
//    private PPU ppu;
//    private FrameBuffer frameBuffer;
//
//    @BeforeEach
//    void setUp() throws IOException {
//        cartridge = new Cartridge(new File("./././Tetris.gb").toPath());
//        memory = new Memory(cartridge);
//        frameBuffer = new FrameBuffer(160, 140);
//        ppu = new PPU(memory, frameBuffer);
//        cpu = new CPU(ppu, memory);
//        opcodeLoader = new OpcodeLoader();
//        opcodeWrapper = opcodeLoader.getOpcodeWrapper();
//        opcodeHandler = new OpcodeHandler(opcodeWrapper);
//    }
//
//    @Test
//    void testRLC_C() {
//        OpcodeInfo opcodeInfo = opcodeWrapper.getCbprefixed().get("0x01"); // RLC C
//
//        // Test rotating 0xCE (11001110) - the first byte of logo data
//        cpu.getRegisters().setPC(0x0000);
//        cpu.getRegisters().setC(0xCE);
//        opcodeHandler.execute(cpu, opcodeInfo);
//
//        assertEquals(0x9D, cpu.getRegisters().getC()); // C should be 0x9D (10011101)
//        assertTrue(cpu.getRegisters().isFlagSet(Registers.FLAG_CARRY)); // Carry flag should be set (bit 7 was 1)
//        assertFalse(cpu.getRegisters().isFlagSet(Registers.FLAG_ZERO)); // Zero flag should not be set
//    }
//
//    @Test
//    void testRLA() {
//        OpcodeInfo opcodeInfo = opcodeWrapper.getUnprefixed().get("0x17"); // RLA
//
//        // Test rotating 0x5D (01011101) with Carry flag set (result of previous RL C)
//        cpu.getRegisters().setPC(0x0000);
//        cpu.getRegisters().setA(0x5D);
//        cpu.getRegisters().setFlag(Registers.FLAG_CARRY);
//        opcodeHandler.execute(cpu, opcodeInfo);
//
//        assertEquals(0xBB, cpu.getRegisters().getA()); // A should be 0xBB (10111011)
//        assertFalse(cpu.getRegisters().isFlagSet(Registers.FLAG_CARRY)); // Carry flag should be reset (bit 7 was 0)
//
//        // Test rotating 0xBA (10111010) with Carry flag clear
//        cpu.getRegisters().setA(0xBA);
//        cpu.getRegisters().clearFlag(Registers.FLAG_CARRY);
//        opcodeHandler.execute(cpu, opcodeInfo);
//
//        assertEquals(0x74, cpu.getRegisters().getA());
//        assertTrue(cpu.getRegisters().isFlagSet(Registers.FLAG_CARRY));
//    }
//
//    @Test
//    void testPUSH_BC() {
//        OpcodeInfo opcodeInfo = opcodeWrapper.getUnprefixed().get("0xC5"); // PUSH BC
//
//        cpu.getRegisters().setBC(0x1234);
//        cpu.getRegisters().setSP(0xFFFE);
//        cpu.getStack().push(0x0028); // Simulate return address being on stack
//
//        opcodeHandler.execute(cpu, opcodeInfo);
//
//        // SP should be decremented by 2
//        assertEquals(0xFFFA, cpu.getRegisters().getSP());
//
//        // BC should be pushed onto the stack (high byte first)
//        assertEquals(0x12, cpu.getMemory().readByte(0xFFFD)); // High byte at SP + 1
//        assertEquals(0x34, cpu.getMemory().readByte(0xFFFC)); // Low byte at SP
//    }
//
//    @Test
//    void testPOP_BC() {
//        OpcodeInfo opcodeInfo = opcodeWrapper.getUnprefixed().get("0xC1"); // POP BC
//        cpu.getRegisters().setBC(0x04CE);
//        cpu.getRegisters().setSP(0xFFFA);
//        cpu.getStack().push(0x04CE);
//
//        opcodeHandler.execute(cpu, opcodeInfo);
//
//        assertEquals(0xFFFC, cpu.getRegisters().getSP());
//        assertEquals(0x04CE, cpu.getRegisters().getBC());
//    }
//
//    @Test
//    void testDEC_B() {
//        OpcodeInfo opcodeInfo = opcodeWrapper.getUnprefixed().get("0x05"); // DEC B
//
//        // Test decrementing from 0x04
//        cpu.getRegisters().setB(0x04);
//        cpu.getRegisters().setF(0); // Clear flags
//        opcodeHandler.execute(cpu, opcodeInfo);
//
//        assertEquals(0x03, cpu.getRegisters().getB()); // B should be decremented
//        assertFalse(cpu.getRegisters().isFlagSet(Registers.FLAG_ZERO)); // Zero flag should not be set
//        assertTrue(cpu.getRegisters().isFlagSet(Registers.FLAG_SUBTRACT)); // Subtract flag should be set
//        assertFalse(cpu.getRegisters().isFlagSet(Registers.FLAG_HALF_CARRY)); // H flag should not be set
//
//        // Test decrementing from 0x01 (to test Zero flag)
//        cpu.getRegisters().setB(0x01);
//        cpu.getRegisters().setF(0); // Clear flags
//        opcodeHandler.execute(cpu, opcodeInfo);
//
//        assertEquals(0x00, cpu.getRegisters().getB()); // B should be decremented to 0
//        assertTrue(cpu.getRegisters().isFlagSet(Registers.FLAG_ZERO)); // Zero flag should be set
//        assertTrue(cpu.getRegisters().isFlagSet(Registers.FLAG_SUBTRACT)); // Subtract flag should be set
//        assertFalse(cpu.getRegisters().isFlagSet(Registers.FLAG_HALF_CARRY)); // H flag should not be set
//
//        // Test decrementing from 0x00 (to test wrapping)
//        cpu.getRegisters().setB(0x00);
//        cpu.getRegisters().setF(0); // Clear flags
//        opcodeHandler.execute(cpu, opcodeInfo);
//
//        assertEquals(0xFF, cpu.getRegisters().getB()); // B should wrap to 0xFF
//        assertFalse(cpu.getRegisters().isFlagSet(Registers.FLAG_ZERO)); // Zero flag should not be set
//        assertTrue(cpu.getRegisters().isFlagSet(Registers.FLAG_SUBTRACT)); // Subtract flag should be set
//        assertTrue(cpu.getRegisters().isFlagSet(Registers.FLAG_HALF_CARRY)); // H flag should be set (borrow from bit 4)
//    }
//
//    @Test
//    void testLD_HL_INC_A() {
//        OpcodeInfo opcodeInfo = opcodeWrapper.getUnprefixed().get("0x22"); // LD (HL+),A
//
//        // Test writing to VRAM
//        cpu.getRegisters().setA(0xB8);
//        cpu.getRegisters().setHL(0x8010);
//        opcodeHandler.execute(cpu, opcodeInfo);
//
//        assertEquals(0xB8, cpu.getMemory().readByte(0x8010)); // Check value written to memory
//        assertEquals(0x8011, cpu.getRegisters().getHL()); // HL should be incremented
//
//        // Test writing to another VRAM location
//        cpu.getRegisters().setA(0x9F);
//        cpu.getRegisters().setHL(0x8012);
//        opcodeHandler.execute(cpu, opcodeInfo);
//
//        assertEquals(0x9F, cpu.getMemory().readByte(0x8012)); // Check value written to memory
//        assertEquals(0x8013, cpu.getRegisters().getHL()); // HL should be incremented
//    }
//
//    @Test
//    void testINC_HL() {
//        OpcodeInfo opcodeInfo = opcodeWrapper.getUnprefixed().get("0x23"); // INC HL
//
//        cpu.getRegisters().setHL(0x8011);
//        opcodeHandler.execute(cpu, opcodeInfo);
//
//        assertEquals(0x8012, cpu.getRegisters().getHL()); // HL should be incremented
//    }
//}