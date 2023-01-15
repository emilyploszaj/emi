package dev.emi.emi.screen;

import java.lang.reflect.Field;
import java.util.Arrays;
import java.util.List;
import java.util.Set;
import java.util.function.Predicate;
import java.util.function.Supplier;

import org.lwjgl.glfw.GLFW;

import com.google.common.collect.Sets;

import dev.emi.emi.EmiPort;
import dev.emi.emi.EmiRenderHelper;
import dev.emi.emi.EmiUtil;
import dev.emi.emi.bind.EmiBind;
import dev.emi.emi.bind.EmiBind.ModifiedKey;
import dev.emi.emi.com.unascribed.qdcss.QDCSS;
import dev.emi.emi.config.ConfigEnum;
import dev.emi.emi.config.EmiConfig;
import dev.emi.emi.config.EmiConfig.Comment;
import dev.emi.emi.config.EmiConfig.ConfigGroup;
import dev.emi.emi.config.EmiConfig.ConfigGroupEnd;
import dev.emi.emi.config.EmiConfig.ConfigValue;
import dev.emi.emi.config.IntGroup;
import dev.emi.emi.config.ScreenAlign;
import dev.emi.emi.config.SidebarPages;
import dev.emi.emi.screen.widget.SizedButtonWidget;
import dev.emi.emi.screen.widget.config.BooleanWidget;
import dev.emi.emi.screen.widget.config.ConfigEntryWidget;
import dev.emi.emi.screen.widget.config.ConfigSearch;
import dev.emi.emi.screen.widget.config.EmiBindWidget;
import dev.emi.emi.screen.widget.config.EmiNameWidget;
import dev.emi.emi.screen.widget.config.EnumWidget;
import dev.emi.emi.screen.widget.config.GroupNameWidget;
import dev.emi.emi.screen.widget.config.IntGroupWidget;
import dev.emi.emi.screen.widget.config.IntWidget;
import dev.emi.emi.screen.widget.config.ListWidget;
import dev.emi.emi.screen.widget.config.ScreenAlignWidget;
import dev.emi.emi.screen.widget.config.SidebarPagesWidget;
import dev.emi.emi.screen.widget.config.SubGroupNameWidget;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.tooltip.TooltipComponent;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.gui.widget.TextFieldWidget;
import net.minecraft.client.resource.language.I18n;
import net.minecraft.client.util.InputUtil;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.text.StringVisitable;
import net.minecraft.text.Style;
import net.minecraft.text.Text;

public class ConfigScreen extends Screen {
	private static final int maxWidth = 240;
	private Screen last;
	private ConfigSearch search;
	public ListWidget list;
	public EmiBind activeBind;
	public int activeBindOffset;
	public int activeModifiers;
	public int lastModifier;
	public String originalConfig;
	public ButtonWidget resetButton;

