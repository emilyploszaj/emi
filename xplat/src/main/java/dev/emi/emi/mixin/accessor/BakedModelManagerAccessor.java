package dev.emi.emi.mixin.accessor;

import java.util.Map;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

import net.minecraft.client.render.model.BakedModel;
import net.minecraft.client.render.model.BakedModelManager;
import net.minecraft.client.util.ModelIdentifier;

@Mixin(BakedModelManager.class)
public interface BakedModelManagerAccessor {
	
	@Accessor("models")
    Map<ModelIdentifier, BakedModel> getModels();
}
