package dev.emi.emi.mixin.accessor;

import net.minecraft.client.render.model.BakedModelManager;

import org.spongepowered.asm.mixin.Mixin;

@Mixin(BakedModelManager.class)
public interface BakedModelManagerAccessor {

//	@Accessor("models")
//    Map<ModelIdentifier, BakedModel> getModels();
}
