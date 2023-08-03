package fx;

import com.itextpdf.kernel.colors.DeviceRgb;
import javafx.event.ActionEvent;
import javafx.event.Event;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.stage.DirectoryChooser;
import javafx.stage.FileChooser;
import org.apache.log4j.BasicConfigurator;
import service.GeneratePDFService;

import java.io.*;
import java.net.URL;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ResourceBundle;

public class VentanaController implements Initializable {

    @FXML
    private TextField ubicacionExcel;
    @FXML
    private TextField ubicacionImagenes;
    @FXML
    private TextField ubicacionCaratula;
    @FXML
    private TextField fontSizeCodigo;
    @FXML
    private TextField fontSizeProducto;
    @FXML
    private TextField fontSizeRubro;
    @FXML
    private TextField fontSizeSubRubro;
    @FXML
    private TextField fontSizeMarca;
    @FXML
    private TextField fontSizePrecio;
    @FXML
    private TextField fontSizeCodigoExterno;
    @FXML
    private ColorPicker codigoColorPicker;
    @FXML
    private ColorPicker productoColorPicker;
    @FXML
    private ColorPicker rubroColorPicker;
    @FXML
    private ColorPicker subRubroColorPicker;
    @FXML
    private ColorPicker marcaColorPicker;
    @FXML
    private ColorPicker precioColorPicker;
    @FXML
    private ColorPicker codigoExternoColorPicker;
    @FXML
    private TextField imageSizeTextInput;
    @FXML
    private TextField pageWidthTextInput;
    @FXML
    private TextField pageHeightTextInput;
    @FXML
    private CheckBox codigoCheckBox;
    @FXML
    private CheckBox productoCheckBox;
    @FXML
    private CheckBox rubroCheckBox;
    @FXML
    private CheckBox subRubroCheckBox;
    @FXML
    private CheckBox marcaCheckBox;
    @FXML
    private CheckBox precioCheckBox;
    @FXML
    private CheckBox codigoExternoCheckBox;
    @FXML
    private CheckBox imagenCheckBox;
    @FXML
    private CheckBox linksCheckBox;
    @FXML
    private TextArea logTextArea;
    @FXML
    private Button generarButton;
    @FXML
    private ProgressIndicator progressIndicator;

    private File archivoExcel;
    private File carpetaImagenes;
    private File archivoPdf;
    private File archivoDestino;

    public void initialize(URL url, ResourceBundle rb) {
        BasicConfigurator.configure(); // configure Log4j
    }

