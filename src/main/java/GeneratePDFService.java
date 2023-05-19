import com.itextpdf.io.image.ImageDataFactory;
import com.itextpdf.kernel.colors.DeviceRgb;
import com.itextpdf.kernel.geom.PageSize;
import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.kernel.pdf.PdfWriter;
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

public class GeneratePDFService extends Service<Void> {

    private final File archivoExcel;
    private final File carpetaImagenes;
    private final float fontSize;
    private final float imageSize;
    private final float pageWidth;
    private final float pageHeight;
    private final TextArea logTextArea;

    public GeneratePDFService(File archivoExcel, File carpetaImagenes, float fontSize, float imageSize, float pageWidth, float pageHeight, TextArea logTextArea) {
        this.archivoExcel = archivoExcel;
        this.carpetaImagenes = carpetaImagenes;
        this.fontSize = fontSize;
        this.imageSize = imageSize;
        this.pageWidth = pageWidth;
        this.pageHeight = pageHeight;
        this.logTextArea = logTextArea;
    }

    @Override
    protected Task<Void> createTask() {
        return new Task<Void>() {
            @Override
            protected Void call() throws Exception {
                generarPDF(archivoExcel, carpetaImagenes, fontSize, imageSize, pageWidth, pageHeight, logTextArea);
                return null;
            }
        };
    }

