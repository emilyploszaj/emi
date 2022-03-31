package dev.emi.emi;

import java.util.List;
import java.util.Map;

import org.apache.commons.compress.utils.Lists;

import dev.emi.emi.api.EmiRecipeHandler;
import dev.emi.emi.handler.CookingRecipeHandler;
import dev.emi.emi.handler.CraftingRecipeHandler;
import dev.emi.emi.handler.InventoryRecipeHandler;
import dev.emi.emi.mixin.accessor.ScreenHandlerAccessor;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.screen.PlayerScreenHandler;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.screen.ScreenHandlerType;
import net.minecraft.screen.slot.Slot;
import net.minecraft.screen.slot.SlotActionType;
import net.minecraft.util.Identifier;

/**
 * add ghost slot support to recipe filling
 * add project table buffer return support to recipe filling
 */
public class EmiMain implements ModInitializer {
	public static final Identifier FILL_RECIPE = new Identifier("emi:fill_recipe");
	public static final EmiRecipeHandler<?> INVENTORY = new InventoryRecipeHandler();
	public static final EmiRecipeHandler<?> CRAFTING = new CraftingRecipeHandler();
	public static final EmiRecipeHandler<?> COOKING = new CookingRecipeHandler();
	public static Map<ScreenHandlerType<?>, EmiRecipeHandler<?>> handlers = Map.of(
		ScreenHandlerType.CRAFTING, CRAFTING,
		ScreenHandlerType.FURNACE, COOKING,
		ScreenHandlerType.BLAST_FURNACE, COOKING,
		ScreenHandlerType.SMOKER, COOKING
	);

	@Override
	@SuppressWarnings({"rawtypes", "unchecked"})
	public void onInitialize() {
		EmiConfig.load();
		ServerPlayNetworking.registerGlobalReceiver(FILL_RECIPE, (server, player, networkHandler, buf, sender) -> {
			int syncId = buf.readInt();
			int action = buf.readByte();
			int size = buf.readVarInt();
			List<ItemStack> stacks = Lists.newArrayList();
			for (int i = 0; i < size; i++) {
				stacks.add(buf.readItemStack());
			}
			server.execute(() -> {
				ScreenHandler sh = player.currentScreenHandler;
				if (sh.syncId == syncId) {
					ScreenHandlerType<?> type = ((ScreenHandlerAccessor) sh).emi$getType();
					EmiRecipeHandler handler = null;
					if (type == null && sh instanceof PlayerScreenHandler) {
						handler = EmiMain.INVENTORY;
					} else if (type != null && handlers.containsKey(type)) {
						handler = handlers.get(type);
					}
					if (handler != null) {
						List<Slot> slots = handler.getInputSources(sh);
						List<Slot> crafting = handler.getCraftingSlots(sh);
						if (crafting.size() >= stacks.size()) {
							List<ItemStack> rubble = Lists.newArrayList();
							for (int i = 0; i < crafting.size(); i++) {
								Slot s = crafting.get(i);
								if (s.canTakeItems(player) && !s.getStack().isEmpty()) {
									rubble.add(s.getStack().copy());
									s.setStack(ItemStack.EMPTY);
								}
							}
							try {	
								for (int i = 0; i < stacks.size(); i++) {
									ItemStack stack = stacks.get(i);
									if (stack.isEmpty()) {
										continue;
									}
									int gotten = grabMatching(player, slots, rubble, crafting, stack);
									if (gotten != stack.getCount()) {
										if (gotten > 0) {
											stack.setCount(gotten);
											player.getInventory().offerOrDrop(stack);
										}
										return;
									} else {
										Slot s = crafting.get(i);
										if (s.canInsert(stack)) {
											s.setStack(stack);
										} else {
											player.getInventory().offerOrDrop(stack);
										}
									}
								}
								Slot output = handler.getOutputSlot(sh);
								if (output != null) {
									if (action == 1) {
										sh.onSlotClick(output.getIndex(), 0, SlotActionType.PICKUP, player);
									} else if (action == 2) {
										sh.onSlotClick(output.getIndex(), 0, SlotActionType.QUICK_MOVE, player);
									}
								}
							} finally {
								for (ItemStack stack : rubble) {
									player.getInventory().offerOrDrop(stack);
								}
							}
						}
					}
				}
			});
		});
	}

	private static int grabMatching(PlayerEntity player, List<Slot> slots, List<ItemStack> rubble, List<Slot> crafting, ItemStack stack) {
		int amount = stack.getCount();
		int grabbed = 0;
		for (int i = 0; i < rubble.size(); i++) {
			if (grabbed >= amount) {
				return grabbed;
			}
			ItemStack r = rubble.get(i);
			if (ItemStack.canCombine(stack, r)) {
				int wanted = amount - grabbed;
				if (r.getCount() <= wanted) {
					grabbed += r.getCount();
					rubble.remove(i);
					i--;
				} else {
					grabbed = amount;
					r.setCount(r.getCount() - wanted);
				}
			}
		}
		for (Slot s : slots) {
			if (grabbed >= amount) {
				return grabbed;
			}
			if (crafting.contains(s) || !s.canTakeItems(player)) {
				continue;
			}
			ItemStack st = s.getStack();
			if (ItemStack.canCombine(stack, st)) {
				int wanted = amount - grabbed;
				if (st.getCount() <= wanted) {
					grabbed += st.getCount();
					s.setStack(ItemStack.EMPTY);
				} else {
					grabbed = amount;
					st.setCount(st.getCount() - wanted);
				}
			}
		}
		return grabbed;
	}
}