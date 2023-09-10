package tech.qiya.importall.data;

import com.intellij.psi.PsiClass;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class CurrentNeedImportClassesData {
    //public static List<PsiClass[]> list;
    public static Map<String,PsiClass[]> map = new HashMap<>();

    //add key and value to map
    public static void add(String key, PsiClass[] value){
        //find key in map
        if(!map.containsKey(key)) {
            //if key exist, add value to the key
            map.put(key, value);
        }
        else {
            //don't update mpa
        }

    }

    //cear map
    public static void clear(){
        map.clear();
    }

    //remove one element from map

}
