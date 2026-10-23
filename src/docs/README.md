# jpdf

A small wrapper around [Apache PDFBox](https://pdfbox.apache.org/) that makes
overlaying one PDF on top of another easy. PDFBox can do this, but the API is
not straightforward.

> **See also:** [extension-pdfbox](extension-pdfbox/src/docs/README.md) is the
> successor of this library — the same four classes, ported to the PDFBox 3.x
> `Loader` API and living under `extension.pdfbox`. Prefer it for new work.

## Usage

```java
try (PdfTemplate pdf = PdfTemplate.newPdf("base.pdf")) {
    pdf.overlay("stamp.pdf");
    pdf.renderToImage(0).writeTo(Path.of("out.png"));
    pdf.save(Path.of("merged.pdf"));
}
```

## Classes

| Class | Purpose |
| --- | --- |
| `PdfTemplate` | Fluent entry point (`AutoCloseable`): open a PDF, overlay layers, render to image, save. |
| `Overlay` | Low-level COS-object merging, including text overlays with fonts and transforms. |
| `LayoutPage` | Wraps a `PDPage` so it can be used as a stackable layer. |
| `PdfTextUtils` | Strips characters the target font cannot render and substitutes unsafe glyphs. |

## Dependency

```xml
<dependency>
  <groupId>com.github.codemonstur</groupId>
  <artifactId>jpdf</artifactId>
  <version>1.0.0</version>
</dependency>
```

Requires `org.apache.pdfbox:pdfbox:3.0.0`.

## Building

```sh
make build
```

## License

MIT
