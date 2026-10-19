package jpdf;

import org.apache.pdfbox.Loader;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.rendering.PDFRenderer;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.*;

import static jpdf.LayoutPage.toLayoutPage;
import static jpdf.Overlay.overlayPDF;

public class PdfTemplate implements AutoCloseable {

    public static PdfTemplate newPdf(final String resource) throws IOException {
        return new PdfTemplate(loadPdfFromResource(resource));
    }

    private final PDDocument template;
    private PdfTemplate(final PDDocument template) {
        this.template = template;
    }

    public PdfTemplate overlay(final String resource) throws IOException {
        // the loaded document is only needed for its first page: close it
        // right away instead of leaking it for the lifetime of the JVM
        try (final var overlayDocument = loadPdfFromResource(resource)) {
            return overlay(overlayDocument.getPage(0));
        }
    }
    public PdfTemplate overlay(final PDDocument overlay) throws IOException {
        if (overlay.getNumberOfPages() < 1)
            throw new IOException("Overlay document has no pages");
        return overlay(overlay.getPage(0));
    }
    public PdfTemplate overlay(final PDPage overlay) throws IOException {
        return overlay(toLayoutPage(overlay));
    }
    public PdfTemplate overlay(final LayoutPage overlay) throws IOException {
        overlayPDF(template, overlay);
        return this;
    }

    public PDDocument toPdf() throws IOException {
        return template;
    }
    @Override
    public void close() throws IOException {
        template.close();
    }
    public BufferedImage toImage() throws IOException {
        return new PDFRenderer(template).renderImage(0);
    }
    public byte[] toByteArray() throws IOException {
        try (final var out = new ByteArrayOutputStream()) {
            template.save(out);
            return out.toByteArray();
        }
    }
    public void writeAsJpg(final OutputStream out) throws IOException {
        ImageIO.write(toImage(), "jpg", out);
    }
    public void writeAsPdf(final OutputStream out) throws IOException {
        template.save(out);
    }

    private static PDDocument loadPdfFromResource(final String resource) throws IOException {
        try (final var in = PdfTemplate.class.getResourceAsStream(resource)) {
            if (in == null) throw new FileNotFoundException("Missing resource " + resource);
            return Loader.loadPDF(in.readAllBytes());
        }
    }

}
