package de.lemaik.chunky.denoiser;

import javafx.fxml.FXMLLoader;
import javafx.scene.Node;

import java.io.IOException;

public class PreviewTab {
    public String getTabTitle() {
        return "Denoiser Preview";
    }

    public Node getTabContent() {
        try {
            FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("/denoiser-preview-tab.fxml"));
            fxmlLoader.setController(this);
            return fxmlLoader.load();
        } catch (IOException e) {
            throw new RuntimeException("Could not initialize denoiser plugin", e);
        }
    }
}
