package service;

import com.itextpdf.io.image.ImageData;
import com.itextpdf.io.image.ImageDataFactory;
import com.itextpdf.kernel.colors.DeviceRgb;
import com.itextpdf.kernel.geom.PageSize;
import com.itextpdf.kernel.pdf.*;
import com.itextpdf.kernel.pdf.action.PdfAction;
import com.itextpdf.kernel.pdf.navigation.PdfExplicitDestination;
import com.itextpdf.layout.Document;
import com.itextpdf.layout.element.*;
import com.itextpdf.layout.element.Cell;
import com.itextpdf.layout.element.Table;
import com.itextpdf.layout.properties.*;
import com.itextpdf.layout.properties.HorizontalAlignment;
import com.itextpdf.layout.properties.VerticalAlignment;
import javafx.application.Platform;
import javafx.concurrent.Service;
import javafx.concurrent.Task;
import javafx.scene.control.TextArea;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import pdf.ClippedTableRenderer;

import java.io.File;
import java.io.FileInputStream;
import java.math.BigDecimal;

public class GeneratePDFService extends Service<Integer> {

    private final File archivoExcel;
    private final File carpetaImagenes;
    private final File archivoPdf;
    private final File archivoDestino;
    private final float codigoFontSize;
    private final float productoFontSize;
    private final float rubroFontSize;
    private final float subRubroFontSize;
    private final float marcaFontSize;
    private final float precioFontSize;
    private final float codigoExternoFontSize;
    private final DeviceRgb codigoColor;
    private final DeviceRgb productoColor;
    private final DeviceRgb rubroColor;
    private final DeviceRgb subRubroColor;
    private final DeviceRgb marcaColor;
    private final DeviceRgb precioColor;
    private final DeviceRgb codigoExternoColor;
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