	public ConfigScreen(Screen last) {
		super(EmiPort.translatable("screen.emi.config"));
		this.last = last;
		originalConfig = EmiConfig.getSavedConfig();
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
	public static List<TooltipComponent> getFieldTooltip(Field field) {
		MinecraftClient client = MinecraftClient.getInstance();
		List<TooltipComponent> text;
		ConfigValue annot = field.getAnnotation(ConfigValue.class);
		String key = "config.emi.tooltip." + annot.value().replace('-', '_');
		Comment comment = field.getAnnotation(Comment.class);
		if (I18n.hasTranslation(key)) {
			text = (List<TooltipComponent>) (Object) Arrays.stream(I18n.translate(key).split("\n"))
				.map(s -> client.textRenderer.wrapLines(StringVisitable.plain(s), maxWidth))
				.flatMap(l -> l.stream()).map(TooltipComponent::of).toList();
		} else if (comment != null) {
			text = (List<TooltipComponent>) (Object) Arrays.stream(comment.value().split("\n"))
				.map(s -> client.textRenderer.wrapLines(StringVisitable.plain(s), maxWidth))
				.flatMap(l -> l.stream()).map(TooltipComponent::of).toList();
		} else {
			text = null;
		}
		if (text == null) {
			return List.of();
		}
		return text;
	}

	@Override
	@SuppressWarnings("unchecked")
	protected void init() {
		super.init();

		// Persistent-ish state
		int scroll = 0;
		String query = "";
		Set<String> collapsed = Sets.newHashSet();
		if (list != null) {
			scroll = (int) list.getScrollAmount();
			query = search.getSearch();
			for (ListWidget.Entry e : list.children()) {
				if (e instanceof GroupNameWidget g) {
					if (g.collapsed) {
						collapsed.add(g.text.getString());
					}
				}
			}
		}

		list = new ListWidget(client, width, height, 40, height - 60);
		this.addDrawable(new EmiNameWidget(width / 2, 16));
		int w = Math.min(400, width - 40);
		int x = (width - w) / 2;
		search = new ConfigSearch(x + 3, height - 51, w / 2 - 4, 18);
		this.addDrawable(search.field);
		this.resetButton = EmiPort.newButton(x + 2, height - 30, w / 2 - 2, 20, EmiPort.translatable("gui.done"), button -> {
			EmiConfig.loadConfig(QDCSS.load("revert", originalConfig));
			MinecraftClient client = MinecraftClient.getInstance();
			this.init(client, client.getWindow().getScaledWidth(), client.getWindow().getScaledHeight());
		});
		this.addDrawableChild(EmiPort.newButton(x + w / 2 + 2, height - 30, w / 2 - 2, 20, EmiPort.translatable("gui.done"), button -> {
			this.close();
		}));
		this.addDrawableChild(EmiPort.newButton(x + w / 2 + 2, height - 52, w / 2 - 24, 20, EmiPort.translatable("screen.emi.presets"), button -> {
			MinecraftClient client = MinecraftClient.getInstance();
			client.setScreen(new ConfigPresetScreen(this));
		}));
		this.addDrawableChild(new SizedButtonWidget(x + w - 20, height - 52, 20, 20, 164, 64, () -> true, widget -> {
			EmiConfig.setGlobalState(!EmiConfig.useGlobalConfig);
			ConfigScreen.this.resize(client, width, height);
		}, () -> (EmiConfig.useGlobalConfig ? 40 : 0), () -> {
			return (List<Text>) (Object) Arrays.stream(I18n.translate("tooltip.emi.config.global").split("\n"))
				.map(s -> client.textRenderer.getTextHandler().wrapLines(StringVisitable.plain(s), maxWidth, Style.EMPTY))
				.flatMap(l -> l.stream()).map(v -> EmiPort.literal(v.getString())).toList();
		}));
		this.addDrawableChild(resetButton);
		this.addSelectableChild(search.field);
		try {
			String lastGroup = "";
			GroupNameWidget lastGroupWidget = null;
			ConfigGroup currentGroup = null;
			SubGroupNameWidget currentSubGroupWidget = null;
			Supplier<String> searchSupplier = () -> search.getSearch();
			for (Field field : EmiConfig.class.getFields()) {
				ConfigValue annot = field.getAnnotation(ConfigValue.class);
				if (annot != null) {
					String group = annot.value().split("\\.")[0];
					if (group.equals("persistent")) {
						continue;
					}
					if (!group.equals(lastGroup)) {
						lastGroup = group;
						Text text = EmiPort.translatable("config.emi.group." + group.replace('-', '_'));
						lastGroupWidget = new GroupNameWidget(text);
						if (collapsed.contains(text.getString())) {
							lastGroupWidget.collapsed = true;
						}
						list.addEntry(lastGroupWidget);
					}
					ConfigGroup configGroup = field.getAnnotation(ConfigGroup.class);
					if (configGroup != null) {
						currentGroup = configGroup;
						Text text = EmiPort.translatable("config.emi.group." + configGroup.value().replace('-', '_'));
						currentSubGroupWidget = new SubGroupNameWidget(text);
						if (collapsed.contains(text.getString())) {
							currentSubGroupWidget.collapsed = true;
						}
						currentSubGroupWidget.parent = lastGroupWidget;
						list.addEntry(currentSubGroupWidget);
					}
					Predicate<?> predicate = EmiConfig.FILTERS.getOrDefault(annot.value(), v -> true);
					Text translation = EmiPort.translatable("config.emi." + annot.value().replace('-', '_'));
					ConfigEntryWidget entry = null;
					if (field.getType() == boolean.class) {
						entry = new BooleanWidget(translation, getFieldTooltip(field), searchSupplier, new Mutator<Boolean>() {

							public Boolean getValue() {
								try {
									return field.getBoolean(null);
								} catch(Exception e) {}
								return false;
							}

							public void setValue(Boolean value) {
								try {
									field.setBoolean(null, value);
								} catch (Exception e) {}
							}						
						});
					} else if (field.getType() == int.class) {
						entry = new IntWidget(translation, getFieldTooltip(field), searchSupplier, new Mutator<Integer>() {

							public Integer getValue() {
								try {
									return field.getInt(null);
								} catch(Exception e) {}
								return -1;
							}

							public void setValue(Integer value) {
								try {
									field.setInt(null, value);
								} catch (Exception e) {}
							}						
						});
					} else if (field.getType() == EmiBind.class) {
						entry = new EmiBindWidget(this, getFieldTooltip(field), searchSupplier, (EmiBind) field.get(null));
					} else if (field.getType() == ScreenAlign.class) {
						entry = new ScreenAlignWidget(translation, getFieldTooltip(field), searchSupplier, objectMutator(field));
					} else if (field.getType() == SidebarPages.class) {
						entry = new SidebarPagesWidget(translation, getFieldTooltip(field), searchSupplier, objectMutator(field));
					} else if (IntGroup.class.isAssignableFrom(field.getType())) {
						entry = new IntGroupWidget(translation, getFieldTooltip(field), searchSupplier, objectMutator(field));
					} else if (ConfigEnum.class.isAssignableFrom(field.getType())) {
						entry = new EnumWidget(translation, getFieldTooltip(field), searchSupplier, objectMutator(field), (Predicate<ConfigEnum>) predicate);
					}
					boolean endGroup = field.getAnnotation(ConfigGroupEnd.class) != null;
					if (entry != null) {
						entry.group = currentGroup;
						entry.endGroup = endGroup;
						list.addEntry(entry);
						if (lastGroupWidget != null) {
							lastGroupWidget.children.add(entry);
							entry.parentGroups.add(lastGroupWidget);
						}
						if (currentSubGroupWidget != null) {
							currentSubGroupWidget.children.add(entry);
							entry.parentGroups.add(currentSubGroupWidget);
						}
					}
					if (endGroup) {
						currentGroup = null;
						currentSubGroupWidget = null;
					}
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}

		this.addSelectableChild(list);
		list.setScrollAmount(scroll);
		search.setText(query);
		updateChanges();
	}

	@SuppressWarnings("unchecked")
	public <T> Mutator<T> objectMutator(Field field) {
		return new Mutator<T>() {
			public T getValue() {
				try {
					return (T) field.get(null);
				} catch(Exception e) {
					throw new RuntimeException(e);
				}
			}

			public void setValue(T en) {
				try {
					field.set(null, en);
				} catch(Exception e) {
					throw new RuntimeException(e);
				}
			}
		};
	}

	public void updateChanges() {
		// Split on the blank lines between config options
		String[] oLines = originalConfig.split("\n\n");
		String[] cLines = EmiConfig.getSavedConfig().split("\n\n");
		int different = 0;
		for (int i = 0; i < oLines.length; i++) {
			if (i >= cLines.length) {
				break;
			}
			if (!oLines[i].equals(cLines[i])) {
				different++;
			}
		}
		this.resetButton.active = different > 0;
		this.resetButton.setMessage(EmiPort.translatable("screen.emi.config.reset", different));
	}
	
	@Override
	public void render(MatrixStack matrices, int mouseX, int mouseY, float delta) {
		list.setScrollAmount(list.getScrollAmount());
		this.renderBackgroundTexture(-100);
		list.render(matrices, mouseX, mouseY, delta);
		super.render(matrices, mouseX, mouseY, delta);
		if (list.getHoveredEntry() != null) {
			EmiRenderHelper.drawTooltip(this, matrices, list.getHoveredEntry().getTooltip(mouseX, mouseY), mouseX, mouseY);
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
				updateChanges();
			}
			return true;
		} else {
			// Element nesting causes crashing for cycling, for some reason
			if (keyCode == GLFW.GLFW_KEY_TAB) {
				return false;
			}
			if (super.keyPressed(keyCode, scanCode, modifiers)) {
				return true;
			}
			if (this.getFocused() instanceof TextFieldWidget tfw && tfw.isFocused()) {
				if (keyCode == GLFW.GLFW_KEY_ESCAPE) {
					tfw.setTextFieldFocused(false);
					return true;
				}
			} else {
				if (keyCode == GLFW.GLFW_KEY_ESCAPE) {
					this.close();
					return true;
				} else if (this.client.options.inventoryKey.matchesKey(keyCode, scanCode)) {
					this.close();
					return true;
				}
			}
		}
		return false;
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

	@Override
	public boolean shouldCloseOnEsc() {
		return false;
	}

	public abstract class Mutator<T> {
		protected abstract T getValue();
		protected abstract void setValue(T value);

		public T get() {
			return getValue();
		}

		public void set(T value) {
			setValue(value);
			updateChanges();
		}
	}
}
