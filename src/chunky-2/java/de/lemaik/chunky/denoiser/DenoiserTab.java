package de.lemaik.chunky.denoiser;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.TextField;
import javafx.stage.FileChooser;
import se.llbit.chunky.PersistentSettings;
import se.llbit.chunky.renderer.scene.Scene;
import se.llbit.chunky.ui.RenderControlsFxController;
import se.llbit.chunky.ui.render.RenderControlsTab;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.ResourceBundle;

public class DenoiserTab implements RenderControlsTab, Initializable {
    private Scene scene;

    @FXML
    private CheckBox albedoMap;
    @FXML
    private TextField albedoSpp;
    @FXML
    private CheckBox normalMap;
    @FXML
    private TextField normalSpp;
    @FXML
    private TextField denoiserPath;
    @FXML
    private Button selectPath;

    @Override
    public void update(Scene scene) {

    }

    @Override
    public void setController(RenderControlsFxController controller) {
        scene = controller.getRenderController().getSceneManager().getScene();
    }

    @Override
    public String getTabTitle() {
        return "Denoiser";
    }

    @Override
    public Node getTabContent() {
        try {
            FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("/denoiser-tab.fxml"));
            fxmlLoader.setController(this);
            return fxmlLoader.load();
        } catch (IOException e) {
            throw new RuntimeException("Could not initialize denoiser plugin", e);
        }
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        albedoMap.selectedProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue == false) {
                // albedo map disabled, disable normal map
                normalMap.setSelected(false);
            }
        });
        albedoSpp.setText(BetterRenderManager.ALBEDO_SPP + "");
        albedoSpp.textProperty().addListener((observable, oldValue, newValue) -> {
            try {
                BetterRenderManager.ALBEDO_SPP = Integer.parseInt(newValue);
            } catch (NumberFormatException ignore) {
            }
        });
        albedoSpp.focusedProperty().addListener(((observable, oldValue, newValue) -> {
            if (newValue == false) {
                albedoSpp.setText(BetterRenderManager.ALBEDO_SPP + "");
            }
        }));

        normalMap.selectedProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue == true) {
                // normal map enabled, enable albedo map
                albedoMap.setSelected(true);
            }
        });
        normalSpp.setText(BetterRenderManager.NORMAL_SPP + "");
        normalSpp.textProperty().addListener((observable, oldValue, newValue) -> {
            try {
                BetterRenderManager.NORMAL_SPP = Integer.parseInt(newValue);
            } catch (NumberFormatException ignore) {
            }
        });
        normalSpp.focusedProperty().addListener(((observable, oldValue, newValue) -> {
            if (newValue == false) {
                normalSpp.setText(BetterRenderManager.NORMAL_SPP + "");
            }
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
