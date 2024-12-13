import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mochaboy.*;

import java.io.File;
import java.io.IOException;

import static org.junit.jupiter.api.Assertions.*;

class CCFOperationTest {

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
    void testCCF_withCarrySet() {
        OpcodeInfo opcodeInfo = opcodeWrapper.getUnprefixed().get("0x3F"); // CCF opcode

        cpu.getRegisters().setFlag(Registers.FLAG_CARRY); // Set the carry flag

        opcodeHandler.execute(cpu, opcodeInfo);

        assertFalse(cpu.getRegisters().isFlagSet(Registers.FLAG_CARRY), "Carry flag should be cleared");
        assertFalse(cpu.getRegisters().isFlagSet(Registers.FLAG_HALF_CARRY), "Half-carry flag should be cleared");
        assertFalse(cpu.getRegisters().isFlagSet(Registers.FLAG_SUBTRACT), "Subtract flag should be cleared");
    }

    @Test
    void testCCF_withCarryClear() {
        OpcodeInfo opcodeInfo = opcodeWrapper.getUnprefixed().get("0x3F"); // CCF opcode

        cpu.getRegisters().clearFlag(Registers.FLAG_CARRY); // Clear the carry flag

        opcodeHandler.execute(cpu, opcodeInfo);

        assertTrue(cpu.getRegisters().isFlagSet(Registers.FLAG_CARRY), "Carry flag should be set");
        assertFalse(cpu.getRegisters().isFlagSet(Registers.FLAG_HALF_CARRY), "Half-carry flag should be cleared");
        assertFalse(cpu.getRegisters().isFlagSet(Registers.FLAG_SUBTRACT), "Subtract flag should be cleared");
    }
}
