package de.lemaik.chunky.denoiser;

import se.llbit.chunky.renderer.scene.Scene;
import se.llbit.json.Json;
import se.llbit.json.JsonObject;

import java.util.ArrayList;
import java.util.function.Consumer;

public class DenoiserSettings {
    private final ArrayList<Consumer<DenoiserSettings>> listeners = new ArrayList<>();

    private boolean saveBeauty = false;
    private boolean renderAlbedo = true;
    private int albedoSpp = 16;
    private boolean saveAlbedo = false;
    private boolean renderNormal = true;
    private int normalSpp = 16;
    private boolean saveNormal = false;
    private boolean normalWaterDisplacement = true;

    private transient Scene scene = null;

    public DenoiserSettings() {
        addListener(settings -> {
            Scene scene = settings.scene;
            if (scene != null) {
                JsonObject denoiserSettings = new JsonObject();
                denoiserSettings.set("saveBeauty", Json.of(saveBeauty));
                denoiserSettings.set("enableAlbedo", Json.of(renderAlbedo));
                denoiserSettings.set("albedoSpp", Json.of(albedoSpp));
                denoiserSettings.set("saveAlbedo", Json.of(saveAlbedo));
                denoiserSettings.set("enableNormal", Json.of(renderNormal));
                denoiserSettings.set("normalSpp", Json.of(normalSpp));
                denoiserSettings.set("saveNormal", Json.of(saveNormal));
                denoiserSettings.set("normalWaterDisplacement", Json.of(normalWaterDisplacement));
                scene.setAdditionalData("denoiser", denoiserSettings);
            }
        });
    }

    public void setScene(Scene scene) {
        this.scene = scene;

        JsonObject denoiserData = scene != null ?
                scene.getAdditionalData("denoiser").asObject() :
                new JsonObject();
        renderAlbedo = denoiserData.get("enableAlbedo").asBoolean(true);
        albedoSpp = denoiserData.get("albedoSpp").asInt(16);
        renderNormal = denoiserData.get("enableNormal").asBoolean(true);
        normalSpp = denoiserData.get("normalSpp").asInt(16);
        normalWaterDisplacement = denoiserData.get("normalWaterDisplacement").asBoolean(true);

        settingsChanged();
    }

    public void addListener(Consumer<DenoiserSettings> listener) {
        listeners.add(listener);
    }

    public void removeListener(Consumer<DenoiserSettings> listener) {
        listeners.remove(listener);
    }

    protected void settingsChanged() {
        listeners.forEach(listener -> listener.accept(this));
    }

    public boolean getSaveBeauty() {
        return saveBeauty;
    }

    public void setSaveBeauty(boolean saveBeauty) {
        if (saveBeauty != this.saveBeauty) {
            this.saveBeauty = saveBeauty;
            settingsChanged();
        }
    }

    public boolean getRenderAlbedo() {
        return renderAlbedo;
    }

    public void setRenderAlbedo(boolean renderAlbedo) {
        if (renderAlbedo != this.renderAlbedo) {
            this.renderAlbedo = renderAlbedo;
            if (!this.renderAlbedo) renderNormal = false;
            settingsChanged();
        }
    }

    public int getAlbedoSpp() {
        return albedoSpp;
    }

    public void setAlbedoSpp(int albedoSpp) {
        if (albedoSpp != this.albedoSpp) {
            this.albedoSpp = albedoSpp;
            settingsChanged();
        }
    }

    public boolean getSaveAlbedo() {
        return saveAlbedo;
    }

    public void setSaveAlbedo(boolean saveAlbedo) {
        if (saveAlbedo != this.saveAlbedo) {
            this.saveAlbedo = saveAlbedo;
            settingsChanged();
        }
    }

    public boolean getRenderNormal() {
        return renderNormal;
    }

    public void setRenderNormal(boolean renderNormal) {
        if (renderNormal != this.renderNormal) {
            this.renderNormal = renderNormal;
            if (this.renderNormal) renderAlbedo = true;
            settingsChanged();
        }
    }

    public int getNormalSpp() {
        return normalSpp;
    }

    public void setNormalSpp(int normalSpp) {
        if (normalSpp != this.normalSpp) {
            this.normalSpp = normalSpp;
            settingsChanged();
        }
    }

    public boolean getSaveNormal() {
        return saveNormal;
    }

    public void setSaveNormal(boolean saveNormal) {
        if (saveNormal != this.saveNormal) {
            this.saveNormal = saveNormal;
            settingsChanged();
        }
    }

    public boolean getNormalWaterDisplacement() {
        return normalWaterDisplacement;
    }

    public void setNormalWaterDisplacement(boolean normalWaterDisplacement) {
        if (normalWaterDisplacement != this.normalWaterDisplacement) {
            this.normalWaterDisplacement = normalWaterDisplacement;
            settingsChanged();
        }
    }
}
