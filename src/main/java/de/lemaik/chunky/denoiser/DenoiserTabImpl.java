package de.lemaik.chunky.denoiser;

import java.io.IOException;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import se.llbit.chunky.renderer.scene.Scene;
import se.llbit.chunky.ui.RenderControlsFxController;
import se.llbit.chunky.ui.render.RenderControlsTab;

public class DenoiserTabImpl implements RenderControlsTab {
    @Override
    public void update(Scene scene) {
    }

    @Override
    public void setController(RenderControlsFxController controller) {
    }

    @Override
    public String getTabTitle() {
        return "Denoiser";
    }

    @Override
    public Node getTabContent() {
        try {
            FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("/denoiser-tab.fxml"));
            fxmlLoader.setController(new DenoiserTab());
            return fxmlLoader.load();
        } catch (IOException e) {
            throw new RuntimeException("Could not initialize denoiser plugin", e);
        }
    }
}
