package dev.emi.emi;

import java.io.InputStream;
import java.util.Collection;
import java.util.List;
import java.util.Random;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.Button.OnPress;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.resources.sounds.SimpleSoundInstance;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.Registry;
import net.minecraft.core.component.DataComponentPatch;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.component.TypedDataComponent;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.nbt.NbtOps;
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.Style;
import net.minecraft.resources.Identifier;
import net.minecraft.server.packs.resources.Resource;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.util.FormattedCharSequence;
import net.minecraft.util.context.ContextMap;
import net.minecraft.world.flag.FeatureFlagSet;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.alchemy.Potion;
import net.minecraft.world.item.alchemy.PotionContents;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.display.RecipeDisplay;
import net.minecraft.world.item.crafting.display.SlotDisplay;
import net.minecraft.world.item.crafting.display.SlotDisplayContext;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.TallFlowerBlock;
import net.minecraft.world.level.block.entity.BannerPatternLayers;
import net.minecraft.world.level.material.Fluid;
import org.jetbrains.annotations.Nullable;

import com.mojang.serialization.DynamicOps;

import dev.emi.emi.api.stack.Comparison;
import dev.emi.emi.api.stack.EmiIngredient;
import dev.emi.emi.api.stack.EmiStack;

/**
 * Multiversion quarantine, to avoid excessive git pain
 */
public final class EmiPort {
	public static MutableComponent literal(String s) {
		return Component.literal(s);
	}

	public static MutableComponent literal(String s, ChatFormatting formatting) {
		return Component.literal(s).withStyle(formatting);
	}

	public static MutableComponent literal(String s, ChatFormatting... formatting) {
		return Component.literal(s).withStyle(formatting);
	}

	public static MutableComponent literal(String s, Style style) {
		return Component.literal(s).setStyle(style);
	}
	
	public static MutableComponent translatable(String s) {
		return Component.translatable(s);
	}
	
	public static MutableComponent translatable(String s, ChatFormatting formatting) {
		return Component.translatable(s).withStyle(formatting);
	}
	
	public static MutableComponent translatable(String s, Object... objects) {
		return Component.translatable(s, objects);
	}

	public static MutableComponent append(MutableComponent text, Component appended) {
		return text.append(appended);
	}

	public static FormattedCharSequence ordered(Component text) {
		return text.getVisualOrderText();
	}

	public static Collection<Identifier> findResources(ResourceManager manager, String prefix, Predicate<String> pred) {
		return manager.listResources(prefix, i -> pred.test(i.toString())).keySet();
	}

	public static InputStream getInputStream(Resource resource) {
		try {
			return resource.open();
		} catch (Exception e) {
			return null;
		}
	}

	public static BannerPatternLayers addRandomBanner(BannerPatternLayers patterns, Random random) {
		var bannerRegistry = Minecraft.getInstance().level.registryAccess().lookupOrThrow(Registries.BANNER_PATTERN);
		return new BannerPatternLayers.Builder().addAll(patterns).add(bannerRegistry.get(random.nextInt(bannerRegistry.size())).get(),
			DyeColor.values()[random.nextInt(DyeColor.values().length)]).build();
	}

	public static boolean canTallFlowerDuplicate(TallFlowerBlock tallFlowerBlock) {
		try {
			return tallFlowerBlock.isValidBonemealTarget(null, null, null) && tallFlowerBlock.isBonemealSuccess(null, null, null, null);
		} catch(Exception e) {
			return false;
		}
	}

	public static int getGuiScale(Minecraft client) {
		return client.getWindow().getGuiScale();
	}

	public static Registry<Item> getItemRegistry() {
		return BuiltInRegistries.ITEM;
	}

	public static Registry<Block> getBlockRegistry() {
		return BuiltInRegistries.BLOCK;
	}

	public static Registry<Fluid> getFluidRegistry() {
		return BuiltInRegistries.FLUID;
	}

	public static Registry<Potion> getPotionRegistry() {
		return BuiltInRegistries.POTION;
	}

	public static Registry<Enchantment> getEnchantmentRegistry() {
		Minecraft client = Minecraft.getInstance();
		return client.level.registryAccess().lookupOrThrow(Registries.ENCHANTMENT);
	}

