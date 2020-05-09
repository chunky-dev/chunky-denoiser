package de.lemaik.chunky.denoiser;

import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import se.llbit.chunky.renderer.scene.Scene;
import se.llbit.chunky.ui.ChunkyFxController;
import se.llbit.chunky.ui.RenderControlsFxController;
import se.llbit.chunky.ui.render.RenderControlsTab;

import java.io.IOException;
import java.lang.reflect.Field;

public class DenoiserTabImpl implements RenderControlsTab {
    @Override
    public void update(Scene scene) {
    }

    @Override
    public void setController(RenderControlsFxController controller) {
        PreviewTab previewTab = new PreviewTab();
        try {
            ChunkyFxController cfx = controller.getChunkyController();
            Field f = cfx.getClass().getDeclaredField("mainTabs");
            f.setAccessible(true);
            TabPane mainTabs = (TabPane) f.get(cfx);
            Tab tab = new Tab();
            tab.setContent(previewTab.getTabContent());
            tab.setText(previewTab.getTabTitle());
            mainTabs.getTabs().add(tab);
        } catch (NoSuchFieldException | IllegalAccessException e) {
            e.printStackTrace();
        }
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
