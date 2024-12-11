package org.mochaboy;

import java.io.IOException;
import java.io.InputStream;

public class Memory {

    private byte[] memory;
    private Cartridge cartridge;

    public Memory(Cartridge cartridge) {
        this.cartridge = cartridge;
        memory = new byte[0x10000];
        init();
    }

    public void init() {
        //Put cart ROM into memory
        loadCart(cartridge);
        //Loaded on top of cart
        loadBootRom();
    }

    /**
     * @param address
     * @return Get a single byte from address.
     */
    public int readByte(int address) {
        return memory[address & 0xFFFF] & 0xFF;
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
        memory[address] = (byte) value;
    }

    /**
     * @param address
     * @return Get a 2-byte value from address.
     */
    public int readWord(int address) {
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
                System.arraycopy(buffer, 0, memory, 0, buffer.length);
            } else {
                System.out.println("Failed to load boot ROM resource as stream.");
            }

        } catch (IOException e) {
            System.out.println("Couldn't load boot ROM: " + e.getMessage());
        }
    }

    private void loadCart(Cartridge cartridge) {
        System.arraycopy(cartridge.getCartData(), 0, memory, 0, cartridge.getCartData().length);
    }

    public int getMemoryLength() {
        return memory.length;
    }


}
