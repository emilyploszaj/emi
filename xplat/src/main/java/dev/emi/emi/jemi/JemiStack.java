package dev.emi.emi.jemi;

import java.util.List;

import com.google.common.collect.Lists;

import dev.emi.emi.EmiPort;
import dev.emi.emi.EmiUtil;
import dev.emi.emi.api.stack.EmiStack;
import dev.emi.emi.config.EmiConfig;
import dev.emi.emi.jemi.impl.JemiTooltipBuilder;
import dev.emi.emi.runtime.EmiDrawContext;
import mezz.jei.api.ingredients.IIngredientHelper;
import mezz.jei.api.ingredients.IIngredientRenderer;
import mezz.jei.api.ingredients.IIngredientType;
import mezz.jei.api.ingredients.IIngredientTypeWithSubtypes;
import mezz.jei.api.ingredients.subtypes.UidContext;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.screens.inventory.tooltip.ClientTooltipComponent;
import net.minecraft.core.component.DataComponentPatch;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.TooltipFlag;

public class JemiStack<T> extends EmiStack {
	private final IIngredientType<T> type;
	private final IIngredientHelper<T> helper;
	public final Object base;
	public final T ingredient;
	public IIngredientRenderer<T> renderer;

	public JemiStack(IIngredientType<T> type, IIngredientHelper<T> helper, IIngredientRenderer<T> renderer, T ingredient) {
		this.type = type;
		this.helper = helper;
		this.renderer = renderer;
		this.ingredient = ingredient;
		if (type instanceof IIngredientTypeWithSubtypes<?, T> iitws) {
			base = iitws.getBase(ingredient);
		} else {
			base = helper.getUid(ingredient, UidContext.Recipe).toString();
		}
	}

	public String getJeiUid() {
		return helper.getUid(ingredient, UidContext.Ingredient).toString();
	}

	@Override
	public void render(GuiGraphicsExtractor raw, int x, int y, float delta, int flags) {
		EmiDrawContext context = EmiDrawContext.wrap(raw);
		int xOff = (16 - renderer.getWidth()) / 2;
		int yOff = (16 - renderer.getHeight()) / 2;
		context.push();
		context.translate(x + xOff, y + yOff);
		renderer.render(context.raw(), ingredient);
		context.pop();
	}

	@Override
	public JemiStack<T> copy() {
		return new JemiStack<T>(type, helper, renderer, helper.copyIngredient(ingredient));
	}

	@Override
	public boolean isEmpty() {
		return !helper.isValidIngredient(ingredient);
	}

	@Override
	public DataComponentPatch getComponentChanges() {
		return DataComponentPatch.EMPTY;
	}

	@Override
	public Object getKey() {
		return base;
	}

	@Override
	public Identifier getId() {
		return helper.getResourceLocation(ingredient);
	}

	@Override
	public List<Component> getTooltipText() {
		return renderer.getTooltip(ingredient, TooltipFlag.NORMAL);
	}

	@Override
	public List<ClientTooltipComponent> getTooltip() {
		List<ClientTooltipComponent> list = Lists.newArrayList();
		Minecraft client = Minecraft.getInstance();
		JemiTooltipBuilder builder = new JemiTooltipBuilder();
		renderer.getTooltip(builder, ingredient, client.options.advancedItemTooltips ? TooltipFlag.ADVANCED : TooltipFlag.NORMAL);
		list.addAll(builder.getTooltip());

		Identifier id = getId();
		if (EmiConfig.appendModId && id != null) {
			String mod = EmiUtil.getModName(id.getNamespace());
			list.add(ClientTooltipComponent.create(EmiPort.ordered(EmiPort.literal(mod, ChatFormatting.BLUE, ChatFormatting.ITALIC))));
		}

		list.addAll(super.getTooltip());
		return list;
	}

	@Override
	public Component getName() {
		return EmiPort.literal(helper.getDisplayName(ingredient));
	}
}
