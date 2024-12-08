package org.mochaboy;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.io.IOException;
import java.io.InputStreamReader;
import java.lang.reflect.Type;
import java.util.HashMap;
import java.util.Map;

public class OpcodeLoader {
    private Map<Integer, OpcodeInfo> unprefixedMap = new HashMap<>();
    private Map<Integer, OpcodeInfo> prefixedMap = new HashMap<>();

    public OpcodeLoader() {
    }

    private void loadOpcodes() throws IOException {
        Gson gson = new Gson();
        InputStreamReader reader = new InputStreamReader(getClass().getResourceAsStream("/Opcodes.json"));
        Type type = new TypeToken<OpcodeWrapper>() {
        }.getType();
        OpcodeWrapper opcodeWrapper = gson.fromJson(reader, type);
        reader.close();
    }
}
