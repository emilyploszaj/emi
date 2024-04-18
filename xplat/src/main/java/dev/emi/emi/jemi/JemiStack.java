package dev.emi.emi.jemi;

import java.util.List;

import com.google.common.collect.Lists;

import dev.emi.emi.EmiPort;
import dev.emi.emi.EmiUtil;
import dev.emi.emi.api.stack.EmiStack;
import dev.emi.emi.config.EmiConfig;
import dev.emi.emi.runtime.EmiDrawContext;
import mezz.jei.api.ingredients.IIngredientHelper;
import mezz.jei.api.ingredients.IIngredientRenderer;
import mezz.jei.api.ingredients.IIngredientType;
import mezz.jei.api.ingredients.IIngredientTypeWithSubtypes;
import mezz.jei.api.ingredients.subtypes.UidContext;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.tooltip.TooltipComponent;
import net.minecraft.client.item.TooltipType;
import net.minecraft.component.ComponentChanges;
import net.minecraft.component.ComponentMap;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.Identifier;

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
			base = helper.getUniqueId(ingredient, UidContext.Recipe);
		}
	}

	public String getJeiUid() {
		return helper.getUniqueId(ingredient, UidContext.Ingredient);
	}

	@Override
	public void render(DrawContext raw, int x, int y, float delta, int flags) {
		EmiDrawContext context = EmiDrawContext.wrap(raw);
		int xOff = (16 - renderer.getWidth()) / 2;
		int yOff = (16 - renderer.getHeight()) / 2;
		context.push();
		context.matrices().translate(x + xOff, y + yOff, 0);
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
	public ComponentMap getComponents() {
		return ComponentMap.EMPTY;
	}

	@Override
	public ComponentChanges getComponentChanges() {
		return ComponentChanges.EMPTY;
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
	public List<Text> getTooltipText() {
		return renderer.getTooltip(ingredient, TooltipType.BASIC);
	}

	@Override
	public List<TooltipComponent> getTooltip() {
		List<TooltipComponent> list = Lists.newArrayList();
		MinecraftClient client = MinecraftClient.getInstance();
		list.addAll(renderer.getTooltip(ingredient, client.options.advancedItemTooltips ? TooltipType.ADVANCED : TooltipType.BASIC)
			.stream().map(EmiPort::ordered).map(TooltipComponent::of).toList());

		Identifier id = getId();
		if (EmiConfig.appendModId && id != null) {
			String mod = EmiUtil.getModName(id.getNamespace());
			list.add(TooltipComponent.of(EmiPort.ordered(EmiPort.literal(mod, Formatting.BLUE, Formatting.ITALIC))));
		}

		list.addAll(super.getTooltip());
		return list;
	}

	@Override
	public Text getName() {
		return EmiPort.literal(helper.getDisplayName(ingredient));
	}
}
