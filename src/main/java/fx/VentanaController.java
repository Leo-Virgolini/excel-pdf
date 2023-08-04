package fx;

import com.itextpdf.kernel.colors.DeviceRgb;
import javafx.event.ActionEvent;
import javafx.event.Event;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.paint.Color;
import javafx.stage.DirectoryChooser;
import javafx.stage.FileChooser;
import org.apache.log4j.BasicConfigurator;
import service.GeneratePDFService;

import java.io.*;
import java.net.URL;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ResourceBundle;
import java.util.prefs.Preferences;

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
        loadPreferences(); // Load previous state from preferences
        Main.stage.setOnCloseRequest(event -> {
            // Execute your code here when the window is closed
//            event.consume(); // Prevent the default close action
            savePreferences();
        });
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
        final File defaultPath = new File("Z:\\Doc. Compartidos\\DUX ERP Linea GE\\IMAGENES (subidas a la Web)");
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

    private void loadPreferences() {
        Preferences prefs = Preferences.userRoot().node("catalogo");
        fontSizeCodigo.setText(prefs.get("fontSizeCodigo", "6"));
        fontSizeProducto.setText(prefs.get("fontSizeProducto", "6"));
        fontSizeRubro.setText(prefs.get("fontSizeRubro", "6"));
        fontSizeSubRubro.setText(prefs.get("fontSizeSubRubro", "6"));
        fontSizeMarca.setText(prefs.get("fontSizeMarca", "6"));
        fontSizePrecio.setText(prefs.get("fontSizePrecio", "7"));
        fontSizeCodigoExterno.setText(prefs.get("fontSizeCodigoExterno", "6"));

        String[] codigoColor = prefs.get("codigoColorPicker", "0,0,0").split(",");
        codigoColorPicker.setValue(new Color(Double.parseDouble(codigoColor[0]), Double.parseDouble(codigoColor[1]), Double.parseDouble(codigoColor[2]), 1));
        String[] productoColor = prefs.get("productoColorPicker", "0,0,139").split(",");
        productoColorPicker.setValue(new Color(Double.parseDouble(productoColor[0]), Double.parseDouble(productoColor[1]), Double.parseDouble(productoColor[2]), 1));
        String[] rubroColor = prefs.get("rubroColorPicker", "0,0,0").split(",");
        rubroColorPicker.setValue(new Color(Double.parseDouble(rubroColor[0]), Double.parseDouble(rubroColor[1]), Double.parseDouble(rubroColor[2]), 1));
        String[] subRubroColor = prefs.get("subRubroColorPicker", "0,0,0").split(",");
        subRubroColorPicker.setValue(new Color(Double.parseDouble(subRubroColor[0]), Double.parseDouble(subRubroColor[1]), Double.parseDouble(subRubroColor[2]), 1));
        String[] marcaColor = prefs.get("marcaColorPicker", "0,0,0").split(",");
        marcaColorPicker.setValue(new Color(Double.parseDouble(marcaColor[0]), Double.parseDouble(marcaColor[1]), Double.parseDouble(marcaColor[2]), 1));
        String[] precioColor = prefs.get("precioColorPicker", "139,0,0").split(",");
        precioColorPicker.setValue(new Color(Double.parseDouble(precioColor[0]), Double.parseDouble(precioColor[1]), Double.parseDouble(precioColor[2]), 1));
        String[] codigoExternoColor = prefs.get("codigoExternoColorPicker", "0,0,0").split(",");
        codigoExternoColorPicker.setValue(new Color(Double.parseDouble(codigoExternoColor[0]), Double.parseDouble(codigoExternoColor[1]), Double.parseDouble(codigoExternoColor[2]), 1));

        imageSizeTextInput.setText(prefs.get("imageSizeTextInput", "89"));
        pageWidthTextInput.setText(prefs.get("pageWidthTextInput", "595"));
        pageHeightTextInput.setText(prefs.get("pageHeightTextInput", "842"));

        codigoCheckBox.setSelected(prefs.getBoolean("codigoCheckBox", true));
        if (!codigoCheckBox.isSelected()) {
            fontSizeCodigo.setDisable(true);
            codigoColorPicker.setDisable(true);
        }
        productoCheckBox.setSelected(prefs.getBoolean("productoCheckBox", true));
        if (!productoCheckBox.isSelected()) {
            fontSizeProducto.setDisable(true);
            productoColorPicker.setDisable(true);
        }
        rubroCheckBox.setSelected(prefs.getBoolean("rubroCheckBox", true));
        if (!rubroCheckBox.isSelected()) {
            fontSizeRubro.setDisable(true);
            rubroColorPicker.setDisable(true);
        }
        subRubroCheckBox.setSelected(prefs.getBoolean("subRubroCheckBox", true));
        if (!subRubroCheckBox.isSelected()) {
            fontSizeSubRubro.setDisable(true);
            subRubroColorPicker.setDisable(true);
        }
        marcaCheckBox.setSelected(prefs.getBoolean("marcaCheckBox", true));
        if (!marcaCheckBox.isSelected()) {
            fontSizeMarca.setDisable(true);
            marcaColorPicker.setDisable(true);
        }
        precioCheckBox.setSelected(prefs.getBoolean("precioCheckBox", true));
        if (!precioCheckBox.isSelected()) {
            fontSizePrecio.setDisable(true);
            precioColorPicker.setDisable(true);
        }
        codigoExternoCheckBox.setSelected(prefs.getBoolean("codigoExternoCheckBox", true));
        if (!codigoExternoCheckBox.isSelected()) {
            fontSizeCodigoExterno.setDisable(true);
            codigoExternoColorPicker.setDisable(true);
        }
        imagenCheckBox.setSelected(prefs.getBoolean("imagenCheckBox", true));
        if (!imagenCheckBox.isSelected()) {
            imageSizeTextInput.setDisable(true);
        }
    }

    private void savePreferences() {
        // Save state to preferences when the application is closed
        Preferences prefs = Preferences.userRoot().node("catalogo");
        prefs.put("fontSizeCodigo", fontSizeCodigo.getText());
        prefs.put("fontSizeProducto", fontSizeProducto.getText());
        prefs.put("fontSizeRubro", fontSizeRubro.getText());
        prefs.put("fontSizeSubRubro", fontSizeSubRubro.getText());
        prefs.put("fontSizeMarca", fontSizeMarca.getText());
        prefs.put("fontSizePrecio", fontSizePrecio.getText());
        prefs.put("fontSizeCodigoExterno", fontSizeCodigoExterno.getText());

        prefs.put("codigoColorPicker", codigoColorPicker.getValue().getRed() + "," + codigoColorPicker.getValue().getGreen() + "," + codigoColorPicker.getValue().getBlue());
        prefs.put("productoColorPicker", productoColorPicker.getValue().getRed() + "," + productoColorPicker.getValue().getGreen() + "," + productoColorPicker.getValue().getBlue());
        prefs.put("rubroColorPicker", rubroColorPicker.getValue().getRed() + "," + rubroColorPicker.getValue().getGreen() + "," + rubroColorPicker.getValue().getBlue());
        prefs.put("subRubroColorPicker", subRubroColorPicker.getValue().getRed() + "," + subRubroColorPicker.getValue().getGreen() + "," + subRubroColorPicker.getValue().getBlue());
        prefs.put("marcaColorPicker", marcaColorPicker.getValue().getRed() + "," + marcaColorPicker.getValue().getGreen() + "," + marcaColorPicker.getValue().getBlue());
        prefs.put("precioColorPicker", precioColorPicker.getValue().getRed() + "," + precioColorPicker.getValue().getGreen() + "," + precioColorPicker.getValue().getBlue());
        prefs.put("codigoExternoColorPicker", codigoExternoColorPicker.getValue().getRed() + "," + codigoExternoColorPicker.getValue().getGreen() + "," + codigoExternoColorPicker.getValue().getBlue());

        prefs.put("imageSizeTextInput", imageSizeTextInput.getText());
        prefs.put("pageWidthTextInput", pageWidthTextInput.getText());
        prefs.put("pageHeightTextInput", pageHeightTextInput.getText());

        prefs.putBoolean("codigoCheckBox", codigoCheckBox.isSelected());
        prefs.putBoolean("productoCheckBox", productoCheckBox.isSelected());
        prefs.putBoolean("rubroCheckBox", rubroCheckBox.isSelected());
        prefs.putBoolean("subRubroCheckBox", subRubroCheckBox.isSelected());
        prefs.putBoolean("marcaCheckBox", marcaCheckBox.isSelected());
        prefs.putBoolean("precioCheckBox", precioCheckBox.isSelected());
        prefs.putBoolean("codigoExternoCheckBox", codigoExternoCheckBox.isSelected());
        prefs.putBoolean("imagenCheckBox", imagenCheckBox.isSelected());
        prefs.putBoolean("linksCheckBox", linksCheckBox.isSelected());
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
        if (defaultPath.exists() && defaultPath.isDirectory()) {
            fileChooser.setInitialDirectory(defaultPath);
        } else {
            fileChooser.setInitialDirectory(new File(System.getProperty("user.dir")));
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