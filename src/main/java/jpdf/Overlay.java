package jpdf;

import org.apache.pdfbox.cos.*;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.PDResources;
import org.apache.pdfbox.pdmodel.common.PDRectangle;
import org.apache.pdfbox.pdmodel.font.PDFont;
import org.apache.pdfbox.pdmodel.graphics.form.PDFormXObject;

import java.awt.*;
import java.awt.geom.AffineTransform;
import java.io.IOException;
import java.io.OutputStream;
import java.math.BigDecimal;

import static java.nio.charset.StandardCharsets.UTF_8;
import static org.apache.pdfbox.cos.COSName.CONTENTS;
import static org.apache.pdfbox.cos.COSName.FLATE_DECODE;

public enum Overlay {;

    public static final class OverlayCOSArray extends COSArray {
        void addAll(final COSBase contents) throws IOException {
            if (contents == null) return;
            if (contents instanceof COSStream) {
                add(contents);
            } else if (contents instanceof COSArray) {
                addAll((COSArray) contents);
            } else {
                throw new IOException("Unknown content type:" + contents.getClass().getName());
            }
        }
    }

    public static void overlayPDF(final PDDocument template, final LayoutPage overlay) throws IOException {
        for (final var page : template.getPages()) {
            final var pageDictionary = page.getCOSObject();
            final var contents = pageDictionary.getDictionaryObject(CONTENTS);

            final var contentArray = new OverlayCOSArray();
            contentArray.add(createStream("q\n"));
            contentArray.addAll(contents);
            // leading whitespace is required: content streams are concatenated verbatim,
            // so a template stream ending in a bare token would otherwise merge into "ETQ"
            contentArray.add(createStream("\nQ\n"));
            contentArray.add(overlayPage(page, overlay));

            pageDictionary.setItem(CONTENTS, contentArray);
        }
    }

    private static COSStream overlayPage(final PDPage page, final LayoutPage overlay) throws IOException {
        if (page.getResources() == null) page.setResources(new PDResources());
        return createOverlayStream(page, overlay, createOverlayXObject(page, overlay, overlay.overlayContentStream));
    }

    private static COSName createOverlayXObject(final PDPage page, final LayoutPage layoutPage, final COSStream contentStream) {
        final var xobjForm = new PDFormXObject(contentStream);
        xobjForm.setResources(new PDResources(layoutPage.overlayResources));
        xobjForm.setFormType(1);
        // the form has an identity matrix, so its BBox stays in the template's
        // user space; retranslating it would clip content at the wrong offset
        xobjForm.setBBox(layoutPage.overlayMediaBox);
        xobjForm.setMatrix(new AffineTransform());
        return page.getResources().add(xobjForm, "OL");
    }

    private static COSStream createOverlayStream(final PDPage page, final LayoutPage layoutPage, final COSName xObjectId)  throws IOException {
        final PDRectangle pageMediaBox = page.getCropBox();
        // centering inside the visible box: the shift is the distance between the
        // lower-left corners after centering the sizes
        final float hShift = pageMediaBox.getLowerLeftX()
                + (pageMediaBox.getWidth() - layoutPage.overlayMediaBox.getWidth()) / 2.0f
                - layoutPage.overlayMediaBox.getLowerLeftX();
        final float vShift = pageMediaBox.getLowerLeftY()
                + (pageMediaBox.getHeight() - layoutPage.overlayMediaBox.getHeight()) / 2.0f
                - layoutPage.overlayMediaBox.getLowerLeftY();
        if (!isFinite(hShift) || !isFinite(vShift))
            throw new IOException("Page or overlay has invalid (NaN/infinite) dimensions");
        final String overlayStream = "q\nq 1 0 0 1 " + float2String(hShift) + " " + float2String(vShift) +
                " cm /" + xObjectId.getName() + " Do Q\nQ\n";
        return createStream(overlayStream);
    }

    private static boolean isFinite(final float value) {
        return !Float.isNaN(value) && !Float.isInfinite(value);
    }

    private static String float2String(final float floatValue) {
        // use a BigDecimal as intermediate state to avoid
        // a floating point string representation of the float value
        final var value = new BigDecimal(String.valueOf(floatValue));
        String stringValue = value.toPlainString();
        // remove fraction digit "0" only
        if (stringValue.indexOf('.') > -1 && !stringValue.endsWith(".0")) {
            while (stringValue.endsWith("0") && !stringValue.endsWith(".0")) {
                stringValue = stringValue.substring(0, stringValue.length() - 1);
            }
        }
        return stringValue;
    }

    private static COSStream createStream(final String content) throws IOException {
        final var stream = new COSStream();
        try (final var out = stream.createOutputStream(FLATE_DECODE)) {
            // used to be ISO-8859-1, don't know why the original code used it
            out.write(content.getBytes(UTF_8));
        }
        return stream;
    }

    public static float centerXofString(final String text, final PDFont font, final int fontSize
            , final float pageWidth) throws IOException {
        final float textWidth = font.getStringWidth(text) / 1000 * fontSize;
        return (pageWidth - textWidth) / 2;
    }

    public static void addTextBlock(final PDPageContentStream contentStream, final PDFont font, final Color color
            , final int fontSize, final float offsetX, final float offsetY, final String text) throws IOException {
        contentStream.beginText();
        contentStream.setFont(font, fontSize);
        contentStream.setStrokingColor(color);
        contentStream.setNonStrokingColor(color);
        contentStream.newLineAtOffset(offsetX, offsetY);
        contentStream.showText(text);
        contentStream.endText();
    }

}
