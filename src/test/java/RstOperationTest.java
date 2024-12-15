import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mochaboy.*;

import java.io.File;
import java.io.IOException;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class RstOperationTest {
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
    void testRst_00() {
        OpcodeInfo opcodeInfo = opcodeWrapper.getUnprefixed().get("0xC7");

        cpu.getRegisters().setPC(0x0100);

        opcodeHandler.execute(cpu, opcodeInfo);

        assertEquals(0x0000, cpu.getRegisters().getPC());
    }

    @Test
    void testRst_08() {
        OpcodeInfo opcodeInfo = opcodeWrapper.getUnprefixed().get("0xCF");

        cpu.getRegisters().setPC(0x0100);

        opcodeHandler.execute(cpu, opcodeInfo);

        assertEquals(0x0008, cpu.getRegisters().getPC());
    }

    @Test
    void testRst_10() {
        OpcodeInfo opcodeInfo = opcodeWrapper.getUnprefixed().get("0xD7");

        cpu.getRegisters().setPC(0x0100);

        opcodeHandler.execute(cpu, opcodeInfo);

        assertEquals(0x0010, cpu.getRegisters().getPC());
    }
}
