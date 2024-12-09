package org.mochaboy;

import java.util.Map;

public class OpcodeWrapper {
    private Map<String, OpcodeInfo> unprefixed;
    private Map<String, OpcodeInfo> cbprefixed;

    public OpcodeWrapper() {
    }

    public Map<String, OpcodeInfo> getUnprefixed() {
        return unprefixed;
    }

    public void setUnprefixed(Map<String, OpcodeInfo> unprefixed) {
        this.unprefixed = unprefixed;
    }

    public Map<String, OpcodeInfo> getCbprefixed() {
        return cbprefixed;
    }

    public void setCbprefixed(Map<String, OpcodeInfo> cbprefixed) {
        this.cbprefixed = cbprefixed;
    }
}
