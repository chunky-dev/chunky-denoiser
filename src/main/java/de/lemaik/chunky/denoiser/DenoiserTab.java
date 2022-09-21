package de.lemaik.chunky.denoiser;

import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.TextField;
import javafx.stage.FileChooser;
import se.llbit.chunky.PersistentSettings;
import se.llbit.chunky.ui.IntegerTextField;

import java.io.File;
import java.net.URL;
import java.util.ResourceBundle;

public class DenoiserTab implements Initializable {
    @FXML private Button denoiseRender;
    @FXML private CheckBox saveBeauty;
    @FXML private CheckBox albedoMap;
    @FXML private IntegerTextField albedoSpp;
    @FXML private CheckBox saveAlbedo;
    @FXML private CheckBox normalMap;
    @FXML private IntegerTextField normalSpp;
    @FXML private CheckBox saveNormal;
    @FXML private CheckBox normalWaterDisplacement;
    @FXML private TextField denoiserPath;
    @FXML private Button selectPath;

    private final DenoiserSettings settings;
    private final DenoiserTabImpl impl;

    public DenoiserTab(DenoiserTabImpl impl) {
        this.settings = impl.settings;
        this.impl = impl;
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        denoiseRender.setOnAction(e -> {
            impl.scene.renderTime = 0;
            impl.scene.setRenderer(DenoiserPlugin.DENOISER_RENDERER_ID);
            impl.scene.haltRender();
            impl.scene.setTargetSpp(Math.max(settings.albedoSpp.get(), settings.normalSpp.get()));
            impl.scene.startRender();
        });

        settings.saveBeauty.addListener(v -> saveBeauty.setSelected(v));
        saveBeauty.selectedProperty().addListener(((observable, oldValue, newValue) ->
                settings.saveBeauty.set(newValue)));

        settings.renderAlbedo.addListener(v -> albedoMap.setSelected(v));
        albedoMap.selectedProperty().addListener((observable, oldValue, newValue) ->
                settings.renderAlbedo.set(newValue));

        settings.albedoSpp.addListener(v -> albedoSpp.valueProperty().set(v));
        albedoSpp.valueProperty().addListener(((observable, oldValue, newValue) ->
                settings.albedoSpp.set(newValue.intValue())));

        settings.saveAlbedo.addListener(v -> saveAlbedo.setSelected(v));
        saveAlbedo.selectedProperty().addListener(((observable, oldValue, newValue) ->
                settings.saveAlbedo.set(newValue)));

        settings.renderNormal.addListener(v -> normalMap.setSelected(v));
        normalMap.selectedProperty().addListener((observable, oldValue, newValue) ->
                settings.renderNormal.set(newValue));

        settings.normalSpp.addListener(v -> normalSpp.valueProperty().set(v));
        normalSpp.valueProperty().addListener(((observable, oldValue, newValue) ->
                settings.normalSpp.set(newValue.intValue())));

        settings.saveNormal.addListener(v -> saveNormal.setSelected(v));
        saveNormal.selectedProperty().addListener(((observable, oldValue, newValue) ->
                settings.saveNormal.set(newValue)));

        settings.normalWaterDisplacement.addListener(v -> normalWaterDisplacement.setSelected(v));
        normalWaterDisplacement.selectedProperty().addListener(((observable, oldValue, newValue) ->
                settings.normalWaterDisplacement.set(newValue)));

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

        settings.updateAll();
    }
}
