import com.google.gson.reflect.TypeToken;
import org.mochaboy.*;
import com.google.gson.Gson;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.io.InputStreamReader;
import java.lang.reflect.Type;

import static org.junit.jupiter.api.Assertions.*;

class OpcodeLoaderTest {

    @Test
    void testLoadOpcodes() throws IOException {

        OpcodeLoader opcodeLoader = new OpcodeLoader();
        OpcodeWrapper opcodeWrapper = opcodeLoader.getOpcodeWrapper();

        assertNotNull(opcodeWrapper, "OpcodeWrapper should not be null");
        assertNotNull(opcodeWrapper.getUnprefixed(), "Unprefixed opcodes map should not be null");
        assertNotNull(opcodeWrapper.getCbprefixed(), "Prefixed opcodes map should not be null");

        // Check for some known opcodes in unprefixed map
        assertTrue(opcodeWrapper.getUnprefixed().containsKey("0x00"), "Unprefixed map should contain key 0x00");
        assertTrue(opcodeWrapper.getUnprefixed().containsKey("0x01"), "Unprefixed map should contain key 0x01");

        // Check for some known opcodes in prefixed map
        assertTrue(opcodeWrapper.getCbprefixed().containsKey("0x00"), "Prefixed map should contain key 0x00");

        // Optional: Print out one of the opcode details for debugging
        System.out.println("Unprefixed 0x00: " + opcodeWrapper.getUnprefixed().get("0x00"));
        System.out.println("Prefixed 0x00: " + opcodeWrapper.getCbprefixed().get("0x00"));
    }
}