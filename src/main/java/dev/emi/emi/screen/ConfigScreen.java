package dev.emi.emi.screen;

import java.lang.reflect.Field;
import java.util.List;

import org.lwjgl.glfw.GLFW;

import dev.emi.emi.EmiConfig;
import dev.emi.emi.EmiConfig.ConfigValue;
import dev.emi.emi.EmiUtil;
import dev.emi.emi.bind.EmiBind;
import dev.emi.emi.bind.EmiBind.ModifiedKey;
import dev.emi.emi.screen.widget.BooleanWidget;
import dev.emi.emi.screen.widget.EmiBindWidget;
import dev.emi.emi.screen.widget.ListWidget;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.Element;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ElementListWidget;
import net.minecraft.client.util.InputUtil;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.text.Text;
import net.minecraft.text.TranslatableText;

public class ConfigScreen extends Screen {
	private Screen last;
	private ListWidget list;
	public EmiBind activeBind;
	public int activeBindOffset;
	public int activeModifiers;
	public int lastModifier;

	public ConfigScreen(Screen last) {
		super(new TranslatableText("screen.emi.config"));
		this.last = last;
	}

	public void setActiveBind(EmiBind bind, int offset) {
		activeBind = bind;
		activeBindOffset = offset;
		activeModifiers = 0;
		lastModifier = 0;
	}

	@Override
	public void onClose() {
		EmiConfig.writeConfig();
		MinecraftClient.getInstance().setScreen(last);
	}

	@Override
	protected void init() {
		super.init();
		list = new ListWidget(client, width, height, 0, height);
		try {
			for (Field field : EmiConfig.class.getFields()) {
				ConfigValue annot = field.getAnnotation(ConfigValue.class);
				if (annot != null) {
					Text translation = new TranslatableText("config.emi." + annot.value().replace('-', '_'));
					if (field.getType() == boolean.class) {
						list.addEntry(new BooleanWidget(translation, new Mutator<Boolean>() {

							public Boolean get() {
								try {
									return field.getBoolean(null);
								} catch(Exception e) {}
								return false;
							}

							public void set(Boolean value) {
								try {
									field.setBoolean(null, value);
								} catch (Exception e) {}
							}						
						}));
					} else if (field.getType() == EmiBind.class) {
						list.addEntry(new EmiBindWidget(this, (EmiBind) field.get(null)));
					}
				}
			}
		} catch (Exception e) {
		}
		for (int i = 0; i < 10; i++) {
			final int j = i;
			list.addEntry(new ListWidget.Entry() {
	
				@Override
				public void render(MatrixStack matrices, int var2, int var3, int var4, int var5, int var6, int var7, int var8,
						boolean var9, float var10) {
					drawStringWithShadow(matrices, textRenderer, "bunny bun bun", var4, var3, -1);
					if (var9) {
						drawStringWithShadow(matrices, textRenderer, "bunny bun bun", var4, var3, 0xff0000);
					}
				}
	
				@Override
				public int getHeight() {
					return 30 + j * 10;
				}
	
				@Override
				public List<? extends Element> children() {
					return List.of();
				}
			});
		}

		this.addSelectableChild(list);
	}
	
	@Override
	public void render(MatrixStack matrices, int mouseX, int mouseY, float delta) {
		this.renderBackgroundTexture((int) list.getScrollAmount());
		list.render(matrices, mouseX, mouseY, delta);
	}
	
	@Override
	public boolean mouseClicked(double mouseX, double mouseY, int button) {
		if (activeBind != null) {
			activeBind.setBind(activeBindOffset, new ModifiedKey(InputUtil.Type.MOUSE.createFromCode(button), activeModifiers));
			activeBind = null;
			return true;
		}
		return super.mouseClicked(mouseX, mouseY, button);
	}

	private void pushModifier(int lastModifier) {
		activeModifiers |= EmiUtil.maskFromCode(this.lastModifier);
		this.lastModifier = lastModifier;
		activeModifiers &= ~EmiUtil.maskFromCode(lastModifier);
	}

	@Override
	public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
		if (activeBind != null) {
			if (EmiUtil.maskFromCode(keyCode) != 0) {
				pushModifier(keyCode);
			} else {
				pushModifier(0);
				if (keyCode == GLFW.GLFW_KEY_ESCAPE) {
					activeBind.setBind(activeBindOffset, new ModifiedKey(InputUtil.UNKNOWN_KEY, 0));
				} else {
					activeBind.setBind(activeBindOffset, new ModifiedKey(InputUtil.Type.KEYSYM.createFromCode(keyCode), activeModifiers));
				}
				activeBind = null;
			}
			return true;
		} else {
			if (keyCode == GLFW.GLFW_KEY_ESCAPE) {
				this.onClose();
				return true;
			} else if (this.client.options.keyInventory.matchesKey(keyCode, scanCode)) {
				this.onClose();
				return true;
			}
		}
		return super.keyPressed(keyCode, scanCode, modifiers);
	}

	@Override
	public boolean keyReleased(int keyCode, int scanCode, int modifiers) {
		if (activeBind != null) {
			activeModifiers &= ~EmiUtil.maskFromCode(keyCode);
			if (keyCode == lastModifier) {
				activeBind.setBind(activeBindOffset, new ModifiedKey(InputUtil.Type.KEYSYM.createFromCode(keyCode), activeModifiers));
				activeBind = null;
			}
			return true;
		}
		return super.keyReleased(keyCode, scanCode, modifiers);
	}

	public static abstract class Entry extends ElementListWidget.Entry<Entry> {
    }

	public static interface Mutator<T> {
		T get();
		void set(T value);
	}
}
