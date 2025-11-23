package com.restaurant.utils;

import java.io.*;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class JsonUtil {

    // --- READING FILE ---
    public static String readFile(String filePath) {
        StringBuilder content = new StringBuilder();
        File f = new File(filePath);
        if (!f.exists()) return "{}";

        try (BufferedReader br = new BufferedReader(new FileReader(f))) {
            String line;
            while ((line = br.readLine()) != null) {
                content.append(line).append("\n");
            }
        } catch (Exception e) {
            e.printStackTrace();
            return "{}";
        }
        return content.toString().trim();
    }

    // --- WRITING FILE ---
    public static void writeFile(String filePath, String content) {
        try {
            File f = new File(filePath);
            f.getParentFile().mkdirs();
            try (PrintWriter pw = new PrintWriter(new FileWriter(f))) {
                pw.print(content);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // --- JSON PARSING PINTAR (BRACKET COUNTER) ---
    // Metode ini memperbaiki masalah "hanya 1 order" dengan menghitung kurung manual
    public static List<String> parseArray(String json, String key) {
        List<String> result = new ArrayList<>();
        if (json == null || json.isEmpty()) return result;

        // 1. Cari posisi awal key (misal: "pesanan": [ )
        String searchKey = "\"" + key + "\"";
        int keyIndex = json.indexOf(searchKey);
        if (keyIndex == -1) return result;

        // 2. Cari kurung siku pembuka '[' setelah key
        int startBracket = json.indexOf("[", keyIndex + searchKey.length());
        if (startBracket == -1) return result;

        // 3. Cari kurung siku penutup ']' PASANGANNYA (Logic Utama)
        int endBracket = -1;
        int depth = 0; // Kedalaman kurung
        
        for (int i = startBracket; i < json.length(); i++) {
            char c = json.charAt(i);
            
            if (c == '[') {
                depth++;
            } else if (c == ']') {
                depth--;
                // Jika kedalaman kembali ke 0, berarti ini penutup utama
                if (depth == 0) {
                    endBracket = i;
                    break;
                }
            }
        }

        if (endBracket == -1) return result; // Struktur JSON rusak

        // 4. Ambil isi di dalam array
        String content = json.substring(startBracket + 1, endBracket).trim();
        if (content.isEmpty()) return result;

        // 5. Pecah menjadi object-object {...} dengan menghitung kurung kurawal
        int objDepth = 0;
        int objStart = -1;
        
        for (int i = 0; i < content.length(); i++) {
            char c = content.charAt(i);
            
            // Abaikan spasi di luar object
            if (objDepth == 0 && Character.isWhitespace(c)) continue;
            
            if (c == '{') {
                if (objDepth == 0) objStart = i; // Mulai object baru
                objDepth++;
            } else if (c == '}') {
                objDepth--;
                if (objDepth == 0) {
                    // Object selesai, masukkan ke list
                    result.add(content.substring(objStart, i + 1));
                }
            }
        }
        
        return result;
    }

    // --- HELPER LAINNYA ---

    public static String getString(String json, String key) {
        Pattern p = Pattern.compile("\"" + key + "\"\\s*:\\s*\"([^\"]*)\"");
        Matcher m = p.matcher(json);
        if (m.find()) return unescape(m.group(1));
        return "";
    }

    public static int getInt(String json, String key) {
        Pattern p = Pattern.compile("\"" + key + "\"\\s*:\\s*(-?\\d+)");
        Matcher m = p.matcher(json);
        if (m.find()) {
            try { return Integer.parseInt(m.group(1)); } catch (Exception e) {}
        }
        return 0;
    }
    
    public static double getDouble(String json, String key) {
        Pattern p = Pattern.compile("\"" + key + "\"\\s*:\\s*(-?\\d+\\.?\\d*)");
        Matcher m = p.matcher(json);
        if (m.find()) {
            try { return Double.parseDouble(m.group(1)); } catch (Exception e) {}
        }
        return 0.0;
    }
    
    public static int getRootInt(String json, String key) {
        return getInt(json, key);
    }

    private static String unescape(String s) {
        return s.replace("\\\"", "\"").replace("\\\\", "\\");
    }
    
    public static String escape(String s) {
        if(s == null) return "";
        return s.replace("\\", "\\\\").replace("\"", "\\\"");
    }
    
    // Helper simple untuk membuat JSON Object string
    public static String jsonObject(String... pairs) {
        StringBuilder sb = new StringBuilder("{");
        for (int i = 0; i < pairs.length; i += 2) {
            if (i > 0) sb.append(", ");
            sb.append("\"").append(pairs[i]).append("\": ");
            String val = pairs[i+1];
            if (val.matches("^-?\\d+(\\.\\d+)?$") || val.equals("true") || val.equals("false")) {
                sb.append(val);
            } else {
                sb.append("\"").append(escape(val)).append("\"");
            }
        }
        sb.append("}");
        return sb.toString();
    }
    
    public static String jsonArray(List<String> items) {
        return "[" + String.join(", ", items) + "]";
    }
    
    public static String jsonWithRoot(String root, String content) {
        return "{\"" + root + "\": " + content + "}";
    }
}