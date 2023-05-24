package dev.emi.emi.api;

/**
 * The primary method of communicating with EMI.
 * Plugins are loaded at runtime to provide information like stacks and recipes.
 * In order to be loaded on Fabric and Quilt, plugins must list their class under the "emi" entrypoint in their mod json.
 * In order to be loaded on Forge, plugins must have the {@link EmiEntrypoint} annotation.
 */
public interface EmiPlugin {

	void register(EmiRegistry registry);
}
