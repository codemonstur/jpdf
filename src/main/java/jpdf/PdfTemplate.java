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

/**
 * The {@code PdfTemplate} class.
 */
public class PdfTemplate implements AutoCloseable {

    /**
     * Returns the result of {@code newPdf}.
     *
     * @param resource the resource
     * @return the result
     * @throws IOException if the operation fails
     */
    public static PdfTemplate newPdf(final String resource) throws IOException {
        return new PdfTemplate(loadPdfFromResource(resource));
    }

    private final PDDocument template;
    private PdfTemplate(final PDDocument template) {
        this.template = template;
    }

    /**
     * Returns the result of {@code overlay}.
     *
     * @param resource the resource
     * @return the result
     * @throws IOException if the operation fails
     */
    public PdfTemplate overlay(final String resource) throws IOException {
        // the loaded document is only needed for its first page: close it
        // right away instead of leaking it for the lifetime of the JVM
        try (final var overlayDocument = loadPdfFromResource(resource)) {
            return overlay(overlayDocument.getPage(0));
        }
    }
    /**
     * Returns the result of {@code overlay}.
     *
     * @param overlay the overlay
     * @return the result
     * @throws IOException if the operation fails
     */
    public PdfTemplate overlay(final PDDocument overlay) throws IOException {
        if (overlay.getNumberOfPages() < 1)
            throw new IOException("Overlay document has no pages");
        return overlay(overlay.getPage(0));
    }
    /**
     * Returns the result of {@code overlay}.
     *
     * @param overlay the overlay
     * @return the result
     * @throws IOException if the operation fails
     */
    public PdfTemplate overlay(final PDPage overlay) throws IOException {
        return overlay(toLayoutPage(overlay));
    }
    /**
     * Returns the result of {@code overlay}.
     *
     * @param overlay the overlay
     * @return the result
     * @throws IOException if the operation fails
     */
    public PdfTemplate overlay(final LayoutPage overlay) throws IOException {
        overlayPDF(template, overlay);
        return this;
    }

    /**
     * Returns the result of {@code toPdf}.
     *
     * @return the result
     * @throws IOException if the operation fails
     */
    public PDDocument toPdf() throws IOException {
        return template;
    }
    /**
     * Closes this resource.
     *
     * @throws IOException if the operation fails
     */
    @Override
    public void close() throws IOException {
        template.close();
    }
    /**
     * Returns the result of {@code toImage}.
     *
     * @return the result
     * @throws IOException if the operation fails
     */
    public BufferedImage toImage() throws IOException {
        return new PDFRenderer(template).renderImage(0);
    }
    /**
     * Returns the result of {@code toByteArray}.
     *
     * @return the result
     * @throws IOException if the operation fails
     */
    public byte[] toByteArray() throws IOException {
        try (final var out = new ByteArrayOutputStream()) {
            template.save(out);
            return out.toByteArray();
        }
    }
    /**
     * Performs the {@code writeAsJpg} operation.
     *
     * @param out the out
     * @throws IOException if the operation fails
     */
    public void writeAsJpg(final OutputStream out) throws IOException {
        ImageIO.write(toImage(), "jpg", out);
    }
    /**
     * Performs the {@code writeAsPdf} operation.
     *
     * @param out the out
     * @throws IOException if the operation fails
     */
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
