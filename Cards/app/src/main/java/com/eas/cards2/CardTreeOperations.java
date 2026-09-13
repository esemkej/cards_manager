package com.eas.cards2;

import java.util.*;

/** Pure tree operations shared by bulk actions and drag/drop. */
final class CardTreeOperations {
    private CardTreeOperations() {}
    static String id(Map<String, Object> item) { return String.valueOf(item.get("id")); }
    @SuppressWarnings("unchecked")
    static List<HashMap<String, Object>> children(HashMap<String, Object> item) {
        Object data = item.get("data");
        return data instanceof List ? (List<HashMap<String, Object>>) data : Collections.emptyList();
    }
    static HashMap<String, Object> find(List<HashMap<String, Object>> tree, String id) {
        for (HashMap<String, Object> item : tree) {
            if (id(item).equals(id)) return item;
            HashMap<String, Object> found = find(children(item), id);
            if (found != null) return found;
        }
        return null;
    }
    static ArrayList<HashMap<String, Object>> roots(List<HashMap<String, Object>> tree, Set<String> ids) {
        ArrayList<HashMap<String, Object>> result = new ArrayList<>();
        for (HashMap<String, Object> item : tree) {
            if (ids.contains(id(item))) result.add(item);
            else result.addAll(roots(children(item), ids));
        }
        return result;
    }
    static boolean remove(List<HashMap<String, Object>> tree, String id) {
        for (Iterator<HashMap<String, Object>> it = tree.iterator(); it.hasNext();) {
            HashMap<String, Object> item = it.next();
            if (id(item).equals(id)) { it.remove(); return true; }
            if (remove(children(item), id)) return true;
        }
        return false;
    }
    static boolean move(ArrayList<HashMap<String, Object>> tree, Set<String> ids, String targetId) {
        HashMap<String, Object> target = targetId.isEmpty() ? null : find(tree, targetId);
        if (!targetId.isEmpty() && (target == null || !Boolean.TRUE.equals(target.get("folder")))) return false;
        ArrayList<HashMap<String, Object>> items = roots(tree, ids);
        if (items.isEmpty()) return false;
        for (HashMap<String, Object> item : items) {
            if (id(item).equals(targetId) || find(children(item), targetId) != null) return false;
        }
        for (HashMap<String, Object> item : items) remove(tree, id(item));
        List<HashMap<String, Object>> contents = target == null ? tree : new ArrayList<>(children(target));
        double lastOrder = -1;
        for (HashMap<String, Object> item : contents) {
            if (item.get("manual_order") instanceof Number) lastOrder = Math.max(lastOrder, ((Number)item.get("manual_order")).doubleValue());
        }
        for (HashMap<String, Object> item : items) item.put("manual_order", ++lastOrder);
        contents.addAll(items);
        if (target != null) target.put("data", contents);
        return true;
    }
    static boolean reorder(List<HashMap<String, Object>> source, Set<String> ids, String beforeId) {
        return reorder(source, ids, beforeId, false);
    }
    static boolean reorder(List<HashMap<String, Object>> source, Set<String> ids, String beforeId, boolean after) {
        HashMap<String, Object> target = null;
        ArrayList<HashMap<String, Object>> group = new ArrayList<>();
        for (HashMap<String, Object> item : source) {
            if (id(item).equals(beforeId)) target = item;
            if (ids.contains(id(item))) group.add(item);
        }
        if (target == null || group.size() != ids.size() || ids.contains(beforeId)) return false;
        for (HashMap<String, Object> item : group) if (!Objects.equals(item.get("folder"), target.get("folder"))) return false;
        ArrayList<HashMap<String, Object>> ordered = new ArrayList<>(source);
        ordered.removeAll(group); ordered.addAll(ordered.indexOf(target) + (after ? 1 : 0), group);
        for (int i=0; i<ordered.size(); i++) ordered.get(i).put("manual_order", (double)i);
        return true;
    }
}
