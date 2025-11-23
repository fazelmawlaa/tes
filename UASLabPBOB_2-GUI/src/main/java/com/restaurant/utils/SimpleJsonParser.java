package com.restaurant.utils;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Simple JSON Parser untuk parse JSON format sederhana
 * Menggunakan regex untuk parsing tanpa library eksternal
 */
public class SimpleJsonParser {

    /**
     * Parse JSON object sederhana dan ekstrak array dengan key tertentu
     */
    public static List<String> extractArray(String json, String arrayKey) {
        List<String> result = new ArrayList<>();

        // Cari pattern: "arrayKey": [ ... ]
        Pattern pattern = Pattern.compile("\"" + arrayKey + "\"\\s*:\\s*\\[([^\\]]+)\\]");
        Matcher matcher = pattern.matcher(json);

        if (matcher.find()) {
            String arrayContent = matcher.group(1);
            // Split by objects (kurung kurawal)
            String[] objects = arrayContent.split("\\},\\s*\\{");
            for (String obj : objects) {
                obj = obj.replace("{", "").replace("}", "").trim();
                if (!obj.isEmpty()) {
                    result.add("{" + obj + "}");
                }
            }
        }

        return result;
    }

    /**
     * Parse JSON object sederhana dan ekstrak value dari key
     */
    public static String getStringValue(String jsonObj, String key) {
        Pattern pattern = Pattern.compile("\"" + key + "\"\\s*:\\s*\"([^\"]+)\"");
        Matcher matcher = pattern.matcher(jsonObj);
        if (matcher.find()) {
            return unescapeJson(matcher.group(1));
        }
        return null;
    }

    public static int getIntValue(String jsonObj, String key) {
        Pattern pattern = Pattern.compile("\"" + key + "\"\\s*:\\s*(\\d+)");
        Matcher matcher = pattern.matcher(jsonObj);
        if (matcher.find()) {
            try {
                return Integer.parseInt(matcher.group(1));
            } catch (NumberFormatException e) {
                return 0;
            }
        }
        return 0;
    }

    public static double getDoubleValue(String jsonObj, String key) {
        Pattern pattern = Pattern.compile("\"" + key + "\"\\s*:\\s*([\\d.]+)");
        Matcher matcher = pattern.matcher(jsonObj);
        if (matcher.find()) {
            try {
                return Double.parseDouble(matcher.group(1));
            } catch (NumberFormatException e) {
                return 0.0;
            }
        }
        return 0.0;
    }

    /**
     * Build JSON object dari key-value pairs
     */
    public static String buildJsonObject(String... keyValues) {
        if (keyValues.length % 2 != 0) {
            throw new IllegalArgumentException("Key-value pairs harus genap");
        }

        StringBuilder sb = new StringBuilder();
        sb.append("{");
        for (int i = 0; i < keyValues.length; i += 2) {
            if (i > 0)
                sb.append(", ");
            String key = keyValues[i];
            String value = keyValues[i + 1];

            sb.append("\"").append(key).append("\": ");
            // Check if value is number or string
            if (value.matches("^-?\\d+(\\.\\d+)?$")) {
                sb.append(value); // Number
            } else if (value.startsWith("[") || value.startsWith("{")) {
                sb.append(value); // Array or object
            } else {
                sb.append("\"").append(escapeJson(value)).append("\""); // String
            }
        }
        sb.append("}");
        return sb.toString();
    }

    /**
     * Build JSON array dari list of objects
     */
    public static String buildJsonArray(List<String> objects) {
        StringBuilder sb = new StringBuilder();
        sb.append("[");
        for (int i = 0; i < objects.size(); i++) {
            if (i > 0)
                sb.append(", ");
            sb.append(objects.get(i));
        }
        sb.append("]");
        return sb.toString();
    }

    /**
     * Build JSON object dengan array
     */
    public static String buildJsonWithArray(String rootKey, String arrayJson) {
        return "{\n  \"" + rootKey + "\": " + arrayJson + "\n}";
    }

    private static String escapeJson(String str) {
        if (str == null)
            return "";
        return str.replace("\\", "\\\\")
                .replace("\"", "\\\"")
                .replace("\n", "\\n")
                .replace("\r", "\\r")
                .replace("\t", "\\t");
    }

    private static String unescapeJson(String str) {
        if (str == null)
            return "";
        return str.replace("\\t", "\t")
                .replace("\\r", "\r")
                .replace("\\n", "\n")
                .replace("\\\"", "\"")
                .replace("\\\\", "\\");
    }

    /**
     * Get integer value from root level JSON
     */
    public static int getRootIntValue(String json, String key) {
        Pattern pattern = Pattern.compile("\"" + key + "\"\\s*:\\s*(\\d+)");
        Matcher matcher = pattern.matcher(json);
        if (matcher.find()) {
            try {
                return Integer.parseInt(matcher.group(1));
            } catch (NumberFormatException e) {
                return 0;
            }
        }
        return 0;
    }
}
