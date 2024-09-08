package dev.emi.emi.jemi.impl;

import java.util.Collection;
import java.util.List;

import com.google.common.collect.Lists;

import mezz.jei.api.gui.builder.ITooltipBuilder;
import mezz.jei.api.ingredients.ITypedIngredient;
import net.minecraft.client.gui.tooltip.TooltipComponent;
import net.minecraft.item.tooltip.TooltipData;
import net.minecraft.text.StringVisitable;
import net.minecraft.text.Text;

public class JemiTooltipBuilder implements ITooltipBuilder {
	public final List<TooltipComponent> tooltip = Lists.newArrayList();
	private final List<Text> legacyText = Lists.newArrayList();

	@Override
	public void add(StringVisitable component) {
		// JEI allows non-text StringVisitable... Minecraft's methods don't easily
		if (component instanceof Text text) {
			tooltip.add(TooltipComponent.of(text.asOrderedText()));
			legacyText.add(text);
		}
	}

	@Override
	public void addAll(Collection<? extends StringVisitable> components) {
		for (StringVisitable v : components) {
			add(v);
		}
	}

	@Override
	public void add(TooltipData data) {
		try {
			tooltip.add(TooltipComponent.of(data));
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	@Override
	public void setIngredient(ITypedIngredient<?> typedIngredient) {
		// EMI's methods bypass the vanilla tooltip render which accepts a stack, so this will do nothing
	}

	@Override
	public void clear() {
		// EMI does not support tooltip removeal, this will only clear the user's additions
	}

	@Override
	public List<Text> toLegacyToComponents() {
		return legacyText;
	}

	@Override
	public void removeAll(List<Text> components) {
		// EMI does not support tooltip removeal
	}
}
