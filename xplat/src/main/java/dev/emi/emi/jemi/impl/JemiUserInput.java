package dev.emi.emi.jemi.impl;

import com.mojang.blaze3d.platform.InputConstants;

import mezz.jei.api.gui.inputs.IJeiUserInput;
import mezz.jei.api.runtime.IJeiKeyMapping;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.input.InputWithModifiers;
import net.minecraft.client.input.KeyEvent;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.client.input.MouseButtonInfo;

public record JemiUserInput(InputConstants.Key key, InputWithModifiers input) implements IJeiUserInput {

	public static JemiUserInput mouse(double mouseX, double mouseY, int button, int modifiers) {
		return new JemiUserInput(InputConstants.Type.MOUSE.getOrCreate(button),
			new MouseButtonEvent(mouseX, mouseY, new MouseButtonInfo(button, modifiers)));
	}

	public static JemiUserInput key(int keyCode, int scanCode, int modifiers) {
		KeyEvent event = new KeyEvent(keyCode, scanCode, modifiers);
		return new JemiUserInput(InputConstants.getKey(event), event);
	}

	@Override
	public InputConstants.Key getKey() {
		return key;
	}

	@Override
	public int getModifiers() {
		return input.modifiers();
	}

	@Override
	public InputWithModifiers getInputWithModifiers() {
		return input;
	}

	@Override
	public boolean isSimulate() {
		return false;
	}

	@Override
	public boolean is(KeyMapping keyMapping) {
		if (input instanceof KeyEvent event) {
			return keyMapping.matches(event);
		} else if (input instanceof MouseButtonEvent event) {
			return keyMapping.matchesMouse(event);
		}
		return false;
	}

	@Override
	public boolean is(IJeiKeyMapping keyMapping) {
		return keyMapping.isActiveAndMatches(key);
	}
}
