package com.eas.cards2;

import com.google.gson.Gson;
import com.google.gson.JsonParser;
import com.google.gson.reflect.TypeToken;
import java.io.IOException;
import java.math.BigDecimal;
import java.util.*;

/** Validate before persistence, so failed imports cannot change cards or their sort. */
final class CardImport {
    interface Store { boolean save(Prepared imported); }
    static final class Prepared {
        final ArrayList<HashMap<String, Object>> cards;
        final String json, sort;
        final long lastId;
        Prepared(ArrayList<HashMap<String, Object>> cards, String sort, long lastId) {
            this.cards = cards; this.json = new Gson().toJson(cards); this.sort = sort; this.lastId = lastId;
        }
    }
    static Prepared apply(String json, String previousSort, Store store) throws IOException {
        if (!JsonParser.parseString(json).isJsonArray()) throw new IllegalArgumentException("Expected a card array");
        ArrayList<HashMap<String, Object>> tree = new Gson().fromJson(json,
                new TypeToken<ArrayList<HashMap<String, Object>>>() {}.getType());
        normalize(tree);
        long highest = -1;
        for (HashMap<String, Object> card : CardIdCompactor.orderedItems(tree)) {
            highest = Math.max(highest, new BigDecimal(String.valueOf(card.get("id"))).longValueExact());
        }
        Prepared result = new Prepared(tree, "manual".equals(previousSort) ? "name" : previousSort, highest);
        if (!store.save(result)) throw new IOException("Could not persist imported cards");
        return result;
    }
    private static void normalize(ArrayList<HashMap<String, Object>> items) {
        for (HashMap<String, Object> item : items) {
            if (item == null) throw new IllegalArgumentException("Null card");
            boolean folder = Boolean.TRUE.equals(item.get("folder")) || "true".equals(item.get("folder"));
            item.put("folder", folder);
            if (folder) {
                Object raw = item.get("data");
                if (!(raw instanceof List)) throw new IllegalArgumentException("Expected folder contents");
                ArrayList<HashMap<String, Object>> children = new ArrayList<>();
                for (Object child : (List<?>) raw) {
                    if (!(child instanceof Map)) throw new IllegalArgumentException("Invalid folder child");
                    @SuppressWarnings("unchecked") Map<String, Object> map = (Map<String, Object>) child;
                    children.add(new HashMap<>(map));
                }
                normalize(children); item.put("data", children);
            }
        }
    }
    private CardImport() {}
}
