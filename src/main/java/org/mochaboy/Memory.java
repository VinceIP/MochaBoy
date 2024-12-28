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
        if(address == 0xFF01){
            return 0xFF;
        }
        if(address == 0xFF02){
            return 0x7E;
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

        if(address == 0xFFB6){
            System.out.println("");
        }

        if (address == 0xFF50) {
            bootRomEnabled = false;
            //System.out.println("Boot rom disabled.");
        }
        // Reset DIV if writing to DIV register
        else if (address == memoryMap.getMap().get("DIV")) {
            value = 0x00;
        }
        else if (address == 0xFFFF) {
            //System.out.printf("IE register written: 0x%02X\n", value);
        }

        // ----- Minimal no-link-cable emulation: handle 0xFF01 and 0xFF02 -----
        if (address == 0xFF01) {
            memory[address] = (byte) value;
            return;
        }
        if (address == 0xFF02) {
            // Tetris is writing the Serial Control register
            memory[address] = (byte) value;

            // Check if bit 7 is set => "Start Transfer"
            if ((value & 0x80) != 0) {
                // 1) Immediately complete the transfer:
                //    Put 0xFF in SB => "no partner" data
                memory[0xFF01] = (byte) 0xFF;

                // 2) Clear bit 7 in SC => transfer done
                int newVal = value & 0x7F;
                memory[address] = (byte) newVal;

                // 3) Optionally raise the Serial interrupt (bit 3 in IF),
                //    if Tetris relies on that to continue
                int ifAddress = memoryMap.getMap().get("IF");
                int ifVal = memory[ifAddress] & 0xFF;
                ifVal |= (1 << 3); // set bit 3 => Serial interrupt
                memory[ifAddress] = (byte) ifVal;
            }
            return;
        }
        // --------------------------------------------------------------------

        // If boot ROM is still enabled and address <= 0x00FF, skip writing
        if (bootRomEnabled && address <= 0x00FF) {
            return;
        }

        // Normal memory write for all other addresses
        memory[address] = (byte) value;
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

    public byte[] getMemoryArray(){
        return memory;
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
