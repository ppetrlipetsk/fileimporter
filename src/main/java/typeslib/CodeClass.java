package typeslib;

import java.util.HashMap;
import java.util.Map;

public class CodeClass{
    static String[] cir={"а","б","в","г","д","е","ё","ж","з","и","й","к","л","м","н","о","п","р","с","т","у","ф","х","ц","ч","ш","щ","ь","ы","ъ","э","ю","я"};
    static String[] lat={"a","b","v","g","d","e","e","zh","z","i","i","k","l","m","n","o","p","r","s","t","u","f","h","ts","ch","sh","sh","","iy","","e","yu","ya"};
    static Map<String,String> charCodeCirLat;


    static {
        charCodeCirLat =new HashMap();
        fillCharCode();
    }

    public CodeClass(){
        charCodeCirLat =new HashMap();
        fillCharCode();
    }

    private static void fillCharCode() {
        for(int i=0;i<cir.length;i++){
            charCodeCirLat.put(cir[i],lat[i]);
        }
    }

    public static String getSymbol(String s){
        if (charCodeCirLat.containsKey(s))
        return charCodeCirLat.get(s);
        else
            return s;
    }
}
