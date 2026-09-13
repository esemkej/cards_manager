package com.eas.cards2;

import java.io.IOException;

public final class CardImportTest {
    private static int checks;
    private static final class Store implements CardImport.Store {
        String json = "original cards", sort = "manual";
        long lastId = 42;
        boolean fail;
        int writes;
        public boolean save(CardImport.Prepared imported) {
            writes++;
            if (fail) return false;
            json = imported.json; sort = imported.sort; lastId = imported.lastId;
            return true;
        }
    }
    private static void check(boolean value, String description) {
        checks++; if (!value) throw new AssertionError(description);
    }
    public static void main(String[] args) throws Exception {
        String nested = "[{\"id\":\"3\",\"name\":\"Folder\",\"folder\":true,\"data\":[{\"id\":\"8\",\"name\":\"Card\",\"folder\":false}]}]";
        Store store = new Store();
        CardImport.Prepared imported = CardImport.apply(nested, store.sort, store);
        check("name".equals(store.sort), "Manual resets to Name on successful import");
        check(store.lastId == 8 && imported.cards.size() == 1, "Nested IDs determine next available ID");
        check(CardTreeOperations.find(imported.cards, "8") != null, "Nested cards remain usable by bulk actions");
        check(store.writes == 1, "Cards, ID and sort are committed together");
        for (String sort : new String[]{"name", "date", "uses"}) {
            store = new Store(); store.sort = sort;
            CardImport.apply(nested, sort, store);
            check(sort.equals(store.sort), "Non-manual sort survives: " + sort);
        }
        store = new Store(); CardImport.apply("[]", "manual", store);
        check(store.lastId == -1 && "name".equals(store.sort), "Empty backup is a successful import");
        for (String invalid : new String[]{"not json", "null", "{}", "[null]", "[{\"id\":-1}]", "[{\"id\":1},{\"id\":1}]", "[{\"id\":1,\"folder\":true,\"data\":{}}]", "[{\"id\":1,\"folder\":true,\"data\":[null]}]", "[{\"id\":\"9223372036854775808\"}]"}) {
            store = new Store();
            boolean rejected = false;
            try { CardImport.apply(invalid, store.sort, store); } catch (RuntimeException expected) { rejected = true; }
            check(rejected && store.writes == 0, "Invalid imports are rejected before persistence: " + invalid);
            check("manual".equals(store.sort) && "original cards".equals(store.json) && store.lastId == 42,
                    "Failed import preserves original cards, sort and ID");
        }
        store = new Store(); store.fail = true;
        boolean failed = false;
        try { CardImport.apply(nested, store.sort, store); } catch (IOException expected) { failed = true; }
        check(failed && "manual".equals(store.sort) && store.lastId == 42, "A failed save cannot report successful import");
        System.out.println("CardImport: " + checks + " checks passed");
    }
}
