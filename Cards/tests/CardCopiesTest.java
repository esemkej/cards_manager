package com.eas.cards2;
import java.io.*;
import java.nio.file.*;
import java.util.*;
public final class CardCopiesTest {
    static int checks;
    static void check(boolean value, String reason) { checks++; if (!value) throw new AssertionError(reason); }
    static HashMap<String,Object> item(String id, boolean folder) {
        HashMap<String,Object> m = new HashMap<>(); m.put("id",id); m.put("folder",folder); m.put("name","Original");
        if(folder) m.put("data",new ArrayList<HashMap<String,Object>>()); return m;
    }
    public static void main(String[] args) throws Exception {
        File dir = Files.createTempDirectory("card-copy-test").toFile(); File images = new File(dir,"images");
        try {
            HashMap<String,Object> card = item("1",false), folder = item("2",true);
            card.put("images",new ArrayList<>(Arrays.asList("photo.jpg"))); card.put("favorite",true);
            card.put("code","12345"); card.put("type","CODE_128");
            CardTreeOperations.children(folder).add(card);
            ArrayList<HashMap<String,Object>> tree = new ArrayList<>(Arrays.asList(folder));
            new File(images,"1").mkdirs(); Files.write(new File(images,"1/photo.jpg").toPath(),new byte[]{1,2,3});
            Set<String> ids = new HashSet<>(Arrays.asList("1","2"));
            CardCopies.Result result = CardCopies.prepare(tree,ids,"",null,2,images);
            check(tree.size()==1,"Original tree unchanged");
            check(result.tree.size()==2,"Selected descendants only copied once");
            HashMap<String,Object> copyFolder = result.tree.get(1), copy = CardTreeOperations.children(copyFolder).get(0);
            check(!copyFolder.get("id").equals("2") && !copy.get("id").equals("1"),"Fresh IDs recursively");
            check(copy.get("code").equals("12345") && Boolean.TRUE.equals(copy.get("favorite")),"Metadata retained");
            ((List<?>)copy.get("images")).clear();
            check(((List<?>)card.get("images")).size()==1,"Independent mutable metadata");
            check(!new File(images,copy.get("id")+"/photo.jpg").exists(),"Files staged until commit");
            result.publish(); result.finish();
            File copiedImage = new File(images,copy.get("id")+"/photo.jpg");
            check(Arrays.equals(Files.readAllBytes(copiedImage.toPath()),new byte[]{1,2,3}),"Photo contents copied");
            Files.write(copiedImage.toPath(),new byte[]{9});
            check(Files.readAllBytes(new File(images,"1/photo.jpg").toPath()).length==3,"Independent photo files");
            result.rollback(); check(!copiedImage.exists() && new File(images,"1/photo.jpg").exists(),"Rollback preserves source");
            CardCopies.Result nested = CardCopies.prepare(tree,ids,"2",null,2,images);
            check(CardTreeOperations.children(nested.tree.get(0)).size()==2,"Can copy into source folder without recursion"); nested.rollback();
            CardCopies.Result newFolder = CardCopies.prepare(tree,Collections.singleton("1"),"",item("unused",true),2,images);
            check(CardTreeOperations.children(newFolder.tree.get(1)).size()==1,"Copy into new folder"); newFolder.rollback();
            card.put("images",Arrays.asList("photo.jpg","missing.jpg"));
            boolean failed=false; try { CardCopies.prepare(tree,ids,"",null,2,images); } catch(IOException expected) { failed=true; }
            check(failed && tree.size()==1 && dir.listFiles().length==1,"Failed file copy rolls back staging");
            failed=false; try { CardCopies.prepare(tree,ids,"1",null,2,images); } catch(IOException expected) { failed=true; }
            check(failed,"Reject card as destination");
            System.out.println("Card copying: "+checks+" checks passed");
        } finally { erase(dir); }
    }
    static void erase(File file) { File[] children=file.listFiles(); if(children!=null) for(File c:children) erase(c); file.delete(); }
}
