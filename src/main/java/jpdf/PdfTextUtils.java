package jpdf;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static java.util.Map.entry;
import static java.util.Map.ofEntries;

public enum PdfTextUtils {;

    public static String removeUnsupportedCharacters(final String input) {
        return input.replaceAll("[^A-Za-z0-9.,?! ]", "");
    }

    public static final Map<Character, String> TO_SAFE_FOR_FONT = ofEntries
        ( entry('ß', "b"), entry('à', "a"), entry('á', "a")
        , entry('â', "a"), entry('ã', "a"), entry('ä', "a")
        , entry('å', "a"), entry('æ', "ae"), entry('ç', "c")
        , entry('è', "e"), entry('é', "e"), entry('ê', "e")
        , entry('ë', "e"), entry('ì', "i"), entry('í', "i")
        , entry('î', "i"), entry('ï', "i"), entry('ð', "o")
        , entry('ñ', "n"), entry('ò', "o"), entry('ó', "o")
        , entry('ô', "o"), entry('õ', "o"), entry('ö', "o")
        , entry('ø', "o"), entry('ù', "u"), entry('ú', "u")
        , entry('û', "u"), entry('ü', "u"), entry('ý', "y")
        , entry('þ', "p"), entry('ÿ', "y"), entry('ā', "a")
        , entry('ă', "a"), entry('ĉ', "c"), entry('č', "c")
        , entry('ď', "d"), entry('ē', "e"), entry('ě', "e")
        , entry('ĝ', "g"), entry('ĥ', "h"), entry('ī', "i")
        , entry('ĵ', "j"), entry('ň', "n"), entry('ō', "o")
        , entry('ő', "o"), entry('œ', "ae"), entry('ř', "r")
        , entry('ŝ', "s"), entry('ş', "s"), entry('š', "s")
        , entry('ţ', "t"), entry('ť', "t"), entry('ū', "u")
        , entry('ŭ', "u"), entry('ů', "u"), entry('ű', "u")
        , entry('ŵ', "w"), entry('ŷ', "y"), entry('ž', "z")
        , entry('ș', "s"), entry('ț', "t"), entry('ё', "e")
        );

    public static String transliterate(final String text, final Map<Character, String> replacements) {
        final StringBuilder builder = new StringBuilder();

        for (final char character : text.toCharArray()) {
            final String replacement = replacements.get(character);
            builder.append( replacement == null ? character : replacement );
        }

        return builder.toString();
    }

    private static final int LINE_LENGTH = 65;

    public static List<String> wordwrap(final String line) {
        return wordwrap(line, LINE_LENGTH);
    }
    public static List<String> wordwrap(String text, final int lineLength) {
        final ArrayList<String> ret = new ArrayList<>();

        while (!text.isEmpty()) {
            final String line = extractFirstLine(text);
            final int offset = getNextLineBreak(line, lineLength);

            ret.add(line.substring(0, offset));
            // TODO I tried to get the wordwrap code to respect double new lines, but I ran out of time to fix the algorithm
            // If you have the time try getting this code to not turn '\n\n' into '\n'
            text = removeLeadingNewline(text.substring(offset));
        }

        return ret;
    }

    private static String removeLeadingNewline(final String input) {
        if (input == null || input.isEmpty()) return input;
        return input.charAt(0) == '\n' ? input.substring(1) : input;
    }

    private static int getNextLineBreak(final String line, final int lineLength) {
        if (line.length() > lineLength) {
            final int space = line.lastIndexOf(' ', lineLength);
            return (space == -1) ? lineLength : space;
        }
        return line.length();
    }

    private static String extractFirstLine(final String text) {
        final int nextNewLine = text.indexOf('\n');
        return text.substring(0, nextNewLine != -1 ? nextNewLine : text.length());
    }

}
