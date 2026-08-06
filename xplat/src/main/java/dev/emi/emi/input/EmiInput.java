package dev.emi.emi.input;

import org.lwjgl.glfw.GLFW;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.input.SystemKeycodes;

public class EmiInput {
	public static final int CONTROL_MASK = 1;
	public static final int ALT_MASK = 2;
	public static final int SHIFT_MASK = 4;

	public static boolean isControlDown() {
		return MinecraftClient.getInstance().isCtrlPressed();
	}

	public static boolean isAltDown() {
		return MinecraftClient.getInstance().isAltPressed();
	}

	public static boolean isShiftDown() {
		return MinecraftClient.getInstance().isShiftPressed();
	}

	public static int maskFromCode(int keyCode) {
		if (SystemKeycodes.IS_MAC_OS) {
			if (keyCode == GLFW.GLFW_KEY_LEFT_SUPER || keyCode == GLFW.GLFW_KEY_RIGHT_SUPER) {
				return CONTROL_MASK;
			}
		}
		if (keyCode == GLFW.GLFW_KEY_LEFT_CONTROL || keyCode == GLFW.GLFW_KEY_RIGHT_CONTROL) {
			return CONTROL_MASK;
		} else if (keyCode == GLFW.GLFW_KEY_LEFT_ALT || keyCode == GLFW.GLFW_KEY_RIGHT_ALT) {
			return ALT_MASK;
		} else if (keyCode == GLFW.GLFW_KEY_LEFT_SHIFT || keyCode == GLFW.GLFW_KEY_RIGHT_SHIFT) {
			return SHIFT_MASK;
		}
		return 0;
	}

	public static int getCurrentModifiers() {
		int ret = 0;
		if (isControlDown()) {
			ret |= CONTROL_MASK;
		}
		if (isAltDown()) {
			ret |= ALT_MASK;
		}
		if (isShiftDown()) {
			ret |= SHIFT_MASK;
		}
		return ret;
	}
}