    private void generarPDF(File archivoExcel, File carpetaImagenes, float fontSize, float imageSize, float pageWidth, float pageHeight, TextArea logTextArea) throws Exception {
        final String[] supportedExtensions = {".jpg", ".jpeg", ".png", ".bmp"};
        final int productsPerPage = 20;
        final int rowsPerPage = 5;
        // Read the Excel file
        final FileInputStream inputStream = new FileInputStream((archivoExcel.getAbsolutePath()));
        final Workbook workbook = new XSSFWorkbook(inputStream);
        final Sheet sheet = workbook.getSheetAt(0);
        // Calculate the actual row count with data
        int rowCount = 0;
        for (Row row : sheet) {
            if (row != null && !isEmptyRow(row)) {
                rowCount++;
            }
        }
        // Create a PDF document
        final PdfDocument pdfDoc = new PdfDocument(new PdfWriter("catálogo.pdf"));
        final Document doc = new Document(pdfDoc, new PageSize(pageWidth, pageHeight), false);
        try {
            doc.setMargins(0, 0, 0, 0);
            // Create a table with 4 columns and 5 rows per page
            Table table = new Table(new float[]{1, 1, 1, 1}).useAllAvailableWidth();

            // Loop through each row in the Excel sheet
            for (int i = 1; i < rowCount; i++) {
                Row row = sheet.getRow(i);

                // Read the product data from the Excel row
                String codigo = String.format("%.0f", Double.parseDouble(getCellValue(row.getCell(0))));
                String producto = getCellValue(row.getCell(1));
                String rubro = getCellValue(row.getCell(2));
                String subRubro = getCellValue(row.getCell(3));
                String marca = getCellValue(row.getCell(4));
                BigDecimal precioVenta;
                try {
                    precioVenta = new BigDecimal(getCellValue(row.getCell(5)));
                } catch (NumberFormatException nfe) {
                    throw new Exception("En la fila #" + (i + 1) + " el precio de venta es incorrecto.");
                }
                String codigoExterno = getCellValue(row.getCell(6));

                // Load the product image
                Image image;
                if (!codigo.isBlank()) {

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
                        byte[] imageData = FileUtils.readFileToByteArray(new File(carpetaImagenes.getAbsolutePath(), "SINIMAGEN.jpg"));
                        image = new Image(ImageDataFactory.create(imageData));
                        Platform.runLater(() -> {
                            logTextArea.setStyle("-fx-text-fill: #d3d700;");
                            logTextArea.appendText("Advertencia: La imagen \"" + codigo + "\" no existe.\n");
                        });
                    }
//                    image.setHeight(imageSize);
//                    image.setWidth(imageSize);
//                    image.setHorizontalAlignment(HorizontalAlignment.CENTER);
//                    image.setMarginBottom(0);
                } else { // si no hay Código
                    throw new Exception("En la fila #" + (i + 1) + " tienes un Código en blanco.");
                }

                image.setHeight(imageSize);
                image.setWidth(imageSize);
                image.setHorizontalAlignment(HorizontalAlignment.CENTER);
                image.setMarginBottom(0);

                // Add the product data to the PDF document
                Paragraph codigoParagraph = new Paragraph();
                codigoParagraph.add(new Text("CODIGO: ").setBold());
                codigoParagraph.add(new Text(codigo));

                Paragraph productoParagraph = new Paragraph(producto);

                Paragraph marcaParagraph = new Paragraph();
                marcaParagraph.add(new Text("MARCA: ").setBold());
                marcaParagraph.add(new Text(marca));

                Paragraph rubroParagraph = new Paragraph();
                rubroParagraph.add(new Text("RUBRO: ").setBold());
                rubroParagraph.add(new Text(rubro));

                Paragraph subRubroParagraph = new Paragraph();
                subRubroParagraph.add(new Text("SUB RUBRO: ").setBold());
                subRubroParagraph.add(new Text(subRubro));

                Paragraph codExtParagraph = new Paragraph();
                codExtParagraph.add(new Text("COD. EXT.: ").setBold());
                codExtParagraph.add(new Text(codigoExterno));

                final String formattedPrecioVenta = precioVenta.compareTo(BigDecimal.ZERO) == 0 ? "$ --" : String.format("$ %(,.2f", precioVenta);
                Paragraph precioParagraph = new Paragraph(formattedPrecioVenta).setBold();

                codigoParagraph.setFontSize(fontSize).setTextAlignment(TextAlignment.CENTER);
                productoParagraph.setFontSize(fontSize).setFontColor(new DeviceRgb(0, 0, 139)).setTextAlignment(TextAlignment.CENTER);
                marcaParagraph.setFontSize(fontSize).setTextAlignment(TextAlignment.CENTER);
                rubroParagraph.setFontSize(fontSize).setTextAlignment(TextAlignment.CENTER);
                subRubroParagraph.setFontSize(fontSize).setTextAlignment(TextAlignment.CENTER);
                codExtParagraph.setFontSize(fontSize).setTextAlignment(TextAlignment.CENTER);
                precioParagraph.setFontSize(7).setFontColor(new DeviceRgb(139, 0, 0)).setTextAlignment(TextAlignment.CENTER);

                // Create a new table cell with the image and product data
                final Cell cell = new Cell();
                cell
                        .add(image)
                        .add(codigoParagraph)
                        .add(productoParagraph)
                        .add(marcaParagraph)
                        .add(rubroParagraph)
                        .add(subRubroParagraph)
                        .add(codExtParagraph)
                        .add(precioParagraph)
                        .setTextAlignment(TextAlignment.CENTER)
                        .setVerticalAlignment(VerticalAlignment.TOP);
//            cell.setWidth(620f);
//            cell.setHeight(100f);
                table.addCell(cell);

                // If we've added the maximum number of products per page, start a new page
                if ((table.getNumberOfRows() % rowsPerPage == 0) && (i % productsPerPage == 0)) {
                    table.setFixedLayout();
//                table.setAutoLayout();
//                table.setWidth(PageSize.A4.getWidth());
//                table.setHeight(PageSize.A4.getHeight());
                    doc.add(table);
                    doc.add(new AreaBreak(AreaBreakType.NEXT_PAGE));
                    table = new Table(new float[]{1, 1, 1, 1}).useAllAvailableWidth();
                }
            }

            // If there are any remaining products, add them to the last page
            if (table.getNumberOfRows() > 0) {
                table.setFixedLayout();
                doc.add(table);
            }

            // Agregar numero de paginas
            final int numberOfPages = pdfDoc.getNumberOfPages();
            for (int i = 1; i <= numberOfPages; i++) {
                // Write aligned text to the specified parameters point
                doc.showTextAligned(new Paragraph(String.format("Página %s de %s", i, numberOfPages)).setFontSize(5).setFontColor(new DeviceRgb(128, 128, 128)),
                        pdfDoc.getPage(i).getPageSize().getWidth() / 2, 3, i, TextAlignment.CENTER, VerticalAlignment.MIDDLE, 0);
            }
        } finally {
            // Close the PDF & Excel documents
            doc.close();
            workbook.close();
        }
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

}

