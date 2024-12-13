package org.mochaboy;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.io.IOException;
import java.io.InputStreamReader;
import java.lang.reflect.Type;
import java.util.HashMap;
import java.util.Map;

public class OpcodeLoader {
    private Map<String, OpcodeInfo> unprefixedMap = new HashMap<>();
    private Map<String, OpcodeInfo> prefixedMap = new HashMap<>();
    private OpcodeWrapper opcodeWrapper;

    public OpcodeLoader() throws IOException {
        loadOpcodes();
    }

    private void loadOpcodes() throws IOException {
        Gson gson = new Gson();
        InputStreamReader reader = new InputStreamReader(getClass().getResourceAsStream("/Opcodes.json"));
        Type type = new TypeToken<OpcodeWrapper>() {}.getType();
        this.opcodeWrapper = gson.fromJson(reader, type);
        reader.close();

        populateOpcodeValues(opcodeWrapper.getUnprefixed(), false);
        populateOpcodeValues(opcodeWrapper.getCbprefixed(), true);
    }

    /**
     * Populate the opcode field of each OpcodeInfo using the key.
     *
     * @param opcodeMap The map containing opcode keys and OpcodeInfo values
     * @param isPrefixed Boolean to distinguish if it's from prefixed opcodes or unprefixed
     */
    private void populateOpcodeValues(Map<String, OpcodeInfo> opcodeMap, boolean isPrefixed) {
        for (Map.Entry<String, OpcodeInfo> entry : opcodeMap.entrySet()) {
            String key = entry.getKey();
            OpcodeInfo opcodeInfo = entry.getValue();

            int opcode = Integer.parseInt(key.substring(2), 16);

            if (isPrefixed) {
                opcode |= 0xCB00;
            }

            opcodeInfo.setOpcode(opcode);
        }
    }

    public Map<String, OpcodeInfo> getUnprefixedMap() {
        return unprefixedMap;
    }

    public Map<String, OpcodeInfo> getPrefixedMap() {
        return prefixedMap;
    }

    public OpcodeWrapper getOpcodeWrapper() {
        return opcodeWrapper;
    }
}
