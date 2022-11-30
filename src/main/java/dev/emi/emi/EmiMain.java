package dev.emi.emi;

import java.util.List;
import java.util.UUID;

import com.google.common.collect.Lists;

import io.netty.buffer.Unpooled;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.screen.slot.Slot;
import net.minecraft.screen.slot.SlotActionType;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.Identifier;

public class EmiMain implements ModInitializer {
	public static final Identifier FILL_RECIPE = new Identifier("emi:fill_recipe");
	public static final Identifier CREATE_ITEM = new Identifier("emi:create_item");
	public static final Identifier DESTROY_HELD = new Identifier("emi:destroy_held");
	public static final Identifier COMMAND = new Identifier("emi:command");
	public static final Identifier CHESS = new Identifier("emi:chess");
	public static final Identifier PING = new Identifier("emi:ping");

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

	@Override
	public void onInitialize() {
		EmiCommands.init();

		ServerPlayConnectionEvents.JOIN.register((handler, sender, server) -> {
			sender.sendPacket(PING, new PacketByteBuf(Unpooled.EMPTY_BUFFER));
		});

		ServerPlayNetworking.registerGlobalReceiver(FILL_RECIPE, (server, player, networkHandler, buf, sender) -> {
			int syncId = buf.readInt();
			int action = buf.readByte();
			ScreenHandler sh = player.currentScreenHandler;
			if (sh != null && sh.syncId == syncId) {
				List<Slot> slots = parseCompressedSlots(sh, buf);
				List<Slot> crafting = Lists.newArrayList();
				int craftingSize = buf.readVarInt();
				for (int i = 0; i < craftingSize; i++) {
					int s = buf.readVarInt();
					if (s >= 0 && s < sh.slots.size()) {
						crafting.add(sh.slots.get(s));
					} else {
						crafting.add(null);
					}
				}
				if (slots == null || crafting == null) {
					return;
				}
				Slot output;
				if (buf.readBoolean()) {
					int i = buf.readVarInt();
					if (i >= 0 && i < sh.slots.size()) {
						output = sh.getSlot(i);
					} else {
						return;
					}
				} else {
					output = null;
				}
				int size = buf.readVarInt();
				List<ItemStack> stacks = Lists.newArrayList();
				for (int i = 0; i < size; i++) {
					stacks.add(buf.readItemStack());
				}
				server.execute(() -> {
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
				});
			}
		});
		ServerPlayNetworking.registerGlobalReceiver(DESTROY_HELD, (server, player, networkHandler, buf, sender) -> {
			if (player.hasPermissionLevel(2) && player.currentScreenHandler != null) {
				server.execute(() -> {
					EmiLog.info(player.getEntityName() + " deleted " + player.currentScreenHandler.getCursorStack());
					player.currentScreenHandler.setCursorStack(ItemStack.EMPTY);
				});
			}
		});
		ServerPlayNetworking.registerGlobalReceiver(CREATE_ITEM, (server, player, networkHandler, buf, sender) -> {
			if (player.hasPermissionLevel(2) && player.currentScreenHandler != null) {
				int mode = buf.readByte();
				ItemStack stack = buf.readItemStack();
				server.execute(() -> {
					EmiLog.info(player.getEntityName() + " cheated in " + stack);
					if (mode == 0) {
						player.getInventory().offerOrDrop(stack);
					} else if (mode == 1) {
						if (player.currentScreenHandler != null) {
							player.currentScreenHandler.setCursorStack(stack);
						}
					}
				});
			}
		});
		ServerPlayNetworking.registerGlobalReceiver(CHESS, (server, player, networkHandler, buf, sender) -> {
			UUID uuid = buf.readUuid();
			PlayerEntity opponent = server.getOverworld().getPlayerByUuid(uuid);
			if (opponent instanceof ServerPlayerEntity spe) {
				PacketByteBuf msg = new PacketByteBuf(Unpooled.buffer());
				msg.writeUuid(player.getUuid());
				msg.writeByte(buf.readByte());
				msg.writeByte(buf.readByte());
				msg.writeByte(buf.readByte());
				ServerPlayNetworking.send(spe, CHESS, msg);
			}
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