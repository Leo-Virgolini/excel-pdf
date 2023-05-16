import com.itextpdf.io.image.ImageDataFactory;
import com.itextpdf.kernel.colors.DeviceRgb;
import com.itextpdf.kernel.geom.PageSize;
import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.kernel.pdf.PdfWriter;
import com.itextpdf.layout.Document;
import com.itextpdf.layout.element.*;
import com.itextpdf.layout.properties.AreaBreakType;
import com.itextpdf.layout.properties.HorizontalAlignment;
import com.itextpdf.layout.properties.TextAlignment;
import com.itextpdf.layout.properties.VerticalAlignment;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.stage.DirectoryChooser;
import javafx.stage.FileChooser;
import org.apache.commons.io.FileUtils;
import org.apache.log4j.BasicConfigurator;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import java.io.*;
import java.net.URL;
import java.util.ResourceBundle;

public class VentanaController implements Initializable {

    @FXML
    private TextField ubicacionExcel;
    @FXML
    private TextField ubicacionImagenes;
    @FXML
    private TextField fontSizeTextInput;
    @FXML
    private TextField imageSizeTextInput;
    @FXML
    private TextField pageWidthTextInput;
    @FXML
    private TextField pageHeightTextInput;
    @FXML
    private TextArea logTextArea;

    private static File archivoExcel;
    private static File carpetaImagenes;

    public void initialize(URL url, ResourceBundle rb) {
    }

