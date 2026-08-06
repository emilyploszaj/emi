package dev.emi.emi.network;

import dev.emi.emi.runtime.EmiLog;

import net.minecraft.command.DefaultPermissions;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.network.RegistryByteBuf;

public class CreateItemC2SPacket implements EmiPacket {
	private final int mode;
	private final ItemStack stack;

	public CreateItemC2SPacket(int mode, ItemStack stack) {
		this.mode = mode;
		this.stack = stack;
	}

	public CreateItemC2SPacket(RegistryByteBuf buf) {
		this(buf.readByte(), ItemStack.OPTIONAL_PACKET_CODEC.decode(buf));
	}

	@Override
	public void write(RegistryByteBuf buf) {
		buf.writeByte(mode);
		ItemStack.OPTIONAL_PACKET_CODEC.encode(buf, stack);
	}

	@Override
	public void apply(PlayerEntity player) {
		if ((player.getPermissions().hasPermission(DefaultPermissions.GAMEMASTERS) || player.isCreative()) && player.currentScreenHandler != null) {
			if (stack.isEmpty()) {
				if (mode == 1 && !player.currentScreenHandler.getCursorStack().isEmpty()) {
					EmiLog.info(player.getStringifiedName() + " deleted " + player.currentScreenHandler.getCursorStack());
					player.currentScreenHandler.setCursorStack(stack);
				}
			} else {
				EmiLog.info(player.getStringifiedName() + " cheated in " + stack);
				if (mode == 0) {
					player.getInventory().offerOrDrop(stack);
				} else if (mode == 1) {
					player.currentScreenHandler.setCursorStack(stack);
				}
			}
		}
	}

	@Override
	public Id<CreateItemC2SPacket> getId() {
		return EmiNetwork.CREATE_ITEM;
	}
}
