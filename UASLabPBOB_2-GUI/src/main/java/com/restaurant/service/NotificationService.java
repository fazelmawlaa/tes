package com.restaurant.service;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import com.restaurant.model.OrderEvent;

/**
 * Service untuk mengelola notifikasi antar role
 * Mencatat semua event perubahan status pesanan
 */
public class NotificationService {

    private static final NotificationService instance = new NotificationService();

    public static NotificationService getInstance() {
        return instance;
    }

    private NotificationService() {
        events = new ArrayList<>();
    }

    private List<OrderEvent> events = new ArrayList<>();

    /**
     * Menambahkan event baru ketika status pesanan berubah
     */
    public void addEvent(OrderEvent event) {
        events.add(event);
        // Batasi jumlah event yang disimpan (hanya 100 terakhir)
        if (events.size() > 100) {
            events.remove(0);
        }
    }

    /**
     * Mendapatkan semua event untuk pesanan tertentu
     */
    public List<OrderEvent> getEventsForPesanan(int pesananId) {
        return events.stream()
                .filter(e -> e.getPesananId() == pesananId)
                .collect(Collectors.toList());
    }

    /**
     * Mendapatkan notifikasi terbaru untuk role tertentu
     * 
     * @param targetRole role yang akan menerima notifikasi
     * @param limit      jumlah notifikasi terbaru yang diambil
     */
    public List<String> getNotificationsForRole(String targetRole, int limit) {
        return events.stream()
                .filter(e -> e.getNotificationForRole(targetRole) != null)
                .sorted((e1, e2) -> e2.getTimestamp().compareTo(e1.getTimestamp())) // terbaru dulu
                .limit(limit)
                .map(e -> e.getNotificationForRole(targetRole))
                .collect(Collectors.toList());
    }

    /**
     * Mendapatkan notifikasi terbaru untuk role tertentu (default 5 terbaru)
     */
    public List<String> getNotificationsForRole(String targetRole) {
        return getNotificationsForRole(targetRole, 5);
    }

    /**
     * Mendapatkan jumlah notifikasi baru untuk role tertentu
     */
    public int getUnreadNotificationCount(String targetRole, int lastCheckedIndex) {
        List<String> notifications = getNotificationsForRole(targetRole, 100);
        return Math.max(0, notifications.size() - lastCheckedIndex);
    }

    /**
     * Mendapatkan semua event
     */
    public List<OrderEvent> getAllEvents() {
        return new ArrayList<>(events);
    }

    /**
     * Menghapus semua event (untuk testing/reset)
     */
    public void clearAll() {
        events.clear();
    }
}
