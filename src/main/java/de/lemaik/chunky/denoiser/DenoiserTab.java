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
            impl.scene.setTargetSpp(Math.max(settings.getAlbedoSpp(), settings.getNormalSpp()));
            impl.scene.startRender();
        });

        settings.addListener(s -> saveBeauty.setSelected(s.getSaveBeauty()));
        saveBeauty.selectedProperty().addListener(((observable, oldValue, newValue) ->
                settings.setSaveBeauty(newValue)));

        settings.addListener(s -> albedoMap.setSelected(s.getRenderAlbedo()));
        albedoMap.selectedProperty().addListener((observable, oldValue, newValue) ->
                settings.setRenderAlbedo(newValue));

        settings.addListener(s -> albedoSpp.valueProperty().set(s.getAlbedoSpp()));
        albedoSpp.valueProperty().addListener(((observable, oldValue, newValue) ->
                settings.setAlbedoSpp(newValue.intValue())));

        settings.addListener(s -> saveAlbedo.setSelected(s.getSaveAlbedo()));
        saveAlbedo.selectedProperty().addListener(((observable, oldValue, newValue) ->
                settings.setSaveAlbedo(newValue)));

        settings.addListener(s -> normalMap.setSelected(s.getRenderNormal()));
        normalMap.selectedProperty().addListener((observable, oldValue, newValue) ->
                settings.setRenderNormal(newValue));

        settings.addListener(s -> normalSpp.valueProperty().set(s.getNormalSpp()));
        normalSpp.valueProperty().addListener(((observable, oldValue, newValue) ->
                settings.setNormalSpp(newValue.intValue())));

        settings.addListener(s -> saveNormal.setSelected(s.getSaveNormal()));
        saveNormal.selectedProperty().addListener(((observable, oldValue, newValue) ->
                settings.setSaveNormal(newValue)));

        settings.addListener(s -> normalWaterDisplacement.setSelected(s.getNormalWaterDisplacement()));
        normalWaterDisplacement.selectedProperty().addListener(((observable, oldValue, newValue) ->
                settings.setNormalWaterDisplacement(newValue)));

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
