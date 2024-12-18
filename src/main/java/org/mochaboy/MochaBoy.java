package org.mochaboy;

import org.mochaboy.gui.GUIEmulator;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;

public class MochaBoy {
    public static void main(String[] args) {

//        if (args.length == 0) {
//            System.out.println("MochaBoy usage: 'MochaBoy *.gb");
//            return;
//        } else {
        //String romFile = args[0];
        String romFile = "./././Tetris.gb";
        Path path = Paths.get(romFile);
        try {
            Cartridge cartridge = new Cartridge(path);
            Memory memory = new Memory(cartridge);
            CPU cpu = new CPU(memory);
            cpu.start();
            GUIEmulator gui = new GUIEmulator();
            gui.run();
            cpu.stopCPU();
        } catch (IOException e) {
            System.out.println("IOException reading cart.");
        }

        //}

        //new GUIEmulator().run();
    }
}