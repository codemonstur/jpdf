package jpdf;

import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class PdfTextUtilsTest {

    @Test
    void removeUnsupportedCharacters() {
        assertEquals("Hello, world!", PdfTextUtils.removeUnsupportedCharacters("Hello, world!"));
        assertEquals("abc", PdfTextUtils.removeUnsupportedCharacters("a@b#c"));
    }

    @Test
    void transliterate() {
        assertEquals("ae", PdfTextUtils.transliterate("æ", PdfTextUtils.TO_SAFE_FOR_FONT));
        assertEquals("a", PdfTextUtils.transliterate("à", PdfTextUtils.TO_SAFE_FOR_FONT));
        assertEquals("x", PdfTextUtils.transliterate("x", PdfTextUtils.TO_SAFE_FOR_FONT));
    }

    @Test
    void wordwrapInvalidLineLength() {
        assertThrows(IllegalArgumentException.class, () -> PdfTextUtils.wordwrap("text", 0));
    }

    @Test
    void wordwrapShortText() {
        assertEquals(List.of("hello"), PdfTextUtils.wordwrap("hello", 10));
    }

    @Test
    void wordwrapBreaksLongLine() {
        List<String> lines = PdfTextUtils.wordwrap("one two three four", 7);
        assertFalse(lines.isEmpty());
        assertTrue(lines.stream().allMatch(l -> l.length() <= 7 || !l.contains(" ")));
    }

    @Test
    void wordwrapRespectsNewlines() {
        List<String> lines = PdfTextUtils.wordwrap("a b\nc d", 10);
        assertEquals(2, lines.size());
    }

    @Test
    void centerXofString() throws Exception {
        var font = new org.apache.pdfbox.pdmodel.font.PDType1Font(org.apache.pdfbox.pdmodel.font.Standard14Fonts.FontName.HELVETICA);
        float x = Overlay.centerXofString("Hi", font, 12, 100f);
        assertTrue(x >= 0);
    }
}
