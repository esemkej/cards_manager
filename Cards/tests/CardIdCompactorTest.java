package com.eas.cards2;

import java.io.*;
import java.nio.file.*;
import java.util.*;

public final class CardIdCompactorTest {
    static class MemoryStore implements CardIdCompactor.Store {
        boolean pending, fail, crash;
        long lastId = 45;
        public boolean pending() { return pending; }
        public void begin() { pending = true; }
        public void rollback() { pending = false; }
        public void finish(List<HashMap<String, Object>> cards, long lastId) throws IOException {
            if (crash) throw new AssertionError("Simulated process death");
            if (fail) throw new IOException("Simulated persistence failure");
            this.lastId = lastId;
            pending = false;
        }
    }

    public static void main(String[] args) throws Exception {
        Path base = Files.createTempDirectory("card-id-tests");
        try {
            Path dir = Files.createDirectory(base.resolve("normal"));
            MemoryStore store = new MemoryStore();
            HashMap<String, Object> high = card("45"), low = card("5"), folder = card("15");
            folder.put("folder", true);
            folder.put("data", new ArrayList<>(Arrays.asList(low)));
            List<HashMap<String, Object>> tree = new ArrayList<>(Arrays.asList(high, folder));
            photo(dir, "5", "low"); photo(dir, "45", "high");
            CardIdCompactor compactor = new CardIdCompactor(dir.toFile(), store);
            check(compactor.compact(tree), "gaps changed");
            check("0".equals(low.get("id")) && "1".equals(folder.get("id")) && "2".equals(high.get("id")), "global numeric ordering");
            check(tree.get(0) == high && tree.get(1) == folder, "display/tree ordering unchanged");
            check(read(dir, "0").equals("low") && read(dir, "2").equals("high"), "photos follow their cards");
            check(store.lastId == 2 && !compactor.compact(tree), "counter and idempotence");

            dir = Files.createDirectory(base.resolve("overlap"));
            store = new MemoryStore();
            tree = new ArrayList<>(Arrays.asList(card("2"), card("1")));
            photo(dir, "1", "first"); photo(dir, "2", "second");
            new CardIdCompactor(dir.toFile(), store).compact(tree);
            check(read(dir, "0").equals("first") && read(dir, "1").equals("second"), "overlapping old/new directories");

            dir = Files.createDirectory(base.resolve("rollback"));
            store = new MemoryStore(); store.fail = true;
            tree = new ArrayList<>(Arrays.asList(card("5")));
            photo(dir, "5", "original");
            try { new CardIdCompactor(dir.toFile(), store).compact(tree); throw new AssertionError("Expected failure"); }
            catch (IOException expected) { }
            check("5".equals(tree.get(0).get("id")) && read(dir, "5").equals("original") && !store.pending, "failed save rollback");

            store.fail = false; store.crash = true;
            try { new CardIdCompactor(dir.toFile(), store).compact(tree); }
            catch (AssertionError simulatedDeath) { }
            check(store.pending && read(dir, "0").equals("original"), "interrupted migration staged");
            new CardIdCompactor(dir.toFile(), store).recover();
            check(read(dir, "5").equals("original") && !store.pending, "startup restores old images");

            dir = Files.createDirectory(base.resolve("empty"));
            store = new MemoryStore();
            new CardIdCompactor(dir.toFile(), store).compact(new ArrayList<>());
            check(store.lastId == -1, "empty tree starts next ID at zero");
            tree = new ArrayList<>(Arrays.asList(card("999999999999999999999"), card(10.0)));
            new CardIdCompactor(dir.toFile(), store).compact(tree);
            check("1".equals(tree.get(0).get("id")) && "0".equals(tree.get(1).get("id")), "huge and numeric legacy IDs");

            dir = Files.createDirectory(base.resolve("occupied"));
            store = new MemoryStore();
            photo(dir, "0", "unassigned"); photo(dir, "5", "assigned");
            tree = new ArrayList<>(Arrays.asList(card("5")));
            try { new CardIdCompactor(dir.toFile(), store).compact(tree); throw new AssertionError("Expected collision"); }
            catch (IOException expected) { }
            check(read(dir, "0").equals("unassigned") && read(dir, "5").equals("assigned"), "unassigned photos preserved");
            try { CardIdCompactor.orderedItems(Arrays.asList(card("5"), card("5"))); throw new AssertionError("Expected duplicate rejection"); }
            catch (IllegalArgumentException expected) { }
            System.out.println("ID ordering, nested folders, photos, overlapping paths, rollback, restart recovery, empty trees, legacy IDs and collision checks passed.");
        } finally {
            try (java.util.stream.Stream<Path> paths = Files.walk(base)) {
                for (Path path : (Iterable<Path>) paths.sorted(Comparator.reverseOrder())::iterator) Files.delete(path);
            }
        }
    }
    static HashMap<String, Object> card(Object id) {
        HashMap<String, Object> card = new HashMap<>();
        card.put("id", id); card.put("folder", false);
        return card;
    }
    static void photo(Path dir, String id, String content) throws IOException {
        Path folder = Files.createDirectories(dir.resolve("card_images").resolve(id));
        Files.write(folder.resolve("photo.jpg"), content.getBytes("UTF-8"));
    }
    static String read(Path dir, String id) throws IOException {
        return new String(Files.readAllBytes(dir.resolve("card_images").resolve(id).resolve("photo.jpg")), "UTF-8");
    }
    static void check(boolean value, String message) {
        if (!value) throw new AssertionError(message);
    }
}
