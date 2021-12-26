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

    private final DenoisedPathTracer renderer;

    public DenoiserTab(DenoisedPathTracer renderer) {
        super();
        this.renderer = renderer;
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        albedoMap.selectedProperty().addListener((observable, oldValue, newValue) -> {
            renderer.enableAlbedo = newValue;
            if (!newValue) {
                // albedo map disabled, disable normal map
                normalMap.setSelected(false);
            }
        });
        albedoSpp.setText(renderer.albedoSpp + "");
        albedoSpp.textProperty().addListener((observable, oldValue, newValue) -> {
            try {
                renderer.albedoSpp = Integer.parseInt(newValue);
            } catch (NumberFormatException ignore) {
            }
        });
        albedoSpp.focusedProperty().addListener(((observable, oldValue, newValue) -> {
            if (!newValue) {
                albedoSpp.setText(renderer.albedoSpp + "");
            }
        }));

        normalMap.selectedProperty().addListener((observable, oldValue, newValue) -> {
            renderer.enableNormal = newValue;
            if (newValue) {
                // normal map enabled, enable albedo map
                albedoMap.setSelected(true);
            }
        });
        normalSpp.setText(renderer.normalSpp + "");
        normalSpp.textProperty().addListener((observable, oldValue, newValue) -> {
            try {
                renderer.normalSpp = Integer.parseInt(newValue);
            } catch (NumberFormatException ignore) {
            }
        });
        normalSpp.focusedProperty().addListener(((observable, oldValue, newValue) -> {
            if (!newValue) {
                normalSpp.setText(renderer.normalSpp + "");
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
}
