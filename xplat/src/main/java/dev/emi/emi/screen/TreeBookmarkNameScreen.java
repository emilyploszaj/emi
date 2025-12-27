package dev.emi.emi.screen;

import java.util.function.Consumer;

import org.lwjgl.glfw.GLFW;

import dev.emi.emi.EmiPort;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.gui.widget.TextFieldWidget;

public class TreeBookmarkNameScreen extends Screen {
	private static final int FIELD_WIDTH = 200;
	private final Screen parent;
	private final Consumer<String> onSave;
	private final String initialName;
	private TextFieldWidget nameField;
	private ButtonWidget done;

	public TreeBookmarkNameScreen(Screen parent, String initialName, Consumer<String> onSave) {
		super(EmiPort.translatable("emi.tree_bookmark.name_title"));
		this.parent = parent;
		this.initialName = initialName == null ? "" : initialName;
		this.onSave = onSave;
	}

	@Override
	protected void init() {
		int fieldX = this.width / 2 - FIELD_WIDTH / 2;
		int fieldY = this.height / 2 - 10;
		nameField = new TextFieldWidget(textRenderer, fieldX, fieldY, FIELD_WIDTH, 20, EmiPort.translatable("emi.tree_bookmark.name_field"));
		nameField.setMaxLength(128);
		nameField.setText(initialName);
		nameField.setFocused(true);
		nameField.setCursorToEnd(false);
		addSelectableChild(nameField);

		int buttonWidth = 98;
		int buttonsY = fieldY + 28;
		done = ButtonWidget.builder(EmiPort.translatable("gui.done"), b -> finish())
			.position(this.width / 2 - buttonWidth - 2, buttonsY)
			.size(buttonWidth, 20)
			.build();
		ButtonWidget cancel = ButtonWidget.builder(EmiPort.translatable("gui.cancel"), b -> closeScreen())
			.position(this.width / 2 + 2, buttonsY)
			.size(buttonWidth, 20)
			.build();
		addDrawableChild(done);
		addDrawableChild(cancel);
		setInitialFocus(nameField);
		updateButtonState();
	}

	@Override
	public void tick() {
		super.tick();
		if (nameField != null) {
			nameField.setCursorToEnd(false);
			updateButtonState();
		}
	}

	@Override
	public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
		if (keyCode == GLFW.GLFW_KEY_ENTER) {
			finish();
			return true;
		} else if (keyCode == GLFW.GLFW_KEY_ESCAPE) {
			closeScreen();
			return true;
		}
		return super.keyPressed(keyCode, scanCode, modifiers);
	}

	@Override
	public void close() {
		closeScreen();
	}

	private void finish() {
		if (!done.active) {
			return;
		}
		if (onSave != null) {
			onSave.accept(nameField.getText().trim());
		}
		closeScreen();
	}

	private void closeScreen() {
		MinecraftClient.getInstance().setScreen(parent);
	}

	private void updateButtonState() {
		done.active = nameField != null && !nameField.getText().trim().isEmpty();
	}

	@Override
	public void render(DrawContext context, int mouseX, int mouseY, float delta) {
		renderBackground(context, this.width / 2, this.height / 2 - 32, 0);
		super.render(context, mouseX, mouseY, delta);
		nameField.render(context, mouseX, mouseY, delta);
		context.drawCenteredTextWithShadow(textRenderer, this.title, this.width / 2, this.height / 2 - 32, 0xFFFFFF);
		nameField.setFocused(true);
	}
}