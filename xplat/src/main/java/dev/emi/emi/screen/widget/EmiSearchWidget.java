package dev.emi.emi.screen.widget;

import net.minecraft.client.input.KeyEvent;

import net.minecraft.client.input.MouseButtonEvent;

import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import org.joml.Matrix3x2fStack;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.resources.language.I18n;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.Style;
import net.minecraft.util.Mth;
import net.minecraft.util.Tuple;
import org.lwjgl.glfw.GLFW;

import com.google.common.collect.Lists;
import dev.emi.emi.EmiPort;
import dev.emi.emi.config.EmiConfig;
import dev.emi.emi.runtime.EmiDrawContext;
import dev.emi.emi.screen.EmiScreenManager;
import dev.emi.emi.search.EmiSearch;
import dev.emi.emi.search.QueryType;

public class EmiSearchWidget extends EditBox {
	private static final Pattern ESCAPE = Pattern.compile("\\\\.");
	private List<String> searchHistory = Lists.newArrayList();
	private int searchHistoryIndex = 0;
	private List<Tuple<Integer, Style>> styles;
	private long lastClick = 0;
	private String last = "";
	private long lastRender = System.currentTimeMillis();
	private long accumulatedSpin = 0;
	public boolean highlight = false;
	// Reimplement focus because other mods keep breaking it
	public boolean isFocused;

	public EmiSearchWidget(Font textRenderer, int x, int y, int width, int height) {
		super(textRenderer, x, y, width, height, EmiPort.literal(""));
		this.setCanLoseFocus(true);
		this.setTextColor(-1);
		this.setTextColorUneditable(-1);
		this.setMaxLength(256);
		this.addFormatter((string, stringStart) -> {
			MutableComponent text = null;
			int s = 0;
			int last = 0;
			for (; s < styles.size(); s++) {
				Tuple<Integer, Style> style = styles.get(s);
				int end = style.getA();
				if (end > stringStart) {
					if (end - stringStart >= string.length()) {
						text = EmiPort.literal(string.substring(0, string.length()), style.getB());
						// Skip second loop
						s = styles.size();
						break;
					}
					text = EmiPort.literal(string.substring(0, end - stringStart), style.getB());
					last = end - stringStart;
					s++;
					break;
				}
			}
			for (; s < styles.size(); s++) {
				Tuple<Integer, Style> style = styles.get(s);
				int end = style.getA();
				if (end - stringStart >= string.length()) {
					EmiPort.append(text, EmiPort.literal(string.substring(last, string.length()), style.getB()));
					break;
				}
				EmiPort.append(text, EmiPort.literal(string.substring(last, end - stringStart), style.getB()));
				last = end - stringStart;
			}
			return EmiPort.ordered(text);
		});
		this.setResponder(string -> {
			if (string.isEmpty()) {
				this.setSuggestion(I18n.get("emi.search"));
			} else {
				this.setSuggestion("");
			}
			EmiScreenManager.updateSearchSidebar();
			Matcher matcher = EmiSearch.TOKENS.matcher(string);
			List<Tuple<Integer, Style>> styles = Lists.newArrayList();
			int last = 0;
			while (matcher.find()) {
				int start = matcher.start();
				int end = matcher.end();
				if (last < start) {
					styles.add(new Tuple<Integer, Style>(start, Style.EMPTY.applyFormat(ChatFormatting.WHITE)));
				}
				String group = matcher.group();
				if (group.startsWith("-")) {
					styles.add(new Tuple<Integer, Style>(start + 1, Style.EMPTY.applyFormat(ChatFormatting.RED)));
					start++;
					group = group.substring(1);
				}
				QueryType type = QueryType.fromString(group);
				int subStart = type.prefix.length();
				if (group.length() > 1 + subStart && group.substring(subStart).startsWith("/") && group.endsWith("/")) {
					int rOff = start + subStart + 1;
					styles.add(new Tuple<Integer, Style>(rOff, type.slashColor));
					Matcher rMatcher = ESCAPE.matcher(string.substring(rOff, end - 1));
					int rLast = 0;
					while (rMatcher.find()) {
						int rStart = rMatcher.start();
						int rEnd = rMatcher.end();
						if (rLast < rStart) {
							styles.add(new Tuple<Integer, Style>(rStart + rOff, type.regexColor));
						}
						styles.add(new Tuple<Integer, Style>(rEnd + rOff, type.escapeColor));
						rLast = rEnd;
					}
					if (rLast < end - 1) {
						styles.add(new Tuple<Integer, Style>(end - 1, type.regexColor));
					}
					styles.add(new Tuple<Integer, Style>(end, type.slashColor));
				} else {
					styles.add(new Tuple<Integer, Style>(end, type.color));
				}

				last = end;
			}
			if (last < string.length()) {
				styles.add(new Tuple<Integer, Style>(string.length(), Style.EMPTY.applyFormat(ChatFormatting.WHITE)));
			}
			this.styles = styles;
			EmiSearch.search(string);
		});
	}

	public void update() {
		setValue(getValue());
	}

