package com.eas.cards2;

import java.util.*;

public final class CardTreeOperationsTest {
    static int checks;
    static HashMap<String, Object> card(String id, boolean folder, HashMap<String, Object>... children) {
        HashMap<String, Object> item = new HashMap<>();
        item.put("id", id); item.put("folder", folder);
        if (folder) item.put("data", new ArrayList<>(Arrays.asList(children)));
        return item;
    }
    static Set<String> ids(String... ids) { return new LinkedHashSet<>(Arrays.asList(ids)); }
    static void check(boolean condition, String message) { checks++; if (!condition) throw new AssertionError(message); }
    public static void main(String[] args) {
        HashMap<String, Object> a=card("a",false), b=card("b",false), nested=card("nested",true,b), folder=card("folder",true,a,nested), other=card("other",true);
        ArrayList<HashMap<String, Object>> tree=new ArrayList<>(Arrays.asList(folder,other));
        check(CardTreeOperations.roots(tree,ids("folder","a","b")).equals(Arrays.asList(folder)),"Selected descendants must not be duplicated");
        check(!CardTreeOperations.move(tree,ids("folder"),"nested"),"Reject descendant cycle");
        check(!CardTreeOperations.move(tree,ids("folder"),"folder"),"Reject self cycle");
        check(CardTreeOperations.find(tree,"b")==b,"Rejected operations preserve descendants");
        check(!CardTreeOperations.move(tree,ids("a"),"missing"),"Reject missing target");
        check(!CardTreeOperations.move(tree,ids("a"),"b"),"Reject card as folder");
        check(CardTreeOperations.move(tree,ids("a","b"),"other"),"Move across nested containers");
        check(CardTreeOperations.children(other).equals(Arrays.asList(a,b)),"Keep source order in grouped move");
        check(CardTreeOperations.children(nested).isEmpty(),"Remove moved nested child");
        check(CardTreeOperations.move(tree,ids("other","a"),""),"Move folder to root with selected child");
        check(CardTreeOperations.children(other).size()==2 && tree.size()==2,"No duplicate descendant or root");
        check(CardTreeOperations.move(tree,ids("a"),"other"),"Moving to current parent is safe");
        check(CardTreeOperations.children(other).size()==2,"Same-parent move does not duplicate");
        check(CardTreeOperations.remove(tree,"other"),"Remove selected subtree");
        check(CardTreeOperations.find(tree,"a")==null && CardTreeOperations.find(tree,"b")==null,"Subtree deletion removes all descendants");
        check(!CardTreeOperations.remove(tree,"missing"),"Missing deletion is harmless");
        HashMap<String, Object> c=card("c",false), d=card("d",false);
        ArrayList<HashMap<String, Object>> source=new ArrayList<>(Arrays.asList(a,b,c,d));
        check(CardTreeOperations.reorder(source,ids("c","d"),"a"),"Reorder selected group");
        check(((Number)c.get("manual_order")).intValue()==0 && ((Number)d.get("manual_order")).intValue()==1 && ((Number)a.get("manual_order")).intValue()==2,"Preserve selected group order");
        check(!CardTreeOperations.reorder(source,ids("a","missing"),"b"),"No partial reorder for invisible selections");
        check(!CardTreeOperations.reorder(source,ids("a"),"a"),"Ignore drop onto selection");
        check(CardTreeOperations.reorder(source,ids("a","b"),"d",true),"Drop after last card");
        check(((Number)a.get("manual_order")).intValue()==2 && ((Number)b.get("manual_order")).intValue()==3,"Can place a group at end");
        source.add(folder);
        check(!CardTreeOperations.reorder(source,ids("a"),"folder"),"Do not reorder cards across folder section");
        check(CardTreeOperations.roots(tree,ids("missing")).isEmpty(),"Stale selection produces no roots");
        System.out.println("CardTreeOperations: " + checks + " checks passed");
    }
}
