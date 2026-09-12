package com.eas.cards2;

import java.io.*;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.*;

final class CardIdCompactor {
    interface Store {
        boolean pending();
        void begin() throws IOException;
        void finish(List<HashMap<String, Object>> cards, long lastId) throws IOException;
        void rollback() throws IOException;
    }

    private final File root, staging, backup;
    private final Store store;

    CardIdCompactor(File filesDir, Store store) {
        root = new File(filesDir, "card_images");
        staging = new File(filesDir, "card_images_reindex_staging");
        backup = new File(filesDir, "card_images_reindex_backup");
        this.store = store;
    }

    void recover() throws IOException {
        if (store.pending()) {
            if (backup.exists()) {
                delete(root);
                move(backup, root);
            }
            store.rollback();
        }
        delete(staging);
        delete(backup);
    }

    static List<HashMap<String, Object>> orderedItems(List<HashMap<String, Object>> cards) {
        List<HashMap<String, Object>> items = new ArrayList<>();
        collect(cards, items);
        Collections.sort(items, (a, b) -> numericId(a.get("id")).compareTo(numericId(b.get("id"))));
        Set<BigInteger> seen = new HashSet<>();
        for (HashMap<String, Object> item : items) {
            if (!seen.add(numericId(item.get("id"))))
                throw new IllegalArgumentException("Duplicate card ID: " + item.get("id"));
        }
        return items;
    }

    private static BigInteger numericId(Object id) {
        BigInteger value = new BigDecimal(String.valueOf(id)).toBigIntegerExact();
        if (value.signum() < 0) throw new IllegalArgumentException("Negative card ID");
        return value;
    }

    @SuppressWarnings("unchecked")
    private static void collect(List<HashMap<String, Object>> cards, List<HashMap<String, Object>> out) {
        for (HashMap<String, Object> card : cards) {
            out.add(card);
            if (Boolean.TRUE.equals(card.get("folder")) && card.get("data") instanceof List)
                collect((List<HashMap<String, Object>>) card.get("data"), out);
        }
    }

    boolean compact(List<HashMap<String, Object>> cards) throws IOException {
        List<HashMap<String, Object>> items = orderedItems(cards);
        Map<String, String> mapping = new LinkedHashMap<>();
        List<Object> oldIds = new ArrayList<>();
        boolean changed = false;
        for (int i = 0; i < items.size(); i++) {
            Object old = items.get(i).get("id");
            String oldName = String.valueOf(old), newName = Integer.toString(i);
            if (!oldName.matches("[0-9]+(?:\\.0+)?"))
                throw new IllegalArgumentException("Unsupported card ID: " + oldName);
            mapping.put(oldName, newName);
            oldIds.add(old);
            changed |= !newName.equals(old);
        }
        if (!changed) {
            store.finish(cards, items.size() - 1L);
            return false;
        }

        recover();
        try {
            if (root.exists()) {
                if (!staging.mkdir()) throw new IOException("Cannot stage card images");
                File[] entries = root.listFiles();
                if (entries == null) throw new IOException("Cannot list card images");
                for (File entry : entries) {
                    String destination = mapping.get(entry.getName());
                    if (destination == null) {
                        if (mapping.containsValue(entry.getName()))
                            throw new IOException("An unassigned image directory occupies ID " + entry.getName());
                        destination = entry.getName();
                    }
                    copy(entry, new File(staging, destination));
                }
            }
            store.begin();
            if (root.exists()) {
                move(root, backup);
                move(staging, root);
            }
            for (int i = 0; i < items.size(); i++) items.get(i).put("id", Integer.toString(i));
            store.finish(cards, items.size() - 1L);
        } catch (IOException | RuntimeException failure) {
            for (int i = 0; i < items.size(); i++) items.get(i).put("id", oldIds.get(i));
            try {
                recover();
            } catch (IOException recoveryFailure) {
                throw new IllegalStateException("Card ID migration needs recovery", recoveryFailure);
            }
            throw failure;
        }
        // A committed migration can leave its backup for the next startup if cleanup fails.
        try { delete(backup); } catch (IOException ignored) { }
        return true;
    }

    private static void move(File from, File to) throws IOException {
        if (!from.renameTo(to)) throw new IOException("Cannot rename " + from + " to " + to);
    }

    private static void copy(File from, File to) throws IOException {
        if (from.isDirectory()) {
            if (!to.mkdir()) throw new IOException("Cannot create " + to);
            File[] children = from.listFiles();
            if (children == null) throw new IOException("Cannot list " + from);
            for (File child : children) copy(child, new File(to, child.getName()));
        } else {
            try (FileInputStream in = new FileInputStream(from); FileOutputStream out = new FileOutputStream(to)) {
                byte[] buffer = new byte[8192];
                int count;
                while ((count = in.read(buffer)) != -1) out.write(buffer, 0, count);
                out.getFD().sync();
            }
        }
    }

    private static void delete(File file) throws IOException {
        if (!file.exists()) return;
        if (file.isDirectory()) {
            File[] children = file.listFiles();
            if (children == null) throw new IOException("Cannot list " + file);
            for (File child : children) delete(child);
        }
        if (!file.delete()) throw new IOException("Cannot delete " + file);
    }
}
