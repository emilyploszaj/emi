package dev.emi.emi.jemi;

import java.util.List;
import java.util.Optional;

import dev.emi.emi.api.stack.EmiIngredient;
import dev.emi.emi.api.stack.EmiStack;
import mezz.jei.api.constants.VanillaTypes;
import mezz.jei.api.fabric.constants.FabricTypes;
import mezz.jei.api.fabric.ingredients.fluids.IJeiFluidIngredient;
import mezz.jei.api.ingredients.IIngredientType;
import mezz.jei.api.ingredients.ITypedIngredient;
import net.minecraft.fluid.Fluid;
import net.minecraft.fluid.Fluids;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;

@SuppressWarnings({"rawtypes", "unchecked"})
public class JemiUtil {
	
	public static EmiIngredient getIngredient(List<ITypedIngredient<?>> ingredients) {
		if (ingredients.isEmpty()) {
			return EmiStack.EMPTY;
		}
		return EmiIngredient.of(ingredients.stream().map(JemiUtil::getStack).filter(i -> !i.isEmpty()).toList());
	}

	public static EmiStack getStack(Object ingredient) {
		Optional<IIngredientType> optional = (Optional<IIngredientType>) (Optional) JemiPlugin.runtime.getIngredientManager()
			.getIngredientTypeChecked(new JemiFluidIngredient(EmiStack.of(Fluids.WATER, 1234)));
		if (optional.isPresent()) {
			return getStack(optional.get(), ingredient);
		}
		return EmiStack.EMPTY;
	}

	public static EmiStack getStack(ITypedIngredient<?> ingredient) {
		return getStack(ingredient.getType(), ingredient.getIngredient());
	}

	public static EmiStack getStack(IIngredientType<?> type, Object ingredient) {
		if (type == VanillaTypes.ITEM_STACK) {
			return EmiStack.of((ItemStack) ingredient);
		} else if (type == getFluidType()) {
			IJeiFluidIngredient fluid = (IJeiFluidIngredient) ingredient;
			return EmiStack.of(fluid.getFluid(), fluid.getTag().orElseGet(() -> null), fluid.getAmount());
		}
		return EmiStack.EMPTY;
	}

	public static Optional<ITypedIngredient<?>> getTyped(EmiStack stack) {
		if (stack.isEmpty()) {
			return Optional.empty();
		} else if (stack.getKey() instanceof Fluid) {
			JemiFluidIngredient jfi = new JemiFluidIngredient(stack);
			return (Optional) JemiPlugin.runtime.getIngredientManager().createTypedIngredient(getFluidType(), jfi);
		}
		return (Optional) JemiPlugin.runtime.getIngredientManager().createTypedIngredient(VanillaTypes.ITEM_STACK, stack.getItemStack());
	}

	private static IIngredientType getFluidType() {
		return JemiPlugin.runtime.getIngredientManager().getIngredientTypeChecked(new JemiFluidIngredient(EmiStack.of(Fluids.WATER, 1234))).orElseGet(() -> null);
	}

	public static record JemiFluidIngredient(EmiStack stack) implements IJeiFluidIngredient {

		@Override
		public Fluid getFluid() {
			return stack.getKeyOfType(Fluid.class);
		}

		@Override
		public long getAmount() {
			return stack.getAmount();
		}

		@Override
		public Optional<NbtCompound> getTag() {
			return Optional.ofNullable(stack.getNbt());
		}
	}
}
