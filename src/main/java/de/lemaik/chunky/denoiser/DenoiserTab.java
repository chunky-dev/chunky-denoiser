package de.lemaik.chunky.denoiser;

import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.TextField;
import javafx.stage.FileChooser;
import se.llbit.chunky.PersistentSettings;
import se.llbit.chunky.ui.render.RenderControlsTab;

import java.io.File;
import java.net.URL;
import java.util.ResourceBundle;

public class DenoiserTab implements Initializable {
    @FXML
    private CheckBox albedoMap;
    @FXML
    private TextField albedoSpp;
    @FXML
    private CheckBox normalMap;
    @FXML
    private TextField normalSpp;
    @FXML
    private CheckBox normalWaterDisplacement;
    @FXML
    private TextField denoiserPath;
    @FXML
    private Button selectPath;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        albedoMap.selectedProperty().addListener((observable, oldValue, newValue) -> {
            DenoisedPathTracer.enableAlbedo = newValue;
            if (newValue == false) {
                // albedo map disabled, disable normal map
                normalMap.setSelected(false);
            }
        });
        albedoSpp.setText(DenoisedPathTracer.albedoSpp + "");
        albedoSpp.textProperty().addListener((observable, oldValue, newValue) -> {
            try {
                DenoisedPathTracer.albedoSpp = Integer.parseInt(newValue);
            } catch (NumberFormatException ignore) {
            }
        });
        albedoSpp.focusedProperty().addListener(((observable, oldValue, newValue) -> {
            if (newValue == false) {
                albedoSpp.setText(DenoisedPathTracer.albedoSpp + "");
            }
        }));

        normalMap.selectedProperty().addListener((observable, oldValue, newValue) -> {
            DenoisedPathTracer.enableNormal = newValue;
            if (newValue == true) {
                // normal map enabled, enable albedo map
                albedoMap.setSelected(true);
            }
        });
        normalSpp.setText(DenoisedPathTracer.normalSpp + "");
        normalSpp.textProperty().addListener((observable, oldValue, newValue) -> {
            try {
                DenoisedPathTracer.normalSpp = Integer.parseInt(newValue);
            } catch (NumberFormatException ignore) {
            }
        });
        normalSpp.focusedProperty().addListener(((observable, oldValue, newValue) -> {
            if (newValue == false) {
                normalSpp.setText(DenoisedPathTracer.normalSpp + "");
            }
        }));
        normalWaterDisplacement.selectedProperty().addListener(((observable, oldValue, newValue) -> {
            NormalTracer.NORMAL_WATER_DISPLACEMENT = newValue;
        }));

        denoiserPath.setText(PersistentSettings.settings.getString("oidnPath", ""));
        selectPath.setOnAction(e -> {
            FileChooser fileChooser = new FileChooser();
            fileChooser.setTitle("Select denoiser executable");
            if (!denoiserPath.getText().isEmpty()) {
                File currentDenoiserPath = new File(denoiserPath.getText());
                if (currentDenoiserPath.exists()) {
                    fileChooser.setInitialDirectory(currentDenoiserPath.getParentFile());
                }
            }
            File denoiser = fileChooser.showOpenDialog(selectPath.getScene().getWindow());
            if (denoiser != null) {
                PersistentSettings.setStringOption("oidnPath", denoiser.getAbsolutePath());
                denoiserPath.setText(denoiser.getAbsolutePath());
            }
        });
    }

    public static RenderControlsTab getImplementation() {
        return new DenoiserTabImpl();
    }
}
