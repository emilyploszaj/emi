package dev.emi.emi.data;

import net.minecraft.resource.ResourceReloader;
import net.minecraft.util.Identifier;

public interface EmiResourceReloadListener extends ResourceReloader {
	
	Identifier getEmiId();
}