    @FXML
    public void buscarExcel(ActionEvent event) {
        logTextArea.clear();
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Elige archivo .XLSX");
        fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("Archivo XLSX", "*.xlsx"));
        fileChooser.setInitialDirectory(new File(System.getProperty("user.dir")));
        archivoExcel = fileChooser.showOpenDialog(Main.stage);
        if (archivoExcel != null) {
            ubicacionExcel.setText(archivoExcel.getAbsolutePath());
        } else {
            ubicacionExcel.clear();
        }
    }

    @FXML
    public void buscarImagenes(ActionEvent event) {
        logTextArea.clear();
        DirectoryChooser directoryChooser = new DirectoryChooser();
        directoryChooser.setTitle("Selecciona la carpeta donde están las imágenes");
        directoryChooser.setInitialDirectory(new File(System.getProperty("user.dir")));
        carpetaImagenes = directoryChooser.showDialog(Main.stage);
        if (carpetaImagenes != null) {
            ubicacionImagenes.setText(carpetaImagenes.getAbsolutePath());
        } else {
            ubicacionImagenes.clear();
        }
    }

    @FXML
    public void generar(ActionEvent event) {
        logTextArea.clear();
        if (archivoExcel != null && archivoExcel.exists() && carpetaImagenes != null && carpetaImagenes.exists()) {
            try {
                if (isNumeric(fontSizeTextInput.getText()) && isNumeric(imageSizeTextInput.getText()) && isNumeric(pageWidthTextInput.getText()) && isNumeric(pageHeightTextInput.getText())) {
                    logTextArea.setStyle("-fx-text-fill: darkgreen;");
                    logTextArea.appendText("Generando PDF...\n");
                    generarPDF(Float.parseFloat(fontSizeTextInput.getText()), Float.parseFloat(imageSizeTextInput.getText()),
                            Float.parseFloat(pageWidthTextInput.getText()), Float.parseFloat(pageHeightTextInput.getText()), logTextArea);
                    logTextArea.setStyle("-fx-text-fill: darkgreen;");
                    logTextArea.appendText("\"catálogo.pdf\" generado correctamente.\n");
//                    logTextArea.appendText("Se descontaron US$100 de tu cuenta bancaria.\n¡Gracias!\n");
                } else {
                    logTextArea.setStyle("-fx-text-fill: firebrick;");
                    logTextArea.appendText("Completa los parámetros correctamente.\n");
                }
            } catch (Exception e) {
                e.printStackTrace();
                logTextArea.setStyle("-fx-text-fill: firebrick;");
                logTextArea.appendText("Error: " + e.getLocalizedMessage() + "\n");
            }
        } else {
            logTextArea.setStyle("-fx-text-fill: firebrick;");
            logTextArea.appendText("Error: las ubicaciones son incorrectas.\n");
        }
    }

    private static void generarPDF(Float fontSize, Float imageSize, Float pageWidth, Float pageHeight, TextArea logTextArea) throws Exception {
        BasicConfigurator.configure(); // configure Log4j

        final String[] supportedExtensions = {".jpg", ".jpeg", ".png", ".bmp"};
        // Read the Excel file
        final FileInputStream inputStream = new FileInputStream((archivoExcel.getAbsolutePath()));
        final Workbook workbook = new XSSFWorkbook(inputStream);
        final Sheet sheet = workbook.getSheetAt(0);
        // Create a PDF document
        final PdfDocument pdfDoc = new PdfDocument(new PdfWriter("catálogo.pdf"));
        final Document doc = new Document(pdfDoc, new PageSize(pageWidth, pageHeight), false);
        doc.setMargins(0, 0, 0, 0);

        final int rowCount = sheet.getPhysicalNumberOfRows();
        final int productsPerPage = 20;
        final int rowsPerPage = 5;

        // Create a table with 4 columns and 5 rows per page
        Table table = new Table(new float[]{1, 1, 1, 1}).useAllAvailableWidth();

        // Loop through each row in the Excel sheet
        for (int i = 1; i < rowCount; i++) {

            Row row = sheet.getRow(i);

//            Código	Producto	Rubro	Sub Rubro	Marca	Precio de Venta	    Código Externo
            // Read the product data from the Excel row
//            CellType cellType = row.getCell(0).getCellType();
            long codigo = row.getCell(0) == null ? 0 : (long) row.getCell(0).getNumericCellValue();
            String producto = row.getCell(1) == null ? "" : row.getCell(1).getStringCellValue();
            String rubro = row.getCell(2) == null ? "" : row.getCell(2).getStringCellValue();
            String subRubro = row.getCell(3) == null ? "" : row.getCell(3).getStringCellValue();
            String marca = row.getCell(4) == null ? "" : row.getCell(4).getStringCellValue();
            double precioVenta = row.getCell(5) == null ? 0 : row.getCell(5).getNumericCellValue();
            String codigoExterno = row.getCell(6) == null ? "" : row.getCell(6).getStringCellValue();

            // Load the product image
            Image image = null;
            if (codigo != 0) {

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
                } else {
                    byte[] imageData = FileUtils.readFileToByteArray(new File(carpetaImagenes.getAbsolutePath(), "SINIMAGEN.jpg"));
                    image = new Image(ImageDataFactory.create(imageData));
                    logTextArea.setStyle("-fx-text-fill: firebrick;");
                    logTextArea.appendText("Advertencia: La imagen \"" + codigo + "\" no existe.\n");
                }
//                image.setMarginTop(100);
//                image.setMarginBottom(100);
//                image.setAutoScale(true);
                image.setHeight(imageSize);
                image.setWidth(imageSize);
                image.setHorizontalAlignment(HorizontalAlignment.CENTER);
                image.setMarginBottom(0);
            }

            // Add the product data to the PDF document
            Paragraph p1 = new Paragraph();
            p1.add(new Text("CODIGO: ").setBold());
            p1.add(new Text("" + codigo));

            Paragraph p2 = new Paragraph(producto);

            Paragraph p3 = new Paragraph();
            p3.add(new Text("MARCA: ").setBold());
            p3.add(new Text(marca));

            Paragraph p4 = new Paragraph();
            p4.add(new Text("RUBRO: ").setBold());
            p4.add(new Text(rubro));

            Paragraph p5 = new Paragraph();
            p5.add(new Text("SUB RUBRO: ").setBold());
            p5.add(new Text(subRubro));

            Paragraph p6 = new Paragraph();
            p6.add(new Text("COD. EXT.: ").setBold());
            p6.add(new Text(codigoExterno));

            Paragraph p7 = new Paragraph(precioVenta != 0 ? String.format("$ %(,.2f", precioVenta) : "$ --").setBold();

            p1.setFontSize(fontSize).setTextAlignment(TextAlignment.CENTER);
            p2.setFontSize(fontSize).setFontColor(new DeviceRgb(0, 0, 139)).setTextAlignment(TextAlignment.CENTER);
            p3.setFontSize(fontSize).setTextAlignment(TextAlignment.CENTER);
            p4.setFontSize(fontSize).setTextAlignment(TextAlignment.CENTER);
            p5.setFontSize(fontSize).setTextAlignment(TextAlignment.CENTER);
            p6.setFontSize(fontSize).setTextAlignment(TextAlignment.CENTER);
            p7.setFontSize(7).setFontColor(new DeviceRgb(139, 0, 0)).setTextAlignment(TextAlignment.CENTER);

            // Create a new table cell with the image and product data
            Cell cell = new Cell();
            cell
                    .add(image)
                    .add(p1)
                    .add(p2)
                    .add(p3)
                    .add(p4)
                    .add(p5)
                    .add(p6)
                    .add(p7)
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

        // Close the PDF document
        doc.close();
        workbook.close();
    }

    private static boolean isNumeric(String strNum) {
        if (strNum == null) {
            return false;
        }
        try {
            Float.parseFloat(strNum);
        } catch (NumberFormatException nfe) {
            return false;
        }
        return true;
    }

}