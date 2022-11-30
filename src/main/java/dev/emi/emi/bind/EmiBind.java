package dev.emi.emi.bind;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.apache.commons.compress.utils.Lists;

import dev.emi.emi.EmiUtil;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.util.InputUtil;

public class EmiBind {
	public static final int MAX_BINDS = 4;
	public final String translationKey;
	public final List<ModifiedKey> defaultKeys;
	public List<ModifiedKey> boundKeys;
	
	public EmiBind(String translationKey, int code) {
		this(translationKey, 0, code);
	}
	
	public EmiBind(String translationKey, int modifiers, int code) {
		this(translationKey, ModifiedKey.of(code, modifiers));
	}

	public EmiBind(String translationKey, ModifiedKey... defaultKeys) {
		this.translationKey = translationKey;
		this.defaultKeys = Arrays.asList(defaultKeys);
		this.boundKeys = this.defaultKeys.stream().collect(Collectors.toCollection(ArrayList::new));
		updateBinds();
	}

	public void updateBinds() {
		if (boundKeys.size() == 0) {
			boundKeys.add(new ModifiedKey(InputUtil.UNKNOWN_KEY, 0));
		}
		for (int i = 0; i < boundKeys.size() - 1; i++) {
			if (boundKeys.get(i).isUnbound()) {
				boundKeys.remove(i);
				i--;
			}
		}
		if (!boundKeys.get(boundKeys.size() - 1).isUnbound() && boundKeys.size() < MAX_BINDS) {
			boundKeys.add(new ModifiedKey(InputUtil.UNKNOWN_KEY, 0));
		}
	}

	public void setToDefault() {
		this.boundKeys = this.defaultKeys.stream().collect(Collectors.toCollection(ArrayList::new));
		updateBinds();
	}

	public void setBinds(ModifiedKey... keys) {
		this.boundKeys = Stream.of(keys).collect(Collectors.toCollection(ArrayList::new));
		updateBinds();
	}

	public void setBind(int offset, ModifiedKey key) {
		if (offset < boundKeys.size() && offset >= 0) {
			boundKeys.set(offset, key);
		}
		updateBinds();
	}

	public boolean isHeld() {
		for (ModifiedKey boundKey : boundKeys) {
			if (EmiUtil.getCurrentModifiers() == boundKey.modifiersToMatch()) {
				if (boundKey.key.getCategory() == InputUtil.Type.KEYSYM && boundKey.key.getCode() != -1) {
					if (InputUtil.isKeyPressed(MinecraftClient.getInstance().getWindow().getHandle(), boundKey.key.getCode())) {
						return true;
					}
				}
			}
		}
		return false;
	}

	public boolean matchesKey(int keyCode, int scanCode) {
		for (ModifiedKey boundKey : boundKeys) {
			if (EmiUtil.getCurrentModifiers() == boundKey.modifiersToMatch()) {
				if (keyCode == InputUtil.UNKNOWN_KEY.getCode()) {
					if (boundKey.key.getCategory() == InputUtil.Type.SCANCODE && boundKey.key.getCode() == scanCode) {
						return true;
					}
				} else {
					if (boundKey.key.getCategory() == InputUtil.Type.KEYSYM && boundKey.key.getCode() == keyCode) {
						return true;
					}
				}
			}
		}
		return false;
	}

	public boolean matchesMouse(int code) {
		for (ModifiedKey boundKey : boundKeys) {
			if (EmiUtil.getCurrentModifiers() == boundKey.modifiersToMatch()) {
				if (boundKey.key.getCategory() == InputUtil.Type.MOUSE && boundKey.key.getCode() == code) {
					return true;
				}
			}
		}
		return false;
	}

	public void setKey(List<String> keys) {
		List<ModifiedKey> modifiedKeys = Lists.newArrayList();
		for (String string : keys) {
			String[] parts = string.split(" ");
			InputUtil.Key key = InputUtil.UNKNOWN_KEY;
			int modifiers = 0;
			if (parts.length > 0) {
				key = InputUtil.fromTranslationKey(parts[parts.length - 1]);
				for (int i = 0; i < parts.length - 1; i++) {
					if (parts[i].equals("ctrl") || parts[i].equals("control")) {
						modifiers |= EmiUtil.CONTROL_MASK;
					} else if (parts[i].equals("alt")) {
						modifiers |= EmiUtil.ALT_MASK;
					} else if (parts[i].equals("shift")) {
						modifiers |= EmiUtil.SHIFT_MASK;
					}
				}
			}
			modifiedKeys.add(new ModifiedKey(key, modifiers));
		}
		this.boundKeys = modifiedKeys;
		updateBinds();
	}

	public static record ModifiedKey(InputUtil.Key key, int modifiers) {

		public static ModifiedKey of(int code, int modifiers) {
			return new ModifiedKey(InputUtil.Type.KEYSYM.createFromCode(code), modifiers);
		}

		public String toName() {
			String name = "";
			if ((modifiers & EmiUtil.CONTROL_MASK) > 0) {
				name += "ctrl ";
			}
			if ((modifiers & EmiUtil.ALT_MASK) > 0) {
				name += "alt ";
			}
			if ((modifiers & EmiUtil.SHIFT_MASK) > 0) {
				name += "shift ";
			}
			name += key.getTranslationKey();
			return name;
		}

		public int modifiersToMatch() {
			int modifiers = this.modifiers;
			if (key.getCategory() == InputUtil.Type.KEYSYM) {
				modifiers ^= EmiUtil.maskFromCode(key.getCode());
			}
			return modifiers;
		}

		public boolean isUnbound() {
			return key == InputUtil.UNKNOWN_KEY;
		}
	}
}
