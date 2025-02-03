package dev.emi.emi.runtime;

import java.io.File;
import java.util.function.Consumer;

import com.mojang.blaze3d.systems.RenderSystem;

import dev.emi.emi.EmiPort;
import dev.emi.emi.config.EmiConfig;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gl.Framebuffer;
import net.minecraft.client.gl.SimpleFramebuffer;
import net.minecraft.client.texture.NativeImage;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.text.ClickEvent;
import net.minecraft.text.Style;
import net.minecraft.text.Text;
import net.minecraft.util.Util;
import net.minecraft.util.math.Matrix4f;

public class EmiScreenshotRecorder {
	private static final String SCREENSHOTS_DIRNAME = "screenshots";

	/**
	 * Saves a screenshot to the game's `screenshots` directory, doing the appropriate setup so that anything rendered in renderer will be captured
	 * and saved.
	 * <p>
	 * <b>Note:</b> the path can have <code>/</code> characters, indicating subdirectories. Java handles these correctly on Windows. The path should
	 * <b>not</b> contain the <code>.png</code> extension, as that will be added after checking for duplicates. If a file with this path already
	 * exists, then path will be suffixed with a <code>_#</code>, before adding the <code>.png</code> extension, where <code>#</code> represents an
	 * increasing number to avoid conflicts.
	 * <p>
	 * <b>Note 2:</b> The width and height parameters are reflected in the viewport when rendering. But the EMI-config
	 * <code>ui.recipe-screenshot-scale</code> value causes the resulting image to be scaled.
	 *
	 * @param path     the path to save the screenshot to, without extension.
	 * @param width    the width of the screenshot, not counting EMI-config scale.
	 * @param height   the height of the screenshot, not counting EMI-config scale.
	 * @param renderer a function to render the things being screenshotted.
	 */
	public static void saveScreenshot(String path, int width, int height, Runnable renderer) {
		if (!RenderSystem.isOnRenderThread()) {
			RenderSystem.recordRenderCall(() -> saveScreenshotInner(path, width, height, renderer));
		} else {
			saveScreenshotInner(path, width, height, renderer);
		}
	}

	private static void saveScreenshotInner(String path, int width, int height, Runnable renderer) {
		MinecraftClient client = MinecraftClient.getInstance();

		int scale;
		if (EmiConfig.recipeScreenshotScale < 1) {
			scale = EmiPort.getGuiScale(client);
		} else {
			scale = EmiConfig.recipeScreenshotScale;
		}

		Framebuffer framebuffer = new SimpleFramebuffer(width * scale, height * scale, true, MinecraftClient.IS_SYSTEM_MAC);
		framebuffer.setClearColor(0f, 0f, 0f, 0f);
		framebuffer.clear(MinecraftClient.IS_SYSTEM_MAC);

		framebuffer.beginWrite(true);

		MatrixStack view = RenderSystem.getModelViewStack();
		view.push();
		view.loadIdentity();
		view.translate(-1.0, 1.0, 0.0);
		view.scale(2f / width, -2f / height, -1f / 1000f);
		view.translate(0.0, 0.0, 10.0);
		EmiPort.applyModelViewMatrix();

		Matrix4f backupProj = RenderSystem.getProjectionMatrix();
		RenderSystem.setProjectionMatrix(Util.make(new Matrix4f(), Matrix4f::loadIdentity));

		renderer.run();

		RenderSystem.setProjectionMatrix(backupProj);
		view.pop();
		EmiPort.applyModelViewMatrix();

		framebuffer.endWrite();
		client.getFramebuffer().beginWrite(true);

		saveScreenshotInner(client.runDirectory, path, framebuffer,
			message -> client.execute(() -> client.inGameHud.getChatHud().addMessage(message)));
	}

	private static void saveScreenshotInner(File gameDirectory, String suggestedPath, Framebuffer framebuffer, Consumer<Text> messageReceiver) {
		NativeImage nativeImage = takeScreenshot(framebuffer);

		File screenshots = new File(gameDirectory, SCREENSHOTS_DIRNAME);
		screenshots.mkdir();

		String filename = getScreenshotFilename(screenshots, suggestedPath);
		File file = new File(screenshots, filename);

		// Make sure the parent file exists. Note: `/`s in suggestedPath are valid, as they indicate subdirectories. Java even translates this
		// correctly on Windows.
		File parent = file.getParentFile();
		parent.mkdirs();

		Util.getIoWorkerExecutor().execute(() -> {
			try {
				nativeImage.writeTo(file);

				Text text = EmiPort.literal(filename,
					Style.EMPTY.withUnderline(true).withClickEvent(new ClickEvent(ClickEvent.Action.OPEN_FILE, file.getAbsolutePath())));
				messageReceiver.accept(EmiPort.translatable("screenshot.success", text));
			} catch (Throwable e) {
				EmiLog.error("Failed to write screenshot");
				e.printStackTrace();
				messageReceiver.accept(EmiPort.translatable("screenshot.failure", e.getMessage()));
			} finally {
				nativeImage.close();
			}
		});
	}

	private static NativeImage takeScreenshot(Framebuffer framebuffer) {
		int i = framebuffer.textureWidth;
		int j = framebuffer.textureHeight;
		NativeImage nativeImage = new NativeImage(i, j, false);
		RenderSystem.bindTexture(framebuffer.getColorAttachment());
		nativeImage.loadFromTextureImage(0, false);
		nativeImage.mirrorVertically();
		return nativeImage;
	}

	private static String getScreenshotFilename(File directory, String path) {
		int i = 1;
		while ((new File(directory, path + (i == 1 ? "" : "_" + i) + ".png")).exists()) {
			++i;
		}
		return path + (i == 1 ? "" : "_" + i) + ".png";
	}
}
