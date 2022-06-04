package dev.emi.emi;

import java.util.Random;
import java.util.Set;
import java.util.function.Consumer;
import java.util.function.Predicate;

import com.mojang.brigadier.CommandDispatcher;

import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.fabricmc.fabric.api.transfer.v1.fluid.FluidVariant;
import net.fabricmc.fabric.api.transfer.v1.fluid.FluidVariantAttributes;
import net.minecraft.block.entity.BannerPattern;
import net.minecraft.client.gl.VertexBuffer;
import net.minecraft.client.render.BufferBuilder;
import net.minecraft.client.render.BufferRenderer;
import net.minecraft.resource.ResourceManager;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.text.MutableText;
import net.minecraft.text.Text;
import net.minecraft.util.DyeColor;
import net.minecraft.util.Identifier;
import net.minecraft.util.registry.Registry;

public final class EmiPort {
	
	// for visibility in decompilation, and also to tell you the reader which version this file
	// is for in case you're confused because your IDE just says "EmiPort.java" in the tab bar
	@SuppressWarnings("unused")
	private static final String MARKER = "1.19";

	public static MutableText literal(String s) {
		return Text.literal(s);
	}
	
	public static MutableText translatable(String s) {
		return Text.translatable(s);
	}
	
	public static MutableText translatable(String s, Object... objects) {
		return Text.translatable(s, objects);
	}

	public static Text fluidName(FluidVariant fluid) {
		return FluidVariantAttributes.getName(fluid);
	}

	public static Set<Identifier> findResources(ResourceManager manager, String prefix, Predicate<String> pred) {
		return manager.findResources(prefix, i -> pred.test(i.toString())).keySet();
	}

	public static void registerCommand(Consumer<CommandDispatcher<ServerCommandSource>> consumer) {
		CommandRegistrationCallback.EVENT.register((dispatcher, registry, env) -> consumer.accept(dispatcher));
	}

	public static BannerPattern.Patterns addRandomBanner(BannerPattern.Patterns patterns, Random random) {
		return patterns.add(Registry.BANNER_PATTERN.getEntry(random.nextInt(Registry.BANNER_PATTERN.size())).get(),
			DyeColor.values()[random.nextInt(DyeColor.values().length)]);
	}

	public static void upload(VertexBuffer vb, BufferBuilder bldr) {
		vb.upload(bldr.end());
	}

	public static void draw(BufferBuilder bufferBuilder) {
		BufferRenderer.drawWithoutShader(bufferBuilder.end());
	}
	
}
