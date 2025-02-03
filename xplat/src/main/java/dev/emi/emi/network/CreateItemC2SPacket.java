package dev.emi.emi.network;

import dev.emi.emi.runtime.EmiLog;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.util.Identifier;

public class CreateItemC2SPacket implements EmiPacket {
	private final int mode;
	private final ItemStack stack;

	public CreateItemC2SPacket(int mode, ItemStack stack) {
		this.mode = mode;
		this.stack = stack;
	}

	public CreateItemC2SPacket(PacketByteBuf buf) {
		this(buf.readByte(), buf.readItemStack());
	}

	@Override
	public void write(PacketByteBuf buf) {
		buf.writeByte(mode);
		buf.writeItemStack(stack);
	}

	@Override
	public void apply(PlayerEntity player) {
		if ((player.hasPermissionLevel(2) || player.isCreative()) && player.currentScreenHandler != null) {
			if (stack.isEmpty()) {
				if (mode == 1 && !player.currentScreenHandler.getCursorStack().isEmpty()) {
					EmiLog.info(player.getEntityName() + " deleted " + player.currentScreenHandler.getCursorStack());
					player.currentScreenHandler.setCursorStack(stack);
				}
			} else {
				EmiLog.info(player.getEntityName() + " cheated in " + stack);
				if (mode == 0) {
					player.getInventory().offerOrDrop(stack);
				} else if (mode == 1) {
					player.currentScreenHandler.setCursorStack(stack);
				}
			}
		}
	}

	@Override
	public Identifier getId() {
		return EmiNetwork.CREATE_ITEM;
	}
}
