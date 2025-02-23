package de.lemaik.chunky.denoiser;

import de.lemaik.chunky.denoiser.utils.ObservableValue;
import se.llbit.chunky.renderer.scene.Scene;
import se.llbit.json.Json;
import se.llbit.json.JsonObject;

public class DenoiserSettings {
    public final ObservableValue<Boolean> saveBeauty = new ObservableValue<>(false);

    public final ObservableValue<Boolean> renderAlbedo = new ObservableValue<>(true);
    public final ObservableValue<Integer> albedoSpp = new ObservableValue<>(16);
    public final ObservableValue<Boolean> saveAlbedo = new ObservableValue<>(false);

    public final ObservableValue<Boolean> renderNormal = new ObservableValue<>(true);
    public final ObservableValue<Integer> normalSpp = new ObservableValue<>(16);
    public final ObservableValue<Boolean> saveNormal = new ObservableValue<>(false);
    public final ObservableValue<Boolean> normalWaterDisplacement = new ObservableValue<>(true);
    private Runnable changeCallback;

    static boolean isWaterDisplacementEnabled(Scene scene) {
        return scene.getAdditionalData("denoiser").asObject()
                .get("normalWaterDisplacement").asBoolean(true);
    }

    public DenoiserSettings() {
        saveBeauty.addListener((newValue) -> {
            if (this.changeCallback != null) {
                this.changeCallback.run();
            }
        });

        renderAlbedo.addListener((newValue) -> {
            if (this.changeCallback != null) {
                this.changeCallback.run();
            }
        });
        albedoSpp.addListener((newValue) -> {
            if (this.changeCallback != null) {
                this.changeCallback.run();
            }
        });
        saveAlbedo.addListener((newValue) -> {
            if (this.changeCallback != null) {
                this.changeCallback.run();
            }
        });

        renderNormal.addListener((newValue) -> {
            if (this.changeCallback != null) {
                this.changeCallback.run();
            }
        });
        normalSpp.addListener((newValue) -> {
            if (this.changeCallback != null) {
                this.changeCallback.run();
            }
        });
        saveNormal.addListener((newValue) -> {
            if (this.changeCallback != null) {
                this.changeCallback.run();
            }
        });
        normalWaterDisplacement.addListener((newValue) -> {
            if (this.changeCallback != null) {
                this.changeCallback.run();
            }
        });
    }

    public void saveToScene(Scene scene) {
        if (scene != null) {
            JsonObject denoiserSettings = new JsonObject();
            denoiserSettings.set("saveBeauty", Json.of(saveBeauty.get()));
            denoiserSettings.set("enableAlbedo", Json.of(renderAlbedo.get()));
            denoiserSettings.set("albedoSpp", Json.of(albedoSpp.get()));
            denoiserSettings.set("saveAlbedo", Json.of(saveAlbedo.get()));
            denoiserSettings.set("enableNormal", Json.of(renderNormal.get()));
            denoiserSettings.set("normalSpp", Json.of(normalSpp.get()));
            denoiserSettings.set("saveNormal", Json.of(saveNormal.get()));
            denoiserSettings.set("normalWaterDisplacement", Json.of(normalWaterDisplacement.get()));
            scene.setAdditionalData("denoiser", denoiserSettings);
        }
    }

    public void loadFromScene(Scene scene) {
        JsonObject denoiserData = scene != null ?
                scene.getAdditionalData("denoiser").asObject() :
                new JsonObject();
        saveBeauty.set(denoiserData.get("saveBeauty").asBoolean(false));

        renderAlbedo.set(denoiserData.get("enableAlbedo").asBoolean(true));
        albedoSpp.set(denoiserData.get("albedoSpp").asInt(16));
        saveAlbedo.set(denoiserData.get("saveAlbedo").asBoolean(false));

        renderNormal.set(denoiserData.get("enableNormal").asBoolean(true));
        normalSpp.set(denoiserData.get("normalSpp").asInt(16));
        saveNormal.set(denoiserData.get("saveNormal").asBoolean(false));
        normalWaterDisplacement.set(denoiserData.get("normalWaterDisplacement").asBoolean(true));
    }

    public void updateAll() {
        saveBeauty.update();

        renderAlbedo.update();
        albedoSpp.update();
        saveAlbedo.update();

        renderNormal.update();
        normalSpp.update();
        saveNormal.update();
        normalWaterDisplacement.update();
    }

    public void setChangeListener(Runnable callback) {
        this.changeCallback = callback;
    }
}
