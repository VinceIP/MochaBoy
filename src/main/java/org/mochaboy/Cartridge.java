package org.mochaboy;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Files;

public class Cartridge {

    private final byte[] cartData;
    //Addresses
    public final static int ENTRY_POINT = 0x0100;
    public final static int NINTENDO_LOGO = 0x0104;
    public final static int TITLE = 0x0134;
    public final static int TITLE_END = 0x0143;
    public final static int MANUFACTURER_CODE = 0x013F; //Part of the title in older carts? Purpose unknown
    public final static int ROM_SIZE = 0x0148; //ROM size on cart
    public final static int RAM_SIZE = 0x0149; //RAM size on cart, if any
    public final static int HEADER_CHECKSUM = 0x014D; //8-bit checksum computed from title -?

    public Cartridge(Path file) throws IOException {
        cartData = Files.readAllBytes(file);
    }

    public void readCartDataAsString(int addressStart, int addressEnd){
        byte[] title = new byte[16];
        StringBuilder titleStr = new StringBuilder();
        int j = 0;
        for (int i = addressStart; i <= addressEnd; i++) {
            title[j] = cartData[i];
            titleStr.append((char) title[j]);
            j++;
        }
        System.out.println(titleStr);
    }

    public byte[] getCartData() {
        return cartData;
    }
}
