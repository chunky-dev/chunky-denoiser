package de.lemaik.chunky.denoiser;

import javafx.fxml.FXMLLoader;
import javafx.scene.control.Tab;
import se.llbit.chunky.renderer.scene.Scene;
import se.llbit.chunky.ui.render.RenderControlsTab;

import java.io.IOException;

public class DenoiserTabImpl implements RenderControlsTab {
    @Override
    public void update(Scene scene) {

    }

    @Override
    public Tab getTab() {
        Tab tab = new Tab("Denoiser");

        try {
            FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("/denoiser-tab.fxml"));
            fxmlLoader.setController(new DenoiserTab());
            tab.setContent(fxmlLoader.load());
            return tab;
        } catch (IOException e) {
            throw new RuntimeException("Could not initialize denoiser plugin", e);
        }
    }
}
