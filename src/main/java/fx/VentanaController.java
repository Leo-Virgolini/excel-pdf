package fx;

import com.itextpdf.kernel.colors.DeviceRgb;
import javafx.event.ActionEvent;
import javafx.event.Event;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.media.AudioClip;
import javafx.scene.paint.Color;
import javafx.scene.paint.Paint;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.stage.DirectoryChooser;
import javafx.stage.FileChooser;
import org.apache.log4j.BasicConfigurator;
import service.GeneratePDFService;

import java.io.*;
import java.net.URL;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
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
    private TextField codigoFontSize;
    @FXML
    private TextField productoFontSize;
    @FXML
    private TextField rubroFontSize;
    @FXML
    private TextField subRubroFontSize;
    @FXML
    private TextField marcaFontSize;
    @FXML
    private TextField precioFontSize;
    @FXML
    private TextField codigoExternoFontSize;
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
    private ComboBox<String> codigoFont;
    @FXML
    private ComboBox<String> productoFont;
    @FXML
    private ComboBox<String> rubroFont;
    @FXML
    private ComboBox<String> subRubroFont;
    @FXML
    private ComboBox<String> marcaFont;
    @FXML
    private ComboBox<String> precioFont;
    @FXML
    private ComboBox<String> codigoExternoFont;
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

    private AudioClip errorSound;
    private AudioClip successSound;

    public void initialize(URL url, ResourceBundle rb) {
        BasicConfigurator.configure(); // configure Log4j
        errorSound = new AudioClip(getClass().getResource("/audios/error.mp3").toExternalForm());
        errorSound.setVolume(0.1);
        successSound = new AudioClip(getClass().getResource("/audios/success.mp3").toExternalForm());
        successSound.setVolume(0.1);
        cargarChoiceBoxes();
        loadPreferences(); // Load previous state from preferences
        Main.stage.setOnCloseRequest(event -> savePreferences());
    }

    @FXML
    public void buscarExcel(ActionEvent event) {
        logTextArea.clear();
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Elige archivo .XLSX");
        fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("Archivo XLSX", "*.xlsx"));
        final File defaultPath = new File("G:\\Otros ordenadores\\Mi PC (1)\\CATALOGOS\\EXCELs");
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
        final File defaultPath = new File("G:\\Otros ordenadores\\Mi PC (1)\\CATALOGOS\\CARATULAS");
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
    public void generarCatalogo(ActionEvent event) {
        logTextArea.clear();
        if (archivoExcel != null && archivoExcel.isFile() && carpetaImagenes != null && carpetaImagenes.exists()) {
            if (validarTextInputs()) {
                if (elegirDestino()) {
                    GeneratePDFService service = new GeneratePDFService(archivoExcel, carpetaImagenes, archivoPdf, archivoDestino,
                            Float.parseFloat(codigoFontSize.getText()), Float.parseFloat(productoFontSize.getText()), Float.parseFloat(rubroFontSize.getText()), Float.parseFloat(subRubroFontSize.getText()),
                            Float.parseFloat(marcaFontSize.getText()), Float.parseFloat(precioFontSize.getText()), Float.parseFloat(codigoExternoFontSize.getText()),
                            getRGB(codigoColorPicker), getRGB(productoColorPicker), getRGB(rubroColorPicker), getRGB(subRubroColorPicker), getRGB(marcaColorPicker), getRGB(precioColorPicker), getRGB(codigoExternoColorPicker),
                            codigoFont.getValue(), productoFont.getValue(), rubroFont.getValue(), subRubroFont.getValue(), marcaFont.getValue(), precioFont.getValue(), codigoExternoFont.getValue(),
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
                        successSound.play();
                        logTextArea.setStyle("-fx-text-fill: darkgreen;");
                        logTextArea.appendText("Se han generado " + service.getValue() + " productos.\n");
                        logTextArea.appendText('"' + archivoDestino.getAbsolutePath() + "\" generado.\n");
                        generarButton.setDisable(false);
                        progressIndicator.setVisible(false);
//                        logTextArea.appendText(""); // Add an empty line to trigger scroll
//                        final ScrollBar verticalScrollBar = (ScrollBar) logTextArea.lookup(".scroll-bar:vertical");
//                        verticalScrollBar.setValue(verticalScrollBar.getMax());
                    });
                    service.setOnFailed(e -> {
//                        service.getException().printStackTrace();
                        errorSound.play();
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
    public void onClickImagenes(Event event) {
        imageSizeTextInput.setDisable(!imagenCheckBox.isSelected());
    }

    @FXML
    public void onClickLinks(Event event) {
        if (linksCheckBox.isSelected()) {
            if (isNumeric(imageSizeTextInput.getText()))
                imageSizeTextInput.setText("" + (Float.parseFloat(imageSizeTextInput.getText()) - 25));
        } else {
            if (isNumeric(imageSizeTextInput.getText()))
                imageSizeTextInput.setText("" + (Float.parseFloat(imageSizeTextInput.getText()) + 25));
        }
    }

    @FXML
    public void onClickCodigoColumn(Event event) {
        deshabilitarColumna(codigoCheckBox, codigoFontSize, codigoColorPicker, codigoFont);
    }

    @FXML
    public void onClickProductoColumn(Event event) {
        deshabilitarColumna(productoCheckBox, productoFontSize, productoColorPicker, productoFont);
    }

    @FXML
    public void onClickRubroColumn(Event event) {
        deshabilitarColumna(rubroCheckBox, rubroFontSize, rubroColorPicker, rubroFont);
    }

    @FXML
    public void onClickSubRubroColumn(Event event) {
        deshabilitarColumna(subRubroCheckBox, subRubroFontSize, subRubroColorPicker, subRubroFont);
    }

    @FXML
    public void onClickMarcaColumn(Event event) {
        deshabilitarColumna(marcaCheckBox, marcaFontSize, marcaColorPicker, marcaFont);
    }

    @FXML
    public void onClickPrecioColumn(Event event) {
        deshabilitarColumna(precioCheckBox, precioFontSize, precioColorPicker, precioFont);
    }

    @FXML
    public void onClickCodigoExternoColumn(Event event) {
        deshabilitarColumna(codigoExternoCheckBox, codigoExternoFontSize, codigoExternoColorPicker, codigoExternoFont);
    }

    @FXML
    public void onCodigoColorChange(Event event) {
        codigoCheckBox.setTextFill(Paint.valueOf((codigoColorPicker.getValue().toString())));
    }

    @FXML
    public void onProductoColorChange(Event event) {
        productoCheckBox.setTextFill(Paint.valueOf((productoColorPicker.getValue().toString())));
    }

    @FXML
    public void onRubroColorChange(Event event) {
        rubroCheckBox.setTextFill(Paint.valueOf((rubroColorPicker.getValue().toString())));
    }

    @FXML
    public void onSubRubroColorChange(Event event) {
        subRubroCheckBox.setTextFill(Paint.valueOf((subRubroColorPicker.getValue().toString())));
    }

    @FXML
    public void onMarcaColorChange(Event event) {
        marcaCheckBox.setTextFill(Paint.valueOf((marcaColorPicker.getValue().toString())));
    }

    @FXML
    public void onPrecioColorChange(Event event) {
        precioCheckBox.setTextFill(Paint.valueOf((precioColorPicker.getValue().toString())));
    }

    @FXML
    public void onCodigoExternoColorChange(Event event) {
        codigoExternoCheckBox.setTextFill(Paint.valueOf((codigoExternoColorPicker.getValue().toString())));
    }

    @FXML
    public void onCodigoFontChange(Event event) {
        codigoCheckBox.setFont(Font.font(codigoFont.getValue(), FontWeight.BOLD, 15));
    }

    @FXML
    public void onProductoFontChange(Event event) {
        productoCheckBox.setFont(Font.font(productoFont.getValue(), FontWeight.BOLD, 15));
    }

    @FXML
    public void onRubroFontChange(Event event) {
        rubroCheckBox.setFont(Font.font(rubroFont.getValue(), FontWeight.BOLD, 15));
    }

    @FXML
    public void onSubRubroFontChange(Event event) {
        subRubroCheckBox.setFont(Font.font(subRubroFont.getValue(), FontWeight.BOLD, 15));
    }

    @FXML
    public void onMarcaFontChange(Event event) {
        marcaCheckBox.setFont(Font.font(marcaFont.getValue(), FontWeight.BOLD, 15));
    }

    @FXML
    public void onPrecioFontChange(Event event) {
        precioCheckBox.setFont(Font.font(precioFont.getValue(), FontWeight.BOLD, 15));
    }

    @FXML
    public void onCodigoExternoFontChange(Event event) {
        codigoExternoCheckBox.setFont(Font.font(codigoExternoFont.getValue(), FontWeight.BOLD, 15));
    }

    private void deshabilitarColumna(CheckBox checkBox, TextField fontSize, ColorPicker colorPicker, ComboBox<String> font) {
        if (checkBox.isSelected()) {
            fontSize.setDisable(false);
            colorPicker.setDisable(false);
            font.setDisable(false);
            if (isNumeric(imageSizeTextInput.getText()) && isNumeric(fontSize.getText()))
                imageSizeTextInput.setText("" + (Float.parseFloat(imageSizeTextInput.getText()) - Float.parseFloat(fontSize.getText())));
        } else {
            fontSize.setDisable(true);
            colorPicker.setDisable(true);
            font.setDisable(true);
            if (isNumeric(imageSizeTextInput.getText()) && isNumeric(fontSize.getText()))
                imageSizeTextInput.setText("" + (Float.parseFloat(imageSizeTextInput.getText()) + Float.parseFloat(fontSize.getText())));
        }
    }

    private void loadPreferences() {
        final Preferences prefs = Preferences.userRoot().node("catalogo");
        codigoFontSize.setText(prefs.get("codigoFontSize", "6"));
        productoFontSize.setText(prefs.get("productoFontSize", "6"));
        rubroFontSize.setText(prefs.get("rubroFontSize", "6"));
        subRubroFontSize.setText(prefs.get("subRubroFontSize", "6"));
        marcaFontSize.setText(prefs.get("marcaFontSize", "6"));
        precioFontSize.setText(prefs.get("precioFontSize", "7"));
        codigoExternoFontSize.setText(prefs.get("codigoExternoFontSize", "6"));

        String[] codigoColor = prefs.get("codigoColorPicker", "0,0,0").split(",");
        codigoColorPicker.setValue(new Color(Double.parseDouble(codigoColor[0]), Double.parseDouble(codigoColor[1]), Double.parseDouble(codigoColor[2]), 1));
        String[] productoColor = prefs.get("productoColorPicker", "0,0,0.54").split(",");
        productoColorPicker.setValue(new Color(Double.parseDouble(productoColor[0]), Double.parseDouble(productoColor[1]), Double.parseDouble(productoColor[2]), 1));
        String[] rubroColor = prefs.get("rubroColorPicker", "0,0,0").split(",");
        rubroColorPicker.setValue(new Color(Double.parseDouble(rubroColor[0]), Double.parseDouble(rubroColor[1]), Double.parseDouble(rubroColor[2]), 1));
        String[] subRubroColor = prefs.get("subRubroColorPicker", "0,0,0").split(",");
        subRubroColorPicker.setValue(new Color(Double.parseDouble(subRubroColor[0]), Double.parseDouble(subRubroColor[1]), Double.parseDouble(subRubroColor[2]), 1));
        String[] marcaColor = prefs.get("marcaColorPicker", "0,0,0").split(",");
        marcaColorPicker.setValue(new Color(Double.parseDouble(marcaColor[0]), Double.parseDouble(marcaColor[1]), Double.parseDouble(marcaColor[2]), 1));
        String[] precioColor = prefs.get("precioColorPicker", "0.54,0,0").split(",");
        precioColorPicker.setValue(new Color(Double.parseDouble(precioColor[0]), Double.parseDouble(precioColor[1]), Double.parseDouble(precioColor[2]), 1));
        String[] codigoExternoColor = prefs.get("codigoExternoColorPicker", "0,0,0").split(",");
        codigoExternoColorPicker.setValue(new Color(Double.parseDouble(codigoExternoColor[0]), Double.parseDouble(codigoExternoColor[1]), Double.parseDouble(codigoExternoColor[2]), 1));

        codigoFont.setValue(prefs.get("codigoFont", "Calibri"));
        productoFont.setValue(prefs.get("productoFont", "Calibri"));
        rubroFont.setValue(prefs.get("rubroFont", "Calibri"));
        subRubroFont.setValue(prefs.get("subRubroFont", "Calibri"));
        marcaFont.setValue(prefs.get("marcaFont", "Calibri"));
        precioFont.setValue(prefs.get("precioFont", "Calibri"));
        codigoExternoFont.setValue(prefs.get("codigoExternoFont", "Calibri"));

        imageSizeTextInput.setText(prefs.get("imageSizeTextInput", "83"));
        pageWidthTextInput.setText(prefs.get("pageWidthTextInput", "595"));
        pageHeightTextInput.setText(prefs.get("pageHeightTextInput", "842"));

        codigoCheckBox.setSelected(prefs.getBoolean("codigoCheckBox", true));
        if (!codigoCheckBox.isSelected()) {
            codigoFontSize.setDisable(true);
            codigoColorPicker.setDisable(true);
            codigoFont.setDisable(true);
        }
        productoCheckBox.setSelected(prefs.getBoolean("productoCheckBox", true));
        if (!productoCheckBox.isSelected()) {
            productoFontSize.setDisable(true);
            productoColorPicker.setDisable(true);
            productoFont.setDisable(true);
        }
        rubroCheckBox.setSelected(prefs.getBoolean("rubroCheckBox", true));
        if (!rubroCheckBox.isSelected()) {
            rubroFontSize.setDisable(true);
            rubroColorPicker.setDisable(true);
            rubroFont.setDisable(true);
        }
        subRubroCheckBox.setSelected(prefs.getBoolean("subRubroCheckBox", true));
        if (!subRubroCheckBox.isSelected()) {
            subRubroFontSize.setDisable(true);
            subRubroColorPicker.setDisable(true);
            subRubroFont.setDisable(true);
        }
        marcaCheckBox.setSelected(prefs.getBoolean("marcaCheckBox", true));
        if (!marcaCheckBox.isSelected()) {
            marcaFontSize.setDisable(true);
            marcaColorPicker.setDisable(true);
            marcaFont.setDisable(true);
        }
        precioCheckBox.setSelected(prefs.getBoolean("precioCheckBox", true));
        if (!precioCheckBox.isSelected()) {
            precioFontSize.setDisable(true);
            precioColorPicker.setDisable(true);
            precioFont.setDisable(true);
        }
        codigoExternoCheckBox.setSelected(prefs.getBoolean("codigoExternoCheckBox", true));
        if (!codigoExternoCheckBox.isSelected()) {
            codigoExternoFontSize.setDisable(true);
            codigoExternoColorPicker.setDisable(true);
            codigoExternoFont.setDisable(true);
        }
        imagenCheckBox.setSelected(prefs.getBoolean("imagenCheckBox", true));
        if (!imagenCheckBox.isSelected()) {
            imageSizeTextInput.setDisable(true);
        }
        linksCheckBox.setSelected(prefs.getBoolean("linksCheckBox", false));

        codigoCheckBox.setTextFill(Paint.valueOf((codigoColorPicker.getValue().toString())));
        productoCheckBox.setTextFill(Paint.valueOf((productoColorPicker.getValue().toString())));
        rubroCheckBox.setTextFill(Paint.valueOf((rubroColorPicker.getValue().toString())));
        subRubroCheckBox.setTextFill(Paint.valueOf((subRubroColorPicker.getValue().toString())));
        marcaCheckBox.setTextFill(Paint.valueOf((marcaColorPicker.getValue().toString())));
        precioCheckBox.setTextFill(Paint.valueOf((precioColorPicker.getValue().toString())));
        codigoExternoCheckBox.setTextFill(Paint.valueOf((codigoExternoColorPicker.getValue().toString())));

        codigoCheckBox.setFont(Font.font(codigoFont.getValue(), FontWeight.BOLD, 15));
        productoCheckBox.setFont(Font.font(productoFont.getValue(), FontWeight.BOLD, 15));
        rubroCheckBox.setFont(Font.font(rubroFont.getValue(), FontWeight.BOLD, 15));
        subRubroCheckBox.setFont(Font.font(subRubroFont.getValue(), FontWeight.BOLD, 15));
        marcaCheckBox.setFont(Font.font(marcaFont.getValue(), FontWeight.BOLD, 15));
        precioCheckBox.setFont(Font.font(precioFont.getValue(), FontWeight.BOLD, 15));
        codigoExternoCheckBox.setFont(Font.font(codigoExternoFont.getValue(), FontWeight.BOLD, 15));
    }

    private void savePreferences() {
        // Save state to preferences when the application is closed
        final Preferences prefs = Preferences.userRoot().node("catalogo");
        prefs.put("codigoFontSize", codigoFontSize.getText());
        prefs.put("productoFontSize", productoFontSize.getText());
        prefs.put("rubroFontSize", rubroFontSize.getText());
        prefs.put("subRubroFontSize", subRubroFontSize.getText());
        prefs.put("marcaFontSize", marcaFontSize.getText());
        prefs.put("precioFontSize", precioFontSize.getText());
        prefs.put("codigoExternoFontSize", codigoExternoFontSize.getText());

        prefs.put("codigoColorPicker", codigoColorPicker.getValue().getRed() + "," + codigoColorPicker.getValue().getGreen() + "," + codigoColorPicker.getValue().getBlue());
        prefs.put("productoColorPicker", productoColorPicker.getValue().getRed() + "," + productoColorPicker.getValue().getGreen() + "," + productoColorPicker.getValue().getBlue());
        prefs.put("rubroColorPicker", rubroColorPicker.getValue().getRed() + "," + rubroColorPicker.getValue().getGreen() + "," + rubroColorPicker.getValue().getBlue());
        prefs.put("subRubroColorPicker", subRubroColorPicker.getValue().getRed() + "," + subRubroColorPicker.getValue().getGreen() + "," + subRubroColorPicker.getValue().getBlue());
        prefs.put("marcaColorPicker", marcaColorPicker.getValue().getRed() + "," + marcaColorPicker.getValue().getGreen() + "," + marcaColorPicker.getValue().getBlue());
        prefs.put("precioColorPicker", precioColorPicker.getValue().getRed() + "," + precioColorPicker.getValue().getGreen() + "," + precioColorPicker.getValue().getBlue());
        prefs.put("codigoExternoColorPicker", codigoExternoColorPicker.getValue().getRed() + "," + codigoExternoColorPicker.getValue().getGreen() + "," + codigoExternoColorPicker.getValue().getBlue());

        prefs.put("codigoFont", codigoFont.getValue());
        prefs.put("productoFont", productoFont.getValue());
        prefs.put("rubroFont", rubroFont.getValue());
        prefs.put("subRubroFont", subRubroFont.getValue());
        prefs.put("marcaFont", marcaFont.getValue());
        prefs.put("precioFont", precioFont.getValue());
        prefs.put("codigoExternoFont", codigoExternoFont.getValue());

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

    private DeviceRgb getRGB(ColorPicker colorPicker) {
        return new DeviceRgb((int) (colorPicker.getValue().getRed() * 255), (int) (colorPicker.getValue().getGreen() * 255), (int) (colorPicker.getValue().getBlue() * 255));
    }

    private boolean validarTextInputs() {
        return isNumeric(codigoFontSize.getText()) && isNumeric(productoFontSize.getText()) && isNumeric(rubroFontSize.getText()) && isNumeric(subRubroFontSize.getText())
                && isNumeric(marcaFontSize.getText()) && isNumeric(precioFontSize.getText()) && isNumeric(codigoExternoFontSize.getText()) &&
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

    private void cargarChoiceBoxes() {
        // Get the list of available font families
        final List<String> fontFamilies = Font.getFamilies();
        // Populate the ChoiceBox with font families
        codigoFont.getItems().addAll(fontFamilies);
        productoFont.getItems().addAll(fontFamilies);
        rubroFont.getItems().addAll(fontFamilies);
        subRubroFont.getItems().addAll(fontFamilies);
        marcaFont.getItems().addAll(fontFamilies);
        precioFont.getItems().addAll(fontFamilies);
        codigoExternoFont.getItems().addAll(fontFamilies);
    }

}