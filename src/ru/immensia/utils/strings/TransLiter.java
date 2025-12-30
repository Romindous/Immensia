package ru.immensia.utils.strings;

//не перемещать, юзает прокси!!
//для перекодировки названия мира с кирилицы
public class TransLiter {

    private static final int CRL_MAX = 0x44F; // Unicode offset for Cyrillic characters
    private static final int CRL_OFFSET = 0x400; // Unicode offset for Cyrillic characters
    private static final int LTN_RANGE = 128; // Latin ASCII range
    private static final char[] MAPPING = new char[LTN_RANGE + CRL_MAX - CRL_OFFSET + 1];

    static {
        // Initialize array with identity mapping
        for (int i = 0; i != LTN_RANGE; i++) MAPPING[i] = (char) i;
        for (int i = LTN_RANGE; i != MAPPING.length; i++) {
            MAPPING[i] = (char) (i + CRL_OFFSET - LTN_RANGE);
        }

        // Mapping based on a standard Russian keyboard layout
        char[][] mappings = {
            {'q', 'й'}, {'w', 'ц'}, {'e', 'у'}, {'r', 'к'}, {'t', 'е'}, {'y', 'н'},
            {'u', 'г'}, {'i', 'ш'}, {'o', 'щ'}, {'p', 'з'}, {'[', 'х'}, {']', 'ъ'},
            {'a', 'ф'}, {'s', 'ы'}, {'d', 'в'}, {'f', 'а'}, {'g', 'п'}, {'h', 'р'},
            {'j', 'о'}, {'k', 'л'}, {'l', 'д'}, {';', 'ж'}, {'\'', 'э'},
            {'z', 'я'}, {'x', 'ч'}, {'c', 'с'}, {'v', 'м'}, {'b', 'и'}, {'n', 'т'},
            {'m', 'ь'}, {',', 'б'}, {'.', 'ю'},
            {'Q', 'Й'}, {'W', 'Ц'}, {'E', 'У'}, {'R', 'К'}, {'T', 'Е'}, {'Y', 'Н'},
            {'U', 'Г'}, {'I', 'Ш'}, {'O', 'Щ'}, {'P', 'З'}, {'{', 'Х'}, {'}', 'Ъ'},
            {'A', 'Ф'}, {'S', 'Ы'}, {'D', 'В'}, {'F', 'А'}, {'G', 'П'}, {'H', 'Р'},
            {'J', 'О'}, {'K', 'Л'}, {'L', 'Д'}, {':', 'Ж'}, {'"', 'Э'},
            {'Z', 'Я'}, {'X', 'Ч'}, {'C', 'С'}, {'V', 'М'}, {'B', 'И'}, {'N', 'Т'},
            {'M', 'Ь'}, {'<', 'Б'}, {'>', 'Ю'}
        };

        for (final char[] pair : mappings) {
            MAPPING[pair[0]] = pair[1];
            final int cyrillicIndex = pair[1] - CRL_OFFSET + LTN_RANGE;
            if (cyrillicIndex < 0 || cyrillicIndex >= MAPPING.length) continue;
            MAPPING[cyrillicIndex] = pair[0]; // Safe mapping for Cyrillic characters
        }
    }


    public static char reLayOut(final char ch) {
        if (ch < LTN_RANGE) return MAPPING[ch];
        if (ch < CRL_OFFSET || ch > CRL_MAX) return ch;
        return MAPPING[ch - CRL_OFFSET + LTN_RANGE];
    }

    public static String reLayOut(String input) {
        final StringBuilder result = new StringBuilder();
        for (char c : input.toCharArray()) result.append(reLayOut(c));
        return result.toString();
    }
}
