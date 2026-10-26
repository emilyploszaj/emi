package dev.emi.emi.jemi.impl;

import java.util.Collection;
import java.util.List;

import com.google.common.collect.Lists;
import com.mojang.datafixers.util.Either;

import dev.emi.emi.EmiPort;
import dev.emi.emi.runtime.EmiLog;
import mezz.jei.api.gui.builder.ITooltipBuilder;
import mezz.jei.api.ingredients.ITypedIngredient;
import mezz.jei.api.runtime.IJeiKeyMapping;
import net.minecraft.client.gui.screens.inventory.tooltip.ClientTooltipComponent;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.FormattedText;
import net.minecraft.world.inventory.tooltip.TooltipComponent;

public class JemiTooltipBuilder implements ITooltipBuilder {
	private final List<Either<FormattedText, TooltipComponent>> lines = Lists.newArrayList();

	public List<ClientTooltipComponent> getTooltip() {
		List<ClientTooltipComponent> tooltip = Lists.newArrayList();
		for (Either<FormattedText, TooltipComponent> line : lines) {
			line.ifLeft(text -> {
				// JEI allows non-text StringVisitable... Minecraft's methods don't easily
				if (text instanceof Component component) {
					tooltip.add(ClientTooltipComponent.create(component.getVisualOrderText()));
				}
			}).ifRight(data -> {
				try {
					tooltip.add(ClientTooltipComponent.create(data));
				} catch (Exception e) {
					EmiLog.error("Error converting TooltipComponent", e);
				}
			});
		}
		return tooltip;
	}

	@Override
	public void add(FormattedText component) {
		lines.add(Either.left(component));
	}

	@Override
	public void addAll(Collection<? extends FormattedText> components) {
		for (FormattedText v : components) {
			add(v);
		}
	}

	@Override
	public void add(TooltipComponent data) {
		lines.add(Either.right(data));
	}

	@Override
	public void addKeyUsageComponent(String translationKey, IJeiKeyMapping keyMapping) {
		add(EmiPort.translatable(translationKey, keyMapping.getTranslatedKeyMessage()));
	}

	@Override
	public void setIngredient(ITypedIngredient<?> typedIngredient) {
		// EMI's methods bypass the vanilla tooltip render which accepts a stack, so this will do nothing
	}

	@Override
	public void clearIngredient() {
	}

	@Override
	public List<Either<FormattedText, TooltipComponent>> getLines() {
		// EMI does not support tooltip removal, this only contains the user's additions
		return lines;
	}
}
