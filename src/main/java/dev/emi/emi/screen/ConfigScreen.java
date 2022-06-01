package dev.emi.emi.screen;

import java.lang.reflect.Field;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import com.mojang.blaze3d.systems.RenderSystem;

import org.lwjgl.glfw.GLFW;

import dev.emi.emi.EmiConfig;
import dev.emi.emi.EmiConfig.Comment;
import dev.emi.emi.EmiConfig.ConfigEnum;
import dev.emi.emi.EmiConfig.ConfigValue;
import dev.emi.emi.EmiPort;
import dev.emi.emi.EmiUtil;
import dev.emi.emi.bind.EmiBind;
import dev.emi.emi.bind.EmiBind.ModifiedKey;
import dev.emi.emi.screen.widget.BooleanWidget;
import dev.emi.emi.screen.widget.EmiBindWidget;
import dev.emi.emi.screen.widget.EmiNameWidget;
import dev.emi.emi.screen.widget.EnumWidget;
import dev.emi.emi.screen.widget.GroupNameWidget;
import dev.emi.emi.screen.widget.IntWidget;
import dev.emi.emi.screen.widget.ListWidget;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.Drawable;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.resource.language.I18n;
import net.minecraft.client.util.InputUtil;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.text.Text;

public class ConfigScreen extends Screen {
	private Screen last;
	private ListWidget list;
	public EmiBind activeBind;
	public int activeBindOffset;
	public int activeModifiers;
	public int lastModifier;

	public ConfigScreen(Screen last) {
		super(EmiPort.translatable("screen.emi.config"));
		this.last = last;
	}

	public void setActiveBind(EmiBind bind, int offset) {
		activeBind = bind;
		activeBindOffset = offset;
		activeModifiers = 0;
		lastModifier = 0;
	}

	@Override
	public void close() {
		EmiConfig.writeConfig();
		MinecraftClient.getInstance().setScreen(last);
	}

	@SuppressWarnings("unchecked")
	public Drawable getTooltipRenderer(Field field) {
		List<Text> text;
		ConfigValue annot = field.getAnnotation(ConfigValue.class);
		String key = "config.emi.tooltip." + annot.value().replace('-', '_');
		System.out.println(key);
		Comment comment = field.getAnnotation(Comment.class);
		if (I18n.hasTranslation(key)) {
			text = (List<Text>) (Object) Arrays.stream(I18n.translate(key).split("\n")).map(EmiPort::literal).toList();
		} else if (comment != null) {
			text = (List<Text>) (Object) Arrays.stream(comment.value().split("\n")).map(EmiPort::literal).toList();
		} else {
			text = null;
		}
		return (matrices, mouseX, mouseY, delta) -> {
			if (text != null) {
				RenderSystem.enableDepthTest();
				renderTooltip(matrices, text,
					Optional.empty(), mouseX, mouseY);
			}
		};
	}

	@Override
	protected void init() {
		super.init();
		list = new ListWidget(client, width, height, 0, height);
		list.addEntry(new EmiNameWidget());
		try {
			String lastGroup = "";
			for (Field field : EmiConfig.class.getFields()) {
				ConfigValue annot = field.getAnnotation(ConfigValue.class);
				if (annot != null) {
					String group = annot.value().split("\\.")[0];
					if (group.equals("persistent")) {
						continue;
					}
					if (!group.equals(lastGroup)) {
						lastGroup = group;
						list.addEntry(new GroupNameWidget(EmiPort.translatable("config.emi.group." + group)));
					}
					Text translation = EmiPort.translatable("config.emi." + annot.value().replace('-', '_'));
					if (field.getType() == boolean.class) {
						list.addEntry(new BooleanWidget(translation, getTooltipRenderer(field), new Mutator<Boolean>() {

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
					} else if (field.getType() == int.class) {
						list.addEntry(new IntWidget(translation, getTooltipRenderer(field), new Mutator<Integer>() {

							public Integer get() {
								try {
									return field.getInt(null);
								} catch(Exception e) {}
								return -1;
							}

							public void set(Integer value) {
								try {
									field.setInt(null, value);
								} catch (Exception e) {}
							}						
						}));
					} else if (field.getType() == EmiBind.class) {
						list.addEntry(new EmiBindWidget(this, getTooltipRenderer(field), (EmiBind) field.get(null)));
					} else if (ConfigEnum.class.isAssignableFrom(field.getType())) {
						list.addEntry(new EnumWidget(translation, getTooltipRenderer(field), new Mutator<ConfigEnum>() {

							public ConfigEnum get() {
								try {
									return (ConfigEnum) field.get(null);
								} catch(Exception e) {
									throw new RuntimeException(e);
								}
							}

							public void set(ConfigEnum en) {
								try {
									field.set(null, en);
								} catch(Exception e) {
									throw new RuntimeException(e);
								}
							}
						}));
					}
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}

		this.addSelectableChild(list);
	}
	
	@Override
	public void render(MatrixStack matrices, int mouseX, int mouseY, float delta) {
		this.renderBackgroundTexture(-100);
		list.render(matrices, mouseX, mouseY, delta);
		if (list.getHoveredEntry() != null) {
			list.getHoveredEntry().renderTooltip(matrices, mouseX, mouseY, delta);
		}
	}
	
	@Override
	public boolean mouseClicked(double mouseX, double mouseY, int button) {
		if (activeBind != null) {
			pushModifier(0);
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
				this.close();
				return true;
			} else if (this.client.options.inventoryKey.matchesKey(keyCode, scanCode)) {
				this.close();
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

	public static interface Mutator<T> {
		T get();
		void set(T value);
	}
}
