package org.mochaboy;

import java.io.IOException;
import java.io.InputStream;
import java.util.Map;

public class Memory {

    private byte[] memory;
    private byte[] bootRom;
    private Cartridge cartridge;
    private MemoryMap memoryMap;
    private boolean bootRomEnabled = true;

    public Memory(Cartridge cartridge) {
        this.cartridge = cartridge;
        memoryMap = new MemoryMap();
        memory = new byte[0x10000];
        bootRom = new byte[0x100];
        init();
    }

    public void init() {
        //Put cart ROM into memory
        loadBootRom();
        loadCart(cartridge);
        //Loaded on top of cart
    }

    /**
     * @param address
     * @return Get a single byte from address.
     */
    public int readByte(int address) {
        address &= 0xFFFF;
        if (bootRomEnabled && address <= 0x00FF) {
            return bootRom[address] & 0xFF;
        }
        return memory[address] & 0xFF;
    }

    public int readUnsignedByte(int address) {
        return memory[address & 0xFFFF];
    }

    /**
     * Write byte at address.
     *
     * @param address
     * @param value
     */
    public void writeByte(int address, int value) {
        value = value & 0xFF;
        address = address & 0xFFFF;
        if (address >= 0x98E0 && address < 0x9940) {
            //System.out.println(String.format("Writing %02X at %02X", value, address));

        }
        if (address == 0xFF50) {
            bootRomEnabled = false;
            //System.out.println("Boot rom disabled.");
        } else if (address == memoryMap.getMap().get("DIV")) {
            value = 0x00;
        }

        if (bootRomEnabled && address <= 0x00FF) {
            return;
        }
        memory[address] = (byte) (value & 0xFF);
    }

    public void writeWord(int address, int value) {
        address = address & 0xFFFF;

        int valueLow = value & 0xFF;
        int valueHigh = (value >> 8) & 0xFF;

        writeByte(address, valueLow);
        int nextAddress = (address + 1) & 0xFFFF;
        writeByte(nextAddress, valueHigh);
    }

    /**
     * @param address
     * @return Get a 2-byte value from address.
     */
    public int readWord(int address) {
        address &= 0xFFFF;
        int lowByte = readByte(address & 0xFFFF);
        int highByte = readByte((address + 1) & 0xFFFF);
        return (highByte << 8) | lowByte;
    }

    /**
     * Loads DMG_BOOT.bin to memory
     */
    private void loadBootRom() {
        try (InputStream inputStream = getClass().getResourceAsStream("/dmg_boot.bin")) {
            if (inputStream != null) {
                byte[] buffer = inputStream.readAllBytes();
                System.arraycopy(buffer, 0, bootRom, 0, buffer.length);
            } else {
                System.out.println("Failed to load boot ROM resource as stream.");
            }

        } catch (IOException e) {
            System.out.println("Couldn't load boot ROM: " + e.getMessage());
        }
    }

    private void loadCart(Cartridge cartridge) {
        byte[] cartData = cartridge.getCartData();

        System.arraycopy(cartData, 0x0000, memory, 0x0000, 0xFFFF / 2);

    }

    public int getMemoryLength() {
        return memory.length;
    }

    public boolean isBootRomEnabled() {
        return bootRomEnabled;
    }

    public void setBootRomEnabled(boolean bootRomEnabled) {
        this.bootRomEnabled = bootRomEnabled;
    }

    public Map<String, Integer> getMemoryMap() {
        return memoryMap.getMap();
    }
}
