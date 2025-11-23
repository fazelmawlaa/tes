package com.restaurant.service;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import com.restaurant.model.menu.*;
import com.restaurant.model.pesanan.*;
import com.restaurant.model.transaksi.*;
import com.restaurant.utils.JsonUtil;

public class FileStorageService {

    // Gunakan path yang konsisten
    private static final String BASE_DIR = System.getProperty("user.dir") + "/src/main/resources/data/";
    private static final String MENU_FILE = BASE_DIR + "menu.json";
    private static final String PESANAN_FILE = BASE_DIR + "pesanan.json";
    private static final String TRANSAKSI_FILE = BASE_DIR + "transaksi.json";

    static { new File(BASE_DIR).mkdirs(); }

    // --- LOAD MENU ---
    public static List<MenuItem> loadMenu() {
        List<MenuItem> hasil = new ArrayList<>();
        String json = JsonUtil.readFile(MENU_FILE);
        List<String> list = JsonUtil.parseArray(json, "menu");
        
        for (String obj : list) {
            String jenis = JsonUtil.getString(obj, "jenis");
            String nama = JsonUtil.getString(obj, "nama");
            double harga = JsonUtil.getDouble(obj, "harga");
            
            if("makanan".equalsIgnoreCase(jenis)) {
                hasil.add(new Makanan(nama, harga, JsonUtil.getString(obj, "kategori"), JsonUtil.getString(obj, "tingkat_pedas")));
            } else {
                hasil.add(new Minuman(nama, harga, JsonUtil.getString(obj, "ukuran"), JsonUtil.getString(obj, "suhu")));
            }
        }
        return hasil;
    }

    // --- LOAD PESANAN (FIXED) ---
    public static List<Pesanan> loadPesanan() {
        List<Pesanan> list = new ArrayList<>();
        String json = JsonUtil.readFile(PESANAN_FILE);
        
        // 1. Ambil array pesanan utama
        List<String> pesananObjs = JsonUtil.parseArray(json, "pesanan");

        for (String pObj : pesananObjs) {
            int id = JsonUtil.getInt(pObj, "id");
            int meja = JsonUtil.getInt(pObj, "meja");
            String status = JsonUtil.getString(pObj, "status");
            
            if(status.isEmpty()) continue;

            Pesanan p = new Pesanan(id, new Meja(meja));
            p.setStatus(status);

            // 2. Ambil array items DI DALAM pesanan
            List<String> itemObjs = JsonUtil.parseArray(pObj, "items");
            
            for (String iObj : itemObjs) {
                String namaMenu = JsonUtil.getString(iObj, "nama");
                int jumlah = JsonUtil.getInt(iObj, "jumlah");
                String catatan = JsonUtil.getString(iObj, "catatan");
                
                // Cari object menu asli berdasarkan nama
                MenuItem mi = findMenuByName(namaMenu);
                if (mi != null) {
                    p.tambahItem(new DetailPesanan(mi, jumlah, catatan));
                } else {
                    // Fallback jika menu dihapus tapi ada di pesanan lama
                    p.tambahItem(new DetailPesanan(new Makanan(namaMenu, 0, "-", "-"), jumlah, catatan));
                }
            }
            list.add(p);
        }
        return list;
    }

    // --- SAVE PESANAN (MANUAL JSON BUILDER) ---
    public static void savePesanan(List<Pesanan> list, int nextId) {
        StringBuilder sb = new StringBuilder();
        sb.append("{\n");
        sb.append("  \"nextId\": ").append(nextId).append(",\n");
        sb.append("  \"pesanan\": [\n");

        for (int i = 0; i < list.size(); i++) {
            Pesanan p = list.get(i);
            
            // Skip yang sudah lunas (opsional, biar file ga penuh)
            // if ("LUNAS".equals(p.getStatus())) continue;

            sb.append("    {\n");
            sb.append("      \"id\": ").append(p.getId()).append(",\n");
            sb.append("      \"meja\": ").append(p.getMeja().getNomor()).append(",\n");
            sb.append("      \"status\": \"").append(p.getStatus()).append("\",\n");
            sb.append("      \"items\": [");
            
            // Loop items
            List<DetailPesanan> items = p.getItems();
            for (int j = 0; j < items.size(); j++) {
                DetailPesanan d = items.get(j);
                if (j > 0) sb.append(", "); // Koma antar item
                
                sb.append("{");
                sb.append("\"nama\": \"").append(JsonUtil.escape(d.getMenu().getNama())).append("\", ");
                sb.append("\"jumlah\": ").append(d.getJumlah()).append(", ");
                sb.append("\"catatan\": \"").append(JsonUtil.escape(d.getCatatan())).append("\"");
                sb.append("}");
            }
            
            sb.append("]\n"); // Tutup items
            sb.append("    }"); // Tutup pesanan
            
            // Koma antar pesanan (kecuali yang terakhir)
            if (i < list.size() - 1) sb.append(",\n");
        }
        
        sb.append("\n  ]\n"); // Tutup array pesanan
        sb.append("}"); // Tutup root

        JsonUtil.writeFile(PESANAN_FILE, sb.toString());
    }

    public static int loadLastId() {
        String json = JsonUtil.readFile(PESANAN_FILE);
        int val = JsonUtil.getRootInt(json, "nextId");
        return (val == 0) ? 1 : val;
    }
    
    public static void saveTransaksi(Transaksi t) {
        List<String> list = new ArrayList<>();
        String json = JsonUtil.readFile(TRANSAKSI_FILE);
        if(!json.equals("{}")) list = JsonUtil.parseArray(json, "transaksi");
        
        String obj = JsonUtil.jsonObject(
            "idPesanan", String.valueOf(t.getPesanan().getId()),
            "total", String.valueOf(t.getTotal()),
            "metode", t.getPembayaran().getJenis(),
            "waktu", t.getWaktuFormatted()
        );
        list.add(obj);
        JsonUtil.writeFile(TRANSAKSI_FILE, JsonUtil.jsonWithRoot("transaksi", JsonUtil.jsonArray(list)));
    }

    private static MenuItem findMenuByName(String nama) {
        List<MenuItem> menus = loadMenu(); 
        for (MenuItem m : menus) {
            if (m.getNama().equalsIgnoreCase(nama)) return m;
        }
        return null;
    }

    public static void saveMenu(List<MenuItem> menu) {
        // Implementasi save menu jika dibutuhkan
    }
}