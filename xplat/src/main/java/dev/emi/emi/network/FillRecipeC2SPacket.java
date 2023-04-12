package dev.emi.emi.network;

import java.util.List;
import java.util.function.Consumer;

import org.jetbrains.annotations.Nullable;

import com.google.common.collect.Lists;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.screen.slot.Slot;
import net.minecraft.screen.slot.SlotActionType;
import net.minecraft.util.Identifier;

public class FillRecipeC2SPacket implements EmiPacket {
	private final ScreenHandler handler;
	private final int action;
	private final List<Slot> slots, crafting;
	private final @Nullable Slot output;
	private final List<ItemStack> stacks;

	public FillRecipeC2SPacket(ScreenHandler handler, int action, List<Slot> slots, List<Slot> crafting, @Nullable Slot output, List<ItemStack> stacks) {
		this.handler = handler;
		this.action = action;
		this.slots = slots;
		this.crafting = crafting;
		this.output = output;
		this.stacks = stacks;
	}

	public FillRecipeC2SPacket(PlayerEntity player, PacketByteBuf buf) {
		int syncId = buf.readInt();
		handler = player.currentScreenHandler;
		action = buf.readByte();
		if (handler != null && handler.syncId == syncId) {
			slots = parseCompressedSlots(handler, buf);
			crafting = Lists.newArrayList();
			int craftingSize = buf.readVarInt();
			for (int i = 0; i < craftingSize; i++) {
				int s = buf.readVarInt();
				if (s >= 0 && s < handler.slots.size()) {
					crafting.add(handler.slots.get(s));
				} else {
					crafting.add(null);
				}
			}
			if (buf.readBoolean()) {
				int i = buf.readVarInt();
				if (i >= 0 && i < handler.slots.size()) {
					output = handler.getSlot(i);
				} else {
					output = null;
				}
			} else {
				output = null;
			}
			int size = buf.readVarInt();
			stacks = Lists.newArrayList();
			for (int i = 0; i < size; i++) {
				stacks.add(buf.readItemStack());
			}
		} else {
			slots = List.of();
			crafting = List.of();
			output = null;
			stacks = List.of();
		}
	}

	@Override
	public void write(PacketByteBuf buf) {
		buf.writeInt(handler.syncId);
		buf.writeByte(action);
		writeCompressedSlots(slots, buf);
		buf.writeVarInt(crafting.size());
		for (Slot s : crafting) {
			buf.writeVarInt(s == null ? -1 : s.id);
		}
		if (output != null) {
			buf.writeBoolean(true);
			buf.writeVarInt(output.id);
		} else {
			buf.writeBoolean(false);
		}
		buf.writeVarInt(stacks.size());
		for (ItemStack stack : stacks) {
			buf.writeItemStack(stack);
		}
	}

	@Override
	public void apply(PlayerEntity player) {
		if (handler == null || slots == null || crafting == null) {
			return;
		}
		if (crafting.size() >= stacks.size()) {
			List<ItemStack> rubble = Lists.newArrayList();
			for (int i = 0; i < crafting.size(); i++) {
				Slot s = crafting.get(i);
				if (s != null && s.canTakeItems(player) && !s.getStack().isEmpty()) {
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
						System.out.println(gotten + " " + stack.getCount());
						return;
					} else {
						Slot s = crafting.get(i);
						if (s != null && s.canInsert(stack) && stack.getCount() <= s.getMaxItemCount()) {
							s.setStack(stack);
						} else {
							player.getInventory().offerOrDrop(stack);
						}
					}
				}
				if (output != null) {
					if (action == 1) {
						handler.onSlotClick(output.getIndex(), 0, SlotActionType.PICKUP, player);
					} else if (action == 2) {
						handler.onSlotClick(output.getIndex(), 0, SlotActionType.QUICK_MOVE, player);
					}
				}
			} finally {
				for (ItemStack stack : rubble) {
					player.getInventory().offerOrDrop(stack);
				}
			}
		}
	}

	private static List<Slot> parseCompressedSlots(ScreenHandler handler, PacketByteBuf buf) {
		List<Slot> list = Lists.newArrayList();
		int amount = buf.readVarInt();
		for (int i = 0; i < amount; i++) {
			int low = buf.readVarInt();
			int high = buf.readVarInt();
			if (low < 0) {
				return null;
			}
			for (int j = low; j <= high; j++) {
				if (j < handler.slots.size()) {
					list.add(handler.getSlot(j));
				} else {
					return null;
				}
			}
		}
		return list;
	}
	
	private static void writeCompressedSlots(List<Slot> slots, PacketByteBuf buf) {
		List<Integer> list = slots.stream()
			.map(s -> s.id)
			.sorted()
			.distinct()
			.toList();
		List<Consumer<PacketByteBuf>> postWrite = Lists.newArrayList();
		int groups = 0;
		int i = 0;
		while (i < list.size()) {
			groups++;
			int start = i;
			int startValue = list.get(start);
			while (i < list.size() && i - start == list.get(i) - startValue) {
				i++;
			}
			int end = i - 1;
			postWrite.add(b -> {
				b.writeVarInt(startValue);
				b.writeVarInt(list.get(end));
			});
		}
		buf.writeVarInt(groups);
		for (Consumer<PacketByteBuf> consumer : postWrite) {
			consumer.accept(buf);
		}
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

	@Override
	public Identifier getId() {
		return EmiNetwork.FILL_RECIPE;
	}
}
