package de.lemaik.chunky.denoiser;

import java.io.IOException;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.canvas.Canvas;
import javafx.scene.control.Tab;
import javafx.scene.image.PixelFormat;
import javafx.scene.image.WritableImage;
import se.llbit.chunky.renderer.RenderController;

public class PreviewTab {

  @FXML
  protected Canvas denoiserPreview;

  public Tab getTab() {
    try {
      FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("/preview-tab.fxml"));
      fxmlLoader.setController(this);
      return fxmlLoader.load();
    } catch (IOException e) {
      throw new RuntimeException("Could not initialize denoiser plugin", e);
    }
  }


  public void setController(RenderController controller) {
    ((BetterRenderManager) controller.getRenderer())
        .setOnDenoiseCompleted(bitmap -> {
          WritableImage img = new WritableImage(bitmap.width, bitmap.height);
          img.getPixelWriter()
              .setPixels(0, 0, bitmap.width, bitmap.height, PixelFormat.getIntArgbInstance(),
                  bitmap.data, 0, bitmap.width);
          Platform.runLater(() -> {
            denoiserPreview.setWidth(bitmap.width);
            denoiserPreview.setHeight(bitmap.height);
            denoiserPreview.getGraphicsContext2D().drawImage(img, 0, 0);
            System.out.println("Repainted! " + bitmap.width + " " + bitmap.height);
          });
        });
  }
}
