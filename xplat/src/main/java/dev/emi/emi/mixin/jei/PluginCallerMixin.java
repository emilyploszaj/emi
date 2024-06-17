package dev.emi.emi.mixin.jei;

import java.util.List;
import java.util.Set;
import java.util.function.Consumer;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

import com.google.common.collect.Sets;

import dev.emi.emi.EmiPort;
import mezz.jei.api.IModPlugin;
import mezz.jei.library.load.PluginCaller;
import net.minecraft.util.Identifier;

@Pseudo
@Mixin(PluginCaller.class)
public class PluginCallerMixin {
	@Unique
	private static final Set<Identifier> SKIPPED = Sets.newHashSet(
		EmiPort.id("jei", "minecraft"), EmiPort.id("jei", "gui"), EmiPort.id("jei", "fabric_gui"), EmiPort.id("jei", "forge_gui")
	);
	
	@Redirect(at = @At(value = "INVOKE", target = "java/util/function/Consumer.accept(Ljava/lang/Object;)V"),
		method = "callOnPlugins", remap = false)
	private static void callOnPlugins(Consumer<IModPlugin> target, Object value, String title, List<IModPlugin> plugins, Consumer<IModPlugin> func) {
		IModPlugin plugin = (IModPlugin) value;
		if (SKIPPED.contains(plugin.getPluginUid())) {
			switch (title) {
				case "Registering categories" -> {}
				case "Registering ingredients" -> {}
				case "Registering vanilla category extensions" -> {}
				case "Sending Runtime" -> {}
				case "Sending Runtime Unavailable" -> {}
				default -> { return; }
			}
		}
		target.accept(plugin);
	}
}
