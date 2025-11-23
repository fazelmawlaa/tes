package com.restaurant.utils;

import java.io.*;

/**
 * Helper class untuk membaca dan menulis file JSON sederhana
 * Format JSON yang digunakan adalah format sederhana tanpa library eksternal
 */
public class JsonHelper {

    /**
     * Membaca seluruh isi file sebagai string
     */
    public static String readFileAsString(String filePath) {
        StringBuilder content = new StringBuilder();
        File f = new File(filePath);
        if (!f.exists()) {
            return "{}"; // Return empty JSON object
        }

        try (BufferedReader br = new BufferedReader(new FileReader(f))) {
            String line;
            while ((line = br.readLine()) != null) {
                content.append(line).append("\n");
            }
        } catch (Exception e) {
            System.err.println("[JsonHelper] Error membaca file: " + e.getMessage());
            return "{}";
        }

        String result = content.toString().trim();
        return result.isEmpty() ? "{}" : result;
    }

    /**
     * Menulis string ke file (atomic write)
     */
    public static void writeStringToFile(String filePath, String content) {
        try {
            File f = new File(filePath);
            f.getParentFile().mkdirs();

            java.nio.file.Path temp = java.nio.file.Files.createTempFile(
                    f.getParentFile().toPath(),
                    f.getName().replace(".json", ""),
                    ".tmp");

            try (PrintWriter pw = new PrintWriter(new FileWriter(temp.toFile()))) {
                pw.print(content);
            }

            java.nio.file.Files.move(
                    temp,
                    f.toPath(),
                    java.nio.file.StandardCopyOption.REPLACE_EXISTING,
                    java.nio.file.StandardCopyOption.ATOMIC_MOVE);
        } catch (Exception e) {
            System.err.println("[JsonHelper] Error menulis file: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * Escape string untuk JSON
     */
    public static String escapeJson(String str) {
        if (str == null)
            return "";
        return str.replace("\\", "\\\\")
                .replace("\"", "\\\"")
                .replace("\n", "\\n")
                .replace("\r", "\\r")
                .replace("\t", "\\t");
    }

    /**
     * Unescape string dari JSON
     */
    public static String unescapeJson(String str) {
        if (str == null)
            return "";
        return str.replace("\\t", "\t")
                .replace("\\r", "\r")
                .replace("\\n", "\n")
                .replace("\\\"", "\"")
                .replace("\\\\", "\\");
    }
}
