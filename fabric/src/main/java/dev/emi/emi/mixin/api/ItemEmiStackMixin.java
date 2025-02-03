package dev.emi.emi.mixin.api;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import dev.emi.emi.api.stack.ItemEmiStack;
import dev.emi.emi.mixinsupport.MixinPlaceholder;
import dev.emi.emi.mixinsupport.annotation.InvokeTarget;
import dev.emi.emi.mixinsupport.annotation.Transform;
import net.fabricmc.fabric.api.transfer.v1.item.ItemVariant;
import net.minecraft.item.ItemStack;

@Mixin(ItemEmiStack.class)
public abstract class ItemEmiStackMixin {
	@Transform(desc = "Ldev/emi/emi/api/stack/EmiStack$Entry;")
	private Object entry;
	public ItemVariant item;

	@InvokeTarget(owner = "dev/emi/emi/api/stack/ItemEmiStack$ItemEntry", name = "<init>",
		desc = "(Lnet/fabricmc/fabric/api/transfer/v1/item/ItemVariant;)V", type = "NEW")
	private static Object newEntry(Object newDup, ItemVariant variant) { throw new AbstractMethodError(); }

	@Inject(at = @At("RETURN"), method = "<init>(Lnet/minecraft/item/ItemStack;J)V")
	public void constructor(ItemStack stack, long amount, CallbackInfo info) {
		this.item = ItemVariant.of(stack);
		this.entry = newEntry(MixinPlaceholder.NEW_DUP, item);
	}

	@InvokeTarget(name = "<init>", owner = "this")
	abstract void constructor(ItemStack stack, long amount);
	
	@Transform(name = "<init>")
	public void constructor(ItemVariant item, long amount) {
		constructor(item.toStack(), amount);
	}

	@Transform(desc = "()Ldev/emi/emi/api/stack/EmiStack$Entry;")
	public Object getEntry() {
		return entry;
	}
}
