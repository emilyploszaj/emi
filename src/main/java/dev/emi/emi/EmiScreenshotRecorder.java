package dev.emi.emi;

import java.io.File;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.function.Consumer;

import com.mojang.blaze3d.systems.RenderSystem;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gl.Framebuffer;
import net.minecraft.client.gl.SimpleFramebuffer;
import net.minecraft.client.texture.NativeImage;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.text.ClickEvent;
import net.minecraft.text.LiteralText;
import net.minecraft.text.MutableText;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.Util;
import net.minecraft.util.math.Matrix4f;

public class EmiScreenshotRecorder {
	private static final DateFormat DATE_FORMAT = new SimpleDateFormat("yyyy-MM-dd_HH.mm.ss");

	public static void saveScreenshot(String prefix, int width, int height, Runnable renderer) {
		if (!RenderSystem.isOnRenderThread()) {
			RenderSystem.recordRenderCall(() -> saveScreenshotInner(prefix, width, height, renderer));
		} else {
			saveScreenshotInner(prefix, width, height, renderer);
		}
	}

	private static void saveScreenshotInner(String prefix, int width, int height, Runnable renderer) {
		MinecraftClient client = MinecraftClient.getInstance();

		int scale;
		if (EmiConfig.recipeScreenshotScale < 0) {
			scale = client.options.guiScale;
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
		RenderSystem.applyModelViewMatrix();

		Matrix4f backupProj = RenderSystem.getProjectionMatrix();
		RenderSystem.setProjectionMatrix(Util.make(new Matrix4f(), Matrix4f::loadIdentity));

		renderer.run();

		RenderSystem.setProjectionMatrix(backupProj);
		view.pop();
		RenderSystem.applyModelViewMatrix();

		framebuffer.endWrite();
		client.getFramebuffer().beginWrite(true);

		saveScreenshotInner(client.runDirectory, prefix, framebuffer, message -> client.execute(() -> client.inGameHud.getChatHud().addMessage(message)));
	}

	private static void saveScreenshotInner(File gameDirectory, String prefix, Framebuffer framebuffer, Consumer<Text> messageReceiver) {
		NativeImage nativeImage = takeScreenshot(framebuffer);
		File file = new File(gameDirectory, "screenshots");
		file.mkdir();
		File file2 = getScreenshotFilename(file, prefix);
		Util.getIoWorkerExecutor().execute(() -> {
			try {
				nativeImage.writeTo(file2);
				MutableText
					text = new LiteralText(file2.getName()).formatted(Formatting.UNDERLINE)
					.styled(style -> style.withClickEvent(new ClickEvent(ClickEvent.Action.OPEN_FILE, file2.getAbsolutePath())));
				messageReceiver.accept(EmiPort.translatable("screenshot.success", text));
			} catch (Exception exception) {
				EmiLog.error(exception);
				messageReceiver.accept(EmiPort.translatable("screenshot.failure", exception.getMessage()));
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

	private static File getScreenshotFilename(File directory, String prefix) {
		String string = DATE_FORMAT.format(new Date());
		int i = 1;
		File file;
		while ((file = new File(directory, prefix + string + (i == 1 ? "" : "_" + i) + ".png")).exists()) {
			++i;
		}
		return file;
	}
}
