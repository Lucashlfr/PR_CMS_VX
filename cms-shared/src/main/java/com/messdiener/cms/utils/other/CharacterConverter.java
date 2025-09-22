package com.messdiener.cms.utils.other;

import java.util.HashMap;
import java.util.Map;

public class CharacterConverter {

    private static final Map<Character, String> conversionMap = new HashMap<>();

    static {
        conversionMap.put('ä', "ae");
        conversionMap.put('Ä', "Ae");
        conversionMap.put('ö', "oe");
        conversionMap.put('Ö', "Oe");
        conversionMap.put('ü', "ue");
        conversionMap.put('Ü', "Ue");
        conversionMap.put('ß', "ss");
        conversionMap.put('à', "a");
        conversionMap.put('á', "a");
        conversionMap.put('â', "a");
        conversionMap.put('ã', "a");
        conversionMap.put('è', "e");
        conversionMap.put('é', "e");
        conversionMap.put('ê', "e");
        conversionMap.put('ë', "e");
        conversionMap.put('ì', "i");
        conversionMap.put('í', "i");
        conversionMap.put('î', "i");
        conversionMap.put('ï', "i");
        conversionMap.put('ò', "o");
        conversionMap.put('ó', "o");
        conversionMap.put('ô', "o");
        conversionMap.put('õ', "o");
        conversionMap.put('ù', "u");
        conversionMap.put('ú', "u");
        conversionMap.put('û', "u");
        conversionMap.put('ý', "y");
        conversionMap.put('ÿ', "y");
    }

    public static String convert(String input) {
        if (input == null || input.isEmpty()) {
            return input;
        }

        StringBuilder result = new StringBuilder();
        for (char c : input.toCharArray()) {
            if (conversionMap.containsKey(c)) {
                result.append(conversionMap.get(c));
            } else {
                result.append(c);
            }
        }

        return result.toString();
    }

}
