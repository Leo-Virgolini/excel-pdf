package service;

import com.itextpdf.io.image.ImageData;
import com.itextpdf.io.image.ImageDataFactory;
import com.itextpdf.kernel.colors.DeviceRgb;
import com.itextpdf.kernel.geom.PageSize;
import com.itextpdf.kernel.pdf.*;
import com.itextpdf.kernel.pdf.action.PdfAction;
import com.itextpdf.kernel.pdf.navigation.PdfExplicitDestination;
import com.itextpdf.layout.Document;
import com.itextpdf.layout.Style;
import com.itextpdf.layout.element.*;
import com.itextpdf.layout.font.FontProvider;
import com.itextpdf.layout.properties.*;
import com.itextpdf.styledxmlparser.resolver.font.BasicFontProvider;
import javafx.application.Platform;
import javafx.concurrent.Service;
import javafx.concurrent.Task;
import javafx.scene.control.TextArea;
import org.apache.poi.openxml4j.opc.OPCPackage;
import org.apache.poi.ss.usermodel.CellType;
import org.apache.poi.ss.usermodel.DateUtil;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import pdf.ClippedTableRenderer;
import pdf.model.CustomFont;

import java.io.File;
import java.math.BigDecimal;
import java.util.Map;

public class GeneratePDFService extends Service<Integer> {

    private final File archivoExcel;
    private final File carpetaImagenes;
    private final File caratulaPdf;
    private final File archivoDestino;
    private final CustomFont codigoFont;
    private final CustomFont productoFont;
    private final CustomFont rubroFont;
    private final CustomFont subRubroFont;
    private final CustomFont marcaFont;
    private final CustomFont precioFont;
    private final CustomFont codigoExternoFont;
    private final float imageSize;
    private final float pageWidth;
    private final float pageHeight;
    private final boolean codigoColumn;
    private final boolean productoColumn;
    private final boolean rubroColumn;
    private final boolean subRubroColumn;
    private final boolean marcaColumn;
    private final boolean precioColumn;
    private final boolean codigoExternoColumn;
    private final boolean imagenes;
    private final boolean links;
    private final TextArea logTextArea;

    public GeneratePDFService(File archivoExcel, File carpetaImagenes, File caratulaPdf, File archivoDestino,
                              CustomFont codigoFont, CustomFont productoFont, CustomFont rubroFont, CustomFont subRubroFont, CustomFont marcaFont, CustomFont precioFont, CustomFont codigoExternoFont,
                              float imageSize, float pageWidth, float pageHeight, boolean codigoColumn, boolean productoColumn,
                              boolean rubroColumn, boolean subRubroColumn, boolean marcaColumn, boolean precioColumn, boolean codigoExternoColumn, boolean imagenes, boolean links, TextArea logTextArea) {
        this.archivoExcel = archivoExcel;
        this.carpetaImagenes = carpetaImagenes;
        this.caratulaPdf = caratulaPdf;
        this.archivoDestino = archivoDestino;
        this.codigoFont = codigoFont;
        this.productoFont = productoFont;
        this.rubroFont = rubroFont;
        this.subRubroFont = subRubroFont;
        this.marcaFont = marcaFont;
        this.precioFont = precioFont;
        this.codigoExternoFont = codigoExternoFont;
        this.imageSize = imageSize;
        this.pageWidth = pageWidth;
        this.pageHeight = pageHeight;
        this.codigoColumn = codigoColumn;
        this.productoColumn = productoColumn;
        this.rubroColumn = rubroColumn;
        this.subRubroColumn = subRubroColumn;
        this.marcaColumn = marcaColumn;
        this.precioColumn = precioColumn;
        this.codigoExternoColumn = codigoExternoColumn;
        this.imagenes = imagenes;
        this.links = links;
        this.logTextArea = logTextArea;
    }

    @Override
    protected Task<Integer> createTask() {
        return new Task<>() {
            @Override
            protected Integer call() throws Exception {
                return generarPDF();
            }
        };
    }

