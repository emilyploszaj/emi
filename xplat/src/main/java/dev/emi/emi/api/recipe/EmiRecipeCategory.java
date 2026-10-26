package dev.emi.emi.api.recipe;

import java.util.Comparator;
import java.util.List;
import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.screens.inventory.tooltip.ClientTooltipComponent;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import org.jetbrains.annotations.Nullable;

import com.google.common.collect.Lists;

import dev.emi.emi.EmiPort;
import dev.emi.emi.EmiUtil;
import dev.emi.emi.api.render.EmiRenderable;
import dev.emi.emi.api.stack.EmiStack;
import dev.emi.emi.config.EmiConfig;
import dev.emi.emi.data.EmiRecipeCategoryProperties;

public class EmiRecipeCategory implements EmiRenderable {
	public Identifier id;
	public EmiRenderable icon, simplified;
	public Comparator<EmiRecipe> sorter;
	
	/**
	 * A constructor to use only a single renderable for both the icon and simplified icon.
	 * It is generally recommended that simplified icons be unique and follow the style of vanilla icons.
	 * 
	 * {@link EmiStack} instances can be passed as {@link EmiRenderable}
	 */
	public EmiRecipeCategory(Identifier id, EmiRenderable icon) {
		this(id, icon, icon);
	}

	/**
	 * {@link EmiStack} instances can be passed as {@link EmiRenderable}
	 */
	public EmiRecipeCategory(Identifier id, EmiRenderable icon, EmiRenderable simplified) {
		this(id, icon, simplified, EmiRecipeSorting.none());
	}

	/**
	 * {@link EmiStack} instances can be passed as {@link EmiRenderable}
	 */
	public EmiRecipeCategory(Identifier id, EmiRenderable icon, EmiRenderable simplified, Comparator<EmiRecipe> sorter) {
		this.id = id;
		this.icon = icon;
		this.simplified = simplified;
		this.sorter = sorter;
	}

	public Component getName() {
		return EmiPort.translatable(EmiUtil.translateId("emi.category.", getId()));
	}

	public Identifier getId() {
		return id;
	}

	@Override
	public void render(GuiGraphicsExtractor draw, int x, int y, float delta) {
		EmiRecipeCategoryProperties.getIcon(this).render(draw, x, y, delta);
	}

	public void renderSimplified(GuiGraphicsExtractor draw, int x, int y, float delta) {
		EmiRecipeCategoryProperties.getSimplifiedIcon(this).render(draw, x, y, delta);
	}

	public List<ClientTooltipComponent> getTooltip() {
		List<ClientTooltipComponent> list = Lists.newArrayList();
		list.add(ClientTooltipComponent.create(EmiPort.ordered(getName())));
		if (EmiUtil.showAdvancedTooltips()) {
			list.add(ClientTooltipComponent.create(EmiPort.ordered(EmiPort.literal(id.toString(), ChatFormatting.DARK_GRAY))));
		}
		if (EmiConfig.appendModId) {
			list.add(ClientTooltipComponent.create(EmiPort.ordered(EmiPort.literal(EmiUtil.getModName(getId().getNamespace()),
				ChatFormatting.BLUE, ChatFormatting.ITALIC))));
		}
		return list;
	}

	public @Nullable Comparator<EmiRecipe> getSort() {
		return sorter;
	}
}
