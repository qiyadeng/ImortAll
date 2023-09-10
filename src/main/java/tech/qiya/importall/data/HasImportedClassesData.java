package tech.qiya.importall.data;

import com.intellij.psi.PsiClass;

import java.util.HashMap;
import java.util.Map;

public class HasImportedClassesData {
    public static Map<String, String> map = new HashMap<>();

    //add key and value to map
    public static void add(String key, String value){
        //find key in map
        if(!map.containsKey(key)) {
            //if key exist, add value to the key
            map.put(key, value);
        }
        else {
            //don't update mpa
        }

    }

    //clear map
    public static void clear(){
        map.clear();
    }
}