    private Integer generarPDF() throws Exception {

        final String[] supportedExtensions = {".jpg", ".jpeg", ".png", ".bmp"};
        final Image sinImagen = new Image(ImageDataFactory.create((getClass().getResource("/images/SINIMAGEN.jpg").toExternalForm())));
        final ImageData buttonImagenData = ImageDataFactory.create(getClass().getResource("/images/button.png").toExternalForm());

        final int productsPerPage = 20;
        final int rowsPerPage = 5;
        int generatedProducts = 0;
        final StringBuilder log = new StringBuilder();
        // Read the Excel file
        try (final OPCPackage pkg = OPCPackage.open(archivoExcel);
             final XSSFWorkbook workbook = new XSSFWorkbook(pkg)) {
            final Sheet sheet = workbook.getSheetAt(0);
            // Verificar que tenga 7 columnas
            final Row firstRow = sheet.getRow(0);
            if (firstRow == null || firstRow.getLastCellNum() < 7) {
                throw new Exception("Verifique que la hoja tenga los 7 encabezados en orden.");
            } else {
                final String encabezado1 = getCellValue(firstRow.getCell(0));
                final String encabezado3 = getCellValue(firstRow.getCell(2));
                final String encabezado4 = getCellValue(firstRow.getCell(3));
                final String encabezado5 = getCellValue(firstRow.getCell(4));
                final String encabezado7 = getCellValue(firstRow.getCell(6));

                // Calculate the actual row count with data
                int rowCount = 0;
                for (Row row : sheet) {
                    if (row != null && !isEmptyRow(row)) {
                        rowCount++;
                    }
                }

                // Create a PDF document
                try (final PdfDocument pdfDoc = new PdfDocument(new PdfWriter(archivoDestino.getAbsolutePath()));
                     final Document doc = new Document(pdfDoc, new PageSize(pageWidth, pageHeight), false)) {

                    // Set the viewer preferences to fit the page
                    pdfDoc.getCatalog().setViewerPreferences(
                            new PdfViewerPreferences().setFitWindow(true)
                    );

                    // To set FitPage or FitWidth
                    pdfDoc.getCatalog().setOpenAction(
                            PdfAction.createGoTo("FitPage") // FitWidth can be used similarly
                    );

                    final FontProvider fontProvider = new BasicFontProvider(true, true);
                    doc.setFontProvider(fontProvider);
                    doc.setMargins(0, 0, 0, 0);

                    if (caratulaPdf != null && caratulaPdf.isFile()) {
                        agregarCaratula(pdfDoc, doc);
                    }

                    final Style codigoStyle = new Style().setFontFamily(codigoFont.getFamily()).setFontSize(codigoFont.getSize()).setFontColor(codigoFont.getRGB()).setTextAlignment(TextAlignment.CENTER);
                    final Style productoStyle = new Style().setFontFamily(productoFont.getFamily()).setFontSize(productoFont.getSize()).setFontColor(productoFont.getRGB()).setTextAlignment(TextAlignment.CENTER);
                    final Style rubroStyle = new Style().setFontFamily(rubroFont.getFamily()).setFontSize(rubroFont.getSize()).setFontColor(rubroFont.getRGB()).setTextAlignment(TextAlignment.CENTER);
                    final Style subRubroStyle = new Style().setFontFamily(subRubroFont.getFamily()).setFontSize(subRubroFont.getSize()).setFontColor(subRubroFont.getRGB()).setTextAlignment(TextAlignment.CENTER);
                    final Style marcaStyle = new Style().setFontFamily(marcaFont.getFamily()).setFontSize(marcaFont.getSize()).setFontColor(marcaFont.getRGB()).setTextAlignment(TextAlignment.CENTER);
                    final Style precioStyle = new Style().setFontFamily(precioFont.getFamily()).setFontSize(precioFont.getSize()).setFontColor(precioFont.getRGB()).setTextAlignment(TextAlignment.CENTER).simulateBold();
                    final Style codigoExternoStyle = new Style().setFontFamily(codigoExternoFont.getFamily()).setFontSize(codigoExternoFont.getSize()).setFontColor(codigoExternoFont.getRGB()).setTextAlignment(TextAlignment.CENTER);

                    // Create a table with 4 columns and 5 rows per page
                    Table table = new Table(UnitValue.createPercentArray(4)).useAllAvailableWidth();
                    // Loop through each row in the Excel sheet
                    for (int i = 1; i < rowCount; i++) {
                        // Read the product data from the Excel rows
                        Row row = sheet.getRow(i);
                        // Create a new table cell
                        Cell cell = new Cell();
                        cell.setPadding(1);
                        cell.setMargin(0);
//                    cell.setMinHeight((pageHeight - 20) / rowsPerPage);
//                    cell.setMaxHeight((pageHeight - 20) / rowsPerPage);
//                    cell.setHeight((pageHeight - 20) / rowsPerPage);
                        if (codigoColumn) { // CODIGO
                            try {
                                final double codigoValue = Double.parseDouble(getCellValue(row.getCell(0)));
                                final String codigo = (codigoValue % 1 == 0) ? String.format("%.0f", codigoValue) : String.valueOf(codigoValue);

                                // Add the bookmark for the product
//                            addBookmarks(pdfDoc, codigo, pdfDoc.getNumberOfPages());
                                final Paragraph codigoParagraph = new Paragraph();
                                codigoParagraph.add(new Text(encabezado1 + ": ").simulateBold());
                                codigoParagraph.add(new Text(codigo));
                                codigoParagraph.addStyle(codigoStyle);
//                                        .setDestination(codigo) //
                                cell.add(codigoParagraph);
                            } catch (NumberFormatException nfe) {
                                throw new NumberFormatException("Fila #" + (i + 1) + " " + encabezado1 + " debe ser un número.");
                            }
                        }

                        if (productoColumn) { // PRODUCTO
                            final String producto = getCellValue(row.getCell(1));
                            final Paragraph productoParagraph = new Paragraph(producto);
                            productoParagraph.addStyle(productoStyle);
                            cell.add(productoParagraph);
                        }

                        if (rubroColumn) { // RUBRO
                            final String rubro = getCellValue(row.getCell(2));
                            final Paragraph rubroParagraph = new Paragraph();
                            rubroParagraph.add(new Text(encabezado3 + ": ").simulateBold());
                            rubroParagraph.add(new Text(rubro));
                            rubroParagraph.addStyle(rubroStyle);
                            cell.add(rubroParagraph);
                        }

                        if (subRubroColumn) { // SUBRUBRO
                            final String subRubro = getCellValue(row.getCell(3));
                            final Paragraph subRubroParagraph = new Paragraph();
                            subRubroParagraph.add(new Text(encabezado4 + ": ").simulateBold());
                            subRubroParagraph.add(new Text(subRubro));
                            subRubroParagraph.addStyle(subRubroStyle);
                            cell.add(subRubroParagraph);
                        }

                        if (marcaColumn) { // MARCA
                            final String marca = getCellValue(row.getCell(4));
                            final Paragraph marcaParagraph = new Paragraph();
                            marcaParagraph.add(new Text(encabezado5 + ": ").simulateBold());
                            marcaParagraph.add(new Text(marca));
                            marcaParagraph.addStyle(marcaStyle);
                            cell.add(marcaParagraph);
                        }

                        if (precioColumn) { // PRECIO
                            try {
                                final BigDecimal precioVenta = new BigDecimal(getCellValue(row.getCell(5)));
                                final String formattedPrecioVenta = precioVenta.compareTo(BigDecimal.ZERO) == 0 ? "$ --" : String.format("$ %(,.2f", precioVenta);
                                final Paragraph precioParagraph = new Paragraph(formattedPrecioVenta);
                                precioParagraph.addStyle(precioStyle);
                                cell.add(precioParagraph);
                            } catch (NumberFormatException nfe) {
                                throw new NumberFormatException("Fila #" + (i + 1) + " el PRECIO DE VENTA es incorrecto.");
                            }
                        }

                        if (codigoExternoColumn) { // CODIGO EXTERNO
                            final String codigoExterno = getCellValue(row.getCell(6));
                            final Paragraph codExtParagraph = new Paragraph();
                            codExtParagraph.add(new Text(encabezado7 + ": ").simulateBold());
                            codExtParagraph.add(new Text(codigoExterno));
                            codExtParagraph.addStyle(codigoExternoStyle);
                            cell.add(codExtParagraph);
                        }

                        if (imagenes) { // IMAGENES
                            try {
                                final double codigoValue = Double.parseDouble(getCellValue(row.getCell(0)));
                                final String codigo = (codigoValue % 1 == 0) ? String.format("%.0f", codigoValue) : String.valueOf(codigoValue);
                                Image image;
                                File imageFile = null;
                                for (String extension : supportedExtensions) {
                                    File file = new File(carpetaImagenes.getAbsolutePath(), codigo + extension);
                                    if (file.isFile()) {
                                        imageFile = file;
                                        break;
                                    }
                                }
                                if (imageFile != null && imageFile.isFile()) {
                                    image = new Image(ImageDataFactory.create(imageFile.getAbsolutePath()));
                                } else { // si no existe el archivo de la imagen usar SINIMAGEN.jpg
                                    image = sinImagen;
                                    log.append("Advertencia: La imagen \"").append(codigo).append("\" no existe.\n");
                                }
                                image
                                        .scaleToFit(imageSize, imageSize)
                                        .setHorizontalAlignment(HorizontalAlignment.CENTER)
                                        .setMarginBottom(0);
                                cell.add(image);
                            } catch (NumberFormatException nfe) {
                                throw new NumberFormatException("Fila #" + (i + 1) + " el CODIGO no es un número.");
                            }
                        }

                        if (links) { // LINKS
                            final String producto = getCellValue(row.getCell(1));
                            if (!producto.isBlank()) {
                                final Image button = new Image(buttonImagenData);
                                button
                                        .setHeight(25)
                                        .setWidth(60)
                                        .setHorizontalAlignment(HorizontalAlignment.CENTER)
                                        .setMarginBottom(0);
                                final StringBuilder url = new StringBuilder().append("https://kitchentools.com.ar/productos/").append(producto.replaceAll("\\([^)]+\\)$", "").trim().replace(" ", "-"));
                                button.setAction(PdfAction.createURI(url.toString()));
                                cell.add(button);
                            }
                        }

//                        addProduct(row, productos);

                        cell
                                .setTextAlignment(TextAlignment.CENTER)
                                .setHorizontalAlignment(HorizontalAlignment.CENTER)
                                .setVerticalAlignment(VerticalAlignment.MIDDLE);
//                    cell.setKeepTogether(true);
                        table.addCell(cell);

                        // If we've added the maximum number of products per page, start a new page
                        if ((table.getNumberOfRows() % rowsPerPage == 0) && (i % productsPerPage == 0)) {
//                        table.setExtendBottomRow(true);
//                        table.setMaxHeight(pageHeight - 6);
                            table.setHeight(pageHeight - 6);
                            table.setMargin(0);
                            table.setPadding(0);
                            table.setFixedLayout();
                            table.setNextRenderer(new ClippedTableRenderer(table));
                            doc.add(table);
                            doc.add(new AreaBreak(AreaBreakType.NEXT_PAGE));
                            table = new Table(UnitValue.createPercentArray(4)).useAllAvailableWidth();
                        }
                        generatedProducts++;
                    }
                    // If there are any remaining products, add them to the last page
                    if (table.getNumberOfRows() > 0) {
                        table.setExtendBottomRow(false);
                        table.setMargin(0);
                        table.setPadding(0);
                        table.setFixedLayout();
                        table.setNextRenderer(new ClippedTableRenderer(table));
                        doc.add(table);
                    }
//                    addLinks(doc, productos);
                    agregarNumeroPagina(pdfDoc, doc);
                } catch (Exception e) {
                    throw e;
                }
            }
        } catch (Exception e) {
            throw e;
        }
        if (log.length() > 0) {
            Platform.runLater(() -> {
                logTextArea.setStyle("-fx-text-fill: #d3d700;");
                logTextArea.appendText(log.toString());
            });
        }
        return generatedProducts;
    }

