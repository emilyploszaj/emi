package dev.emi.emi.mixin.accessor;

import net.minecraft.component.ComponentMapImpl;
import net.minecraft.item.ItemConvertible;
import net.minecraft.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;
import org.spongepowered.asm.mixin.gen.Invoker;

@Mixin(ItemStack.class)
public interface ItemStackAccessor {
    @Accessor("components")
    ComponentMapImpl getBackingComponentMap();

    @Invoker("<init>")
    static ItemStack withExistingComponentMap(ItemConvertible item, int count, ComponentMapImpl components) {
        throw new AssertionError();
    }
}
