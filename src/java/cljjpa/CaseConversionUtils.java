package cljjpa;

/**
 * @author kawasima
 */
public class CaseConversionUtils {

    public static String toKebab(String camelString) {
        if (camelString == null) {
            return null;
        }

        StringBuilder sb = new StringBuilder();
        int pos = 0;
        for (int i = 1; i < camelString.length(); i++) {
            if (Character.isUpperCase(camelString.charAt(i))) {
                if (sb.length() != 0) {
                    sb.append('-');
                }
                sb.append(camelString.substring(pos, i).toLowerCase());
                pos = i;
            }
        }
        if (sb.length() != 0) {
            sb.append('-');
        }
        sb.append(camelString.substring(pos, camelString.length()).toLowerCase());
        return sb.toString();
    }

    public static String capitalize(String s) {
        if (s == null || s.length() == 0) {
            return s;
        }

        return s.charAt(0) + s.substring(1);

    }

    public static String toCamel(String kebabString) {
        if (kebabString == null) {
            return null;
        }

        String s = kebabString.toLowerCase();
        String[] array = s.split("-");
        if (array.length == 1) {
            return capitalize(s);
        }

        StringBuilder sb = new StringBuilder();
        for (int i=0; i<array.length; i++) {
            sb.append(capitalize(array[i]));
        }
        return sb.toString();
    }
}
