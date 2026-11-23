package dev.emi.emi.data;

import net.minecraft.resources.Identifier;
import net.minecraft.server.packs.resources.PreparableReloadListener;

public interface EmiResourceReloadListener extends PreparableReloadListener {
	
	Identifier getEmiId();
}
