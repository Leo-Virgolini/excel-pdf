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
import com.itextpdf.layout.properties.AreaBreakType;
import com.itextpdf.layout.properties.HorizontalAlignment;
import com.itextpdf.layout.properties.TextAlignment;
import com.itextpdf.layout.properties.VerticalAlignment;
import javafx.application.Platform;
import javafx.concurrent.Service;
import javafx.concurrent.Task;
import javafx.scene.control.TextArea;
import org.apache.commons.io.FileUtils;
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

    public GeneratePDFService(File archivoExcel, File carpetaImagenes, float fontSizeCodigo, float fontSizeProducto, float fontSizeRubro, float fontSizeSubRubro,
                              float fontSizeMarca, float fontSizePrecio, float fontSizeCodigoExterno, float imageSize, float pageWidth, float pageHeight, boolean codigoColumn,
                              boolean productoColumn, boolean rubroColumn, boolean subRubroColumn, boolean marcaColumn, boolean precioColumn, boolean codigoExternoColumn, boolean imagenes,
                              boolean links, TextArea logTextArea) {
        this.archivoExcel = archivoExcel;
        this.carpetaImagenes = carpetaImagenes;
        this.fontSizeCodigo = fontSizeCodigo;
        this.fontSizeProducto = fontSizeProducto;
        this.fontSizeRubro = fontSizeRubro;
        this.fontSizeSubRubro = fontSizeSubRubro;
        this.fontSizeMarca = fontSizeMarca;
        this.fontSizePrecio = fontSizePrecio;
        this.fontSizeCodigoExterno = fontSizeCodigoExterno;
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
                        imageSize, pageWidth, pageHeight, codigoColumn, productoColumn, rubroColumn, subRubroColumn, marcaColumn, precioColumn, codigoExternoColumn, imagenes, links, logTextArea);
            }
        };
    }

    private Integer generarPDF(File archivoExcel, File carpetaImagenes, float fontSizeCodigo, float fontSizeProducto, float fontSizeRubro, float fontSizeSubRubro,
                               float fontSizeMarca, float fontSizePrecio, float fontSizeCodigoExterno, float imageSize, float pageWidth, float pageHeight, boolean codigoColumn, boolean productoColumn,
                               boolean rubroColumn, boolean subRubroColumn, boolean marcaColumn, boolean precioColumn, boolean codigoExternoColumn, boolean imagenes, boolean links, TextArea logTextArea) throws Exception {

        final String[] supportedExtensions = {".jpg", ".jpeg", ".png", ".bmp"};
        byte[] sinImagenData = null;
        for (String extension : supportedExtensions) {
            File sinImagenfile = new File(carpetaImagenes.getAbsolutePath(), "SINIMAGEN" + extension);
            if (sinImagenfile.isFile()) {
                sinImagenData = FileUtils.readFileToByteArray(sinImagenfile);
                break;
            }
        }

        final int productsPerPage = 20;
        final int rowsPerPage = 5;
        int generatedRows = 0;
        // Read the Excel file
        try (final FileInputStream inputStream = new FileInputStream(archivoExcel.getAbsolutePath());
             final Workbook workbook = new XSSFWorkbook(inputStream)) {
            // Code that uses the workbook
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
                Table table = new Table(new float[]{1, 1, 1, 1}).useAllAvailableWidth();
                // Loop through each row in the Excel sheet
                for (int i = 1; i < rowCount; i++) {
                    Row row = sheet.getRow(i);
                    // Create a new table cell with the image and product data
                    Cell cell = new Cell();
                    // Read the product data from the Excel rows
                    String codigo = null;
                    if (codigoColumn) { // CODIGO
                        try {
                            final double codigoValue = Double.parseDouble(getCellValue(row.getCell(0)));
                            codigo = (codigoValue % 1 == 0) ? String.format("%.0f", codigoValue) : String.valueOf(codigoValue);
                        } catch (NumberFormatException nfe) {
                            throw new NumberFormatException("Fila #" + (i + 1) + " el CODIGO no es un número.");
                        }

                        final Paragraph codigoParagraph = new Paragraph();
                        codigoParagraph.add(new Text("CODIGO: ").setBold());
                        codigoParagraph.add(new Text(codigo));
                        codigoParagraph.setFontSize(fontSizeCodigo).setTextAlignment(TextAlignment.CENTER);
                        cell.add(codigoParagraph);
                    }

                    String producto = null;
                    if (productoColumn) { // PRODUCTO
                        producto = getCellValue(row.getCell(1));
                        final Paragraph productoParagraph = new Paragraph(producto);
                        productoParagraph.setFontSize(fontSizeProducto).setFontColor(new DeviceRgb(0, 0, 139)).setTextAlignment(TextAlignment.CENTER);
                        cell.add(productoParagraph);
                    }

                    if (rubroColumn) { // RUBRO
                        String rubro = getCellValue(row.getCell(2));
                        final Paragraph rubroParagraph = new Paragraph();
                        rubroParagraph.add(new Text("RUBRO: ").setBold());
                        rubroParagraph.add(new Text(rubro));
                        rubroParagraph.setFontSize(fontSizeRubro).setTextAlignment(TextAlignment.CENTER);
                        cell.add(rubroParagraph);
                    }

                    if (subRubroColumn) { // SUBRUBRO
                        String subRubro = getCellValue(row.getCell(3));
                        final Paragraph subRubroParagraph = new Paragraph();
                        subRubroParagraph.add(new Text("SUB RUBRO: ").setBold());
                        subRubroParagraph.add(new Text(subRubro));
                        subRubroParagraph.setFontSize(fontSizeSubRubro).setTextAlignment(TextAlignment.CENTER);
                        cell.add(subRubroParagraph);
                    }

                    if (marcaColumn) { // MARCA
                        String marca = getCellValue(row.getCell(4));
                        final Paragraph marcaParagraph = new Paragraph();
                        marcaParagraph.add(new Text("MARCA: ").setBold());
                        marcaParagraph.add(new Text(marca));
                        marcaParagraph.setFontSize(fontSizeMarca).setTextAlignment(TextAlignment.CENTER);
                        cell.add(marcaParagraph);
                    }

                    if (precioColumn) { // PRECIO
                        try {
                            BigDecimal precioVenta = new BigDecimal(getCellValue(row.getCell(5)));
                            final String formattedPrecioVenta = precioVenta.compareTo(BigDecimal.ZERO) == 0 ? "$ --" : String.format("$ %(,.2f", precioVenta);
                            final Paragraph precioParagraph = new Paragraph(formattedPrecioVenta).setBold();
                            precioParagraph.setFontSize(fontSizePrecio).setFontColor(new DeviceRgb(139, 0, 0)).setTextAlignment(TextAlignment.CENTER);
                            cell.add(precioParagraph);
                        } catch (NumberFormatException nfe) {
                            throw new NumberFormatException("Fila #" + (i + 1) + " el PRECIO DE VENTA es incorrecto.");
                        }
                    }

                    if (codigoExternoColumn) { // CODIGO EXTERNO
                        String codigoExterno = getCellValue(row.getCell(6));
                        final Paragraph codExtParagraph = new Paragraph();
                        codExtParagraph.add(new Text("COD. EXT.: ").setBold());
                        codExtParagraph.add(new Text(codigoExterno));
                        codExtParagraph.setFontSize(fontSizeCodigoExterno).setTextAlignment(TextAlignment.CENTER);
                        cell.add(codExtParagraph);
                    }

                    if (imagenes && !codigo.isBlank()) { // IMAGENES
                        Image image;
                        // Load the product image
                        File imageFile = null;
                        for (String extension : supportedExtensions) {
                            File file = new File(carpetaImagenes.getAbsolutePath(), codigo + extension);
                            if (file.isFile()) {
                                imageFile = file;
                                break;
                            }
                        }
                        if (imageFile != null && imageFile.isFile()) {
                            byte[] imageData = FileUtils.readFileToByteArray(imageFile);
                            image = new Image(ImageDataFactory.create(imageData));
                        } else { // si no existe el archivo de la imagen
                            if (sinImagenData == null) {
                                throw new Exception("La imagen \"SINIMAGEN\" no está en la carpeta.");
                            } else {
                                image = new Image(ImageDataFactory.create(sinImagenData));
                            }
                            final String finalCodigo = codigo;
                            Platform.runLater(() -> {
                                logTextArea.setStyle("-fx-text-fill: #d3d700;");
                                logTextArea.appendText("Advertencia: La imagen \"" + finalCodigo + "\" no existe.\n");
                            });
                        }
                        image
                                .setHeight(imageSize)
                                .setWidth(imageSize)
                                .setHorizontalAlignment(HorizontalAlignment.CENTER)
                                .setMarginBottom(0);

                        cell.add(image);
                    }

                    if (links) { // LINKS
                        if (producto != null && !producto.isBlank()) {
                            Image button = new Image(ImageDataFactory.create(getClass().getResource("/images/button.png").toExternalForm()));
                            button
                                    .setHeight(25)
                                    .setWidth(60)
                                    .setHorizontalAlignment(HorizontalAlignment.CENTER)
                                    .setMarginBottom(0);
                            final String url = "https://kitchentools.com.ar/productos/" + producto.replaceAll("\\([^)]+\\)$", "").trim().replace(" ", "-");
                            button.setAction(PdfAction.createURI(url));
                            cell.add(button);
                        }
                    }

                    cell
                            .setTextAlignment(TextAlignment.CENTER)
                            .setVerticalAlignment(VerticalAlignment.TOP);
//                    cell.setHeight(100f);
                    table.addCell(cell);

                    // If we've added the maximum number of products per page, start a new page
                    if ((table.getNumberOfRows() % rowsPerPage == 0) && (i % productsPerPage == 0)) {
                        table.setFixedLayout();
                        doc.add(table);
                        doc.add(new AreaBreak(AreaBreakType.NEXT_PAGE));
                        table = new Table(new float[]{1, 1, 1, 1}).useAllAvailableWidth();
                    }
                    generatedRows++;
                }
                // If there are any remaining products, add them to the last page
                if (table.getNumberOfRows() > 0) {
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
                if (DateUtil.isCellDateFormatted(cell)) {
                    // date values
                    return cell.getDateCellValue().toString();
                } else {
                    // numeric values
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
        // Agregar numero de paginas
        final int numberOfPages = pdfDoc.getNumberOfPages();
        for (int i = 1; i <= numberOfPages; i++) {
            // Write aligned text to the specified parameters point
            doc.showTextAligned(new Paragraph(String.format("Página %s de %s", i, numberOfPages)).setFontSize(5).setFontColor(new DeviceRgb(128, 128, 128)),
                    pdfDoc.getPage(i).getPageSize().getWidth() / 2, 3, i, TextAlignment.CENTER, VerticalAlignment.MIDDLE, 0);
        }
    }

}