    public GeneratePDFService(File archivoExcel, File carpetaImagenes, File archivoPdf, File archivoDestino, float codigoFontSize, float productoFontSize, float rubroFontSize, float subRubroFontSize, float marcaFontSize,
                              float precioFontSize, float codigoExternoFontSize, DeviceRgb codigoColor, DeviceRgb productoColor, DeviceRgb rubroColor, DeviceRgb subRubroColor, DeviceRgb marcaColor,
                              DeviceRgb precioColor, DeviceRgb codigoExternoColor, float imageSize, float pageWidth, float pageHeight, boolean codigoColumn, boolean productoColumn, boolean rubroColumn,
                              boolean subRubroColumn, boolean marcaColumn, boolean precioColumn, boolean codigoExternoColumn, boolean imagenes, boolean links, TextArea logTextArea) {
        this.archivoExcel = archivoExcel;
        this.carpetaImagenes = carpetaImagenes;
        this.archivoPdf = archivoPdf;
        this.archivoDestino = archivoDestino;
        this.codigoFontSize = codigoFontSize;
        this.productoFontSize = productoFontSize;
        this.rubroFontSize = rubroFontSize;
        this.subRubroFontSize = subRubroFontSize;
        this.marcaFontSize = marcaFontSize;
        this.precioFontSize = precioFontSize;
        this.codigoExternoFontSize = codigoExternoFontSize;
        this.codigoColor = codigoColor;
        this.productoColor = productoColor;
        this.rubroColor = rubroColor;
        this.subRubroColor = subRubroColor;
        this.marcaColor = marcaColor;
        this.precioColor = precioColor;
        this.codigoExternoColor = codigoExternoColor;
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
                return generarPDF(archivoExcel, carpetaImagenes, archivoPdf, archivoDestino, codigoFontSize, productoFontSize, rubroFontSize, subRubroFontSize, marcaFontSize, precioFontSize, codigoExternoFontSize,
                        codigoColor, productoColor, rubroColor, subRubroColor, marcaColor, precioColor, codigoExternoColor, imageSize, pageWidth, pageHeight, codigoColumn, productoColumn,
                        rubroColumn, subRubroColumn, marcaColumn, precioColumn, codigoExternoColumn, imagenes, links, logTextArea);
            }
        };
    }

    private Integer generarPDF(File archivoExcel, File carpetaImagenes, File archivoPdf, File archivoDestino, float codigoFontSize, float productoFontSize, float rubroFontSize, float subRubroFontSize,
                               float marcaFontSize, float precioFontSize, float codigoExternoFontSize, DeviceRgb codigoColor, DeviceRgb productoColor, DeviceRgb rubroColor, DeviceRgb subRubroColor,
                               DeviceRgb marcaColor, DeviceRgb precioColor, DeviceRgb codigoExternoColor, float imageSize, float pageWidth, float pageHeight, boolean codigoColumn, boolean productoColumn,
                               boolean rubroColumn, boolean subRubroColumn, boolean marcaColumn, boolean precioColumn, boolean codigoExternoColumn, boolean imagenes, boolean links, TextArea logTextArea) throws Exception {

        final String[] supportedExtensions = {".jpg", ".jpeg", ".png", ".bmp"};
        final Image sinImagen = new Image(ImageDataFactory.create((getClass().getResource("/images/SINIMAGEN.jpg").toExternalForm())));
        final ImageData buttonImagenData = ImageDataFactory.create(getClass().getResource("/images/button.png").toExternalForm());
        final int productsPerPage = 20;
        final int rowsPerPage = 5;
        int generatedRows = 0;
        // Read the Excel file
        try (final FileInputStream inputStream = new FileInputStream(archivoExcel.getAbsolutePath());
             final Workbook workbook = new XSSFWorkbook(inputStream)) {
            final FormulaEvaluator evaluator = workbook.getCreationHelper().createFormulaEvaluator();
            final Sheet sheet = workbook.getSheetAt(0);
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

                doc.setMargins(0, 0, 0, 0);

                // Indice
//                generarIndice(doc, pdfDoc);
                if (archivoPdf != null && archivoPdf.isFile()) {
                    agregarCaratula(pdfDoc, doc);
                }

                // Create a table with 4 columns and 5 rows per page
                Table table = new Table(UnitValue.createPercentArray(4)).useAllAvailableWidth();
                // Loop through each row in the Excel sheet
                for (int i = 1; i < rowCount; i++) {
                    // Read the product data from the Excel rows
                    Row row = sheet.getRow(i);
                    // Create a new table cell with the image and product data
                    Cell cell = new Cell();
                    cell.setPadding(1);
                    cell.setMargin(0);
//                    cell.setMinHeight((pageHeight - 20) / rowsPerPage);
//                    cell.setMaxHeight((pageHeight - 20) / rowsPerPage);
//                    cell.setHeight((pageHeight - 20) / rowsPerPage);
                    if (codigoColumn) { // CODIGO
                        try {
                            final double codigoValue = Double.parseDouble(getCellValue(row.getCell(0), evaluator));
                            final String codigo = (codigoValue % 1 == 0) ? String.format("%.0f", codigoValue) : String.valueOf(codigoValue);
                            // Add the bookmark for the product
//                            addBookmarks(pdfDoc, codigo, pdfDoc.getNumberOfPages());
                            final Paragraph codigoParagraph = new Paragraph();
                            codigoParagraph.add(new Text("CODIGO: ").setBold());
                            codigoParagraph.add(new Text(codigo));
                            codigoParagraph
                                    .setFontSize(codigoFontSize)
                                    .setFontColor(codigoColor)
                                    .setTextAlignment(TextAlignment.CENTER);
                            cell.add(codigoParagraph);
                        } catch (NumberFormatException nfe) {
                            throw new NumberFormatException("Fila #" + (i + 1) + " el CODIGO no es un número.");
                        }
                    }

                    if (productoColumn) { // PRODUCTO
                        final String producto = getCellValue(row.getCell(1), evaluator);
                        final Paragraph productoParagraph = new Paragraph(producto);
                        productoParagraph
                                .setFontSize(productoFontSize)
                                .setFontColor(productoColor)
                                .setTextAlignment(TextAlignment.CENTER);
                        cell.add(productoParagraph);
                    }

                    if (rubroColumn) { // RUBRO
                        final String rubro = getCellValue(row.getCell(2), evaluator);
                        final Paragraph rubroParagraph = new Paragraph();
                        rubroParagraph.add(new Text("RUBRO: ").setBold());
                        rubroParagraph.add(new Text(rubro));
                        rubroParagraph
                                .setFontSize(rubroFontSize)
                                .setFontColor(rubroColor)
                                .setTextAlignment(TextAlignment.CENTER);
                        cell.add(rubroParagraph);
                    }

                    if (subRubroColumn) { // SUBRUBRO
                        final String subRubro = getCellValue(row.getCell(3), evaluator);
                        final Paragraph subRubroParagraph = new Paragraph();
                        subRubroParagraph.add(new Text("SUB RUBRO: ").setBold());
                        subRubroParagraph.add(new Text(subRubro));
                        subRubroParagraph
                                .setFontSize(subRubroFontSize)
                                .setFontColor(subRubroColor)
                                .setTextAlignment(TextAlignment.CENTER);
                        cell.add(subRubroParagraph);
                    }

                    if (marcaColumn) { // MARCA
                        final String marca = getCellValue(row.getCell(4), evaluator);
                        final Paragraph marcaParagraph = new Paragraph();
                        marcaParagraph.add(new Text("MARCA: ").setBold());
                        marcaParagraph.add(new Text(marca));
                        marcaParagraph
                                .setFontSize(marcaFontSize)
                                .setFontColor(marcaColor)
                                .setTextAlignment(TextAlignment.CENTER);
                        cell.add(marcaParagraph);
                    }

                    if (precioColumn) { // PRECIO
                        try {
                            final BigDecimal precioVenta = new BigDecimal(getCellValue(row.getCell(5), evaluator));
                            final String formattedPrecioVenta = precioVenta.compareTo(BigDecimal.ZERO) == 0 ? "$ --" : String.format("$ %(,.2f", precioVenta);
                            final Paragraph precioParagraph = new Paragraph(formattedPrecioVenta).setBold();
                            precioParagraph
                                    .setFontSize(precioFontSize)
                                    .setFontColor(precioColor)
                                    .setTextAlignment(TextAlignment.CENTER);
                            cell.add(precioParagraph);
                        } catch (NumberFormatException nfe) {
                            throw new NumberFormatException("Fila #" + (i + 1) + " el PRECIO DE VENTA es incorrecto.");
                        }
                    }

                    if (codigoExternoColumn) { // CODIGO EXTERNO
                        final String codigoExterno = getCellValue(row.getCell(6), evaluator);
                        final Paragraph codExtParagraph = new Paragraph();
                        codExtParagraph.add(new Text("COD. EXT.: ").setBold());
                        codExtParagraph.add(new Text(codigoExterno));
                        codExtParagraph
                                .setFontSize(codigoExternoFontSize)
                                .setFontColor(codigoExternoColor)
                                .setTextAlignment(TextAlignment.CENTER);
                        cell.add(codExtParagraph);
                    }

                    if (imagenes) { // IMAGENES
                        try {
                            final double codigoValue = Double.parseDouble(getCellValue(row.getCell(0), evaluator));
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
                                Platform.runLater(() -> {
                                    logTextArea.setStyle("-fx-text-fill: #d3d700;");
                                    logTextArea.appendText("Advertencia: La imagen \"" + codigo + "\" no existe.\n");
                                });
                            }
                            image
                                    .setHeight(imageSize)
                                    .setWidth(imageSize)
                                    .setHorizontalAlignment(HorizontalAlignment.CENTER)
                                    .setMarginBottom(0);
                            cell.add(image);
                        } catch (NumberFormatException nfe) {
                            throw new NumberFormatException("Fila #" + (i + 1) + " el CODIGO no es un número.");
                        }
                    }

                    if (links) { // LINKS
                        final String producto = getCellValue(row.getCell(1), evaluator);
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

                    cell
                            .setTextAlignment(TextAlignment.CENTER)
                            .setHorizontalAlignment(HorizontalAlignment.CENTER)
                            .setVerticalAlignment(VerticalAlignment.MIDDLE);
//                    cell.setKeepTogether(true);

                    table.addCell(cell);
//                    System.out.println("cell height: " + cell.getHeight());

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
                    generatedRows++;
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
                agregarNumeroPagina(pdfDoc, doc);
            } catch (Exception e) {
                throw e;
            }
        } catch (Exception e) {
            throw e;
        }

        return generatedRows;
    }

    private boolean isEmptyRow(Row row) {
        for (int i = row.getFirstCellNum(); i <= row.getLastCellNum(); i++) {
            org.apache.poi.ss.usermodel.Cell cell = row.getCell(i);
            if (cell != null && cell.getCellType() != CellType.BLANK) {
                return false;
            }
        }
        return true;
    }

    private String getCellValue(org.apache.poi.ss.usermodel.Cell cell, FormulaEvaluator evaluator) {
        if (cell == null) {
            return "";
        }

        final CellType cellType = cell.getCellType();
        switch (cellType) {
            case STRING:
                return cell.getStringCellValue();
            case NUMERIC:
                if (DateUtil.isCellDateFormatted(cell)) { // date values
                    return cell.getDateCellValue().toString();
                } else { // numeric values
                    return String.valueOf(cell.getNumericCellValue());
                }
            case BOOLEAN:
                return String.valueOf(cell.getBooleanCellValue());
            case FORMULA:
                final CellValue formulaValue = evaluator.evaluate(cell);
                switch (formulaValue.getCellType()) {
                    case STRING:
                        return formulaValue.getStringValue();
                    case NUMERIC:
                        if (DateUtil.isCellDateFormatted(cell)) {
                            return cell.getDateCellValue().toString();
                        } else {
                            return String.valueOf(formulaValue.getNumberValue());
                        }
                    case BOOLEAN:
                        return String.valueOf(formulaValue.getBooleanValue());
                    default:
                        return "";
                }
            case BLANK:
                return "";
            default:
                return "";
        }
    }

    private void agregarNumeroPagina(PdfDocument pdfDoc, Document doc) {
        final int numberOfPages = pdfDoc.getNumberOfPages();
        final DeviceRgb fontColor = new DeviceRgb(128, 128, 128);
        for (int i = 1; i <= numberOfPages; i++) {
            // Write aligned text to the specified parameters point
            doc.showTextAligned(new Paragraph(String.format("Página %s de %s", i, numberOfPages)).setFontSize(5).setFontColor(fontColor),
                    pageWidth / 2, 3, i, TextAlignment.CENTER, VerticalAlignment.MIDDLE, 0);
        }
    }

    private void agregarCaratula(PdfDocument targetPdfDoc, Document doc) throws Exception {
        // Path to the PDF file containing the page to be added
        final String pageFilePath = archivoPdf.getAbsolutePath();
        try (// Create a PdfReader object for the page PDF file
             PdfReader pagePdfReader = new PdfReader(pageFilePath);
             // Create a PdfDocument object for the page PDF
             PdfDocument pagePdfDocument = new PdfDocument(pagePdfReader)) {
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

    private void addBookmarks(PdfDocument pdfDoc, String productName, int pageNumber) {
        pdfDoc.addNamedDestination(productName, PdfExplicitDestination.createFit(pdfDoc.getPage(pageNumber)).getPdfObject());
    }

}