    private boolean isEmptyRow(Row row) {
        for (int i = row.getFirstCellNum(); i <= row.getLastCellNum(); i++) {
            final org.apache.poi.ss.usermodel.Cell cell = row.getCell(i);
            if (cell != null && cell.getCellType() != CellType.BLANK) {
                return false;
            }
        }
        return true;
    }

    private String getCellValue(org.apache.poi.ss.usermodel.Cell cell) throws Exception {
        if (cell == null) {
            return "";
        }

        final CellType cellType = cell.getCellType();
        switch (cellType) {
            case STRING:
                return cell.getStringCellValue();
            case NUMERIC:
                if (DateUtil.isCellDateFormatted(cell)) { // date
                    return cell.getDateCellValue().toString();
                } else { // numeric
                    return String.valueOf(cell.getNumericCellValue());
                }
            case BOOLEAN:
                return String.valueOf(cell.getBooleanCellValue());
            case FORMULA:
//                final CellType formulaCellType = evaluator.evaluateFormulaCell(cell);
                switch (cell.getCachedFormulaResultType()) {
                    case STRING:
                        return cell.getStringCellValue();
                    case NUMERIC:
                        if (DateUtil.isCellDateFormatted(cell)) {
                            return cell.getDateCellValue().toString();
                        } else {
                            return String.valueOf(cell.getNumericCellValue());
                        }
                    case BOOLEAN:
                        return String.valueOf(cell.getBooleanCellValue());
                    case ERROR:
                        return "0";
                    default:
                        return "";
                }
            case BLANK:
                return "";
            case ERROR:
                throw new Exception("Error en la celda fila: " + cell.getAddress().getRow() + 1 + " columna: " + cell.getAddress().getColumn() + 1);
            default:
                return "";
        }
    }

