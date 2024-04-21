package dev.emi.emi.api.stack;

import org.jetbrains.annotations.ApiStatus;

import net.minecraft.nbt.NbtCompound;
import net.minecraft.registry.Registry;

/**
 * Provides EMI context for a {@link Registry} to construct stacks from the objects in the registry.
 * This allows EMI to construct tag ingredients from stacks from the given registry.
 */
@ApiStatus.Experimental
public interface EmiRegistryAdapater<T> {

	/**
	 * @return The base class for objects in the registry.
	 */
	Class<T> getBaseClass();

	/**
	 * @return
	 */
	Registry<T> getRegistry();

	/**
	 * Constructs an {@link EmiStack} from a given object from the registry, or {@link EmiStack#EMPTY} if somehow invalid.
	 */
	EmiStack of(T t, NbtCompound nbt, long amount);

	/**
	 * Convenience method for creating an {@link EmiRegistryAdapter}.
	 */
	public static <T> EmiRegistryAdapater<T> simple(Class<T> clazz, Registry<T> registry, StackConstructor<T> constructor) {
		return new EmiRegistryAdapater<T>() {

			@Override
			public Class<T> getBaseClass() {
				return clazz;
			}

			@Override
			public Registry<T> getRegistry() {
				return registry;
			}

			@Override
			public EmiStack of(T t, NbtCompound nbt, long amount) {
				return constructor.of(t, nbt, amount);
			}
		};
	}

	public static interface StackConstructor<T> {
		EmiStack of(T t, NbtCompound nbt, long amount);
	}
}
