package dev.emi.emi.runtime;

import java.io.File;
import java.util.function.Consumer;
import net.minecraft.Util;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.ClickEvent;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.Style;
import org.joml.Matrix4f;
import com.mojang.blaze3d.pipeline.RenderTarget;
import com.mojang.blaze3d.pipeline.TextureTarget;
import com.mojang.blaze3d.platform.NativeImage;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.VertexSorting;
import dev.emi.emi.EmiPort;
import dev.emi.emi.config.EmiConfig;
import org.joml.Matrix4fStack;

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
		Minecraft client = Minecraft.getInstance();

		int scale;
		if (EmiConfig.recipeScreenshotScale < 1) {
			scale = EmiPort.getGuiScale(client);
		} else {
			scale = EmiConfig.recipeScreenshotScale;
		}

		RenderTarget framebuffer = new TextureTarget(width * scale, height * scale, true, Minecraft.ON_OSX);
		framebuffer.setClearColor(0f, 0f, 0f, 0f);
		framebuffer.clear(Minecraft.ON_OSX);

		framebuffer.bindWrite(true);

		Matrix4fStack view = RenderSystem.getModelViewStack();
		view.pushMatrix();
		view.identity();
		view.translate(-1.0f, 1.0f, 0.0f);
		view.scale(2f / width, -2f / height, -1f / 1000f);
		view.translate(0.0f, 0.0f, 10.0f);
		EmiPort.applyModelViewMatrix();

		Matrix4f backupProj = RenderSystem.getProjectionMatrix();
		RenderSystem.setProjectionMatrix(new Matrix4f().identity(), VertexSorting.ORTHOGRAPHIC_Z);

		renderer.run();

		RenderSystem.setProjectionMatrix(backupProj, VertexSorting.ORTHOGRAPHIC_Z);
		view.popMatrix();
		EmiPort.applyModelViewMatrix();

		framebuffer.unbindWrite();
		client.getMainRenderTarget().bindWrite(true);

		saveScreenshotInner(client.gameDirectory, path, framebuffer,
			message -> client.execute(() -> client.gui.getChat().addMessage(message)));
	}

	private static void saveScreenshotInner(File gameDirectory, String suggestedPath, RenderTarget framebuffer, Consumer<Component> messageReceiver) {
		NativeImage nativeImage = takeScreenshot(framebuffer);

		File screenshots = new File(gameDirectory, SCREENSHOTS_DIRNAME);
		screenshots.mkdir();

		String filename = getScreenshotFilename(screenshots, suggestedPath);
		File file = new File(screenshots, filename);

		// Make sure the parent file exists. Note: `/`s in suggestedPath are valid, as they indicate subdirectories. Java even translates this
		// correctly on Windows.
		File parent = file.getParentFile();
		parent.mkdirs();

		Util.ioPool().execute(() -> {
			try {
				nativeImage.writeToFile(file);

				Component text = EmiPort.literal(filename,
					Style.EMPTY.withUnderlined(true).withClickEvent(new ClickEvent(ClickEvent.Action.OPEN_FILE, file.getAbsolutePath())));
				messageReceiver.accept(EmiPort.translatable("screenshot.success", text));
			} catch (Throwable e) {
				EmiLog.error("Failed to write screenshot", e);
				messageReceiver.accept(EmiPort.translatable("screenshot.failure", e.getMessage()));
			} finally {
				nativeImage.close();
			}
		});
	}

	private static NativeImage takeScreenshot(RenderTarget framebuffer) {
		int i = framebuffer.width;
		int j = framebuffer.height;
		NativeImage nativeImage = new NativeImage(i, j, false);
		RenderSystem.bindTexture(framebuffer.getColorTextureId());
		nativeImage.downloadTexture(0, false);
		nativeImage.flipY();
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
