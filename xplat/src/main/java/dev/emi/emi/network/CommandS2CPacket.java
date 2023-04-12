package dev.emi.emi.network;

import dev.emi.emi.EmiCommands;
import dev.emi.emi.EmiRecipes;
import dev.emi.emi.api.EmiApi;
import dev.emi.emi.api.recipe.EmiRecipe;
import dev.emi.emi.api.stack.EmiStack;
import dev.emi.emi.bom.BoM;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.util.Identifier;

public class CommandS2CPacket implements EmiPacket {
	private final byte type;
	private final Identifier id;

	public CommandS2CPacket(byte type, Identifier id) {
		this.type = type;
		this.id = id;
	}

	public CommandS2CPacket(PacketByteBuf buf) {
		type = buf.readByte();
		if (type == EmiCommands.VIEW_RECIPE || type == EmiCommands.TREE_GOAL || type == EmiCommands.TREE_RESOLUTION) {
			id = buf.readIdentifier();
		} else {
			id = null;
		}
	}

	@Override
	public void write(PacketByteBuf buf) {
		buf.writeByte(type);
		if (type == EmiCommands.VIEW_RECIPE || type == EmiCommands.TREE_GOAL || type == EmiCommands.TREE_RESOLUTION) {
			buf.writeIdentifier(id);
		}
	}

	@Override
	public void apply(PlayerEntity player) {
		if (type == EmiCommands.VIEW_RECIPE) {
			EmiRecipe recipe = EmiRecipes.byId.get(id);
			if (recipe != null) {
				EmiApi.displayRecipe(recipe);
			}
		} else if (type == EmiCommands.VIEW_TREE) {
			EmiApi.viewRecipeTree();
		} else if (type == EmiCommands.TREE_GOAL) {
			EmiRecipe recipe = EmiRecipes.byId.get(id);
			if (recipe != null) {
				BoM.setGoal(recipe);
			}
		} else if (type == EmiCommands.TREE_RESOLUTION) {
			EmiRecipe recipe = EmiRecipes.byId.get(id);
			if (recipe != null && BoM.tree != null) {
				for (EmiStack stack : recipe.getOutputs()) {
					BoM.tree.addResolution(stack, recipe);
				}
			}
		}
	}

	@Override
	public Identifier getId() {
		return EmiNetwork.COMMAND;
	}
}
