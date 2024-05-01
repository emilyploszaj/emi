package dev.emi.emi.network;

import java.util.List;
import java.util.function.Consumer;

import net.minecraft.network.RegistryByteBuf;
import net.minecraft.registry.entry.RegistryEntry;
import org.jetbrains.annotations.Nullable;

import com.google.common.collect.Lists;

import dev.emi.emi.runtime.EmiLog;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.screen.slot.Slot;
import net.minecraft.screen.slot.SlotActionType;
import net.minecraft.util.Identifier;

public class FillRecipeC2SPacket implements EmiPacket {
	private final int syncId;
	private final int action;
	private final List<Integer> slots, crafting;
	private final int output;
	private final List<ItemStack> stacks;

	public FillRecipeC2SPacket(ScreenHandler handler, int action, List<Slot> slots, List<Slot> crafting, @Nullable Slot output, List<ItemStack> stacks) {
		this.syncId = handler.syncId;
		this.action = action;
		this.slots = slots.stream().map(s -> s == null ? -1 : s.id).toList();
		this.crafting = crafting.stream().map(s -> s == null ? -1 : s.id).toList();
		this.output = output == null ? -1 : output.id;
		this.stacks = stacks;
	}

	public FillRecipeC2SPacket(RegistryByteBuf buf) {
		syncId = buf.readInt();
		action = buf.readByte();
		slots = parseCompressedSlots(buf);
		crafting = Lists.newArrayList();
		int craftingSize = buf.readVarInt();
		for (int i = 0; i < craftingSize; i++) {
			int s = buf.readVarInt();
			crafting.add(s);
		}
		if (buf.readBoolean()) {
			output = buf.readVarInt();
		} else {
			output = -1;
		}
		int size = buf.readVarInt();
		stacks = Lists.newArrayList();
		for (int i = 0; i < size; i++) {
			stacks.add(ItemStack.OPTIONAL_PACKET_CODEC.decode(buf));
		}
	}

	@Override
	public void write(RegistryByteBuf buf) {
		buf.writeInt(syncId);
		buf.writeByte(action);
		writeCompressedSlots(slots, buf);
		buf.writeVarInt(crafting.size());
		for (Integer s : crafting) {
			buf.writeVarInt(s);
		}
		if (output != -1) {
			buf.writeBoolean(true);
			buf.writeVarInt(output);
		} else {
			buf.writeBoolean(false);
		}
		buf.writeVarInt(stacks.size());
		for (ItemStack stack : stacks) {
			ItemStack.OPTIONAL_PACKET_CODEC.encode(buf, stack);
		}
	}

	@Override
	public void apply(PlayerEntity player) {
		if (slots == null || crafting == null) {
			EmiLog.error("Client requested fill but passed input and crafting slot information was invalid, aborting");
			return;
		}
		ScreenHandler handler = player.currentScreenHandler;
		if (handler == null || handler.syncId != syncId) {
			EmiLog.warn("Client requested fill but screen handler has changed, aborting");
			return;
		}
		List<Slot> slots = Lists.newArrayList();
		List<Slot> crafting = Lists.newArrayList();
		Slot output = null;
		for (int i : this.slots) {
			if (i < 0 || i >= handler.slots.size()) {
				EmiLog.error("Client requested fill but passed input slots don't exist, aborting");
				return;
			}
			slots.add(handler.slots.get(i));
		}
		for (int i : this.crafting) {
			if (i >= 0 && i < handler.slots.size()) {
				crafting.add(handler.slots.get(i));
			} else {
				crafting.add(null);
			}
		}
		if (this.output != -1) {
			if (this.output >= 0 && this.output < handler.slots.size()) {
				output = handler.slots.get(this.output);
			}
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

	private static List<Integer> parseCompressedSlots(PacketByteBuf buf) {
		List<Integer> list = Lists.newArrayList();
		int amount = buf.readVarInt();
		for (int i = 0; i < amount; i++) {
			int low = buf.readVarInt();
			int high = buf.readVarInt();
			if (low < 0) {
				return null;
			}
			for (int j = low; j <= high; j++) {
				list.add(j);
			}
		}
		return list;
	}
	
	private static void writeCompressedSlots(List<Integer> list, PacketByteBuf buf) {
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
			if (ItemStack.areItemsAndComponentsEqual(stack, r)) {
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
			if (ItemStack.areItemsAndComponentsEqual(stack, st)) {
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
	public Id<FillRecipeC2SPacket> getId() {
		return EmiNetwork.FILL_RECIPE;
	}
}
