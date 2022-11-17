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

public class PdfTemplate {

    public static PdfTemplate newPdf(final String resource) throws IOException {
        return new PdfTemplate(loadPdfFromResource(resource));
    }

    private final PDDocument template;
    private PdfTemplate(final PDDocument template) {
        this.template = template;
    }

    public PdfTemplate overlay(final String resource) throws IOException {
        return overlay(loadPdfFromResource(resource));
    }
    public PdfTemplate overlay(final PDDocument overlay) throws IOException {
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
            return Loader.loadPDF(in);
        }
    }

}
