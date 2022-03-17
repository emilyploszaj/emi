package dev.emi.emi.api;

public interface EmiPlugin {

	default void register(EmiRegistry registry) {
	}
}
