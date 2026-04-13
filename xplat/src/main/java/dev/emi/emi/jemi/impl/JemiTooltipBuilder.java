package dev.emi.emi.jemi.impl;

import java.util.Collection;
import java.util.List;

import com.google.common.collect.Lists;
import com.mojang.datafixers.util.Either;

import dev.emi.emi.runtime.EmiLog;
import mezz.jei.api.gui.builder.ITooltipBuilder;
import mezz.jei.api.ingredients.ITypedIngredient;
import mezz.jei.api.runtime.IJeiKeyMapping;

import net.minecraft.client.gui.tooltip.TooltipComponent;
import net.minecraft.item.tooltip.TooltipData;
import net.minecraft.text.StringVisitable;
import net.minecraft.text.Text;

public class JemiTooltipBuilder implements ITooltipBuilder {
	public final List<TooltipComponent> tooltip = Lists.newArrayList();

	@Override
	public void add(StringVisitable component) {
		// JEI allows non-text StringVisitable... Minecraft's methods don't easily
		if (component instanceof Text text) {
			tooltip.add(TooltipComponent.of(text.asOrderedText()));
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
			EmiLog.error("Error converting TooltipComponent", e);
		}
	}

    @Override
    public void addKeyUsageComponent(String s, IJeiKeyMapping iJeiKeyMapping) {

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
    public void clearIngredient() {

    }

    @Override
    public List<Either<StringVisitable, TooltipData>> getLines() {
        return List.of();
    }

}