	public void swap() {
		String last = this.getValue();
		this.setValue(this.last);
		this.last = last;
	}

	@Override
	public void setFocused(boolean focused) {
		if (focused == isFocused) {
			return;
		}
		if (!focused) {
			searchHistoryIndex = 0;
			String currentSearch = getValue();
			if (!currentSearch.isBlank() && !currentSearch.isEmpty()) {
				searchHistory.removeIf(String::isBlank);
				searchHistory.remove(currentSearch);
				searchHistory.add(0, currentSearch);
				if (searchHistory.size() > 36) {
					searchHistory.remove(searchHistory.size() - 1);
				}
			}
		}
		isFocused = focused;
		super.setFocused(focused);
		EmiScreenManager.updateSearchSidebar();
	}

	@Override
	public boolean isFocused() {
		return isFocused;
	}

	@Override
	public boolean mouseClicked(MouseButtonEvent event, boolean doubleClick) {
		double mouseX = event.x();
		double mouseY = event.y();
		int button = event.button();
		if (!isMouseOver(mouseX, mouseY) || !EmiConfig.enabled) {
			EmiPort.focus(this, false);
			return false;
		} else {
			boolean b = super.mouseClicked(event, doubleClick);
			if (isMouseOver(mouseX, mouseY)) {
				EmiPort.focus(this, true);
			}
			if (this.isFocused()) {
				if (button == 0) {
					if (System.currentTimeMillis() - lastClick < 500) {
						highlight = !highlight;
						lastClick = 0;
					} else {
						lastClick = System.currentTimeMillis();
					}
				} else if (button == 1) {
					this.setValue("");
					EmiPort.focus(this, true);
				}
			}
			return b;
		}
	}

	@Override
	public boolean keyPressed(KeyEvent event) {
		int keyCode = event.key();
		int scanCode = event.scancode();
		int modifiers = event.modifiers();
		if (this.isFocused()) {
			if (EmiConfig.clearSearch.matchesKey(keyCode, scanCode)) {
				setValue("");
				return true;
			}
			if ((EmiConfig.focusSearch.matchesKey(keyCode, scanCode)
					|| keyCode == GLFW.GLFW_KEY_ENTER || keyCode == GLFW.GLFW_KEY_ESCAPE)) {
				EmiPort.focus(this, false);
				return true;
			}
			if (keyCode == GLFW.GLFW_KEY_UP || keyCode == GLFW.GLFW_KEY_DOWN) {
				int offset = keyCode == GLFW.GLFW_KEY_UP ? 1 : -1;
				if (searchHistoryIndex + offset >= 0 && searchHistoryIndex + offset < searchHistory.size()) {
					if (searchHistoryIndex >= 0 && searchHistoryIndex < searchHistory.size()) {
						searchHistory.set(searchHistoryIndex, getValue());
					}
					searchHistoryIndex += offset;
					setValue(searchHistory.get(searchHistoryIndex));
				}
			}
		}
		return super.keyPressed(event);
	}

	@Override
	public void extractWidgetRenderState(GuiGraphicsExtractor raw, int mouseX, int mouseY, float delta) {
		EmiDrawContext context = EmiDrawContext.wrap(raw);
		this.setEditable(EmiConfig.enabled);
		String lower = getValue().toLowerCase();

		boolean dinnerbone = lower.contains("dinnerbone");
		accumulatedSpin += (dinnerbone ? 1 : -1) * Math.abs(System.currentTimeMillis() - lastRender);
		if (accumulatedSpin < 0) {
			accumulatedSpin = 0;
		} else if (accumulatedSpin > 500) {
			accumulatedSpin = 500;
		}
		lastRender = System.currentTimeMillis();
		long deg = accumulatedSpin * -180 / 500;
		Matrix3x2fStack view = context.matrices();
		view.pushMatrix();
		if (deg != 0) {
			view.translate(this.x + this.width / 2, this.y + this.height / 2);
			view.rotate((float) Math.toRadians(-deg));
			view.translate(-(this.x + this.width / 2), -(this.y + this.height / 2));
		}

		if (lower.contains("jeb_")) {
			int amount = 0x3FF;
			float h = ((lastRender & amount) % (float) amount) / (float) amount;
			int rgb = Mth.hsvToRgb(h, 1, 1);
			context.setColor(((rgb >> 16) & 0xFF) / 255f, ((rgb >> 8) & 0xFF) / 255f, ((rgb >> 0) & 0xFF) / 255f);
		}

		if (EmiConfig.enabled) {
			super.extractWidgetRenderState(context.raw(), mouseX, mouseY, delta);
			if (highlight) {
				int border = 0xffeeee00;
				context.fill(this.x - 1, this.y - 1, this.width + 2, 1, border);
				context.fill(this.x - 1, this.y + this.height, this.width + 2, 1, border);
				context.fill(this.x - 1, this.y - 1, 1, this.height + 2, border);
				context.fill(this.x + this.width, this.y - 1, 1, this.height + 2, border);
			}
		}
		context.resetColor();
		view.popMatrix();
	}
}
