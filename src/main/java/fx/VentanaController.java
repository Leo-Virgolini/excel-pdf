package fx;

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
import java.util.ResourceBundle;

public class VentanaController implements Initializable {

    @FXML
    private TextField ubicacionExcel;
    @FXML
    private TextField ubicacionImagenes;
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

    private static File archivoExcel;
    private static File carpetaImagenes;

    public void initialize(URL url, ResourceBundle rb) {
        BasicConfigurator.configure(); // configure Log4j
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
        if (archivoExcel != null && archivoExcel.isFile() && carpetaImagenes != null && carpetaImagenes.exists()) {
            if (validarTextInputs()) {
                GeneratePDFService service = new GeneratePDFService(archivoExcel, carpetaImagenes,
                        Float.parseFloat(fontSizeCodigo.getText()), Float.parseFloat(fontSizeProducto.getText()), Float.parseFloat(fontSizeRubro.getText()), Float.parseFloat(fontSizeSubRubro.getText()),
                        Float.parseFloat(fontSizeMarca.getText()), Float.parseFloat(fontSizePrecio.getText()), Float.parseFloat(fontSizeCodigoExterno.getText()),
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
                    logTextArea.appendText('"' + System.getProperty("user.dir") + System.getProperty("file.separator") + "catálogo.pdf\" generado.\n");
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
        } else {
            fontSizeCodigo.setDisable(true);
        }
    }

    @FXML
    public void onClickProductoColumn(Event event) {
        if (productoCheckBox.isSelected()) {
            fontSizeProducto.setDisable(false);
        } else {
            fontSizeProducto.setDisable(true);
        }
    }

    @FXML
    public void onClickRubroColumn(Event event) {
        if (rubroCheckBox.isSelected()) {
            fontSizeRubro.setDisable(false);
        } else {
            fontSizeRubro.setDisable(true);
        }
    }

    @FXML
    public void onClickSubRubroColumn(Event event) {
        if (subRubroCheckBox.isSelected()) {
            fontSizeSubRubro.setDisable(false);
        } else {
            fontSizeSubRubro.setDisable(true);
        }
    }

    @FXML
    public void onClickMarcaColumn(Event event) {
        if (marcaCheckBox.isSelected()) {
            fontSizeMarca.setDisable(false);
        } else {
            fontSizeMarca.setDisable(true);
        }
    }

    @FXML
    public void onClickPrecioColumn(Event event) {
        if (precioCheckBox.isSelected()) {
            fontSizePrecio.setDisable(false);
        } else {
            fontSizePrecio.setDisable(true);
        }
    }

    @FXML
    public void onClickCodigoExternoColumn(Event event) {
        if (codigoExternoCheckBox.isSelected()) {
            fontSizeCodigoExterno.setDisable(false);
        } else {
            fontSizeCodigoExterno.setDisable(true);
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

}