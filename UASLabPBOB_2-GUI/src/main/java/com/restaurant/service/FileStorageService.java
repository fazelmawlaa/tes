package com.restaurant.service;

import java.io.*;
import java.nio.file.Files;
import java.util.*;

import com.restaurant.model.menu.*;
import com.restaurant.model.pesanan.*;
import com.restaurant.model.transaksi.*;
import com.restaurant.utils.JsonUtil;

public class FileStorageService {

    // Menggunakan path absolut project agar file tidak hilang saat re-build
    private static final String BASE_DIR = System.getProperty("user.dir") + "/src/main/resources/data/";
    
    private static final String MENU_FILE = BASE_DIR + "menu.json";
    private static final String PESANAN_FILE = BASE_DIR + "pesanan.json";
    private static final String TRANSAKSI_FILE = BASE_DIR + "transaksi.json";

    static {
        new File(BASE_DIR).mkdirs();
    }

    // -----------------------
    // LOAD MENU
    // -----------------------
    public static List<MenuItem> loadMenu() {
        List<MenuItem> hasil = new ArrayList<>();
        String json = JsonUtil.readFile(MENU_FILE);

        if (json.equals("{}")) {
            hasil = dummyMenu();
            try { saveMenu(hasil); } catch (IOException e) {}
            return hasil;
        }

        List<String> menuObjects = JsonUtil.parseArray(json, "menu");
        for (String menuObj : menuObjects) {
            try {
                String jenis = JsonUtil.getString(menuObj, "jenis");
                String nama = JsonUtil.getString(menuObj, "nama");
                double harga = JsonUtil.getDouble(menuObj, "harga");

                if (jenis == null || nama == null) continue;

                if ("makanan".equalsIgnoreCase(jenis)) {
                    String kategori = JsonUtil.getString(menuObj, "kategori");
                    String tingkatPedas = JsonUtil.getString(menuObj, "tingkat_pedas");
                    hasil.add(new Makanan(nama, harga, kategori, tingkatPedas));
                } else if ("minuman".equalsIgnoreCase(jenis)) {
                    String ukuran = JsonUtil.getString(menuObj, "ukuran");
                    String suhu = JsonUtil.getString(menuObj, "suhu");
                    hasil.add(new Minuman(nama, harga, ukuran, suhu));
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        if (hasil.isEmpty()) {
            hasil = dummyMenu();
            try { saveMenu(hasil); } catch (IOException e) {}
        }
        return hasil;
    }

    public static void saveMenu(List<MenuItem> menu) throws IOException {
        List<String> menuJsonList = new ArrayList<>();
        for (MenuItem m : menu) {
            if (m instanceof Makanan) {
                Makanan mm = (Makanan) m;
                menuJsonList.add(JsonUtil.jsonObject("jenis", "makanan", "nama", mm.getNama(), "harga", String.valueOf((int) mm.getHarga()), "kategori", mm.getKategori(), "tingkat_pedas", mm.getTingkatPedas()));
            } else if (m instanceof Minuman) {
                Minuman mn = (Minuman) m;
                menuJsonList.add(JsonUtil.jsonObject("jenis", "minuman", "nama", mn.getNama(), "harga", String.valueOf((int) mn.getHarga()), "ukuran", mn.getUkuran(), "suhu", mn.getSuhu()));
            }
        }
        JsonUtil.writeFile(MENU_FILE, JsonUtil.jsonWithRoot("menu", JsonUtil.jsonArray(menuJsonList)));
    }

    // -----------------------
    // LOAD PESANAN
    // -----------------------
    public static List<Pesanan> loadPesanan() {
        List<Pesanan> list = new ArrayList<>();
        String json = JsonUtil.readFile(PESANAN_FILE);

        if (json.equals("{}")) return list;

        List<String> pesananObjects = JsonUtil.parseArray(json, "pesanan");

        for (String pesananObj : pesananObjects) {
            try {
                int id = JsonUtil.getInt(pesananObj, "id");
                int meja = JsonUtil.getInt(pesananObj, "meja");
                String status = JsonUtil.getString(pesananObj, "status");

                if (status == null || status.isEmpty() || "LUNAS".equals(status)) continue;

                Pesanan pes = new Pesanan(id, new Meja(meja));
                pes.setStatus(status);

                // Parse items
                List<String> items = JsonUtil.parseArray(pesananObj, "items");
                for (String itemObj : items) {
                    try {
                        String nama = JsonUtil.getString(itemObj, "nama");
                        int jumlah = JsonUtil.getInt(itemObj, "jumlah");
                        String catatan = JsonUtil.getString(itemObj, "catatan");
                        if (catatan == null) catatan = "";

                        if (nama != null && jumlah > 0) {
                            MenuItem mi = findMenuByName(nama);
                            // Fallback jika menu dihapus dari database
                            if (mi == null) {
                                mi = new Makanan(nama + " [Data Hilang]", 0, "-", "-");
                            }
                            pes.tambahItem(new DetailPesanan(mi, jumlah, catatan));
                        }
                    } catch (Exception e) {
                        System.err.println("Gagal parse item: " + itemObj);
                    }
                }
                list.add(pes);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return list;
    }

    public static int loadLastId() {
        String json = JsonUtil.readFile(PESANAN_FILE);
        if (json.equals("{}")) return 1;
        int nextId = JsonUtil.getRootInt(json, "nextId");
        return (nextId == 0) ? 1 : nextId;
    }

    // -----------------------
    // SAVE PESANAN (PERBAIKAN MANUAL JSON)
    // -----------------------
    public static void savePesanan(List<Pesanan> list, int nextId) {
        try {
            StringBuilder sb = new StringBuilder();
            sb.append("{\n");
            sb.append("  \"nextId\": ").append(nextId).append(",\n");
            sb.append("  \"pesanan\": [\n");

            boolean firstOrder = true;
            for (Pesanan p : list) {
                if ("LUNAS".equals(p.getStatus())) continue;

                if (!firstOrder) sb.append(",\n");
                firstOrder = false;

                sb.append("    {\n");
                sb.append("      \"id\": ").append(p.getId()).append(",\n");
                sb.append("      \"meja\": ").append(p.getMeja().getNomor()).append(",\n");
                sb.append("      \"status\": \"").append(p.getStatus()).append("\",\n");
                sb.append("      \"items\": [");

                // Build items manual agar aman dari bug JsonUtil
                List<DetailPesanan> items = p.getItems();
                for (int i = 0; i < items.size(); i++) {
                    DetailPesanan d = items.get(i);
                    if (i > 0) sb.append(", ");
                    sb.append("{");
                    sb.append("\"nama\": \"").append(d.getMenu().getNama()).append("\", ");
                    sb.append("\"jumlah\": ").append(d.getJumlah()).append(", ");
                    String cat = d.getCatatan() == null ? "" : d.getCatatan();
                    sb.append("\"catatan\": \"").append(cat).append("\"");
                    sb.append("}");
                }

                sb.append("]\n");
                sb.append("    }");
            }
            sb.append("\n  ]\n");
            sb.append("}");

            JsonUtil.writeFile(PESANAN_FILE, sb.toString());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void saveTransaksi(Transaksi t) {
        try {
            String json = JsonUtil.readFile(TRANSAKSI_FILE);
            List<String> transaksiJsonList = new ArrayList<>();
            if (!json.equals("{}")) transaksiJsonList = JsonUtil.parseArray(json, "transaksi");

            transaksiJsonList.add(JsonUtil.jsonObject(
                    "idPesanan", String.valueOf(t.getPesanan().getId()),
                    "noMeja", String.valueOf(t.getPesanan().getMeja().getNomor()),
                    "total", String.valueOf((int) t.getTotal()),
                    "jenisPembayaran", t.getPembayaran().getJenis(),
                    "waktu", t.getWaktuFormatted(),
                    "status", "LUNAS"));

            JsonUtil.writeFile(TRANSAKSI_FILE, JsonUtil.jsonWithRoot("transaksi", JsonUtil.jsonArray(transaksiJsonList)));
        } catch (Exception e) { e.printStackTrace(); }
    }

    private static MenuItem findMenuByName(String nama) {
        List<MenuItem> menu = RestaurantSystem.getInstance().getMenuList();
        for (MenuItem m : menu) {
            if (m.getNama().equalsIgnoreCase(nama) || m.getNama().toLowerCase().contains(nama.toLowerCase()))
                return m;
        }
        return null;
    }

    private static List<MenuItem> dummyMenu() {
        List<MenuItem> d = new ArrayList<>();
        d.add(new Makanan("Mie Aceh", 25000, "Main Course", "Sedang"));
        d.add(new Makanan("Mie Aceh Kepiting", 45000, "Main Course", "Pedas"));
        d.add(new Minuman("Es Teh", 8000, "Medium", "Dingin"));
        return d;
    }
}