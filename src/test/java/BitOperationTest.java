//import org.junit.jupiter.api.BeforeEach;
//import org.junit.jupiter.api.Test;
//import org.mochaboy.*;
//
//import java.io.File;
//import java.io.IOException;
//
//import static org.junit.jupiter.api.Assertions.*;
//
//class BitOperationTest {
//
//    private Cartridge cartridge;
//    private Memory memory;
//    private CPU cpu;
//    private OpcodeLoader opcodeLoader;
//    private OpcodeHandler opcodeHandler;
//    private OpcodeWrapper opcodeWrapper;
//
//    @BeforeEach
//    void setUp() throws IOException {
//        cartridge = new Cartridge(new File("./././Tetris.gb").toPath());
//        memory = new Memory(cartridge);
//        cpu = new CPU(memory);
//        opcodeLoader = new OpcodeLoader();
//        opcodeWrapper = opcodeLoader.getOpcodeWrapper();
//        opcodeHandler = new OpcodeHandler(opcodeWrapper);
//    }
//
//    /**
//     * Test BIT 0, B when the bit is set (bit 0 = 1)
//     */
//    @Test
//    void testBIT_0_B_BitSet() {
//        OpcodeInfo opcodeInfo = opcodeWrapper.getCbprefixed().get("0x40"); // BIT 0, B
//        cpu.getRegisters().setB(0b00000001); // Bit 0 is set (1)
//        opcodeHandler.execute(cpu, opcodeInfo);
//        assertFalse(cpu.getRegisters().isFlagSet(Registers.FLAG_ZERO)); // Z = 0 since bit 0 is 1
//        assertFalse(cpu.getRegisters().isFlagSet(Registers.FLAG_SUBTRACT)); // N = 0 (always)
//        assertTrue(cpu.getRegisters().isFlagSet(Registers.FLAG_HALF_CARRY)); // H = 1 (always)
//    }
//
//    /**
//     * Test BIT 0, B when the bit is clear (bit 0 = 0)
//     */
//    @Test
//    void testBIT_0_B_BitClear() {
//        OpcodeInfo opcodeInfo = opcodeWrapper.getCbprefixed().get("0x40"); // BIT 0, B
//        cpu.getRegisters().setB(0b00000000); // Bit 0 is clear (0)
//        opcodeHandler.execute(cpu, opcodeInfo);
//        assertTrue(cpu.getRegisters().isFlagSet(Registers.FLAG_ZERO)); // Z = 1 since bit 0 is 0
//        assertFalse(cpu.getRegisters().isFlagSet(Registers.FLAG_SUBTRACT)); // N = 0 (always)
//        assertTrue(cpu.getRegisters().isFlagSet(Registers.FLAG_HALF_CARRY)); // H = 1 (always)
//    }
//
//    /**
//     * Test BIT 7, B when the bit is set (bit 7 = 1)
//     */
//    @Test
//    void testBIT_7_B_BitSet() {
//        OpcodeInfo opcodeInfo = opcodeWrapper.getCbprefixed().get("0x78"); // BIT 7, B
//        cpu.getRegisters().setB(0b10000000); // Bit 7 is set (1)
//        opcodeHandler.execute(cpu, opcodeInfo);
//        assertFalse(cpu.getRegisters().isFlagSet(Registers.FLAG_ZERO)); // Z = 0 since bit 7 is 1
//        assertFalse(cpu.getRegisters().isFlagSet(Registers.FLAG_SUBTRACT)); // N = 0 (always)
//        assertTrue(cpu.getRegisters().isFlagSet(Registers.FLAG_HALF_CARRY)); // H = 1 (always)
//    }
//
//    /**
//     * Test BIT 7, B when the bit is clear (bit 7 = 0)
//     */
//    @Test
//    void testBIT_7_B_BitClear() {
//        OpcodeInfo opcodeInfo = opcodeWrapper.getCbprefixed().get("0x78"); // BIT 7, B
//        cpu.getRegisters().setB(0b01111111); // Bit 7 is clear (0)
//        opcodeHandler.execute(cpu, opcodeInfo);
//        assertTrue(cpu.getRegisters().isFlagSet(Registers.FLAG_ZERO)); // Z = 1 since bit 7 is 0
//        assertFalse(cpu.getRegisters().isFlagSet(Registers.FLAG_SUBTRACT)); // N = 0 (always)
//        assertTrue(cpu.getRegisters().isFlagSet(Registers.FLAG_HALF_CARRY)); // H = 1 (always)
//    }
//
//    /**
//     * Test BIT 3, (HL) where HL points to memory location with the bit set
//     */
//    @Test
//    void testBIT_3_HL_BitSet() {
//        OpcodeInfo opcodeInfo = opcodeWrapper.getCbprefixed().get("0x5E"); // BIT 3, (HL)
//        cpu.getRegisters().setH(0x20);
//        cpu.getRegisters().setL(0x00);
//        cpu.getMemory().writeByte(cpu.getRegisters().getHL(), 0b00001000); // Bit 3 is set (1)
//        opcodeHandler.execute(cpu, opcodeInfo);
//        assertFalse(cpu.getRegisters().isFlagSet(Registers.FLAG_ZERO)); // Z = 0 since bit 3 is 1
//        assertFalse(cpu.getRegisters().isFlagSet(Registers.FLAG_SUBTRACT)); // N = 0 (always)
//        assertTrue(cpu.getRegisters().isFlagSet(Registers.FLAG_HALF_CARRY)); // H = 1 (always)
//    }
//
//    /**
//     * Test BIT 3, (HL) where HL points to memory location with the bit clear
//     */
//    @Test
//    void testBIT_3_HL_BitClear() {
//        OpcodeInfo opcodeInfo = opcodeWrapper.getCbprefixed().get("0x5E"); // BIT 3, (HL)
//        cpu.getRegisters().setH(0x20);
//        cpu.getRegisters().setL(0x00);
//        cpu.getMemory().writeByte(cpu.getRegisters().getHL(), 0b11110111); // Bit 3 is clear (0)
//        opcodeHandler.execute(cpu, opcodeInfo);
//        assertTrue(cpu.getRegisters().isFlagSet(Registers.FLAG_ZERO)); // Z = 1 since bit 3 is 0
//        assertFalse(cpu.getRegisters().isFlagSet(Registers.FLAG_SUBTRACT)); // N = 0 (always)
//        assertTrue(cpu.getRegisters().isFlagSet(Registers.FLAG_HALF_CARRY)); // H = 1 (always)
//    }
//
//    /**
//     * Test BIT 0, C when Carry flag is already set (C should not be affected)
//     */
//    @Test
//    void testBIT_0_C_DoesNotAffectCarry() {
//        OpcodeInfo opcodeInfo = opcodeWrapper.getCbprefixed().get("0x41"); // BIT 0, C
//        cpu.getRegisters().setFlag(Registers.FLAG_CARRY); // Set Carry flag before the BIT instruction
//        cpu.getRegisters().setC(0b00000001); // Bit 0 is set (1)
//        opcodeHandler.execute(cpu, opcodeInfo);
//        assertTrue(cpu.getRegisters().isFlagSet(Registers.FLAG_CARRY)); // C flag is unaffected by BIT
//    }
//
//    /**
//     * Test BIT 7, D when Carry flag is not set (C should not be affected)
//     */
//    @Test
//    void testBIT_7_D_DoesNotAffectCarry() {
//        OpcodeInfo opcodeInfo = opcodeWrapper.getCbprefixed().get("0x7A"); // BIT 7, D
//        cpu.getRegisters().setD(0b10000000); // Bit 7 is set (1)
//        opcodeHandler.execute(cpu, opcodeInfo);
//        assertFalse(cpu.getRegisters().isFlagSet(Registers.FLAG_CARRY)); // Carry flag should be unchanged
//    }
//}