    @FXML
    public void buscarExcel(ActionEvent event) {
        logTextArea.clear();
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Elige archivo .XLSX");
        fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("Archivo XLSX", "*.xlsx"));
        final File defaultPath = new File("Z:\\Doc. Compartidos\\CATALOGOS");
        if (!defaultPath.exists() || !defaultPath.isDirectory()) {
            fileChooser.setInitialDirectory(new File(System.getProperty("user.dir")));
        } else {
            fileChooser.setInitialDirectory(defaultPath);
        }
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
        final File defaultPath = new File("Z:\\Doc. Compartidos\\DUX ERP Linea GE\\IMAGENES");
        if (!defaultPath.exists() || !defaultPath.isDirectory()) {
            directoryChooser.setInitialDirectory(new File(System.getProperty("user.dir")));
        } else {
            directoryChooser.setInitialDirectory(defaultPath);
        }
        carpetaImagenes = directoryChooser.showDialog(Main.stage);
        if (carpetaImagenes != null) {
            ubicacionImagenes.setText(carpetaImagenes.getAbsolutePath());
        } else {
            ubicacionImagenes.clear();
        }
    }

    @FXML
    public void buscarCaratula(ActionEvent event) {
        logTextArea.clear();
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Elige archivo .PDF");
        fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("Archivo PDF", "*.pdf"));
        final File defaultPath = new File("Z:\\Doc. Compartidos\\CATALOGOS\\CATALOGOS VENDEDORES\\CARATULAS");
        if (!defaultPath.exists() || !defaultPath.isDirectory()) {
            fileChooser.setInitialDirectory(new File(System.getProperty("user.dir")));
        } else {
            fileChooser.setInitialDirectory(defaultPath);
        }
        archivoPdf = fileChooser.showOpenDialog(Main.stage);
        if (archivoPdf != null) {
            ubicacionCaratula.setText(archivoPdf.getAbsolutePath());
        } else {
            ubicacionCaratula.clear();
        }
    }

    @FXML
    public void generar(ActionEvent event) {
        logTextArea.clear();
        if (archivoExcel != null && archivoExcel.isFile() && carpetaImagenes != null && carpetaImagenes.exists()) {
            if (validarTextInputs()) {
                if (elegirDestino()) {
                    GeneratePDFService service = new GeneratePDFService(archivoExcel, carpetaImagenes, archivoPdf, archivoDestino,
                            Float.parseFloat(fontSizeCodigo.getText()), Float.parseFloat(fontSizeProducto.getText()), Float.parseFloat(fontSizeRubro.getText()), Float.parseFloat(fontSizeSubRubro.getText()),
                            Float.parseFloat(fontSizeMarca.getText()), Float.parseFloat(fontSizePrecio.getText()), Float.parseFloat(fontSizeCodigoExterno.getText()),
                            new DeviceRgb((int) (codigoColorPicker.getValue().getRed() * 255), (int) (codigoColorPicker.getValue().getGreen() * 255), (int) (codigoColorPicker.getValue().getBlue() * 255)),
                            new DeviceRgb((int) (productoColorPicker.getValue().getRed() * 255), (int) (productoColorPicker.getValue().getGreen() * 255), (int) (productoColorPicker.getValue().getBlue() * 255)),
                            new DeviceRgb((int) (rubroColorPicker.getValue().getRed() * 255), (int) (rubroColorPicker.getValue().getGreen() * 255), (int) (rubroColorPicker.getValue().getBlue() * 255)),
                            new DeviceRgb((int) (subRubroColorPicker.getValue().getRed() * 255), (int) (subRubroColorPicker.getValue().getGreen() * 255), (int) (subRubroColorPicker.getValue().getBlue() * 255)),
                            new DeviceRgb((int) (marcaColorPicker.getValue().getRed() * 255), (int) (marcaColorPicker.getValue().getGreen() * 255), (int) (marcaColorPicker.getValue().getBlue() * 255)),
                            new DeviceRgb((int) (precioColorPicker.getValue().getRed() * 255), (int) (precioColorPicker.getValue().getGreen() * 255), (int) (precioColorPicker.getValue().getBlue() * 255)),
                            new DeviceRgb((int) (codigoExternoColorPicker.getValue().getRed() * 255), (int) (codigoExternoColorPicker.getValue().getGreen() * 255), (int) (codigoExternoColorPicker.getValue().getBlue() * 255)),
                            Float.parseFloat(imageSizeTextInput.getText()), Float.parseFloat(pageWidthTextInput.getText()), Float.parseFloat(pageHeightTextInput.getText()),
                            codigoCheckBox.isSelected(), productoCheckBox.isSelected(), rubroCheckBox.isSelected(), subRubroCheckBox.isSelected(), marcaCheckBox.isSelected(), precioCheckBox.isSelected(),
                            codigoExternoCheckBox.isSelected(), imagenCheckBox.isSelected(), linksCheckBox.isSelected(), logTextArea);
                    service.setOnRunning(e -> {
                        generarButton.setDisable(true);
                        progressIndicator.setVisible(true);
                        logTextArea.setStyle("-fx-text-fill: darkblue;");
                        logTextArea.appendText("Generando PDF...\n");
                    });
                    service.setOnSucceeded(e -> {
                        logTextArea.setStyle("-fx-text-fill: darkgreen;");
                        logTextArea.appendText("Se han generado " + service.getValue() + " productos.\n");
                        logTextArea.appendText('"' + archivoDestino.getAbsolutePath() + "\" generado.\n");
                        generarButton.setDisable(false);
                        progressIndicator.setVisible(false);
                    });
                    service.setOnFailed(e -> {
//                    service.getException().printStackTrace();
                        logTextArea.setStyle("-fx-text-fill: firebrick;");
                        logTextArea.appendText("Error: " + service.getException().getLocalizedMessage() + "\n");
                        generarButton.setDisable(false);
                        progressIndicator.setVisible(false);
                    });
                    service.start();
                }
            } else {
                logTextArea.setStyle("-fx-text-fill: firebrick;");
                logTextArea.appendText("Completa los parámetros correctamente.\n");
            }
        } else {
            logTextArea.setStyle("-fx-text-fill: firebrick;");
            logTextArea.appendText("Error: las ubicaciones son incorrectas.\n");
        }
    }

    @FXML
    public void onClickLinks(Event event) {
        if (linksCheckBox.isSelected()) {
            imageSizeTextInput.setText("64");
        } else {
            imageSizeTextInput.setText("89");
        }
    }

    @FXML
    public void onClickImagenes(Event event) {
        if (imagenCheckBox.isSelected()) {
            imageSizeTextInput.setDisable(false);
        } else {
            imageSizeTextInput.setDisable(true);
        }
    }

    @FXML
    public void onClickCodigoColumn(Event event) {
        if (codigoCheckBox.isSelected()) {
            fontSizeCodigo.setDisable(false);
            codigoColorPicker.setDisable(false);
        } else {
            fontSizeCodigo.setDisable(true);
            codigoColorPicker.setDisable(true);
        }
    }

    @FXML
    public void onClickProductoColumn(Event event) {
        if (productoCheckBox.isSelected()) {
            fontSizeProducto.setDisable(false);
            productoColorPicker.setDisable(false);
        } else {
            fontSizeProducto.setDisable(true);
            productoColorPicker.setDisable(true);
        }
    }

    @FXML
    public void onClickRubroColumn(Event event) {
        if (rubroCheckBox.isSelected()) {
            fontSizeRubro.setDisable(false);
            rubroColorPicker.setDisable(false);
            rubroColorPicker.getValue();
        } else {
            fontSizeRubro.setDisable(true);
            rubroColorPicker.setDisable(true);
        }
    }

    @FXML
    public void onClickSubRubroColumn(Event event) {
        if (subRubroCheckBox.isSelected()) {
            fontSizeSubRubro.setDisable(false);
            subRubroColorPicker.setDisable(false);
        } else {
            fontSizeSubRubro.setDisable(true);
            subRubroColorPicker.setDisable(true);
        }
    }

    @FXML
    public void onClickMarcaColumn(Event event) {
        if (marcaCheckBox.isSelected()) {
            fontSizeMarca.setDisable(false);
            marcaColorPicker.setDisable(false);
        } else {
            fontSizeMarca.setDisable(true);
            marcaColorPicker.setDisable(true);
        }
    }

    @FXML
    public void onClickPrecioColumn(Event event) {
        if (precioCheckBox.isSelected()) {
            fontSizePrecio.setDisable(false);
            precioColorPicker.setDisable(false);
        } else {
            fontSizePrecio.setDisable(true);
            precioColorPicker.setDisable(true);
        }
    }

    @FXML
    public void onClickCodigoExternoColumn(Event event) {
        if (codigoExternoCheckBox.isSelected()) {
            fontSizeCodigoExterno.setDisable(false);
            codigoExternoColorPicker.setDisable(false);
        } else {
            fontSizeCodigoExterno.setDisable(true);
            codigoExternoColorPicker.setDisable(true);
        }
    }

    private boolean isNumeric(String strNum) {
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

    private boolean validarTextInputs() {
        return isNumeric(fontSizeCodigo.getText()) && isNumeric(fontSizeProducto.getText()) && isNumeric(fontSizeRubro.getText()) && isNumeric(fontSizeSubRubro.getText())
                && isNumeric(fontSizeMarca.getText()) && isNumeric(fontSizePrecio.getText()) && isNumeric(fontSizeCodigoExterno.getText()) &&
                isNumeric(imageSizeTextInput.getText()) && isNumeric(pageWidthTextInput.getText()) && isNumeric(pageHeightTextInput.getText());
    }

    private boolean elegirDestino() {
        final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd-MM-yy");
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Seleccione destino y nombre a guardar");
        final File defaultPath = new File("Z:\\Doc. Compartidos\\CATALOGOS\\CATALOGOS VENDEDORES");
        if (!defaultPath.exists() || !defaultPath.isDirectory()) {
            fileChooser.setInitialDirectory(new File(System.getProperty("user.dir")));
        } else {
            fileChooser.setInitialDirectory(defaultPath);
        }
        fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("Archivo PDF", "*.pdf"));
        fileChooser.setInitialFileName((archivoPdf != null ? archivoPdf.getName().replaceFirst("[.][^.]+$", "").toUpperCase() : "") + " - " + formatter.format(LocalDate.now()));
        archivoDestino = fileChooser.showSaveDialog(Main.stage);
        if (archivoDestino != null) {
            return true;
        } else {
            return false;
        }
    }

}