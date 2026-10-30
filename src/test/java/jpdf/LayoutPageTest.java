package jpdf;

import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class LayoutPageTest {

    @Test
    void toLayoutPageWithoutContentsThrows() throws Exception {
        try (var doc = new PDDocument()) {
            var page = new PDPage();
            doc.addPage(page);
            assertThrows(Exception.class, () -> LayoutPage.toLayoutPage(page));
        }
    }

    @Test
    void toLayoutPageWithContents() throws Exception {
        try (var doc = new PDDocument()) {
            var page = new PDPage();
            doc.addPage(page);
            try (var cs = new PDPageContentStream(doc, page)) {
                cs.beginText();
                cs.endText();
            }
            var lp = LayoutPage.toLayoutPage(page);
            assertNotNull(lp.overlayMediaBox);
            assertNotNull(lp.overlayContentStream);
            assertNotNull(lp.overlayResources);
        }
    }
}
