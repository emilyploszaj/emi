package dev.emi.emi.network;

import dev.emi.emi.api.EmiApi;
import dev.emi.emi.api.recipe.EmiRecipe;
import dev.emi.emi.api.stack.EmiStack;
import dev.emi.emi.bom.BoM;
import dev.emi.emi.registry.EmiCommands;
import dev.emi.emi.runtime.EmiShareRecipe;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.network.RegistryByteBuf;
import net.minecraft.util.Identifier;

public class CommandS2CPacket implements EmiPacket {
	private final byte type;
	private final Identifier id;
	private String extraInfo;

	public CommandS2CPacket(byte type, Identifier id) {
		this.type = type;
		this.id = id;
	}

	public CommandS2CPacket(byte type, Identifier id, String extraInfo) {
		this.type = type;
		this.id = id;
		this.extraInfo = extraInfo;

	}

	public CommandS2CPacket(PacketByteBuf buf) {
		type = buf.readByte();
		if (type == EmiCommands.VIEW_RECIPE || type == EmiCommands.TREE_GOAL || type == EmiCommands.TREE_RESOLUTION) {
			id = buf.readIdentifier();
		} else if (type == EmiCommands.SHARE_RECIPE) {
			id = buf.readIdentifier();
			extraInfo = buf.readString();

		} else {
			id = null;
		}
	}

	@Override
	public void write(RegistryByteBuf buf) {
		buf.writeByte(type);
		if (type == EmiCommands.VIEW_RECIPE || type == EmiCommands.TREE_GOAL || type == EmiCommands.TREE_RESOLUTION) {
			buf.writeIdentifier(id);
		} else if (type == EmiCommands.SHARE_RECIPE) {
			buf.writeIdentifier(id);
			buf.writeString(this.extraInfo);
		}
	}

	@Override
	public void apply(PlayerEntity player) {
		if (type == EmiCommands.VIEW_RECIPE) {
			EmiRecipe recipe = EmiApi.getRecipeManager().getRecipe(id);
			if (recipe != null) {
				EmiApi.displayRecipe(recipe);
			}
		} else if (type == EmiCommands.VIEW_TREE) {
			EmiApi.viewRecipeTree();
		} else if (type == EmiCommands.TREE_GOAL) {
			EmiRecipe recipe = EmiApi.getRecipeManager().getRecipe(id);
			if (recipe != null) {
				BoM.setGoal(recipe);
			}
		} else if (type == EmiCommands.TREE_RESOLUTION) {
			EmiRecipe recipe = EmiApi.getRecipeManager().getRecipe(id);
			if (recipe != null && BoM.tree != null) {
				for (EmiStack stack : recipe.getOutputs()) {
					BoM.tree.addResolution(stack, recipe);
				}
			}
		} else if (type == EmiCommands.SHARE_RECIPE) {
			EmiShareRecipe.receiveMessage(player, id, extraInfo);
		}
	}

	@Override
	public Id<CommandS2CPacket> getId() {
		return EmiNetwork.COMMAND;
	}
}
