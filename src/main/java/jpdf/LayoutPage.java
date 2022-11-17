package jpdf;

import org.apache.pdfbox.cos.*;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDResources;
import org.apache.pdfbox.pdmodel.common.PDRectangle;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;

import static org.apache.pdfbox.cos.COSName.CONTENTS;
import static org.apache.pdfbox.cos.COSName.FLATE_DECODE;

public final class LayoutPage {
    public final PDRectangle overlayMediaBox;
    public final COSStream overlayContentStream;
    public final COSDictionary overlayResources;

    private LayoutPage(final PDRectangle mediaBox, final COSStream contentStream, final COSDictionary resources) {
        overlayMediaBox = mediaBox;
        overlayContentStream = contentStream;
        overlayResources = resources;
    }

    public static LayoutPage toLayoutPage(final PDPage page) throws IOException {
        final var contents = page.getCOSObject().getDictionaryObject(CONTENTS);
        var resources = page.getResources();
        if (resources == null) resources = new PDResources();

        return new LayoutPage(page.getMediaBox(), createContentStream(contents), resources.getCOSObject());
    }

    private static COSStream createContentStream(final COSBase contents) throws IOException {
        final List<COSStream> contentStreams = createContentStreamList(contents);

        final COSStream concatStream = new COSStream();
        try (final var out = concatStream.createOutputStream(FLATE_DECODE)) {
            final var buffer = new byte[2048];
            for (final var contentStream : contentStreams) {
                try (final var in = contentStream.createInputStream()) {
                    for (int bytesRead; (bytesRead = in.read(buffer)) != -1;) {
                        out.write(buffer, 0, bytesRead);
                    }
                    out.flush();
                }
            }
        }
        return concatStream;
    }

    private static List<COSStream> createContentStreamList(final COSBase contents) throws IOException {
        final var contentStreams = new ArrayList<COSStream>();
        if (contents instanceof COSStream) {
            contentStreams.add((COSStream) contents);
        } else if (contents instanceof COSArray) {
            for (final var item : (COSArray) contents) {
                contentStreams.addAll(createContentStreamList(item));
            }
        } else if (contents instanceof COSObject) {
            contentStreams.addAll(createContentStreamList(((COSObject) contents).getObject()));
        } else {
            throw new IOException("Contents are unknown type:" + contents.getClass().getName());
        }
        return contentStreams;
    }

}
