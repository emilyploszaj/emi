package dev.emi.emi.network;

import dev.emi.emi.runtime.EmiLog;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.server.permissions.Permissions;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

public class CreateItemC2SPacket implements EmiPacket {
	private final int mode;
	private final ItemStack stack;

	public CreateItemC2SPacket(int mode, ItemStack stack) {
		this.mode = mode;
		this.stack = stack;
	}

	public CreateItemC2SPacket(RegistryFriendlyByteBuf buf) {
		this(buf.readByte(), ItemStack.OPTIONAL_STREAM_CODEC.decode(buf));
	}

	@Override
	public void write(RegistryFriendlyByteBuf buf) {
		buf.writeByte(mode);
		ItemStack.OPTIONAL_STREAM_CODEC.encode(buf, stack);
	}

	@Override
	public void apply(Player player) {
		if ((player.permissions().hasPermission(Permissions.COMMANDS_GAMEMASTER) || player.isCreative()) && player.containerMenu != null) {
			if (stack.isEmpty()) {
				if (mode == 1 && !player.containerMenu.getCarried().isEmpty()) {
					EmiLog.info(player.getName() + " deleted " + player.containerMenu.getCarried());
					player.containerMenu.setCarried(stack);
				}
			} else {
				EmiLog.info(player.getName() + " cheated in " + stack);
				if (mode == 0) {
					player.getInventory().placeItemBackInInventory(stack);
				} else if (mode == 1) {
					player.containerMenu.setCarried(stack);
				}
			}
		}
	}

	@Override
	public Type<CreateItemC2SPacket> type() {
		return EmiNetwork.CREATE_ITEM;
	}
}
