package dev.emi.emi.runtime;

import java.io.File;
import java.util.function.Consumer;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.network.chat.ClickEvent;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.Style;
import net.minecraft.util.ARGB;
import net.minecraft.util.Util;
import org.jetbrains.annotations.Nullable;

import com.mojang.blaze3d.buffers.GpuBuffer;
import com.mojang.blaze3d.pipeline.RenderTarget;
import com.mojang.blaze3d.platform.NativeImage;
import com.mojang.blaze3d.systems.CommandEncoder;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.textures.GpuTexture;
import dev.emi.emi.EmiPort;
import dev.emi.emi.config.EmiConfig;

/**
 * GUI rendering can only target the main framebuffer, so screenshots are drawn on top of a regular frame and read back.
 * The image is captured once on black and once on white to recover transparency.
 */
public class EmiScreenshotRecorder {
	private static final String SCREENSHOTS_DIRNAME = "screenshots";
	private static @Nullable Request request;

	/**
	 * Saves a screenshot to the game's `screenshots` directory, capturing anything rendered in renderer over the next frames.
	 * <p>
	 * <b>Note:</b> the path can have <code>/</code> characters, indicating subdirectories. Java handles these correctly on Windows. The path should
	 * <b>not</b> contain the <code>.png</code> extension, as that will be added after checking for duplicates. If a file with this path already
	 * exists, then path will be suffixed with a <code>_#</code>, before adding the <code>.png</code> extension, where <code>#</code> represents an
	 * increasing number to avoid conflicts.
	 * <p>
	 * <b>Note 2:</b> The width and height parameters are in GUI units. The EMI-config
	 * <code>ui.recipe-screenshot-scale</code> value causes the resulting image to be scaled.
	 *
	 * @param path     the path to save the screenshot to, without extension.
	 * @param width    the width of the screenshot, not counting EMI-config scale.
	 * @param height   the height of the screenshot, not counting EMI-config scale.
	 * @param renderer a function to render the things being screenshotted, starting at the origin.
	 */
	public static void saveScreenshot(String path, int width, int height, Consumer<EmiDrawContext> renderer) {
		Minecraft.getInstance().execute(() -> {
			if (request == null) {
				request = new Request(path, width, height, renderer);
			}
		});
	}

	/**
	 * Called at the very end of GUI extraction
	 */
	public static void extract(GuiGraphicsExtractor raw) {
		Request req = request;
		if (req == null || req.drawn) {
			return;
		}
		Minecraft client = Minecraft.getInstance();
		EmiDrawContext context = EmiDrawContext.wrap(raw);
		int guiScale = EmiPort.getGuiScale(client);
		int scale = EmiConfig.recipeScreenshotScale < 1 ? guiScale : EmiConfig.recipeScreenshotScale;
		RenderTarget target = client.getMainRenderTarget();
		req.pixelWidth = Math.min(req.width * scale, target.width);
		req.pixelHeight = Math.min(req.height * scale, target.height);

		raw.nextStratum();
		context.push();
		context.scale((float) scale / guiScale, (float) scale / guiScale);
		context.resetColor();
		raw.fill(0, 0, req.width, req.height, req.black == null ? 0xFF000000 : 0xFFFFFFFF);
		raw.nextStratum();
		try {
			req.renderer.accept(context);
		} catch (Throwable t) {
			EmiLog.error("Error rendering screenshot", t);
		}
		context.pop();
		context.resetColor();
		req.drawn = true;
	}

	/**
	 * Called after the GUI has been rendered to the main framebuffer
	 */
	public static void capture() {
		Request req = request;
		if (req == null || !req.drawn || req.capturing) {
			return;
		}
		req.capturing = true;
		readMainTarget(req.pixelWidth, req.pixelHeight, image -> {
			if (req.black == null) {
				req.black = image;
				req.drawn = false;
				req.capturing = false;
			} else {
				request = null;
				save(req, combine(req.black, image));
				req.black.close();
				image.close();
			}
		});
	}