	public static Button newButton(int x, int y, int w, int h, Component name, OnPress action) {
		return Button.builder(name, action).pos(x, y).size(w, h).build();
	}

	public static ItemStack getOutput(Recipe<?> recipe) {
		Minecraft client = Minecraft.getInstance();
		List<RecipeDisplay> displays = recipe.display();
		if (displays.isEmpty() || client.level == null) {
			return ItemStack.EMPTY;
		}
		return displays.get(0).result().resolveForFirstStack(displayContext());
	}

	/**
	 * Converts a recipe display slot to an ingredient, keeping tags intact
	 */
	public static EmiIngredient getIngredient(SlotDisplay display) {
		if (display instanceof SlotDisplay.TagSlotDisplay tag) {
			return EmiIngredient.of(tag.tag());
		} else if (display instanceof SlotDisplay.WithRemainder remainder) {
			return getIngredient(remainder.input());
		} else if (display instanceof SlotDisplay.Empty) {
			return EmiStack.EMPTY;
		}
		return EmiIngredient.of(display.resolveForStacks(displayContext()).stream().map(EmiStack::of).toList());
	}

	public static List<Item> getDyeItems() {
		return getItemRegistry().stream().filter(i -> i.components().has(DataComponents.DYE)).toList();
	}

	public static @Nullable DyeColor getDyeColor(Item item) {
		return item.components().get(DataComponents.DYE);
	}

	public static ContextMap displayContext() {
		return SlotDisplayContext.fromLevel(Minecraft.getInstance().level);
	}

	public static void focus(EditBox widget, boolean focused) {
		// Also ensure a current focus-element in the screen is cleared if it changes
		Minecraft client = Minecraft.getInstance();
		if (client != null && client.screen != null) {
			var currentFocus = client.screen.getFocused();
			if (!focused && currentFocus == widget || focused && currentFocus != widget) {
				client.screen.setFocused(null);
			}
		}
		widget.setFocused(focused);
	}

	public static Stream<Item> getDisabledItems() {
		Minecraft client = Minecraft.getInstance();
		FeatureFlagSet fs = client.level.enabledFeatures();
		return getItemRegistry().stream().filter(i -> !i.isEnabled(fs));
	}

	public static Comparison compareStrict() {
		return Comparison.compareComponents();
	}

	public static ItemStack setPotion(ItemStack stack, Potion potion) {
		stack.update(DataComponents.POTION_CONTENTS, PotionContents.EMPTY, getPotionRegistry().wrapAsHolder(potion), PotionContents::withPotion);
		return stack;
	}

	public static DataComponentPatch emptyExtraData() {
		return DataComponentPatch.EMPTY;
	}

	public static Identifier id(String id) {
		return Identifier.parse(id);
	}

	public static Identifier id(String namespace, String path) {
		return Identifier.fromNamespaceAndPath(namespace, path);
	}

	/**
	 * Formats a stack as a give command argument, item id with its component changes
	 */
	public static String getGiveString(ItemStack stack, HolderLookup.Provider registries) {
		StringBuilder sb = new StringBuilder(getItemRegistry().getKey(stack.getItem()).toString());
		DynamicOps<Tag> ops = registries.createSerializationContext(NbtOps.INSTANCE);
		String components = stack.getComponentsPatch().entrySet().stream().flatMap(entry -> {
			Identifier id = BuiltInRegistries.DATA_COMPONENT_TYPE.getKey(entry.getKey());
			if (entry.getValue().isPresent()) {
				return TypedDataComponent.createUnchecked(entry.getKey(), entry.getValue().get()).encodeValue(ops).result().stream().map(tag -> id + "=" + tag);
			}
			return Stream.of("!" + id);
		}).collect(Collectors.joining(","));
		if (!components.isEmpty()) {
			sb.append('[').append(components).append(']');
		}
		return sb.toString();
	}

	public static void playClickSound() {
		Minecraft.getInstance().getSoundManager().play(SimpleSoundInstance.forUI(SoundEvents.UI_BUTTON_CLICK, 1.0f));
	}
}
