package org.mochaboy;

import java.io.IOException;
import java.io.InputStream;
import java.util.Map;

public class Memory {

    private byte[] memory;
    private byte[] bootRom;
    private Cartridge cartridge;
    private Map<String, Integer> map;
    private PPU ppu;
    private CPU cpu;
    private boolean bootRomEnabled = true;
    private boolean oamBlocked = false;
    private boolean vramBlocked = false;
    private LastWrite lastWrite;

    public Memory(Cartridge cartridge) {
        this.cartridge = cartridge;
        map = new MemoryMap().getMap();
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

        //If reading from OAM
        if (address >= map.get("OAM_START") && address <= map.get("OAM_END")) {
            if (oamBlocked) return 0xFF;
        }

        if(address >= map.get("VRAM_START") && address <= map.get("VRAM_END")){
            if(vramBlocked) return 0xFF;
        }

        if (bootRomEnabled && address <= 0x00FF) {
            return bootRom[address] & 0xFF;
        }
        if (address == 0xFF01) {
            return 0xFF;
        }
        if (address == 0xFF02) {
            return 0x7E;
        }
        return memory[address] & 0xFF;
    }

    public int readByteUnrestricted(int address){
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

        //System.out.printf("Writing byte %02X at address %04X\n", value, address);

        //Prohibit writes to ROM space.
        //TODO: Implement MBC detection and bank switching here
        if (address < 0x8000) {
            return;
        }

        //Prohibited usage
        //Look into OAM corruption bug when writes are requested here
        if (address >= map.get("OAM_START") && address <= map.get("OAM_END")) {
            if (oamBlocked) return;
        }

        if(address >= map.get("VRAM_START") && address <= map.get("VRAM_END")){
            if(vramBlocked) return;
        }

        if (address == 0xFF50) {
            bootRomEnabled = false;
            //System.out.println("Boot rom disabled.");
        }
        // Reset DIV if writing to DIV register
        else if (address == map.get("DIV")) {
            value = 0x00;
        }

        // ----- Minimal no-link-cable emulation: handle 0xFF01 and 0xFF02 -----
        if (address == 0xFF01) {
            memory[address] = (byte) value;
            lastWrite = new LastWrite(address, value, cpu.getCurrentOpcodeObject().getFetchedAt());
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
                int ifAddress = map.get("IF");
                int ifVal = memory[ifAddress] & 0xFF;
                ifVal |= (1 << 3); // set bit 3 => Serial interrupt
                memory[ifAddress] = (byte) ifVal;
            }
            lastWrite = new LastWrite(address, value, cpu.getCurrentOpcodeObject().getFetchedAt());
            return;
        }
        // --------------------------------------------------------------------

        // If boot ROM is still enabled and address <= 0x00FF, skip writing
        if (bootRomEnabled && address <= 0x00FF) {
            return;
        }

        // Normal memory write for all other addresses
        if(cpu !=null)lastWrite = new LastWrite(address, value, cpu.getCurrentOpcodeObject().getFetchedAt());
        memory[address] = (byte) value;
    }

    public void writeByteUnrestricted(int address, int value){
        value = value & 0xFF;
        address = address & 0xFFFF;
        memory[address] = (byte) value;
        if(cpu!=null) lastWrite = new LastWrite(address, value, cpu.getCurrentOpcodeObject().getFetchedAt());
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

    public byte[] getMemoryArray() {
        return memory;
    }

    public boolean isBootRomEnabled() {
        return bootRomEnabled;
    }

    public void setBootRomEnabled(boolean bootRomEnabled) {
        this.bootRomEnabled = bootRomEnabled;
    }

    public Map<String, Integer> getMemoryMap() {
        return map;
    }

    public void setPpu(PPU ppu) {
        this.ppu = ppu;
    }

    public boolean isOamBlocked() {
        return oamBlocked;
    }

    public void setOamBlocked(boolean oamBlocked) {
        this.oamBlocked = oamBlocked;
    }

    public boolean isVramBlocked() {
        return vramBlocked;
    }

    public void setVramBlocked(boolean vramBlocked) {
        this.vramBlocked = vramBlocked;
    }

    public LastWrite getLastWrite() {
        return lastWrite;
    }

    public void setLastWrite(LastWrite lastWrite) {
        this.lastWrite = lastWrite;
    }

    public CPU getCpu() {
        return cpu;
    }

    public void setCpu(CPU cpu) {
        this.cpu = cpu;
    }

    public static class LastWrite{
        private int address;
        private int value;
        private int cycleMarker;

        public LastWrite(int address, int value, int cycleMarker){
            this.address = address;
            this.value = value;
            this.cycleMarker = cycleMarker;
        }

        public int getAddress() {
            return address;
        }

        public void setAddress(int address) {
            this.address = address;
        }

        public int getValue() {
            return value;
        }

        public void setValue(int value) {
            this.value = value;
        }

        public int getCycleMarker() {
            return cycleMarker;
        }

        public void setCycleMarker(int cycleMarker) {
            this.cycleMarker = cycleMarker;
        }
    }
}
