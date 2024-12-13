import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mochaboy.*;

import java.io.File;
import java.io.IOException;

import static org.junit.jupiter.api.Assertions.*;

class CallOperationTest {

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

    // Unconditional CALL test
    @Test
    void testCALL_Unconditional() {
        OpcodeInfo opcodeInfo = opcodeWrapper.getUnprefixed().get("0xCD"); // CALL a16

        cpu.getRegisters().setPC(0x0100); // PC is at the start of the CALL
        memory.writeByte(0x0101, 0x34); // Low byte of a16 = 0x34
        memory.writeByte(0x0102, 0x12); // High byte of a16 = 0x12
        cpu.getRegisters().setSP(0xFFFE); // Stack pointer at 0xFFFE (top of stack)

        opcodeHandler.execute(cpu, opcodeInfo);

        assertEquals(0x1234, cpu.getRegisters().getPC(), "PC should be set to the call address 0x1234");
        assertEquals(0xFFFC, cpu.getRegisters().getSP(), "SP should be decremented by 2");
        assertEquals(0x0103, cpu.getMemory().readWord(0xFFFC), "Return address (0x0103) should be pushed to the stack");
    }

    // Conditional CALL test (CALL Z, a16) where Z = 1
    @Test
    void testCALL_Conditional_Z_FlagSet() {
        OpcodeInfo opcodeInfo = opcodeWrapper.getUnprefixed().get("0xCC"); // CALL Z, a16

        cpu.getRegisters().setPC(0x0100);
        memory.writeByte(0x0101, 0x34); // Low byte of a16 = 0x34
        memory.writeByte(0x0102, 0x12); // High byte of a16 = 0x12
        cpu.getRegisters().setSP(0xFFFE);
        cpu.getRegisters().setFlag(Registers.FLAG_ZERO); // Set the Z flag

        opcodeHandler.execute(cpu, opcodeInfo);

        assertEquals(0x1234, cpu.getRegisters().getPC(), "PC should be set to the call address 0x1234");
        assertEquals(0xFFFC, cpu.getRegisters().getSP(), "SP should be decremented by 2");
        assertEquals(0x0103, cpu.getMemory().readWord(0xFFFC), "Return address (0x0103) should be pushed to the stack");
    }

    // Conditional CALL test (CALL Z, a16) where Z = 0 (call is NOT taken)
    @Test
    void testCALL_Conditional_Z_FlagNotSet() {
        OpcodeInfo opcodeInfo = opcodeWrapper.getUnprefixed().get("0xCC"); // CALL Z, a16

        cpu.getRegisters().setPC(0x0100);
        memory.writeByte(0x0101, 0x34); // Low byte of a16 = 0x34
        memory.writeByte(0x0102, 0x12); // High byte of a16 = 0x12
        cpu.getRegisters().setSP(0xFFFE);
        cpu.getRegisters().clearFlag(Registers.FLAG_ZERO); // Clear the Z flag

        opcodeHandler.execute(cpu, opcodeInfo);

        assertEquals(0x0103, cpu.getRegisters().getPC(), "PC should continue to the next instruction");
        assertEquals(0xFFFE, cpu.getRegisters().getSP(), "SP should remain unchanged because the call was not taken");
    }

    // Conditional CALL test (CALL C, a16) where C = 1
    @Test
    void testCALL_Conditional_C_FlagSet() {
        OpcodeInfo opcodeInfo = opcodeWrapper.getUnprefixed().get("0xDC"); // CALL C, a16

        cpu.getRegisters().setPC(0x0100);
        memory.writeByte(0x0101, 0x78); // Low byte of a16 = 0x78
        memory.writeByte(0x0102, 0x56); // High byte of a16 = 0x56
        cpu.getRegisters().setSP(0xFFFE);
        cpu.getRegisters().setFlag(Registers.FLAG_CARRY); // Set the C flag

        opcodeHandler.execute(cpu, opcodeInfo);

        assertEquals(0x5678, cpu.getRegisters().getPC(), "PC should be set to the call address 0x5678");
        assertEquals(0xFFFC, cpu.getRegisters().getSP(), "SP should be decremented by 2");
        assertEquals(0x0103, cpu.getMemory().readWord(0xFFFC), "Return address (0x0103) should be pushed to the stack");
    }

    // Conditional CALL test (CALL C, a16) where C = 0 (call is NOT taken)
    @Test
    void testCALL_Conditional_C_FlagNotSet() {
        OpcodeInfo opcodeInfo = opcodeWrapper.getUnprefixed().get("0xDC"); // CALL C, a16

        cpu.getRegisters().setPC(0x0100);
        memory.writeByte(0x0101, 0x78); // Low byte of a16 = 0x78
        memory.writeByte(0x0102, 0x56); // High byte of a16 = 0x56
        cpu.getRegisters().setSP(0xFFFE);
        cpu.getRegisters().clearFlag(Registers.FLAG_CARRY); // Clear the C flag

        opcodeHandler.execute(cpu, opcodeInfo);

        assertEquals(0x0103, cpu.getRegisters().getPC(), "PC should continue to the next instruction");
        assertEquals(0xFFFE, cpu.getRegisters().getSP(), "SP should remain unchanged because the call was not taken");
    }

    // Edge case: Test CALL to 0x0000 (lower bounds)
    @Test
    void testCALL_LowerBound() {
        OpcodeInfo opcodeInfo = opcodeWrapper.getUnprefixed().get("0xCD"); // CALL a16

        cpu.getRegisters().setPC(0x0100);
        memory.writeByte(0x0101, 0x00); // Low byte of a16 = 0x00
        memory.writeByte(0x0102, 0x00); // High byte of a16 = 0x00
        cpu.getRegisters().setSP(0xFFFE);

        opcodeHandler.execute(cpu, opcodeInfo);

        assertEquals(0x0000, cpu.getRegisters().getPC(), "PC should be set to 0x0000");
        assertEquals(0xFFFC, cpu.getRegisters().getSP(), "SP should be decremented by 2");
        assertEquals(0x0103, cpu.getMemory().readWord(0xFFFC), "Return address (0x0103) should be pushed to the stack");
    }

    // Edge case: Test CALL to 0xFFFF (upper bounds)
    @Test
    void testCALL_UpperBound() {
        OpcodeInfo opcodeInfo = opcodeWrapper.getUnprefixed().get("0xCD"); // CALL a16

        cpu.getRegisters().setPC(0x0100);
        memory.writeByte(0x0101, 0xFF); // Low byte of a16 = 0xFF
        memory.writeByte(0x0102, 0xFF); // High byte of a16 = 0xFF
        cpu.getRegisters().setSP(0xFFFE);

        opcodeHandler.execute(cpu, opcodeInfo);

        assertEquals(0xFFFF, cpu.getRegisters().getPC(), "PC should be set to 0xFFFF");
        assertEquals(0xFFFC, cpu.getRegisters().getSP(), "SP should be decremented by 2");
        assertEquals(0x0103, cpu.getMemory().readWord(0xFFFC), "Return address (0x0103) should be pushed to the stack");
    }
}
