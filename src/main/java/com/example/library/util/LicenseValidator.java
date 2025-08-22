package com.example.library.util;

import java.io.File;
import java.nio.file.Files;

public class LicenseValidator {
    public static boolean isValid() {
        try {
            File file = new File("license.lic");
            if (!file.exists()) return false;

            String savedLicense = new String(Files.readAllBytes(file.toPath())).trim();
            String currentMac = HardwareInfo.getMacAddress();

            return currentMac.equals(savedLicense);
        } catch (Exception e) {
            return false;
        }
    }
}
