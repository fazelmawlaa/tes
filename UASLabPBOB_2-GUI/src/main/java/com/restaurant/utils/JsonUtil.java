package com.restaurant.utils;

import java.io.*;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Utility class untuk konversi antara object dan JSON format
 * Format JSON sederhana tanpa library eksternal
 */
public class JsonUtil {

    /**
     * Escape string untuk JSON
     */
    public static String escape(String str) {
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
    public static String unescape(String str) {
        if (str == null)
            return "";
        return str.replace("\\t", "\t")
                .replace("\\r", "\r")
                .replace("\\n", "\n")
                .replace("\\\"", "\"")
                .replace("\\\\", "\\");
    }

    /**
     * Build JSON object dari key-value pairs
     */
    public static String jsonObject(String... pairs) {
        if (pairs.length % 2 != 0) {
            throw new IllegalArgumentException("Pairs harus genap (key-value)");
        }

        StringBuilder sb = new StringBuilder("{");
        for (int i = 0; i < pairs.length; i += 2) {
            if (i > 0)
                sb.append(", ");
            String key = pairs[i];
            String value = pairs[i + 1];

            sb.append("\"").append(key).append("\": ");

            // Check if value is already JSON
            if (value.startsWith("{") || value.startsWith("[")) {
                sb.append(value);
            } else if (value.matches("^-?\\d+(\\.\\d+)?$")) {
                sb.append(value); // Number
            } else {
                sb.append("\"").append(escape(value)).append("\""); // String
            }
        }
        sb.append("}");
        return sb.toString();
    }

    /**
     * Build JSON array dari list of JSON objects
     */
    public static String jsonArray(List<String> items) {
        StringBuilder sb = new StringBuilder("[");
        for (int i = 0; i < items.size(); i++) {
            if (i > 0)
                sb.append(", ");
            sb.append(items.get(i));
        }
        sb.append("]");
        return sb.toString();
    }

    /**
     * Build JSON dengan root key dan array
     */
    public static String jsonWithRoot(String rootKey, String jsonArray) {
        return "{\n  \"" + rootKey + "\": " + jsonArray + "\n}";
    }

    /**
     * Build JSON dengan root key dan value
     */
    public static String jsonWithRoot(String rootKey, int value, String secondKey, String jsonArray) {
        return "{\n  \"" + rootKey + "\": " + value + ",\n  \"" + secondKey + "\": " + jsonArray + "\n}";
    }

    /**
     * Parse JSON dan ekstrak array dengan key tertentu
     */
    public static List<String> parseArray(String json, String arrayKey) {
        List<String> result = new ArrayList<>();

        // Pattern untuk menangkap array: "key": [ ... ]
        Pattern pattern = Pattern.compile("\"" + Pattern.quote(arrayKey) + "\"\\s*:\\s*\\[(.*?)\\]", Pattern.DOTALL);
        Matcher matcher = pattern.matcher(json);

        if (matcher.find()) {
            String arrayContent = matcher.group(1).trim();
            if (arrayContent.isEmpty()) {
                return result;
            }

            // Split objects berdasarkan }, { atau },{
            String[] objects = arrayContent.split("}\\s*,\\s*\\{");
            for (int i = 0; i < objects.length; i++) {
                String obj = objects[i].trim();
                if (obj.isEmpty())
                    continue;

                if (!obj.startsWith("{")) {
                    obj = "{" + obj;
                }
                if (!obj.endsWith("}")) {
                    obj = obj + "}";
                }
                result.add(obj);
            }
        }

        return result;
    }

    /**
     * Get string value from JSON object
     */
    public static String getString(String jsonObj, String key) {
        Pattern pattern = Pattern.compile("\"" + Pattern.quote(key) + "\"\\s*:\\s*\"([^\"]+)\"");
        Matcher matcher = pattern.matcher(jsonObj);
        if (matcher.find()) {
            return unescape(matcher.group(1));
        }
        return null;
    }

    /**
     * Get int value from JSON object
     */
    public static int getInt(String jsonObj, String key) {
        Pattern pattern = Pattern.compile("\"" + Pattern.quote(key) + "\"\\s*:\\s*(-?\\d+)");
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

    /**
     * Get double value from JSON object
     */
    public static double getDouble(String jsonObj, String key) {
        Pattern pattern = Pattern.compile("\"" + Pattern.quote(key) + "\"\\s*:\\s*(-?\\d+\\.?\\d*)");
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
     * Get int value from root level JSON
     */
    public static int getRootInt(String json, String key) {
        Pattern pattern = Pattern.compile("\"" + Pattern.quote(key) + "\"\\s*:\\s*(-?\\d+)");
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

    /**
     * Read file as string
     */
    public static String readFile(String filePath) {
        StringBuilder content = new StringBuilder();
        File f = new File(filePath);
        if (!f.exists()) {
            return "{}";
        }

        try (BufferedReader br = new BufferedReader(new FileReader(f))) {
            String line;
            while ((line = br.readLine()) != null) {
                content.append(line).append("\n");
            }
        } catch (Exception e) {
            System.err.println("[JsonUtil] Error membaca file: " + e.getMessage());
            return "{}";
        }

        String result = content.toString().trim();
        return result.isEmpty() ? "{}" : result;
    }

    /**
     * Write string to file (atomic)
     */
    public static void writeFile(String filePath, String content) {
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
            System.err.println("[JsonUtil] Error menulis file: " + e.getMessage());
            e.printStackTrace();
        }
    }
}
