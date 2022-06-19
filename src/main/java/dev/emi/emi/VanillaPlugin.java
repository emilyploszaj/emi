package dev.emi.emi;

import static dev.emi.emi.api.recipe.VanillaEmiRecipeCategories.ANVIL_REPAIRING;
import static dev.emi.emi.api.recipe.VanillaEmiRecipeCategories.BLASTING;
import static dev.emi.emi.api.recipe.VanillaEmiRecipeCategories.BREWING;
import static dev.emi.emi.api.recipe.VanillaEmiRecipeCategories.CAMPFIRE_COOKING;
import static dev.emi.emi.api.recipe.VanillaEmiRecipeCategories.CRAFTING;
import static dev.emi.emi.api.recipe.VanillaEmiRecipeCategories.SMELTING;
import static dev.emi.emi.api.recipe.VanillaEmiRecipeCategories.SMITHING;
import static dev.emi.emi.api.recipe.VanillaEmiRecipeCategories.SMOKING;
import static dev.emi.emi.api.recipe.VanillaEmiRecipeCategories.STONECUTTING;
import static dev.emi.emi.api.recipe.VanillaEmiRecipeCategories.WORLD_INTERACTION;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Predicate;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.datafixers.util.Pair;

import dev.emi.emi.api.EmiPlugin;
import dev.emi.emi.api.EmiRegistry;
import dev.emi.emi.api.recipe.EmiCraftingRecipe;
import dev.emi.emi.api.recipe.EmiRecipe;
import dev.emi.emi.api.recipe.EmiRecipeCategory;
import dev.emi.emi.api.recipe.EmiWorldInteractionRecipe;
import dev.emi.emi.api.render.EmiRenderable;
import dev.emi.emi.api.stack.Comparison;
import dev.emi.emi.api.stack.EmiIngredient;
import dev.emi.emi.api.stack.EmiStack;
import dev.emi.emi.api.widget.Bounds;
import dev.emi.emi.api.widget.GeneratedSlotWidget;
import dev.emi.emi.handler.CookingRecipeHandler;
import dev.emi.emi.handler.CraftingRecipeHandler;
import dev.emi.emi.handler.InventoryRecipeHandler;
import dev.emi.emi.mixin.accessor.HandledScreenAccessor;
import dev.emi.emi.mixin.accessor.HoeItemAccessor;
import dev.emi.emi.recipe.EmiAnvilRecipe;
import dev.emi.emi.recipe.EmiBrewingRecipe;
import dev.emi.emi.recipe.EmiCookingRecipe;
import dev.emi.emi.recipe.EmiShapedRecipe;
import dev.emi.emi.recipe.EmiShapelessRecipe;
import dev.emi.emi.recipe.EmiSmithingRecipe;
import dev.emi.emi.recipe.EmiStonecuttingRecipe;
import dev.emi.emi.recipe.EmiTagRecipe;
import dev.emi.emi.recipe.special.EmiArmorDyeRecipe;
import dev.emi.emi.recipe.special.EmiBannerShieldRecipe;
import dev.emi.emi.recipe.special.EmiBookCloningRecipe;
import dev.emi.emi.recipe.special.EmiSuspiciousStewRecipe;
import dev.emi.emi.screen.RecipeScreen;
import net.fabricmc.fabric.api.transfer.v1.fluid.FluidVariant;
import net.fabricmc.fabric.mixin.content.registry.AxeItemAccessor;
import net.fabricmc.fabric.mixin.content.registry.ShovelItemAccessor;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.Oxidizable;
import net.minecraft.block.ShulkerBoxBlock;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawableHelper;
import net.minecraft.client.gui.screen.ingame.AbstractInventoryScreen;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.fluid.Fluid;
import net.minecraft.fluid.Fluids;
import net.minecraft.item.ArmorItem;
import net.minecraft.item.DyeItem;
import net.minecraft.item.DyeableItem;
import net.minecraft.item.HoneycombItem;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ItemUsageContext;
import net.minecraft.item.Items;
import net.minecraft.item.ToolItem;
import net.minecraft.potion.Potion;
import net.minecraft.potion.PotionUtil;
import net.minecraft.potion.Potions;
import net.minecraft.recipe.ArmorDyeRecipe;
import net.minecraft.recipe.BlastingRecipe;
import net.minecraft.recipe.BookCloningRecipe;
import net.minecraft.recipe.BrewingRecipeRegistry;
import net.minecraft.recipe.CampfireCookingRecipe;
import net.minecraft.recipe.CraftingRecipe;
import net.minecraft.recipe.Ingredient;
import net.minecraft.recipe.RecipeType;
import net.minecraft.recipe.ShapedRecipe;
import net.minecraft.recipe.ShapelessRecipe;
import net.minecraft.recipe.ShieldDecorationRecipe;
import net.minecraft.recipe.ShulkerBoxColoringRecipe;
import net.minecraft.recipe.SmeltingRecipe;
import net.minecraft.recipe.SmithingRecipe;
import net.minecraft.recipe.SmokingRecipe;
import net.minecraft.recipe.StonecuttingRecipe;
import net.minecraft.recipe.SuspiciousStewRecipe;
import net.minecraft.recipe.TippedArrowRecipe;
import net.minecraft.screen.BlastFurnaceScreenHandler;
import net.minecraft.screen.FurnaceScreenHandler;
import net.minecraft.screen.ScreenHandlerType;
import net.minecraft.screen.SmokerScreenHandler;
import net.minecraft.tag.ItemTags;
import net.minecraft.tag.TagKey;
import net.minecraft.util.DyeColor;
import net.minecraft.util.Formatting;
import net.minecraft.util.Identifier;
import net.minecraft.util.registry.Registry;
import net.minecraft.util.registry.RegistryEntryList;

