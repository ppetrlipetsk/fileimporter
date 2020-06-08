package typeslib;

public class TransLiterateClass {

    public static String getTranslitStr(String s){
        StringBuilder dest=new StringBuilder();
        for(int i = 0, n = s.length() ; i < n ; i++) {
            char c = s.charAt(i);
            dest.append(CodeClass.getSymbol(new Character(c).toString()));
        }
        return dest.toString();
    }


}
