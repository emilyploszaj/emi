package dev.emi.emi;

import java.util.function.Consumer;

import com.mojang.brigadier.CommandDispatcher;

import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.fabricmc.fabric.api.transfer.v1.fluid.FluidVariant;
import net.fabricmc.fabric.api.transfer.v1.fluid.FluidVariantAttributes;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.text.Text;

public class EmiFabric {

	public static Text fluidName(FluidVariant fluid) {
		return FluidVariantAttributes.getName(fluid);
	}

	public static void registerCommand(Consumer<CommandDispatcher<ServerCommandSource>> consumer) {
		CommandRegistrationCallback.EVENT.register((dispatcher, registry, env) -> consumer.accept(dispatcher));
	}
}