	private static NativeImage combine(NativeImage black, NativeImage white) {
		NativeImage out = new NativeImage(black.getWidth(), black.getHeight(), false);
		for (int y = 0; y < out.getHeight(); y++) {
			for (int x = 0; x < out.getWidth(); x++) {
				int b = black.getPixel(x, y);
				int w = white.getPixel(x, y);
				// A pixel blended over black and white differs by exactly its transparency
				int alpha = 255 - Math.max(ARGB.red(w) - ARGB.red(b), Math.max(ARGB.green(w) - ARGB.green(b), ARGB.blue(w) - ARGB.blue(b)));
				alpha = Math.clamp(alpha, 0, 255);
				if (alpha == 0) {
					out.setPixel(x, y, 0);
				} else {
					out.setPixel(x, y, ARGB.color(alpha,
						Math.min(255, ARGB.red(b) * 255 / alpha),
						Math.min(255, ARGB.green(b) * 255 / alpha),
						Math.min(255, ARGB.blue(b) * 255 / alpha)));
				}
			}
		}
		return out;
	}

	private static void readMainTarget(int width, int height, Consumer<NativeImage> consumer) {
		RenderTarget target = Minecraft.getInstance().getMainRenderTarget();
		GpuTexture texture = target.getColorTexture();
		if (texture == null) {
			request = null;
			return;
		}
		int fullWidth = target.width;
		int fullHeight = target.height;
		int pixelSize = texture.getFormat().pixelSize();
		GpuBuffer buffer = RenderSystem.getDevice().createBuffer(() -> "EMI screenshot buffer",
			GpuBuffer.USAGE_MAP_READ | GpuBuffer.USAGE_COPY_DST, (long) fullWidth * fullHeight * pixelSize);
		CommandEncoder encoder = RenderSystem.getDevice().createCommandEncoder();
		encoder.copyTextureToBuffer(texture, buffer, 0, () -> {
			try (GpuBuffer.MappedView view = encoder.mapBuffer(buffer, true, false)) {
				NativeImage image = new NativeImage(width, height, false);
				for (int y = 0; y < height; y++) {
					// Framebuffer rows start at the bottom
					int row = fullHeight - y - 1;
					for (int x = 0; x < width; x++) {
						image.setPixelABGR(x, y, view.data().getInt((x + row * fullWidth) * pixelSize) | 0xFF000000);
					}
				}
				consumer.accept(image);
			}
			buffer.close();
		}, 0);
	}

	private static void save(Request req, NativeImage nativeImage) {
		Minecraft client = Minecraft.getInstance();
		File screenshots = new File(client.gameDirectory, SCREENSHOTS_DIRNAME);
		screenshots.mkdir();

		String filename = getScreenshotFilename(screenshots, req.path);
		File file = new File(screenshots, filename);

		// Make sure the parent file exists. Note: `/`s in suggestedPath are valid, as they indicate subdirectories. Java even translates this
		// correctly on Windows.
		File parent = file.getParentFile();
		parent.mkdirs();

		Consumer<Component> messageReceiver = message -> client.execute(() -> client.gui.getChat().addClientSystemMessage(message));
		Util.ioPool().execute(() -> {
			try {
				nativeImage.writeToFile(file);

				Component text = EmiPort.literal(filename,
					Style.EMPTY.withUnderlined(true).withClickEvent(new ClickEvent.OpenFile(file.getAbsolutePath())));
				messageReceiver.accept(EmiPort.translatable("screenshot.success", text));
			} catch (Throwable e) {
				EmiLog.error("Failed to write screenshot", e);
				messageReceiver.accept(EmiPort.translatable("screenshot.failure", e.getMessage()));
			} finally {
				nativeImage.close();
			}
		});
	}

	private static String getScreenshotFilename(File directory, String path) {
		int i = 1;
		while ((new File(directory, path + (i == 1 ? "" : "_" + i) + ".png")).exists()) {
			++i;
		}
		return path + (i == 1 ? "" : "_" + i) + ".png";
	}

	private static class Request {
		private final String path;
		private final int width, height;
		private final Consumer<EmiDrawContext> renderer;
		private int pixelWidth, pixelHeight;
		private boolean drawn, capturing;
		private @Nullable NativeImage black;

		private Request(String path, int width, int height, Consumer<EmiDrawContext> renderer) {
			this.path = path;
			this.width = width;
			this.height = height;
			this.renderer = renderer;
		}
	}
}
