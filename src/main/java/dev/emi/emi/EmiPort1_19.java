/*package dev.emi.emi;

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

public class EmiPort1_19 extends EmiPort.Impl {
	
	public static void init() {
		EmiPort.impl = new EmiPort1_19();
	}

	@Override
	public MutableText literal(String s) {
		return Text.literal(s);
	}
	
	@Override
	public MutableText translatable(String s) {
		return Text.translatable(s);
	}
	
	@Override
	public MutableText translatable(String s, Object... objects) {
		return Text.translatable(s, objects);
	}

	@Override
	public Text fluidName(FluidVariant fluid) {
		return FluidVariantAttributes.getName(fluid);
	}

	@Override
	public Set<Identifier> findResources(ResourceManager manager, String prefix, Predicate<String> pred) {
		return manager.findResources(prefix, i -> pred.test(i.toString())).keySet();
	}

	@Override
	public void registerCommand(Consumer<CommandDispatcher<ServerCommandSource>> consumer) {
		CommandRegistrationCallback.EVENT.register((dispatcher, registry, env) -> consumer.accept(dispatcher));
	}

	@Override
	public BannerPattern.Patterns addRandomBanner(BannerPattern.Patterns patterns, Random random) {
		return patterns.add(Registry.BANNER_PATTERN.getEntry(random.nextInt(Registry.BANNER_PATTERN.size())).get(),
			DyeColor.values()[random.nextInt(DyeColor.values().length)]);
	}

	@Override
	public void upload(VertexBuffer vb, BufferBuilder bldr) {
		vb.upload(bldr.end());
	}

	@Override
	public void draw(BufferBuilder bufferBuilder) {
		BufferRenderer.drawWithoutShader(bufferBuilder.end());
	}
}*/