    private void agregarNumeroPagina(PdfDocument pdfDoc, Document doc) {
        final int numberOfPages = pdfDoc.getNumberOfPages();
        final Style fontStyle = new Style().setFontSize(5).setFontColor(new DeviceRgb(128, 128, 128));
        for (int i = 1; i <= numberOfPages; i++) {
            // Write aligned text to the specified parameters point
            doc.showTextAligned(new Paragraph(String.format("Página %s de %s", i, numberOfPages)).addStyle(fontStyle),
                    pageWidth / 2, 2.5f, i, TextAlignment.CENTER, VerticalAlignment.MIDDLE, 0);
        }
    }

    private void agregarCaratula(PdfDocument targetPdfDoc, Document doc) throws Exception {
        // Path to the PDF file containing the page to be added
        final String pageFilePath = caratulaPdf.getAbsolutePath();
        try (// Create a PdfReader object for the page PDF file
             PdfReader pagePdfReader = new PdfReader(pageFilePath);
             // Create a PdfDocument object for the page PDF
             PdfDocument pagePdfDocument = new PdfDocument(pagePdfReader)
        ) {
            // Get the first page from the page PDF
            PdfPage page = pagePdfDocument.getPage(1);
            // Insert the page at the beginning of the target PDF
            targetPdfDoc.addPage(1, page.copyTo(targetPdfDoc));
            doc.add(new AreaBreak(AreaBreakType.NEXT_PAGE));
        } catch (Exception e) {
            throw e;
        }
    }

