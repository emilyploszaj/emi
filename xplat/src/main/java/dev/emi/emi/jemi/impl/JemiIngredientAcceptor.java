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
import dev.emi.emi.runtime.EmiLog;
import mezz.jei.api.gui.builder.IIngredientAcceptor;
import mezz.jei.api.gui.ingredient.IRecipeSlotTooltipCallback;
import mezz.jei.api.ingredients.IIngredientType;
import mezz.jei.api.ingredients.ITypedIngredient;
import mezz.jei.api.recipe.RecipeIngredientRole;
import net.minecraft.client.item.TooltipContext;
import net.minecraft.fluid.Fluid;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.text.Text;

public class JemiIngredientAcceptor implements IIngredientAcceptor<JemiIngredientAcceptor> {
	public static final Pattern FLUID_END = Pattern.compile("(^|\\s)([\\d,]+)\\s*mB$");
	public final RecipeIngredientRole role;
	public final List<EmiStack> stacks = Lists.newArrayList();

	public JemiIngredientAcceptor(RecipeIngredientRole role) {
		this.role = role;
	}

	@SuppressWarnings({"rawtypes", "unchecked"})
	public void coerceStacks(IRecipeSlotTooltipCallback tooltipCallback, Map<IIngredientType<?>, IngredientRenderer<?>> renderers) {
		if (tooltipCallback == null && renderers == null) {
			return;
		}
		for (EmiStack stack : stacks) {
			ITypedIngredient typed = JemiUtil.getTyped(stack).orElse(null);
			if (typed != null && (stack instanceof JemiStack || stack.getKey() instanceof Fluid)) {
				List<Text> base = Lists.newArrayList();
				if (renderers != null && renderers.containsKey(typed.getType())) {
					base.addAll(((IngredientRenderer) renderers.get(typed.getType())).renderer().getTooltip(typed.getIngredient(), TooltipContext.Default.BASIC));
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
					tooltipCallback.onTooltip(jsr, base);
				}
				for (int i = 0; i < 2 && i < base.size(); i++) {
					Text t = base.get(i);
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
	public <I> JemiIngredientAcceptor addIngredients(IIngredientType<I> ingredientType, List<@Nullable I> ingredients) {
		for (I i : ingredients) {
			addIngredient(ingredientType, i);
		}
		return this;
	}

	@Override
	public <I> JemiIngredientAcceptor addIngredient(IIngredientType<I> ingredientType, I ingredient) {
		addStack(JemiUtil.getStack(ingredientType, ingredient));
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
	public JemiIngredientAcceptor addFluidStack(Fluid fluid) {
		return addFluidStack(fluid, FluidUnit.BUCKET);
	}

	@Override
	public JemiIngredientAcceptor addFluidStack(Fluid fluid, long amount) {
		addStack(EmiStack.of(fluid, amount));
		return this;
	}

	@Override
	public JemiIngredientAcceptor addFluidStack(Fluid fluid, long amount, NbtCompound tag) {
		addStack(EmiStack.of(fluid, tag, amount));
		return this;
	}

	@Override
	@SuppressWarnings({"rawtypes", "unchecked"})
	public JemiIngredientAcceptor addTypedIngredients(List<ITypedIngredient<?>> ingredients) {
		for (ITypedIngredient<?> i : ingredients) {
			addIngredient(((IIngredientType) i.getType()), i.getIngredient());
		}
		return this;
	}

	@Override
	@SuppressWarnings({"rawtypes", "unchecked"})
	public JemiIngredientAcceptor addOptionalTypedIngredients(List<Optional<ITypedIngredient<?>>> ingredients) {
		for (Optional<ITypedIngredient<?>> opt : ingredients) {
			if (opt.isPresent()) {
				ITypedIngredient<?> i = opt.get();
				addIngredient(((IIngredientType) i.getType()), i.getIngredient());
			}
		}
		return this;
	}
}
