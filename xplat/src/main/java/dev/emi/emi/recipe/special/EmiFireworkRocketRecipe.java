package dev.emi.emi.recipe.special;

import java.util.List;
import java.util.Random;
import java.util.stream.Stream;

import com.google.common.collect.Lists;

import dev.emi.emi.api.recipe.EmiPatternCraftingRecipe;
import dev.emi.emi.api.stack.EmiStack;
import dev.emi.emi.api.widget.GeneratedSlotWidget;
import dev.emi.emi.api.widget.SlotWidget;
import net.minecraft.item.DyeItem;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtList;
import net.minecraft.util.DyeColor;
import net.minecraft.util.Identifier;


public class EmiFireworkRocketRecipe extends EmiPatternCraftingRecipe {
	private static final List<DyeItem> DYES = Stream.of(DyeColor.values()).map(DyeItem::byColor).toList();

	public EmiFireworkRocketRecipe(Identifier id) {
		super(List.of(
				EmiStack.of(Items.PAPER),
						EmiStack.of(Items.FIREWORK_STAR),
						EmiStack.of(Items.GUNPOWDER)),
				EmiStack.of(Items.FIREWORK_ROCKET), id, id);
	}

	@Override
	public SlotWidget getInputWidget(int slot, int x, int y) {
		if (slot == 0) {
			return new SlotWidget(EmiStack.of(Items.PAPER), x, y);
		} else {
			final int s = slot - 1;
			return new GeneratedSlotWidget(r -> {
				List<EmiStack> items = getItems(r);
				if (s < items.size()) {
					return items.get(s);
				}
				return EmiStack.EMPTY;
			}, unique, x, y);
		}
	}

	@Override
	public SlotWidget getOutputWidget(int x, int y) {
		return new GeneratedSlotWidget(this::getFireworkRocket, unique, x, y);
	}

	private EmiStack getFireworkRocket(Random random) {
		ItemStack stack = new ItemStack(Items.FIREWORK_ROCKET);
		NbtCompound tag = new NbtCompound();
		NbtCompound fireworks = new NbtCompound();
		NbtList explosions = new NbtList();

		List<EmiStack> items = getItems(random);
		int gunpowder = 0;
		for (EmiStack item : items) {
			if (item.getId() == EmiStack.of(Items.FIREWORK_STAR).getId()){
				explosions.add(item.getNbt().get("Explosion"));
			} else if (item.isEqual(EmiStack.of(Items.GUNPOWDER))) {
				gunpowder++;
			}
		}
		if (gunpowder > 1) {
			fireworks.putByte("Flight", (byte) gunpowder);
		}
		if (!(items.isEmpty())) {
			fireworks.put("Explosions", explosions);
		}
		tag.put("Fireworks", fireworks);
		stack.setNbt(tag);
		return EmiStack.of(stack, 3);
	}

	private List<EmiStack> getItems(Random random) {
		List<EmiStack> items = Lists.newArrayList();
		int amount = random.nextInt(3);
		for(int i= 0; i<= amount; i++) {
			items.add(EmiStack.of(Items.GUNPOWDER));
		}
		amount = random.nextInt(8-items.size());
		for(int i= 0; i<= amount; i++) {
			items.add(getFireworkStar(random));
		}

		return items;
	}

	private List<DyeItem> getDyes(Random random, int max) {
		List<DyeItem> dyes = Lists.newArrayList();
		int amount = 1 + random.nextInt(max);
		for (int i = 0; i < amount; i++) {
			dyes.add(DYES.get(random.nextInt(DYES.size())));
		}
		return dyes;
	}

	private EmiStack getFireworkStar(Random random) {
		ItemStack stack = new ItemStack(Items.FIREWORK_STAR);
		NbtCompound tag = new NbtCompound();
		NbtCompound explosion = new NbtCompound();
		int items = 0;

		int amount = random.nextInt(5);
		explosion.putByte("Type", (byte) amount);

		if (!(amount == 0)) {
			items++;
		}

		amount = random.nextInt(4);

		if (amount == 0) {
			explosion.putByte("Flicker", (byte) 1);
			items++;
		} else if (amount == 1) {
			explosion.putByte("Trail", (byte) 1);
			items++;
		} else if (amount == 2){
			explosion.putByte("Trail", (byte) 1);
			explosion.putByte("Flicker", (byte) 1);
			items = items + 2;
		}

		List<DyeItem> dyeItems = getDyes(random, 8 - items);
		List<Integer> colors = Lists.newArrayList();
		for (DyeItem dyeItem : dyeItems) {
			colors.add(dyeItem.getColor().getFireworkColor());
		}
		explosion.putIntArray("Colors", colors);

		amount = random.nextInt(2);

		if (amount == 1) {
			List<DyeItem> dyeItemsFaded = getDyes(random, 8);
			List<Integer> fadedColors = Lists.newArrayList();
			for (DyeItem dyeItem : dyeItemsFaded) {
				fadedColors.add(dyeItem.getColor().getFireworkColor());
			}
			explosion.putIntArray("FadeColors", fadedColors);
		}

		tag.put("Explosion", explosion);
		stack.setNbt(tag);
		return EmiStack.of(stack);
	}
}