    private void generarIndice(Document doc, PdfDocument pdfDoc) {
//         Set the open action to go to a specific page (e.g., page 3)
        PdfPage targetPage = pdfDoc.getPage(3);
        PdfExplicitDestination destination = PdfExplicitDestination.createFit(targetPage);
        PdfAction openAction = PdfAction.createGoToE(destination, false, null);
        pdfDoc.getCatalog().setOpenAction(openAction);
//         Add bookmarks for the cover and table of contents
        pdfDoc.addNamedDestination(PdfName.FirstPage.getValue(), PdfExplicitDestination.createFit(pdfDoc.getFirstPage()).getPdfObject());
        // Add a blank page as a placeholder for the table of contents
        doc.add(new AreaBreak(AreaBreakType.NEXT_PAGE));
        // Add the table of contents as a bookmark
        pdfDoc.addNamedDestination("Table of Contents", PdfExplicitDestination.createFit(pdfDoc.getPage(2)).getPdfObject());
        // Add a blank page as a placeholder for the table of contents
        doc.add(new AreaBreak(AreaBreakType.NEXT_PAGE));
    }

    private void addProduct(Row row, Map<String, String> productos) throws Exception {
        final double codigoValue = Double.parseDouble(getCellValue(row.getCell(0)));
        final String codigo = (codigoValue % 1 == 0) ? String.format("%.0f", codigoValue) : String.valueOf(codigoValue);
        final String producto = getCellValue(row.getCell(1));
        productos.put(codigo, producto);
    }

    private void addLinks(Document doc, Map<String, String> productos) {
        for (Map.Entry<String, String> entry : productos.entrySet()) {
            final Link link = new Link(entry.getKey() + ": " + entry.getValue(), PdfAction.createGoTo(entry.getKey()));
            // Add the link to the document
            doc.add(new Paragraph(link).setFontFamily("Cambria")
                    .setFontSize(10)
                    .setFontColor(DeviceRgb.BLUE)
                    .setTextAlignment(TextAlignment.LEFT));
        }
    }

}

