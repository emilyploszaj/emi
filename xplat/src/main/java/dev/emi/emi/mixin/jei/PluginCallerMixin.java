package dev.emi.emi.mixin.jei;

import java.util.List;
import java.util.function.Consumer;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

import mezz.jei.api.IModPlugin;
import mezz.jei.library.load.PluginCaller;

@Pseudo
@Mixin(PluginCaller.class)
public class PluginCallerMixin {
	
	@Redirect(at = @At(value = "INVOKE", target = "java/util/function/Consumer.accept(Ljava/lang/Object;)V"),
		method = "callOnPlugins", remap = false)
	private static void callOnPlugins(Consumer<IModPlugin> target, Object value, String title, List<IModPlugin> plugins, Consumer<IModPlugin> func) {
		IModPlugin plugin = (IModPlugin) value;
		if (title.equals("Registering Runtime") && plugin.getPluginUid().getNamespace().equals("jei")) {

		} else {
			target.accept(plugin);
		}
	}
}
