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

    // **Unconditional CALL**
    @Test
    void testCALL_Unconditional() {
        OpcodeInfo opcodeInfo = opcodeWrapper.getUnprefixed().get("0xCD"); // CALL n16
        cpu.getRegisters().setPC(0x1000);
        memory.writeWord(0x1000, 0x2000); // Write 16-bit target address 0x2000 at PC

        opcodeHandler.execute(cpu, opcodeInfo);

        assertEquals(0x2000, cpu.getRegisters().getPC()); // PC should be set to 0x2000
        assertEquals(0x1003, memory.readWord(cpu.getRegisters().getSP())); // Check return address
        assertEquals(0xFFFC, cpu.getRegisters().getSP()); // SP should be decremented by 2
    }

    @Test
    void testCALL_Unconditional_Wraparound() {
        OpcodeInfo opcodeInfo = opcodeWrapper.getUnprefixed().get("0xCD"); // CALL n16
        cpu.getRegisters().setPC(0xFFFF);
        memory.writeWord(0xFFFF, 0x1234); // Address wraps around to 0x0000

        opcodeHandler.execute(cpu, opcodeInfo);

        assertEquals(0x1234, cpu.getRegisters().getPC()); // PC should be set to 0x1234
        assertEquals(0x0001, memory.readWord(cpu.getRegisters().getSP())); // Check return address
        assertEquals(0xFFFC, cpu.getRegisters().getSP()); // SP should be decremented by 2
    }

    // **Conditional CALL**
    @Test
    void testCALL_Z_Condition_True() {
        OpcodeInfo opcodeInfo = opcodeWrapper.getUnprefixed().get("0xCC"); // CALL Z, n16
        cpu.getRegisters().setFlag(Registers.FLAG_ZERO, true); // Z flag is set
        cpu.getRegisters().setPC(0x1000);
        memory.writeWord(0x1000, 0x3456); // Target address 0x3456

        opcodeHandler.execute(cpu, opcodeInfo);

        assertEquals(0x3456, cpu.getRegisters().getPC()); // PC should be set to 0x3456
        assertEquals(0x1003, memory.readWord(cpu.getRegisters().getSP())); // Return address should be 0x1003
        assertEquals(0xFFFC, cpu.getRegisters().getSP()); // SP should be decremented by 2
    }

    @Test
    void testCALL_Z_Condition_False() {
        OpcodeInfo opcodeInfo = opcodeWrapper.getUnprefixed().get("0xCC"); // CALL Z, n16
        cpu.getRegisters().setFlag(Registers.FLAG_ZERO, false); // Z flag is clear
        cpu.getRegisters().setPC(0x1000);
        memory.writeWord(0x1000, 0x3456); // Target address 0x3456

        opcodeHandler.execute(cpu, opcodeInfo);

        assertEquals(0x1003, cpu.getRegisters().getPC()); // PC should increment by 3
        assertEquals(0xFFFE, cpu.getRegisters().getSP()); // SP should remain the same (no push)
    }

    @Test
    void testCALL_NZ_Condition_True() {
        OpcodeInfo opcodeInfo = opcodeWrapper.getUnprefixed().get("0xC4"); // CALL NZ, n16
        cpu.getRegisters().setFlag(Registers.FLAG_ZERO, false); // Z flag is clear
        cpu.getRegisters().setPC(0x2000);
        memory.writeWord(0x2000, 0x4000); // Target address 0x4000

        opcodeHandler.execute(cpu, opcodeInfo);

        assertEquals(0x4000, cpu.getRegisters().getPC()); // PC should be set to 0x4000
        assertEquals(0x2003, memory.readWord(cpu.getRegisters().getSP())); // Return address should be 0x2003
        assertEquals(0xFFFC, cpu.getRegisters().getSP()); // SP should be decremented by 2
    }

    @Test
    void testCALL_C_Condition_True() {
        OpcodeInfo opcodeInfo = opcodeWrapper.getUnprefixed().get("0xDC"); // CALL C, n16
        cpu.getRegisters().setFlag(Registers.FLAG_CARRY, true); // C flag is set
        cpu.getRegisters().setPC(0x3000);
        memory.writeWord(0x3000, 0x1234); // Target address 0x1234

        opcodeHandler.execute(cpu, opcodeInfo);

        assertEquals(0x1234, cpu.getRegisters().getPC()); // PC should be set to 0x1234
        assertEquals(0x3003, memory.readWord(cpu.getRegisters().getSP())); // Return address should be 0x3003
        assertEquals(0xFFFC, cpu.getRegisters().getSP()); // SP should be decremented by 2
    }

    @Test
    void testCALL_NC_Condition_False() {
        OpcodeInfo opcodeInfo = opcodeWrapper.getUnprefixed().get("0xD4"); // CALL NC, n16
        cpu.getRegisters().setFlag(Registers.FLAG_CARRY, true); // C flag is set
        cpu.getRegisters().setPC(0x4000);
        memory.writeWord(0x4000, 0x5000); // Target address 0x5000

        opcodeHandler.execute(cpu, opcodeInfo);

        assertEquals(0x4003, cpu.getRegisters().getPC()); // PC should increment by 3
        assertEquals(0xFFFE, cpu.getRegisters().getSP()); // SP should remain the same (no push)
    }

    // **Edge Cases**
    @Test
    void testCALL_MaxAddress() {
        OpcodeInfo opcodeInfo = opcodeWrapper.getUnprefixed().get("0xCD"); // CALL n16
        cpu.getRegisters().setPC(0xFFF0);
        memory.writeWord(0xFFF0, 0xFFFF); // Target address is 0xFFFF

        opcodeHandler.execute(cpu, opcodeInfo);

        assertEquals(0xFFFF, cpu.getRegisters().getPC()); // PC should be set to 0xFFFF
        assertEquals(0xFFF3, memory.readWord(cpu.getRegisters().getSP())); // Return address should be 0xFFF3
        assertEquals(0xFFFC, cpu.getRegisters().getSP()); // SP should be decremented by 2
    }

    @Test
    void testCALL_StackOverflow() {
        OpcodeInfo opcodeInfo = opcodeWrapper.getUnprefixed().get("0xCD"); // CALL n16
        cpu.getRegisters().setSP(0x0002); // Set SP low, near 0x0000
        cpu.getRegisters().setPC(0x1000);
        memory.writeWord(0x1000, 0x3000); // Target address is 0x3000

        opcodeHandler.execute(cpu, opcodeInfo);

        assertEquals(0x3000, cpu.getRegisters().getPC()); // PC should be set to 0x3000
        assertEquals(0x1003, memory.readWord(0x0000)); // Return address wraps to 0x0000 and 0x0001
        assertEquals(0xFFFE, cpu.getRegisters().getSP()); // Stack underflow to 0xFFFE
    }
}
