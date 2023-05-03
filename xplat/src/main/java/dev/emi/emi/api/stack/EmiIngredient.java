package dev.emi.emi.api.stack;

import java.text.DecimalFormat;
import java.util.Arrays;
import java.util.List;

import dev.emi.emi.EmiPort;
import dev.emi.emi.EmiUtil;
import dev.emi.emi.api.render.EmiRenderable;
import dev.emi.emi.registry.EmiTags;
import net.minecraft.client.gui.tooltip.TooltipComponent;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.fluid.Fluid;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.recipe.Ingredient;
import net.minecraft.registry.tag.TagKey;
import net.minecraft.text.Text;

public interface EmiIngredient extends EmiRenderable {
	public static final DecimalFormat TEXT_FORMAT = new DecimalFormat("0.##");
	public static final Text EMPTY_TEXT = EmiPort.literal("");
	public static final int RENDER_ICON = 1;
	public static final int RENDER_AMOUNT = 2;
	public static final int RENDER_INGREDIENT = 4;
	public static final int RENDER_REMAINDER = 8;
	
	/**
	 * @return The {@link EmiStack}s represented by this ingredient.
	 * 	List is never empty. For an empty ingredient, us {@link EmiStack#EMPTY}
	 */
	List<EmiStack> getEmiStacks();

	default boolean isEmpty() {
		for (EmiStack stack : getEmiStacks()) {
			if (!stack.isEmpty()) {
				return false;
			}
		}
		return true;
	}

	EmiIngredient copy();

	long getAmount();

	EmiIngredient setAmount(long amount);

	float getChance();

	EmiIngredient setChance(float chance);

	@Override
	default void render(MatrixStack matrices, int x, int y, float delta) {
		render(matrices, x, y, delta, -1);
	}

	void render(MatrixStack matrices, int x, int y, float delta, int flags);

	List<TooltipComponent> getTooltip();

	public static boolean areEqual(EmiIngredient a, EmiIngredient b) {
		List<EmiStack> as = a.getEmiStacks();
		List<EmiStack> bs = b.getEmiStacks();
		if (as.size() != bs.size()) {
			return false;
		}
		for (int i = 0; i < as.size(); i++) {
			if (!as.get(i).isEqual(bs.get(i))) {
				return false;
			}
		}
		return true;
	}

	public static EmiIngredient of(TagKey<Item> key) {
		return of(key, 1);
	}
	
	public static EmiIngredient of(TagKey<Item> key, long amount) {
		List<EmiStack> stacks = EmiUtil.values(key).map(ItemStack::new).map(EmiStack::of).toList();
		if (stacks.isEmpty()) {
			return EmiStack.EMPTY;
		} else if (stacks.size() == 1) {
			return stacks.get(0);
		}
		return new TagEmiIngredient(key, stacks, amount);
	}

	public static EmiIngredient of(Ingredient ingredient) {
		return of(ingredient, 1);
	}

	public static EmiIngredient of(Ingredient ingredient, long amount) {
		if (ingredient == null) {
			return EmiStack.EMPTY;
		}
		return EmiTags.getIngredient(Item.class, Arrays.stream(ingredient.getMatchingStacks()).map(EmiStack::of).toList(), amount);
	}

	public static EmiIngredient of(List<? extends EmiIngredient> list) {
		return of(list, 1);
	}

	public static EmiIngredient of(List<? extends EmiIngredient> list, long amount) {
		if (list.size() == 0) {
			return EmiStack.EMPTY;
		} else if (list.size() == 1) {
			return list.get(0);
		} else {
			long internalAmount = list.get(0).getAmount();
			for (EmiIngredient i : list) {
				if (i.getAmount() != internalAmount) {
					internalAmount = 1;
				}
			}
			if (internalAmount > 1) {
				amount = internalAmount;
				list = list.stream().map(st -> st.copy().setAmount(1)).toList();
			}
			Class<?> tagType = null;
			for (EmiIngredient i : list) {
				for (EmiStack s : i.getEmiStacks()) {
					if (!s.isEmpty()) {
						Class<?> tt = null;
						if (s.getKey() instanceof Item) {
							tt = Item.class;
						} else if (s.getKey() instanceof Fluid) {
							tt = Fluid.class;
						}
						if (tt == null || (tagType != null && tt != tagType)) {
							tagType = tt;
							return new ListEmiIngredient(list, amount);
						}
					}
				}
			}
			return EmiTags.getIngredient(tagType, list.stream().flatMap(i -> i.getEmiStacks().stream()).toList(), amount);
		}
	}
}
