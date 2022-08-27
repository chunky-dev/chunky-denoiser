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

    private transient Scene scene = null;

    public DenoiserSettings() {
        saveBeauty.addListener(this::save);

        renderAlbedo.addListener(this::save);
        albedoSpp.addListener(this::save);
        saveAlbedo.addListener(this::save);

        renderNormal.addListener(this::save);
        normalSpp.addListener(this::save);
        saveNormal.addListener(this::save);
        normalWaterDisplacement.addListener(this::save);
    }

    private <T> void save(T newValue) {
        Scene scene = this.scene;
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

    public void setScene(Scene scene) {
        this.scene = scene;

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
}
