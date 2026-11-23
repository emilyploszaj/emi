package dev.emi.emi.network;

import dev.emi.emi.api.EmiApi;
import dev.emi.emi.api.recipe.EmiRecipe;
import dev.emi.emi.api.stack.EmiStack;
import dev.emi.emi.bom.BoM;
import dev.emi.emi.registry.EmiCommands;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.player.Player;

public class CommandS2CPacket implements EmiPacket {
	private final byte type;
	private final Identifier id;

	public CommandS2CPacket(byte type, Identifier id) {
		this.type = type;
		this.id = id;
	}

	public CommandS2CPacket(FriendlyByteBuf buf) {
		type = buf.readByte();
		if (type == EmiCommands.VIEW_RECIPE || type == EmiCommands.TREE_GOAL || type == EmiCommands.TREE_RESOLUTION) {
			id = buf.readIdentifier();
		} else {
			id = null;
		}
	}

	@Override
	public void write(RegistryFriendlyByteBuf buf) {
		buf.writeByte(type);
		if (type == EmiCommands.VIEW_RECIPE || type == EmiCommands.TREE_GOAL || type == EmiCommands.TREE_RESOLUTION) {
			buf.writeIdentifier(id);
		}
	}

	@Override
	public void apply(Player player) {
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
		}
	}

	@Override
	public Type<CommandS2CPacket> type() {
		return EmiNetwork.COMMAND;
	}
}
