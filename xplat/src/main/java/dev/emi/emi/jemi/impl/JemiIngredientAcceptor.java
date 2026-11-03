package dev.emi.emi.jemi.impl;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.jetbrains.annotations.Nullable;

import com.google.common.collect.Lists;

import dev.emi.emi.EmiPort;
import dev.emi.emi.api.stack.EmiIngredient;
import dev.emi.emi.api.stack.EmiStack;
import dev.emi.emi.config.FluidUnit;
import dev.emi.emi.jemi.JemiStack;
import dev.emi.emi.jemi.JemiUtil;
import dev.emi.emi.jemi.impl.JemiRecipeSlot.IngredientRenderer;
import mezz.jei.api.gui.builder.IIngredientAcceptor;
import mezz.jei.api.gui.ingredient.IRecipeSlotRichTooltipCallback;
import mezz.jei.api.ingredients.IIngredientType;
import mezz.jei.api.ingredients.ITypedIngredient;
import mezz.jei.api.recipe.RecipeIngredientRole;
import net.minecraft.core.component.DataComponentPatch;
import net.minecraft.util.context.ContextMap;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ItemStackTemplate;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.display.SlotDisplay;
import net.minecraft.world.level.ItemLike;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.material.Fluid;

public class JemiIngredientAcceptor implements IIngredientAcceptor<JemiIngredientAcceptor> {
	public static final Pattern FLUID_END = Pattern.compile("(^|\\s)([\\d,]+)\\s*mB$");
	public final RecipeIngredientRole role;
	public final List<EmiStack> stacks = Lists.newArrayList();

	public JemiIngredientAcceptor(RecipeIngredientRole role) {
		this.role = role;
	}

	@SuppressWarnings({"rawtypes", "unchecked"})
	public void coerceStacks(IRecipeSlotRichTooltipCallback tooltipCallback, Map<IIngredientType<?>, IngredientRenderer<?>> renderers) {
		if (tooltipCallback == null && renderers == null) {
			return;
		}
		for (EmiStack stack : stacks) {
			ITypedIngredient typed = JemiUtil.getTyped(stack).orElse(null);
			if (typed != null && (stack instanceof JemiStack || stack.getKey() instanceof Fluid)) {
				List<Component> base = Lists.newArrayList();
				if (renderers != null && renderers.containsKey(typed.getType())) {
					base.addAll(((IngredientRenderer) renderers.get(typed.getType())).renderer().getTooltip(typed.getIngredient(), TooltipFlag.NORMAL));
				}
				if (base == null || base.isEmpty()) {
					if (tooltipCallback == null) {
						continue;
					}
					base.add(stack.getName());
					base.add(EmiPort.literal(""));
				}
				if (tooltipCallback != null) {
					JemiRecipeSlot jsr = new JemiRecipeSlot(role, stack);
					JemiTooltipBuilder builder = new JemiTooltipBuilder();
					tooltipCallback.onRichTooltip(jsr, builder);
					for (var line : builder.getLines()) {
						line.ifLeft(text -> {
							if (text instanceof Component component) {
								base.add(component);
							}
						});
					}
				}
				for (int i = 0; i < 2 && i < base.size(); i++) {
					Component t = base.get(i);
					if (t != null) {
						Matcher m = FLUID_END.matcher(t.getString());
						if (m.find()) {
							long amount = Long.parseLong(m.group(2).replace(",", ""));
							if (amount != stack.getAmount()) {
								stack.setAmount(amount);
							}
						}
					}
				}
			}
		}
	}

	public EmiIngredient build() {
		return EmiIngredient.of(stacks);
	}

	private void addStack(EmiStack stack) {
		if (!stack.isEmpty()) {
			stacks.add(stack);
		}
	}

	@Override
	public JemiIngredientAcceptor add(SlotDisplay slotDisplay) {
		for (EmiStack stack : EmiPort.getIngredient(slotDisplay).getEmiStacks()) {
			addStack(stack);
		}
		return this;
	}

	@Override
	public <I> JemiIngredientAcceptor add(IIngredientType<I> ingredientType, SlotDisplay slotDisplay) {
		return add(slotDisplay);
	}

	@Override
	public JemiIngredientAcceptor add(ItemStack itemStack) {
		addStack(EmiStack.of(itemStack));
		return this;
	}

	@Override
	public JemiIngredientAcceptor add(ItemLike itemLike) {
		addStack(EmiStack.of(itemLike));
		return this;
	}

	@Override
	public JemiIngredientAcceptor add(ItemStackTemplate itemStackTemplate) {
		return add(itemStackTemplate.create());
	}

	@Override
	public JemiIngredientAcceptor add(Ingredient ingredient) {
		for (EmiStack stack : EmiIngredient.of(ingredient).getEmiStacks()) {
			addStack(stack);
		}
		return this;
	}

	@Override
	public <I> JemiIngredientAcceptor add(IIngredientType<I> ingredientType, Ingredient ingredient) {
		return add(ingredient);
	}

	@Override
	public <I> JemiIngredientAcceptor add(ITypedIngredient<I> typedIngredient) {
		return add(typedIngredient.getType(), typedIngredient.getIngredient());
	}

	@Override
	public <I> JemiIngredientAcceptor add(IIngredientType<I> ingredientType, I ingredient) {
		addStack(JemiUtil.getStack(ingredientType, ingredient));
		return this;
	}

	@Override
	public <I> JemiIngredientAcceptor addIngredients(IIngredientType<I> ingredientType, List<@Nullable I> ingredients) {
		for (I i : ingredients) {
			if (i != null) {
				add(ingredientType, i);
			}
		}
		return this;
	}

	@Override
	public JemiIngredientAcceptor addIngredientsUnsafe(List<?> ingredients) {
		for (Object o : ingredients) {
			addStack(JemiUtil.getStack(o));
		}
		return this;
	}

	@Override
	public JemiIngredientAcceptor add(Fluid fluid) {
		return add(fluid, FluidUnit.BUCKET);
	}

	@Override
	public JemiIngredientAcceptor add(Fluid fluid, long amount) {
		addStack(EmiStack.of(fluid, amount));
		return this;
	}
	
	@Override
	public JemiIngredientAcceptor add(Fluid fluid, long amount, DataComponentPatch componentChanges) {
		addStack(EmiStack.of(fluid, componentChanges, amount));
		return this;
	}

	@Override
	public JemiIngredientAcceptor addTypedIngredients(List<ITypedIngredient<?>> ingredients) {
		for (ITypedIngredient<?> i : ingredients) {
			add(i);
		}
		return this;
	}

	@Override
	public JemiIngredientAcceptor addOptionalTypedIngredients(List<Optional<ITypedIngredient<?>>> ingredients) {
		for (Optional<ITypedIngredient<?>> opt : ingredients) {
			opt.ifPresent(this::add);
		}
		return this;
	}

	@Override
	public JemiIngredientAcceptor addItemStacks(List<ItemStack> itemStacks) {
		for (ItemStack stack : itemStacks) {
			add(stack);
		}
		return this;
	}

	@Override
	public ContextMap getContextMap() {
		return EmiPort.displayContext();
	}
}
