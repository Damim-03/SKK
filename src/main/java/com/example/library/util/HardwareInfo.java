package com.example.library.util;

import java.net.InetAddress;
import java.net.NetworkInterface;

public class HardwareInfo {
    public static String getMacAddress() {
        try {
            InetAddress ip = InetAddress.getLocalHost();
            NetworkInterface network = NetworkInterface.getByInetAddress(ip);
            byte[] mac = network.getHardwareAddress();

            if (mac == null) return "UNKNOWN";

            StringBuilder sb = new StringBuilder();
            for (int i = 0; i < mac.length; i++) {
                sb.append(String.format("%02X", mac[i]));
            }
            return sb.toString();
        } catch (Exception e) {
            return "UNKNOWN";
        }
    }
}
