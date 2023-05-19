import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.stage.DirectoryChooser;
import javafx.stage.FileChooser;
import org.apache.log4j.BasicConfigurator;

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
            if (isNumeric(fontSizeTextInput.getText()) && isNumeric(imageSizeTextInput.getText()) && isNumeric(pageWidthTextInput.getText()) && isNumeric(pageHeightTextInput.getText())) {
                GeneratePDFService service = new GeneratePDFService(archivoExcel, carpetaImagenes, Float.parseFloat(fontSizeTextInput.getText()), Float.parseFloat(imageSizeTextInput.getText()),
                        Float.parseFloat(pageWidthTextInput.getText()), Float.parseFloat(pageHeightTextInput.getText()), logTextArea);
                service.setOnRunning(e -> {
                    logTextArea.setStyle("-fx-text-fill: darkblue;");
                    logTextArea.appendText("Generando PDF...\n");
                });
                service.setOnSucceeded(e -> {
                    logTextArea.setStyle("-fx-text-fill: darkgreen;");
                    logTextArea.appendText('"' + System.getProperty("user.dir") + System.getProperty("file.separator") + "catálogo.pdf\" generado correctamente.\n");
                });
                service.setOnFailed(e -> {
                    logTextArea.setStyle("-fx-text-fill: firebrick;");
                    service.getException().printStackTrace();
                    logTextArea.appendText("Error: " + service.getException().getLocalizedMessage() + "\n");
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