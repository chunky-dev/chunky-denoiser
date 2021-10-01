package de.lemaik.chunky.denoiser;

import de.lemaik.chunky.denoiser.pfm.PortableFloatMap;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.ByteOrder;
import se.llbit.chunky.PersistentSettings;
import se.llbit.chunky.renderer.DefaultRenderManager;
import se.llbit.chunky.renderer.PathTracingRenderer;
import se.llbit.chunky.renderer.RenderContext;
import se.llbit.chunky.renderer.RenderMode;
import se.llbit.chunky.renderer.RenderStatusListener;
import se.llbit.chunky.renderer.SnapshotControl;
import se.llbit.chunky.renderer.postprocessing.PixelPostProcessingFilter;
import se.llbit.chunky.renderer.postprocessing.PostProcessingFilter;
import se.llbit.chunky.renderer.scene.Scene;
import se.llbit.chunky.resources.BitmapImage;
import se.llbit.log.Log;
import se.llbit.imageformats.png.PngFileWriter;
import se.llbit.util.TaskTracker;

public class BetterRenderManager extends DefaultRenderManager {

  public static int ALBEDO_SPP = 16;
  public static int NORMAL_SPP = 16;
  public static boolean ENABLE_ALBEDO = true;
  public static boolean ENABLE_NORMAL = true;
  public static boolean NORMAL_WATER_DISPLACEMENT = true;

  private final RenderContext context;
  private boolean isFirst = true;
  private RenderMode mode = RenderMode.PREVIEW;

  public BetterRenderManager(RenderContext context, boolean headless) {
    super(context, headless);
    this.context = context;

    this.addRenderListener(new RenderStatusListener() {
      int oldTargetSpp = 0;

      @Override
      public void setSpp(int spp) {
        if (mode == RenderMode.PREVIEW) {
          return;
        }

        if (!isFirst && spp >= bufferedScene.getTargetSpp()) {
          Scene scene = context.getChunky().getSceneManager().getScene();
          if (getRenderer() instanceof NormalRenderer) {
            try (OutputStream out = new BufferedOutputStream(
                context.getSceneFileOutputStream(scene.name + ".normal.pfm"))) {
              writeNormalPfmImage(out);
            } catch (IOException e) {
              Log.error("Saving the normal PFM failed", e);
            }
            if (ENABLE_ALBEDO) {
              renderAlbedoMap();
            } else {
              renderImage(oldTargetSpp);
            }
          } else if (getRenderer() instanceof AlbedoRenderer) {
            try (OutputStream out = new BufferedOutputStream(
                context.getSceneFileOutputStream(scene.name + ".albedo.pfm"))) {
              writePfmImage(out, false);
            } catch (IOException e) {
              Log.error("Saving the albedo PFM failed", e);
            }
            renderImage(oldTargetSpp);
          } else if (getRenderer() instanceof PathTracingRenderer) {
            try (OutputStream out = new BufferedOutputStream(
                context.getSceneFileOutputStream(scene.name + ".pfm"))) {
              writePfmImage(out, true);
            } catch (IOException e) {
              Log.error("Saving the render PFM failed", e);
            }

            String denoiserPath = PersistentSettings.settings.getString("oidnPath", null);
            if (denoiserPath != null) {
              File denoisedPfm = new File(context.getSceneDirectory(),
                  scene.name + ".denoised.pfm");
              try {
                OidnBinaryDenoiser.denoise(denoiserPath,
                    new File(context.getSceneDirectory(), scene.name + ".pfm"),
                    ENABLE_ALBEDO ? new File(context.getSceneDirectory(),
                        scene.name + ".albedo.pfm") : null,
                    ENABLE_NORMAL ? new File(context.getSceneDirectory(),
                        scene.name + ".normal.pfm") : null,
                    denoisedPfm
                );
                BitmapImage img = PortableFloatMap.readToRgbImage(new FileInputStream(denoisedPfm));
                File snapshotsDir = new File(context.getSceneDirectory(), "snapshots");
                snapshotsDir.mkdirs();
                try (PngFileWriter pngWriter = new PngFileWriter(
                    new File(snapshotsDir,
                        scene.name + "-" + spp + ".denoised.png"))) {
                  pngWriter.write(img.data, img.width, img.height, TaskTracker.Task.NONE);
                }
              } catch (IOException | InterruptedException e) {
                e.printStackTrace();
              }
            }
          }
        } else if (isFirst) {
          isFirst = false;
          oldTargetSpp = bufferedScene.getTargetSpp();

          if (ENABLE_NORMAL) {
            renderNormalMap();
          } else if (ENABLE_ALBEDO) {
            renderAlbedoMap();
          } else {
            renderImage(oldTargetSpp);
          }
        }
      }

      @Override
      public void setRenderTime(long l) {
      }

      @Override
      public void setSamplesPerSecond(int i) {
      }

      @Override
      public void renderStateChanged(RenderMode renderMode) {
        RenderMode oldMode = BetterRenderManager.this.mode;
        BetterRenderManager.this.mode = renderMode;

        if (renderMode == RenderMode.RENDERING && oldMode != RenderMode.PAUSED) {
          isFirst = true;
          oldTargetSpp = context.getChunky().getSceneManager().getScene().getTargetSpp();
        }
      }
    });
  }