public class VanillaPlugin implements EmiPlugin {
	public static EmiRecipeCategory TAG = new EmiRecipeCategory(new Identifier("emi:tag"),
		EmiStack.of(Items.NAME_TAG), simplifiedRenderer(240, 208));

	// composting, fuel
	
	public static EmiRecipeCategory INGREDIENT = new EmiRecipeCategory(new Identifier("emi:ingredient"),
		EmiStack.of(Items.COMPASS), simplifiedRenderer(240, 208));
	public static EmiRecipeCategory RESOLUTION = new EmiRecipeCategory(new Identifier("emi:resolution"),
		EmiStack.of(Items.COMPASS), simplifiedRenderer(240, 208));

	@Override
	public void register(EmiRegistry registry) {
		CRAFTING = new EmiRecipeCategory(new Identifier("minecraft:crafting"),
			EmiStack.of(Items.CRAFTING_TABLE), simplifiedRenderer(240, 240));
		SMELTING = new EmiRecipeCategory(new Identifier("minecraft:smelting"),
			EmiStack.of(Items.FURNACE), simplifiedRenderer(224, 240));
		BLASTING = new EmiRecipeCategory(new Identifier("minecraft:blasting"),
			EmiStack.of(Items.BLAST_FURNACE), simplifiedRenderer(208, 240));
		SMOKING = new EmiRecipeCategory(new Identifier("minecraft:smoking"),
			EmiStack.of(Items.SMOKER), simplifiedRenderer(192, 240));
		CAMPFIRE_COOKING = new EmiRecipeCategory(new Identifier("minecraft:campfire_cooking"),
			EmiStack.of(Items.CAMPFIRE), simplifiedRenderer(176, 240));
		STONECUTTING = new EmiRecipeCategory(new Identifier("minecraft:stonecutting"),
			EmiStack.of(Items.STONECUTTER), simplifiedRenderer(160, 240));
		SMITHING = new EmiRecipeCategory(new Identifier("minecraft:smithing"),
			EmiStack.of(Items.SMITHING_TABLE), simplifiedRenderer(240, 224));
		ANVIL_REPAIRING = new EmiRecipeCategory(new Identifier("emi:anvil_repairing"),
			EmiStack.of(Items.ANVIL), simplifiedRenderer(240, 224));
		BREWING = new EmiRecipeCategory(new Identifier("minecraft:brewing"),
			EmiStack.of(Items.BREWING_STAND), simplifiedRenderer(224, 224));
		WORLD_INTERACTION = new EmiRecipeCategory(new Identifier("emi:world_interaction"),
			EmiStack.of(Items.GRASS_BLOCK), simplifiedRenderer(208, 224));
		registry.addCategory(CRAFTING);
		registry.addCategory(SMELTING);
		registry.addCategory(BLASTING);
		registry.addCategory(SMOKING);
		registry.addCategory(CAMPFIRE_COOKING);
		registry.addCategory(STONECUTTING);
		registry.addCategory(SMITHING);
		registry.addCategory(ANVIL_REPAIRING);
		registry.addCategory(BREWING);
		registry.addCategory(WORLD_INTERACTION);
		registry.addCategory(TAG);
		registry.addCategory(INGREDIENT);
		registry.addCategory(RESOLUTION);

		registry.addWorkstation(CRAFTING, EmiStack.of(Items.CRAFTING_TABLE));
		registry.addWorkstation(SMELTING, EmiStack.of(Items.FURNACE));
		registry.addWorkstation(BLASTING, EmiStack.of(Items.BLAST_FURNACE));
		registry.addWorkstation(SMOKING, EmiStack.of(Items.SMOKER));
		registry.addWorkstation(CAMPFIRE_COOKING, EmiStack.of(Items.CAMPFIRE));
		registry.addWorkstation(CAMPFIRE_COOKING, EmiStack.of(Items.SOUL_CAMPFIRE));
		registry.addWorkstation(STONECUTTING, EmiStack.of(Items.STONECUTTER));
		registry.addWorkstation(SMITHING, EmiStack.of(Items.SMITHING_TABLE));
		registry.addWorkstation(ANVIL_REPAIRING, EmiStack.of(Items.ANVIL));
		registry.addWorkstation(ANVIL_REPAIRING, EmiStack.of(Items.CHIPPED_ANVIL));
		registry.addWorkstation(ANVIL_REPAIRING, EmiStack.of(Items.DAMAGED_ANVIL));
		registry.addWorkstation(BREWING, EmiStack.of(Items.BREWING_STAND));

		registry.addRecipeHandler(null, new InventoryRecipeHandler());
		registry.addRecipeHandler(ScreenHandlerType.CRAFTING, new CraftingRecipeHandler());
		registry.addRecipeHandler(ScreenHandlerType.FURNACE, new CookingRecipeHandler<FurnaceScreenHandler>(SMELTING));
		registry.addRecipeHandler(ScreenHandlerType.BLAST_FURNACE, new CookingRecipeHandler<BlastFurnaceScreenHandler>(BLASTING));
		registry.addRecipeHandler(ScreenHandlerType.SMOKER, new CookingRecipeHandler<SmokerScreenHandler>(SMOKING));

		registry.addExclusionArea(RecipeScreen.class, RecipeScreen::getExclusion);

		registry.addGenericExclusionArea((screen, consumer) -> {
			if (screen instanceof AbstractInventoryScreen<?> inv) {
				MinecraftClient client = MinecraftClient.getInstance();
				Collection<StatusEffectInstance> collection = client.player.getStatusEffects();
				if (!collection.isEmpty()) {
					int k = 33;
					if (collection.size() > 5) {
						k = 132 / (collection.size() - 1);
					}
					int right = ((HandledScreenAccessor) inv).getX() + ((HandledScreenAccessor) inv).getBackgroundWidth() + 2;
					int rightWidth = inv.width - right;
					if (rightWidth >= 32) {
						int height = (collection.size() - 1) * k + 32;
						int left, width;
						if (EmiConfig.moveEffects) {
							boolean wide = ((HandledScreenAccessor) inv).getX() >= 122;
							if (wide) {
								left = ((HandledScreenAccessor) inv).getX() - 122;
								width = 120;
							} else {
								left = ((HandledScreenAccessor) inv).getX() - 34;
								width = 32;
							}
						} else {
							left = right;
							boolean wide = rightWidth >= 120;
							if (wide) {
								width = 120;
							} else {
								width = 32;
							}
						}
						consumer.accept(new Bounds(left, ((HandledScreenAccessor) inv).getY(), width, height));
					}
				}
			}
		});

		Function<Comparison, Comparison> compareNbt = c -> c.copy().nbt(true).build();
		registry.setDefaultComparison(Items.POTION, compareNbt);
		registry.setDefaultComparison(Items.SPLASH_POTION, compareNbt);
		registry.setDefaultComparison(Items.LINGERING_POTION, compareNbt);
		registry.setDefaultComparison(Items.TIPPED_ARROW, compareNbt);
		registry.setDefaultComparison(Items.ENCHANTED_BOOK, compareNbt);

		for (CraftingRecipe recipe : registry.getRecipeManager().listAllOfType(RecipeType.CRAFTING)) {
			if (recipe instanceof ShapedRecipe shaped) {
				registry.addRecipe(new EmiShapedRecipe(shaped));
			} else if (recipe instanceof ShapelessRecipe shapeless) {
				registry.addRecipe(new EmiShapelessRecipe(shapeless));
			} else if (recipe instanceof ArmorDyeRecipe dye) {
				for (Item i : EmiArmorDyeRecipe.DYEABLE_ITEMS) {
					registry.addRecipe(new EmiArmorDyeRecipe(i, null));
				}
			} else if (recipe instanceof SuspiciousStewRecipe stew) {
				registry.addRecipe(new EmiSuspiciousStewRecipe(null));
			} else if (recipe instanceof ShulkerBoxColoringRecipe shulker) {
				for (DyeColor dye : DyeColor.values()) {
					DyeItem dyeItem = DyeItem.byColor(dye);
					Identifier id = new Identifier("emi", "dye_shulker_box/" + EmiUtil.subId(dyeItem));
					registry.addRecipe(new EmiCraftingRecipe(
						List.of(EmiStack.of(Items.SHULKER_BOX), EmiStack.of(dyeItem)),
						EmiStack.of(ShulkerBoxBlock.getItemStack(dye)), id));
				}
			} else if (recipe instanceof ShieldDecorationRecipe shield) {
				registry.addRecipe(new EmiBannerShieldRecipe(shield.getId()));
			} else if (recipe instanceof BookCloningRecipe book) {
				registry.addRecipe(new EmiBookCloningRecipe(book.getId()));
			} else if (recipe instanceof TippedArrowRecipe tipped) {
				Registry.POTION.streamEntries().forEach(entry -> {
					if (entry.value() == Potions.EMPTY) {
						return;
					}
					EmiStack arrow = EmiStack.of(Items.ARROW);
					registry.addRecipe(new EmiCraftingRecipe(List.of(
							arrow, arrow, arrow, arrow,
							EmiStack.of(PotionUtil.setPotion(new ItemStack(Items.LINGERING_POTION), entry.value())),
							arrow, arrow, arrow, arrow
						),
						EmiStack.of(PotionUtil.setPotion(new ItemStack(Items.TIPPED_ARROW, 8), entry.value())),
						new Identifier("emi", "tipped_arrow/" + EmiUtil.subId(Registry.POTION.getId(entry.value()))),
						false));
				});
			}
		}

		for (SmeltingRecipe recipe : registry.getRecipeManager().listAllOfType(RecipeType.SMELTING)) {
			registry.addRecipe(new EmiCookingRecipe(recipe, SMELTING, 1, false));
		}
		for (BlastingRecipe recipe : registry.getRecipeManager().listAllOfType(RecipeType.BLASTING)) {
			registry.addRecipe(new EmiCookingRecipe(recipe, BLASTING, 2, false));
		}
		for (SmokingRecipe recipe : registry.getRecipeManager().listAllOfType(RecipeType.SMOKING)) {
			registry.addRecipe(new EmiCookingRecipe(recipe, SMOKING, 2, false));
		}
		for (CampfireCookingRecipe recipe : registry.getRecipeManager().listAllOfType(RecipeType.CAMPFIRE_COOKING)) {
			registry.addRecipe(new EmiCookingRecipe(recipe, CAMPFIRE_COOKING, 1, true));
		}
		for (SmithingRecipe recipe : registry.getRecipeManager().listAllOfType(RecipeType.SMITHING)) {
			registry.addRecipe(new EmiSmithingRecipe(recipe));
		}
		for (StonecuttingRecipe recipe : registry.getRecipeManager().listAllOfType(RecipeType.STONECUTTING)) {
			registry.addRecipe(new EmiStonecuttingRecipe(recipe));
		}

		for (Item i : Registry.ITEM) {
			if (i instanceof ArmorItem ai) {
				registry.addRecipe(new EmiAnvilRecipe(EmiStack.of(i), EmiIngredient.of(ai.getMaterial().getRepairIngredient())));
			} else if (i instanceof ToolItem ti) {
				registry.addRecipe(new EmiAnvilRecipe(EmiStack.of(i), EmiIngredient.of(ti.getMaterial().getRepairIngredient())));
			}
		}
		registry.addRecipe(new EmiAnvilRecipe(EmiStack.of(Items.ELYTRA), EmiStack.of(Items.PHANTOM_MEMBRANE)));
		registry.addRecipe(new EmiAnvilRecipe(EmiStack.of(Items.SHIELD), EmiIngredient.of(ItemTags.PLANKS)));

		for (Ingredient ingredient : BrewingRecipeRegistry.POTION_TYPES) {
			for (ItemStack stack : ingredient.getMatchingStacks()) {
				String pid = EmiUtil.subId(stack.getItem());
				for (BrewingRecipeRegistry.Recipe<Potion> recipe : BrewingRecipeRegistry.POTION_RECIPES) {
					if (recipe.ingredient.getMatchingStacks().length > 0) {
						Identifier id = new Identifier("emi", "brewing/potion/" + pid
							+ "/" + EmiUtil.subId(recipe.ingredient.getMatchingStacks()[0].getItem())
							+ "/" + EmiUtil.subId(Registry.POTION.getId(recipe.input))
							+ "/" + EmiUtil.subId(Registry.POTION.getId(recipe.output)));
						registry.addRecipe(new EmiBrewingRecipe(
							EmiStack.of(PotionUtil.setPotion(stack.copy(), recipe.input)), EmiIngredient.of(recipe.ingredient),
							EmiStack.of(PotionUtil.setPotion(stack.copy(), recipe.output)), id));
					}
				}
			}
		}

		for (BrewingRecipeRegistry.Recipe<Item> recipe : BrewingRecipeRegistry.ITEM_RECIPES) {
			if (recipe.ingredient.getMatchingStacks().length > 0) {
				String gid = EmiUtil.subId(recipe.ingredient.getMatchingStacks()[0].getItem());
				String iid = EmiUtil.subId(recipe.input);
				String oid = EmiUtil.subId(recipe.output);
				Registry.POTION.streamEntries().forEach(entry -> {
					Potion potion = entry.value();
					if (potion == Potions.EMPTY) {
						return;
					}
					if (BrewingRecipeRegistry.isBrewable(potion)) {
						Identifier id = new Identifier("emi", "brewing/item/"
							+ EmiUtil.subId(entry.getKey().get().getValue()) + "/" + gid + "/" + iid + "/" + oid);
						registry.addRecipe(new EmiBrewingRecipe(
							EmiStack.of(PotionUtil.setPotion(new ItemStack(recipe.input), potion)), EmiIngredient.of(recipe.ingredient),
							EmiStack.of(PotionUtil.setPotion(new ItemStack(recipe.output), potion)), id));
					}
				});
			}
		}

		EmiStack water = EmiStack.of(Fluids.WATER);
		addConcreteRecipe(registry, Blocks.WHITE_CONCRETE_POWDER, water, Blocks.WHITE_CONCRETE);
		addConcreteRecipe(registry, Blocks.ORANGE_CONCRETE_POWDER, water, Blocks.ORANGE_CONCRETE);
		addConcreteRecipe(registry, Blocks.MAGENTA_CONCRETE_POWDER, water, Blocks.MAGENTA_CONCRETE);
		addConcreteRecipe(registry, Blocks.LIGHT_BLUE_CONCRETE_POWDER, water, Blocks.LIGHT_BLUE_CONCRETE);
		addConcreteRecipe(registry, Blocks.YELLOW_CONCRETE_POWDER, water, Blocks.YELLOW_CONCRETE);
		addConcreteRecipe(registry, Blocks.LIME_CONCRETE_POWDER, water, Blocks.LIME_CONCRETE);
		addConcreteRecipe(registry, Blocks.PINK_CONCRETE_POWDER, water, Blocks.PINK_CONCRETE);
		addConcreteRecipe(registry, Blocks.GRAY_CONCRETE_POWDER, water, Blocks.GRAY_CONCRETE);
		addConcreteRecipe(registry, Blocks.LIGHT_GRAY_CONCRETE_POWDER, water, Blocks.LIGHT_GRAY_CONCRETE);
		addConcreteRecipe(registry, Blocks.CYAN_CONCRETE_POWDER, water, Blocks.CYAN_CONCRETE);
		addConcreteRecipe(registry, Blocks.PURPLE_CONCRETE_POWDER, water, Blocks.PURPLE_CONCRETE);
		addConcreteRecipe(registry, Blocks.BLUE_CONCRETE_POWDER, water, Blocks.BLUE_CONCRETE);
		addConcreteRecipe(registry, Blocks.BROWN_CONCRETE_POWDER, water, Blocks.BROWN_CONCRETE);
		addConcreteRecipe(registry, Blocks.GREEN_CONCRETE_POWDER, water, Blocks.GREEN_CONCRETE);
		addConcreteRecipe(registry, Blocks.RED_CONCRETE_POWDER, water, Blocks.RED_CONCRETE);
		addConcreteRecipe(registry, Blocks.BLACK_CONCRETE_POWDER, water, Blocks.BLACK_CONCRETE);

		EmiIngredient axes = EmiStack.of(Items.IRON_AXE);
		for (Map.Entry<Block, Block> entry : AxeItemAccessor.getStrippedBlocks().entrySet()) {
			Identifier id = new Identifier("emi", "stripping/" + EmiUtil.subId(entry.getValue())
				+ "/from/" + EmiUtil.subId(entry.getKey()));
			registry.addRecipe(basicWorld(EmiStack.of(entry.getKey()), axes, EmiStack.of(entry.getValue()), id));
		}
		for (Map.Entry<Block, Block> entry : Oxidizable.OXIDATION_LEVEL_DECREASES.get().entrySet()) {
			Identifier id = new Identifier("emi", "stripping/" + EmiUtil.subId(entry.getValue())
				+ "/from/" + EmiUtil.subId(entry.getKey()));
			registry.addRecipe(basicWorld(EmiStack.of(entry.getKey()), axes, EmiStack.of(entry.getValue()), id));
		}
		for (Map.Entry<Block, Block> entry : HoneycombItem.WAXED_TO_UNWAXED_BLOCKS.get().entrySet()) {
			Identifier id = new Identifier("emi", "stripping/" + EmiUtil.subId(entry.getValue())
				+ "/from/" + EmiUtil.subId(entry.getKey()));
			registry.addRecipe(basicWorld(EmiStack.of(entry.getKey()), axes, EmiStack.of(entry.getValue()), id));
		}
		
		EmiIngredient shears = EmiStack.of(Items.SHEARS);
		registry.addRecipe(EmiWorldInteractionRecipe.builder()
			.id(new Identifier("emi", "shearing/minecraft/pumpkin"))
			.leftInput(EmiStack.of(Items.PUMPKIN))
			.rightInput(shears, true)
			.output(EmiStack.of(Items.PUMPKIN_SEEDS, 4))
			.output(EmiStack.of(Items.CARVED_PUMPKIN))
			.build());
		EmiIngredient hoes = EmiStack.of(Items.IRON_HOE);
		for (Map.Entry<Block, Pair<Predicate<ItemUsageContext>, Consumer<ItemUsageContext>>> entry
				: HoeItemAccessor.getTillingActions().entrySet()) {
			Consumer<ItemUsageContext> consumer = entry.getValue().getSecond();
			if (EmiClient.HOE_ACTIONS.containsKey(consumer)) {
				Block b = entry.getKey();
				Identifier id = new Identifier("emi", "tilling/" + EmiUtil.subId(b));
				List<EmiStack> list = EmiClient.HOE_ACTIONS.get(consumer);
				if (list.size() == 1) {
					registry.addRecipe(basicWorld(EmiStack.of(b), hoes, list.get(0), id));
				} else if (list.size() == 2) {
					registry.addRecipe(EmiWorldInteractionRecipe.builder()
						.id(id)
						.leftInput(EmiStack.of(b))
						.rightInput(hoes, true)
						.output(list.get(0))
						.output(list.get(1))
						.build());
				} else {
					EmiLog.warn("Encountered hoe action of peculiar size " + list.size() + ", skipping.");
				}
			}
		}

		EmiIngredient shovels = EmiStack.of(Items.IRON_SHOVEL);
		for (Map.Entry<Block, BlockState> entry : ShovelItemAccessor.getPathStates().entrySet()) {
			Block result = entry.getValue().getBlock();
			Identifier id = new Identifier("emi", "flattening/" + EmiUtil.subId(result)
				+ "/from/" + EmiUtil.subId(entry.getKey()));
			registry.addRecipe(basicWorld(EmiStack.of(entry.getKey()), shovels, EmiStack.of(result), id));
		}

		EmiIngredient honeycomb = EmiStack.of(Items.HONEYCOMB);
		for (Map.Entry<Block, Block> entry : HoneycombItem.UNWAXED_TO_WAXED_BLOCKS.get().entrySet()) {
			Identifier id = new Identifier("emi", "waxing/" + EmiUtil.subId(entry.getValue())
				+ "/from/" + EmiUtil.subId(entry.getKey()));
			registry.addRecipe(basicWorld(EmiStack.of(entry.getKey()), honeycomb, EmiStack.of(entry.getValue()), id, false));
		}

		for (Item i : EmiArmorDyeRecipe.DYEABLE_ITEMS) {
			EmiStack cauldron = EmiStack.of(Items.CAULDRON);
			EmiStack waterThird = EmiStack.of(FluidVariant.of(Fluids.WATER), 81_000 / 3);
			registry.addRecipe(EmiWorldInteractionRecipe.builder()
				.leftInput(EmiStack.EMPTY, s -> new GeneratedSlotWidget(r -> {
					ItemStack stack = new ItemStack(i);
					((DyeableItem) i).setColor(stack, r.nextInt(0xFFFFFF + 1));
					return EmiStack.of(stack);
				}, EmiUtil.RANDOM.nextInt(), s.getBounds().x(), s.getBounds().y()))
				.rightInput(cauldron, true)
				.rightInput(waterThird, false)
				.output(EmiStack.of(i))
				.build());
		}

		water = EmiStack.of(Fluids.WATER, 81_000);
		EmiStack lava = EmiStack.of(Fluids.LAVA, 81_000);
		EmiStack waterCatalyst = water.copy().setRemainder(water);
		EmiStack lavaCatalyst = lava.copy().setRemainder(lava);

		registry.addRecipe(EmiWorldInteractionRecipe.builder()
			.id(new Identifier("emi", "emi/fluid_spring/water"))
			.leftInput(waterCatalyst)
			.rightInput(waterCatalyst, false)
			.output(EmiStack.of(Fluids.WATER, 81_000))
			.build());
		registry.addRecipe(EmiWorldInteractionRecipe.builder()
			.id(new Identifier("emi", "emi/fluid_interaction/cobblestone"))
			.leftInput(waterCatalyst)
			.rightInput(lavaCatalyst, false)
			.output(EmiStack.of(Items.COBBLESTONE))
			.build());
		registry.addRecipe(EmiWorldInteractionRecipe.builder()
			.id(new Identifier("emi", "emi/fluid_interaction/stone"))
			.leftInput(waterCatalyst)
			.rightInput(lavaCatalyst, false)
			.output(EmiStack.of(Items.STONE))
			.build());
		registry.addRecipe(EmiWorldInteractionRecipe.builder()
			.id(new Identifier("emi", "emi/fluid_interaction/obsidian"))
			.leftInput(lava)
			.rightInput(waterCatalyst, false)
			.output(EmiStack.of(Items.OBSIDIAN))
			.build());
	
		EmiStack soulSoil = EmiStack.of(Items.SOUL_SOIL);
		soulSoil.setRemainder(soulSoil);
		EmiStack blueIce = EmiStack.of(Items.BLUE_ICE);
		blueIce.setRemainder(blueIce);

		registry.addRecipe(EmiWorldInteractionRecipe.builder()
			.id(new Identifier("emi", "emi/fluid_interaction/basalt"))
			.leftInput(lavaCatalyst)
			.rightInput(soulSoil, false, s -> s.appendTooltip(EmiPort
				.translatable("tooltip.emi.fluid_interaction.basalt.soul_soil", Formatting.GREEN)))
			.rightInput(blueIce, false, s -> s.appendTooltip(EmiPort
				.translatable("tooltip.emi.fluid_interaction.basalt.blue_ice", Formatting.GREEN)))
			.output(EmiStack.of(Items.BASALT))
			.build());

		Registry.FLUID.streamEntries().forEach(entry -> {
			Fluid fluid = entry.value();
			Item bucket = fluid.getBucketItem();
			if (fluid.isStill(fluid.getDefaultState()) && bucket != Items.AIR) {
				registry.addRecipe(basicWorld(EmiStack.of(Items.BUCKET), EmiStack.of(fluid, 81_000), EmiStack.of(bucket),
					new Identifier("emi", "fill_bucket/" + EmiUtil.subId(fluid)), false));
			}
		});

		registry.addRecipe(basicWorld(EmiStack.of(Items.GLASS_BOTTLE), water,
			EmiStack.of(PotionUtil.setPotion(new ItemStack(Items.POTION), Potions.WATER)),
			new Identifier("emi", "fill_water_bottle")));

		Registry.ITEM.streamTagsAndEntries().forEach(pair -> {
			TagKey<Item> key = pair.getFirst();
			if (EmiClient.excludedTags.contains(key.id())) {
				return;
			}
			RegistryEntryList.Named<Item> list = pair.getSecond();
			if (list.size() > 1) {
				registry.addRecipe(new EmiTagRecipe(key, list.stream().map(ItemStack::new).map(EmiStack::of).toList()));
			}
		});

		if (!EmiPort.VERSION.equals("1.18.2")) {
			EmiStack waterBottle = EmiStack.of(PotionUtil.setPotion(new ItemStack(Items.POTION), Potions.WATER))
				.setRemainder(EmiStack.of(Items.GLASS_BOTTLE));
			EmiStack mud = EmiStack.of(Registry.ITEM.get(new Identifier("minecraft:mud")));
			registry.addRecipe(basicWorld(EmiStack.of(Items.DIRT), waterBottle, mud,
				new Identifier("emi:emi/mud"), false));
		}
	}

	private static EmiRenderable simplifiedRenderer(int u, int v) {
		return (matrices, x, y, delta) -> {
			RenderSystem.setShaderTexture(0, EmiRenderHelper.WIDGETS);
			DrawableHelper.drawTexture(matrices, x, y, u, v, 16, 16, 256, 256);
		};
	}

	private void addConcreteRecipe(EmiRegistry registry, Block powder, EmiStack water, Block result) {
		registry.addRecipe(basicWorld(EmiStack.of(powder), water, EmiStack.of(result),
			new Identifier("emi", "concrete/" + EmiUtil.subId(result))));
	}

	private EmiRecipe basicWorld(EmiIngredient left, EmiIngredient right, EmiStack output, Identifier id) {
		return basicWorld(left, right, output, id, true);
	}

	private EmiRecipe basicWorld(EmiIngredient left, EmiIngredient right, EmiStack output, Identifier id, boolean catalyst) {
		return EmiWorldInteractionRecipe.builder()
			.id(id)
			.leftInput(left)
			.rightInput(right, catalyst)
			.output(output)
			.build();
	}
}