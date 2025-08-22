package com.example.library.util;

import java.io.FileWriter;
import java.io.IOException;

public class LicenseGenerator {
    public static void main(String[] args) {
        String mac = HardwareInfo.getMacAddress();
        try (FileWriter fw = new FileWriter("license.lic")) {
            fw.write(mac);
        } catch (IOException e) {
            e.printStackTrace();
        }
        System.out.println("✅ License file generated with MAC: " + mac);
    }
}