  @Override
  public void setSnapshotControl(SnapshotControl sc) {
    super.setSnapshotControl(new SnapshotControl() {
      @Override
      public boolean saveSnapshot(Scene scene, int nextSpp) {
        return !scene.getRenderer().equals(AlbedoRenderer.ID)
            && !scene.getRenderer().equals(NormalRenderer.ID)
            && sc.saveSnapshot(scene, nextSpp);
      }

      @Override
      public boolean saveRenderDump(Scene scene, int nextSpp) {
        return !scene.getRenderer().equals(AlbedoRenderer.ID)
            && !scene.getRenderer().equals(NormalRenderer.ID)
            && sc.saveRenderDump(scene, nextSpp);
      }
    });
  }

  private void renderAlbedoMap() {
    Scene scene = this.context.getChunky().getSceneManager().getScene();
    scene.setRenderer(AlbedoRenderer.ID);
    scene.haltRender();
    scene.setTargetSpp(ALBEDO_SPP);
    scene.startRender();
  }

  private void renderNormalMap() {
    Scene scene = this.context.getChunky().getSceneManager().getScene();
    scene.setRenderer(NormalRenderer.ID);
    scene.haltRender();
    scene.setTargetSpp(NORMAL_SPP);
    scene.startRender();

  }

  private void renderImage(int spp) {
    Scene scene = this.context.getChunky().getSceneManager().getScene();
    scene.setRenderer(DefaultRenderManager.ChunkyPathTracerID);
    scene.haltRender();
    scene.setTargetSpp(spp);
    scene.startRender();
  }

  private void writePfmImage(OutputStream out, boolean postProcess) throws IOException {
    Scene scene = bufferedScene;
    double[] samples = scene.getSampleBuffer();
    double[] pixels = new double[samples.length];

    for (int y = 0; y < scene.height; y++) {
      for (int x = 0; x < scene.width; x++) {
        double[] result = new double[3];
        if (postProcess) {
          PostProcessingFilter filter = scene.getPostProcessingFilter();
          if (filter instanceof PixelPostProcessingFilter) {
            ((PixelPostProcessingFilter) filter)
                .processPixel(scene.width, scene.height, samples, x, y, scene.getExposure(),
                    result);
          }
        } else {
          result[0] = samples[(y * scene.width + x) * 3 + 0];
          result[1] = samples[(y * scene.width + x) * 3 + 1];
          result[2] = samples[(y * scene.width + x) * 3 + 2];
        }
        pixels[(y * scene.width + x) * 3] = Math.min(1.0, result[0]);
        pixels[(y * scene.width + x) * 3 + 1] = Math.min(1.0, result[1]);
        pixels[(y * scene.width + x) * 3 + 2] = Math.min(1.0, result[2]);
      }
    }

    PortableFloatMap.writeImage(pixels, scene.width, scene.height, ByteOrder.LITTLE_ENDIAN, out);
  }

  private void writeNormalPfmImage(OutputStream out) throws IOException {
    Scene scene = bufferedScene;
    double[] samples = scene.getSampleBuffer();
    double[] pixels;
    if (NormalRenderer.MAP_POSITIVE) {
      pixels = new double[samples.length];
      for (int i = 0; i < samples.length; i++) {
        pixels[i] = Math.min(1.0, samples[i]) * 2 - 1;
      }
    } else {
      pixels = samples;
    }
    PortableFloatMap.writeImage(pixels, scene.width, scene.height, ByteOrder.LITTLE_ENDIAN, out);
  }
}
