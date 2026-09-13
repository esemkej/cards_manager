package com.eas.cards2;

import java.io.*;
import java.util.*;

/** Prepare a complete independent copy before committing any card data. */
final class CardCopies {
    static final class Result {
        final ArrayList<HashMap<String, Object>> tree;
        long lastId;
        final ArrayList<File> createdDirectories = new ArrayList<>();
        final ArrayList<File> published = new ArrayList<>();
        final File images, staging;
        Result(ArrayList<HashMap<String, Object>> tree, long lastId, File images) {
            this.tree = tree; this.lastId = lastId; this.images = images;
            staging = new File(images.getParentFile(), "card-copy-" + UUID.randomUUID());
        }
        void publish() throws IOException {
            if (!createdDirectories.isEmpty() && !images.isDirectory() && !images.mkdirs()) throw new IOException("Cannot create image root");
            for (File dir : createdDirectories) {
                File target = new File(images, dir.getName());
                if (target.exists() || !dir.renameTo(target)) throw new IOException("Cannot publish copied images");
                published.add(target);
            }
        }
        void finish() { erase(staging); }
        void rollback() { for (File dir : published) erase(dir); erase(staging); }
    }
    @SuppressWarnings("unchecked")
    static <T> T snapshot(T value) {
        if (value instanceof Map) {
            HashMap<String, Object> map = new HashMap<>();
            for (Map.Entry<String, Object> entry : ((Map<String, Object>) value).entrySet()) map.put(entry.getKey(), snapshot(entry.getValue()));
            return (T) map;
        }
        if (value instanceof List) {
            ArrayList<Object> list = new ArrayList<>();
            for (Object item : (List<?>) value) list.add(snapshot(item));
            return (T) list;
        }
        return value;
    }
    static Result prepare(ArrayList<HashMap<String, Object>> tree, Set<String> ids, String targetId,
                          HashMap<String, Object> newFolder, long lastId, File images) throws IOException {
        Result result = new Result(snapshot(tree), lastId, images);
        try {
            HashMap<String, Object> target = targetId.isEmpty() ? null : CardTreeOperations.find(result.tree, targetId);
            if (!targetId.isEmpty() && (target == null || !Boolean.TRUE.equals(target.get("folder")))) throw new IOException("Invalid destination");
            ArrayList<HashMap<String, Object>> sources = CardTreeOperations.roots(result.tree, ids);
            if (sources.isEmpty()) throw new IOException("Missing source");
            ArrayList<HashMap<String, Object>> copies = new ArrayList<>();
            for (HashMap<String, Object> source : sources) copies.add(copyItem(source, result, images));
            List<HashMap<String, Object>> destination = target == null ? result.tree : new ArrayList<>(CardTreeOperations.children(target));
            if (newFolder != null) {
                HashMap<String, Object> folder = snapshot(newFolder);
                folder.put("id", nextId(result, images));
                folder.put("data", copies);
                append(destination, Collections.singletonList(folder));
            } else append(destination, copies);
            if (target != null) target.put("data", destination);
            return result;
        } catch (IOException | RuntimeException failure) {
            result.rollback(); throw failure;
        }
    }
    private static String nextId(Result result, File images) throws IOException {
        while (result.lastId < Long.MAX_VALUE) {
            String id = String.valueOf(++result.lastId);
            if (CardTreeOperations.find(result.tree, id) == null && !new File(images, id).exists()) return id;
        }
        throw new IOException("No available IDs");
    }
    private static HashMap<String, Object> copyItem(HashMap<String, Object> source, Result result, File images) throws IOException {
        HashMap<String, Object> copy = snapshot(source);
        String newId = nextId(result, images);
        copy.put("id", newId);
        if (source.get("data") instanceof List) {
            ArrayList<HashMap<String, Object>> children = new ArrayList<>();
            for (HashMap<String, Object> child : CardTreeOperations.children(source)) children.add(copyItem(child, result, images));
            copy.put("data", children);
        }
        if (source.get("images") instanceof List && !((List<?>) source.get("images")).isEmpty()) {
            File directory = new File(result.staging, newId);
            if (!directory.mkdirs()) throw new IOException("Cannot create image directory");
            result.createdDirectories.add(directory);
            File original = new File(images, CardTreeOperations.id(source));
            if (!original.getCanonicalFile().getParentFile().equals(images.getCanonicalFile())) throw new IOException("Invalid image owner");
            byte[] buffer = new byte[32768];
            for (Object image : (List<?>) source.get("images")) {
                String name = String.valueOf(image);
                File input = new File(original, name);
                if (!input.getCanonicalFile().getParentFile().equals(original.getCanonicalFile())
                        || name.contains("/") || name.contains("\\")) throw new IOException("Invalid image path");
                try (InputStream in = new FileInputStream(input); OutputStream out = new FileOutputStream(new File(directory, name))) {
                    int count;
                    while ((count = in.read(buffer)) != -1) out.write(buffer, 0, count);
                }
            }
        }
        return copy;
    }
    private static void append(List<HashMap<String, Object>> destination, List<HashMap<String, Object>> copies) {
        double order = -1;
        for (HashMap<String, Object> item : destination) if (item.get("manual_order") instanceof Number)
            order = Math.max(order, ((Number) item.get("manual_order")).doubleValue());
        for (HashMap<String, Object> item : copies) { item.put("manual_order", ++order); destination.add(item); }
    }
    private static void erase(File file) {
        File[] children = file.listFiles();
        if (children != null) for (File child : children) erase(child);
        file.delete();
    }
}
