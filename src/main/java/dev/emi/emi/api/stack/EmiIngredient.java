package dev.emi.emi.api.stack;

import java.text.DecimalFormat;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import org.apache.commons.compress.utils.Lists;

import dev.emi.emi.EmiClient;
import dev.emi.emi.EmiPort;
import dev.emi.emi.EmiUtil;
import dev.emi.emi.api.render.EmiRenderable;
import net.minecraft.client.gui.tooltip.TooltipComponent;
import net.minecraft.client.util.math.MatrixStack;
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

	default Text getAmountText(double amount) {
		if (amount == 0) {
			return EMPTY_TEXT;
		} else {
			return EmiPort.literal(TEXT_FORMAT.format(amount));
		}
	}

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
		List<Item> items = Arrays.stream(ingredient.getMatchingStacks())
			.filter(s -> !s.isEmpty())
			.map(i -> i.getItem())
			.distinct()
			.collect(Collectors.toList());
		if (items.size() == 0) {
			return EmiStack.EMPTY;
		} else if (items.size() == 1) {
			return EmiStack.of(ingredient.getMatchingStacks()[0], amount);
		}
		List<TagKey<Item>> keys = Lists.newArrayList();
		for (TagKey<Item> key : EmiClient.itemTags) {
			List<Item> values = EmiUtil.values(key).map(i -> i.value()).toList();
			if (values.size() < 2) {
				continue;
			}
			if (items.containsAll(values)) {
				items.removeAll(values);
				keys.add(key);
			}
			if (items.size() == 0) {
				break;
			}
		}
		if (keys.isEmpty()) {
			return new ListEmiIngredient(Arrays.stream(ingredient.getMatchingStacks()).map(EmiStack::of).toList(), amount);
		} else if (items.isEmpty()) {
			if (keys.size() == 1) {
				return new TagEmiIngredient(keys.get(0), amount);
			} else {
				return new ListEmiIngredient(keys.stream().map(k -> new TagEmiIngredient(k, amount)).toList(), amount);
			}
		} else {
			return new ListEmiIngredient(List.of(items.stream().map(ItemStack::new).map(EmiStack::of).toList(),
					keys.stream().map(k -> new TagEmiIngredient(k, amount)).toList())
				.stream().flatMap(a -> a.stream()).toList(), amount);
		}

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
				for (EmiIngredient i : list) {
					if (i instanceof EmiStack s) {
						s.setAmount(1);
					}
				}
			}
			for (EmiIngredient i : list) {
				for (EmiStack s : i.getEmiStacks()) {
					if (!(s.getKey() instanceof Item)) {
						return new ListEmiIngredient(list, amount);
					}
				}
			}
			return EmiIngredient.of(Ingredient.ofStacks(list.stream().flatMap(i -> i.getEmiStacks().stream().map(s -> s.getItemStack()))), amount);
		}
	}
}
