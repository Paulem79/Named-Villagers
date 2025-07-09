package ovh.paulem.namedvillagers.utils;

import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

public class Names {
    public static String randomFromList(List<String> list) {
        if (list.isEmpty()) {
            return "";
        }
        return list.get(ThreadLocalRandom.current().nextInt(list.size()));
    }

    public static String capitalizeEveryWord(String text) {
        if (text.isEmpty()) {
            return text;
        }

        char[] chars = text.toLowerCase().toCharArray();
        boolean found = false;
        for (int i = 0; i < chars.length; i++) {
            if (!found && Character.isLetter(chars[i])) {
                chars[i] = Character.toUpperCase(chars[i]);
                found = true;
            }
            else if (!(Character.isDigit(chars[i]) || Character.isLetter(chars[i]))) {
                found = false;
            }
        }
        return String.valueOf(chars);
    }
}