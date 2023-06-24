package service;

import com.itextpdf.io.image.ImageData;
import com.itextpdf.io.image.ImageDataFactory;
import com.itextpdf.kernel.colors.DeviceRgb;
import com.itextpdf.kernel.geom.PageSize;
import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.kernel.pdf.PdfWriter;
import com.itextpdf.kernel.pdf.action.PdfAction;
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

import java.io.File;
import java.io.FileInputStream;
import java.math.BigDecimal;

public class GeneratePDFService extends Service<Integer> {

    private final File archivoExcel;
    private final File carpetaImagenes;
    private final float fontSizeCodigo;
    private final float fontSizeProducto;
    private final float fontSizeRubro;
    private final float fontSizeSubRubro;
    private final float fontSizeMarca;
    private final float fontSizePrecio;
    private final float fontSizeCodigoExterno;
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

    public GeneratePDFService(File archivoExcel, File carpetaImagenes, float fontSizeCodigo, float fontSizeProducto, float fontSizeRubro, float fontSizeSubRubro, float fontSizeMarca,
                              float fontSizePrecio, float fontSizeCodigoExterno, DeviceRgb codigoColor, DeviceRgb productoColor, DeviceRgb rubroColor, DeviceRgb subRubroColor, DeviceRgb marcaColor,
                              DeviceRgb precioColor, DeviceRgb codigoExternoColor, float imageSize, float pageWidth, float pageHeight, boolean codigoColumn, boolean productoColumn,
                              boolean rubroColumn, boolean subRubroColumn, boolean marcaColumn, boolean precioColumn, boolean codigoExternoColumn, boolean imagenes, boolean links, TextArea logTextArea) {
        this.archivoExcel = archivoExcel;
        this.carpetaImagenes = carpetaImagenes;
        this.fontSizeCodigo = fontSizeCodigo;
        this.fontSizeProducto = fontSizeProducto;
        this.fontSizeRubro = fontSizeRubro;
        this.fontSizeSubRubro = fontSizeSubRubro;
        this.fontSizeMarca = fontSizeMarca;
        this.fontSizePrecio = fontSizePrecio;
        this.fontSizeCodigoExterno = fontSizeCodigoExterno;
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
                return generarPDF(archivoExcel, carpetaImagenes, fontSizeCodigo, fontSizeProducto, fontSizeRubro, fontSizeSubRubro, fontSizeMarca, fontSizePrecio, fontSizeCodigoExterno,
                        codigoColor, productoColor, rubroColor, subRubroColor, marcaColor, precioColor, codigoExternoColor, imageSize, pageWidth, pageHeight, codigoColumn, productoColumn,
                        rubroColumn, subRubroColumn, marcaColumn, precioColumn, codigoExternoColumn, imagenes, links, logTextArea);
            }
        };
    }

    private Integer generarPDF(File archivoExcel, File carpetaImagenes, float fontSizeCodigo, float fontSizeProducto, float fontSizeRubro, float fontSizeSubRubro,
                               float fontSizeMarca, float fontSizePrecio, float fontSizeCodigoExterno, DeviceRgb codigoColor, DeviceRgb productoColor, DeviceRgb rubroColor, DeviceRgb subRubroColor,
                               DeviceRgb marcaColor, DeviceRgb precioColor, DeviceRgb codigoExternoColor, float imageSize, float pageWidth, float pageHeight, boolean codigoColumn, boolean productoColumn,
                               boolean rubroColumn, boolean subRubroColumn, boolean marcaColumn, boolean precioColumn, boolean codigoExternoColumn, boolean imagenes, boolean links, TextArea logTextArea) throws Exception {

        final String[] supportedExtensions = {".jpg", ".jpeg", ".png", ".bmp"};

        final ImageData buttonImagenData = ImageDataFactory.create(getClass().getResource("/images/button.png").toExternalForm());

        Image sinImagen = null;
        for (String extension : supportedExtensions) {
            File sinImagenfile = new File(carpetaImagenes.getAbsolutePath(), "SINIMAGEN" + extension);
            if (sinImagenfile.isFile()) {
                sinImagen = new Image(ImageDataFactory.create(sinImagenfile.getAbsolutePath()));
                break;
            }
        }

        final int productsPerPage = 20;
        final int rowsPerPage = 5;
        int generatedRows = 0;
        // Read the Excel file
        try (final FileInputStream inputStream = new FileInputStream(archivoExcel.getAbsolutePath());
             final Workbook workbook = new XSSFWorkbook(inputStream)) {

            final Sheet sheet = workbook.getSheetAt(0);
            // Calculate the actual row count with data
            int rowCount = 0;
            for (Row row : sheet) {
                if (row != null && !isEmptyRow(row)) {
                    rowCount++;
                }
            }

            // Create a PDF document
            try (final PdfDocument pdfDoc = new PdfDocument(new PdfWriter("catálogo.pdf"));
                 final Document doc = new Document(pdfDoc, new PageSize(pageWidth, pageHeight), false)) {
                doc.setMargins(0, 0, 0, 0);
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
//                    cell.setMinHeight((pageHeight - 6) / rowsPerPage);
//                    cell.setMaxHeight((pageHeight - 6) / rowsPerPage);
//                    cell.setHeight((pageHeight - 6) / rowsPerPage);
                    if (codigoColumn) { // CODIGO
                        try {
                            final double codigoValue = Double.parseDouble(getCellValue(row.getCell(0)));
                            final String codigo = (codigoValue % 1 == 0) ? String.format("%.0f", codigoValue) : String.valueOf(codigoValue);
                            final Paragraph codigoParagraph = new Paragraph();
                            codigoParagraph.add(new Text("CODIGO: ").setBold());
                            codigoParagraph.add(new Text(codigo));
                            codigoParagraph
                                    .setFontSize(fontSizeCodigo)
                                    .setFontColor(codigoColor)
                                    .setTextAlignment(TextAlignment.CENTER);
                            cell.add(codigoParagraph);
                        } catch (NumberFormatException nfe) {
                            throw new NumberFormatException("Fila #" + (i + 1) + " el CODIGO no es un número.");
                        }
                    }

                    if (productoColumn) { // PRODUCTO
                        final String producto = getCellValue(row.getCell(1));
                        final Paragraph productoParagraph = new Paragraph(producto);
                        productoParagraph
                                .setFontSize(fontSizeProducto)
                                .setFontColor(productoColor)
                                .setTextAlignment(TextAlignment.CENTER);
                        cell.add(productoParagraph);
                    }

                    if (rubroColumn) { // RUBRO
                        final String rubro = getCellValue(row.getCell(2));
                        final Paragraph rubroParagraph = new Paragraph();
                        rubroParagraph.add(new Text("RUBRO: ").setBold());
                        rubroParagraph.add(new Text(rubro));
                        rubroParagraph
                                .setFontSize(fontSizeRubro)
                                .setFontColor(rubroColor)
                                .setTextAlignment(TextAlignment.CENTER);
                        cell.add(rubroParagraph);
                    }

                    if (subRubroColumn) { // SUBRUBRO
                        final String subRubro = getCellValue(row.getCell(3));
                        final Paragraph subRubroParagraph = new Paragraph();
                        subRubroParagraph.add(new Text("SUB RUBRO: ").setBold());
                        subRubroParagraph.add(new Text(subRubro));
                        subRubroParagraph
                                .setFontSize(fontSizeSubRubro)
                                .setFontColor(subRubroColor)
                                .setTextAlignment(TextAlignment.CENTER);
                        cell.add(subRubroParagraph);
                    }

                    if (marcaColumn) { // MARCA
                        final String marca = getCellValue(row.getCell(4));
                        final Paragraph marcaParagraph = new Paragraph();
                        marcaParagraph.add(new Text("MARCA: ").setBold());
                        marcaParagraph.add(new Text(marca));
                        marcaParagraph
                                .setFontSize(fontSizeMarca)
                                .setFontColor(marcaColor)
                                .setTextAlignment(TextAlignment.CENTER);
                        cell.add(marcaParagraph);
                    }

                    if (precioColumn) { // PRECIO
                        try {
                            final BigDecimal precioVenta = new BigDecimal(getCellValue(row.getCell(5)));
                            final String formattedPrecioVenta = precioVenta.compareTo(BigDecimal.ZERO) == 0 ? "$ --" : String.format("$ %(,.2f", precioVenta);
                            final Paragraph precioParagraph = new Paragraph(formattedPrecioVenta).setBold();
                            precioParagraph
                                    .setFontSize(fontSizePrecio)
                                    .setFontColor(precioColor)
                                    .setTextAlignment(TextAlignment.CENTER);
                            cell.add(precioParagraph);
                        } catch (NumberFormatException nfe) {
                            throw new NumberFormatException("Fila #" + (i + 1) + " el PRECIO DE VENTA es incorrecto.");
                        }
                    }

                    if (codigoExternoColumn) { // CODIGO EXTERNO
                        final String codigoExterno = getCellValue(row.getCell(6));
                        final Paragraph codExtParagraph = new Paragraph();
                        codExtParagraph.add(new Text("COD. EXT.: ").setBold());
                        codExtParagraph.add(new Text(codigoExterno));
                        codExtParagraph
                                .setFontSize(fontSizeCodigoExterno)
                                .setFontColor(codigoExternoColor)
                                .setTextAlignment(TextAlignment.CENTER);
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
                            } else { // si no existe el archivo de la imagen
                                if (sinImagen == null) {
                                    throw new Exception("La imagen \"SINIMAGEN\" no está en la carpeta.");
                                } else {
                                    image = sinImagen;
                                }
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
                        final String producto = getCellValue(row.getCell(1));
                        if (!producto.isBlank()) {
                            final Image button = new Image(buttonImagenData);
                            button
                                    .setHeight(25)
                                    .setWidth(60)
                                    .setHorizontalAlignment(HorizontalAlignment.CENTER)
                                    .setMarginBottom(0);
                            final String url = new StringBuilder().append("https://kitchentools.com.ar/productos/").append(producto.replaceAll("\\([^)]+\\)$", "").trim().replace(" ", "-")).toString();
                            button.setAction(PdfAction.createURI(url));
                            cell.add(button);
                        }
                    }

                    cell
                            .setTextAlignment(TextAlignment.CENTER)
                            .setHorizontalAlignment(HorizontalAlignment.CENTER)
                            .setVerticalAlignment(VerticalAlignment.MIDDLE);
//                    cell.setKeepTogether(true);

                    System.out.println("cell h:" + cell.getHeight());

                    table.addCell(cell);

                    // If we've added the maximum number of products per page, start a new page
                    if ((table.getNumberOfRows() % rowsPerPage == 0) && (i % productsPerPage == 0)) {
//                        table.setExtendBottomRow(true);
//                        table.setMaxHeight(pageHeight - 6);
                        table.setHeight(pageHeight - 6);
                        table.setMargin(0);
                        table.setPadding(0);
                        table.setFixedLayout();
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

    private String getCellValue(org.apache.poi.ss.usermodel.Cell cell) {
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
                return cell.getCellFormula();
            case BLANK:
                return "";
            default:
                return "";
        }
    }

    private void agregarNumeroPagina(PdfDocument pdfDoc, Document doc) {
        final int numberOfPages = pdfDoc.getNumberOfPages();
        for (int i = 1; i <= numberOfPages; i++) {
            // Write aligned text to the specified parameters point
            doc.showTextAligned(new Paragraph(String.format("Página %s de %s", i, numberOfPages)).setFontSize(5).setFontColor(new DeviceRgb(128, 128, 128)),
                    pageWidth / 2, 3, i, TextAlignment.CENTER, VerticalAlignment.MIDDLE, 0);
        }
    }

}

