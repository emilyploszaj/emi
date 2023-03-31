package dev.emi.emi.mixin.accessor;

import java.util.Map;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.item.ShovelItem;

@Mixin(ShovelItem.class)
public interface ShovelItemAccessor {

	@Accessor("PATH_STATES")
	static Map<Block, BlockState> getPathStates() {
		throw new UnsupportedOperationException();
	}
}
