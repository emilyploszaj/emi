package dev.emi.emi.data;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.PreparableReloadListener;

public interface EmiResourceReloadListener extends PreparableReloadListener {
	
	ResourceLocation getEmiId();
}
