package dev.emi.emi.runtime;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.google.common.collect.Lists;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import dev.emi.emi.EmiPort;
import dev.emi.emi.EmiRenderHelper;
import dev.emi.emi.api.EmiApi;
import dev.emi.emi.api.recipe.EmiResolutionRecipe;
import dev.emi.emi.api.recipe.EmiRecipe;
import dev.emi.emi.api.recipe.EmiPlayerInventory;
import dev.emi.emi.api.render.EmiRender;
import dev.emi.emi.api.stack.EmiIngredient;
import dev.emi.emi.api.stack.EmiStack;
import dev.emi.emi.api.stack.serializer.EmiIngredientSerializer;
import dev.emi.emi.bom.BoM;
import dev.emi.emi.bom.FoldState;
import dev.emi.emi.bom.MaterialNode;
import dev.emi.emi.bom.MaterialTree;
import dev.emi.emi.bom.ProgressState;
import dev.emi.emi.bom.TreeCost;
import dev.emi.emi.runtime.EmiDrawContext;
import dev.emi.emi.screen.BoMScreen;
import dev.emi.emi.screen.tooltip.EmiTextTooltipWrapper;
import dev.emi.emi.screen.tooltip.IngredientTooltipComponent;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.tooltip.TooltipComponent;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import net.minecraft.util.JsonHelper;

public class EmiTreeBookmarks {
	public static List<TreeBookmark> bookmarks = Lists.newArrayList();

	public static JsonArray save() {
		JsonArray arr = new JsonArray();
		for (TreeBookmark bookmark : bookmarks) {
			arr.add(bookmark.serialize());
		}
		return arr;
	}

	public static void load(JsonArray arr) {
		bookmarks.clear();
		for (JsonElement element : arr) {
			if (element.isJsonObject()) {
				TreeBookmark bookmark = TreeBookmark.deserialize(element.getAsJsonObject());
				if (bookmark != null) {
					bookmarks.add(bookmark);
				}
			}
		}
	}

	public static void addBookmark(List<MaterialTree> trees, int selectedIndex, boolean craftingMode) {
		addBookmark(trees, selectedIndex, craftingMode, null);
	}

	public static void addBookmark(List<MaterialTree> trees, int selectedIndex, boolean craftingMode, String name) {
		if (trees == null || trees.isEmpty()) {
			return;
		}
		TreeBookmark bookmark = TreeBookmark.fromTrees(trees, selectedIndex, craftingMode, name);
		if (bookmark != null) {
			bookmarks.add(bookmark);
			EmiPersistentData.save();
		}
	}

	public static String suggestName(List<MaterialTree> trees, int selectedIndex, boolean craftingMode) {
		TreeBookmark bookmark = TreeBookmark.fromTrees(trees, selectedIndex, craftingMode, null);
		if (bookmark == null) {
			return EmiPort.translatable("emi.tree_bookmark").getString();
		}
		return bookmark.getName();
	}

	public static void removeBookmark(TreeBookmark bookmark) {
		bookmarks.remove(bookmark);
		EmiPersistentData.save();
	}

	public static void renameBookmark(TreeBookmark bookmark, String name) {
		int index = bookmarks.indexOf(bookmark);
		if (index < 0) {
			return;
		}
		TreeBookmark renamed = bookmark.withName(name);
		bookmarks.set(index, renamed);
		EmiPersistentData.save();
	}

	public static void apply(TreeBookmark bookmark) {
		if (bookmark == null) {
			return;
		}
		List<MaterialTree> trees = bookmark.instantiateTrees();
		BoM.trees.clear();
		BoM.trees.addAll(trees);
		if (trees.isEmpty()) {
			BoM.treeIndex = -1;
			BoM.craftingMode = false;
		} else {
			BoM.treeIndex = Math.max(0, Math.min(bookmark.getSelectedIndex(), trees.size() - 1));
			BoM.craftingMode = bookmark.isCraftingMode();
		}
		MinecraftClient client = MinecraftClient.getInstance();
		if (client.player != null) {
			BoM.calculateCombinedCosts(EmiPlayerInventory.of(client.player));
		} else {
			for (MaterialTree tree : trees) {
				tree.calculateCost();
			}
		}
		EmiPersistentData.save();
		if (client.currentScreen instanceof BoMScreen screen) {
			screen.init();
		}
	}

	public static class TreeBookmark implements EmiIngredient {
		private final List<TreeSnapshot> trees;
		private final List<EmiIngredient> roots;
		private final int selectedIndex;
		private final boolean craftingMode;
 		private final String name;

		public static TreeBookmark fromTrees(List<MaterialTree> trees, int selectedIndex, boolean craftingMode, String name) {
			List<TreeSnapshot> snaps = TreeSnapshot.ofAll(trees);
			if (snaps.isEmpty()) {
				return null;
			}
			return new TreeBookmark(snaps, selectedIndex, craftingMode, name);
		}

		private TreeBookmark(List<TreeSnapshot> trees, int selectedIndex, boolean craftingMode, String name) {
			this.trees = Collections.unmodifiableList(trees);
			List<EmiIngredient> roots = Lists.newArrayList();
			for (TreeSnapshot snap : trees) {
				EmiIngredient ingredient = snap.rootIngredient();
				if (ingredient != null && !ingredient.isEmpty()) {
					roots.add(ingredient);
				}
			}
			this.roots = Collections.unmodifiableList(roots);
			if (trees.isEmpty()) {
				this.selectedIndex = -1;
			} else {
				this.selectedIndex = Math.max(0, Math.min(selectedIndex, trees.size() - 1));
			}
			this.craftingMode = craftingMode;
			this.name = normalizeName(name, trees);
		}

		private static String normalizeName(String name, List<TreeSnapshot> trees) {
			String trimmed = name == null ? "" : name.trim();
			if (!trimmed.isEmpty()) {
				return trimmed;
			}
			String fallback = EmiPort.translatable("emi.tree_bookmark").getString();
			for (TreeSnapshot snap : trees) {
				EmiIngredient ingredient = snap.rootIngredient();
				if (ingredient != null && !ingredient.isEmpty() && !ingredient.getEmiStacks().isEmpty()) {
					Text text = ingredient.getEmiStacks().get(0).getItemStack().getName();
					if (text != null && !text.getString().isEmpty()) {
						String rootName = text.getString();
						if (trees.size() > 1) {
							return rootName + " +" + (trees.size() - 1);
						}
						return rootName;
					}
				}
			}
			if (trees.size() > 1) {
				return fallback + " " + trees.size();
			}
			return fallback;
		}

		public JsonObject serialize() {
			JsonObject json = new JsonObject();
			json.addProperty("name", name);
			json.addProperty("selected", selectedIndex);
			json.addProperty("crafting", craftingMode);
			JsonArray treesArr = new JsonArray();
			for (TreeSnapshot snap : trees) {
				treesArr.add(snap.serialize());
			}
			json.add("trees", treesArr);
			return json;
		}

		public static TreeBookmark deserialize(JsonObject obj) {
			if (!JsonHelper.hasArray(obj, "trees")) {
				return null;
			}
			String name = JsonHelper.getString(obj, "name", null);
			int selected = JsonHelper.getInt(obj, "selected", -1);
			boolean crafting = JsonHelper.getBoolean(obj, "crafting", false);
			List<TreeSnapshot> snaps = Lists.newArrayList();
			for (JsonElement el : JsonHelper.getArray(obj, "trees")) {
				if (el.isJsonObject()) {
					TreeSnapshot snap = TreeSnapshot.deserialize(el.getAsJsonObject());
					if (snap != null) {
						snaps.add(snap);
					}
				}
			}
			if (snaps.isEmpty()) {
				return null;
			}
			return new TreeBookmark(snaps, selected, crafting, name);
		}

		public TreeBookmark withName(String name) {
			return new TreeBookmark(trees, selectedIndex, craftingMode, name);
		}

		public List<MaterialTree> instantiateTrees() {
			List<MaterialTree> instantiated = Lists.newArrayList();
			for (TreeSnapshot snap : trees) {
				MaterialNode node = snap.toNode();
				if (node == null) {
					continue;
				}
				MaterialTree tree = new MaterialTree(node, snap.batches);
				snap.applyResolutions(tree.resolutions);
				applyNodeResolutions(tree.goal, tree.resolutions);
				tree.cost = new TreeCost();
				tree.calculateCost();
				instantiated.add(tree);
			}
			return instantiated;
		}

		public int getSelectedIndex() {
			return selectedIndex;
		}

		public boolean isCraftingMode() {
			return craftingMode;
		}

		public String getName() {
			return name;
		}

		private long getRootAmount(TreeSnapshot snap) {
			if (snap.root == null) {
				return 0;
			}
			if (snap.root.catalyst) {
				return snap.root.amount;
			}
			return snap.root.amount * snap.batches;
		}

		@Override
		public void render(DrawContext draw, int x, int y, float delta, int flags) {
			if (trees.isEmpty() || roots.isEmpty()) {
				return;
			}
			int idx = (int) (System.currentTimeMillis() / 1000 % trees.size());
			TreeSnapshot snap = trees.get(idx);
			EmiIngredient ingredient = roots.get(idx % roots.size());
			if ((flags & RENDER_ICON) != 0) {
				ingredient.render(draw, x, y, delta, flags & ~RENDER_AMOUNT);
			}
			if ((flags & RENDER_INGREDIENT) != 0) {
				EmiRender.renderIngredientIcon(this, draw, x, y);
			}
			if ((flags & RENDER_AMOUNT) != 0) {
				EmiDrawContext context = EmiDrawContext.wrap(draw);
				long amount = getRootAmount(snap);
				EmiRenderHelper.renderAmount(context, x, y,
					EmiRenderHelper.getAmountText(ingredient, amount));
			}
		}

		@Override
		public boolean equals(Object obj) {
			return this == obj;
		}

		@Override
		public EmiIngredient copy() {
			return new TreeBookmark(trees, selectedIndex, craftingMode, name);
		}

		@Override
		public long getAmount() {
			return 1;
		}

		@Override
		public EmiIngredient setAmount(long amount) {
			return null;
		}

		@Override
		public float getChance() {
			return 1;
		}

		@Override
		public EmiIngredient setChance(float chance) {
			return null;
		}

		@Override
		public List<EmiStack> getEmiStacks() {
			return EmiStack.EMPTY.getEmiStacks();
		}

		@Override
		public boolean isEmpty() {
			return false;
		}

		@Override
		public List<TooltipComponent> getTooltip() {
			List<TooltipComponent> tooltip = new ArrayList<>();
			tooltip.add(new EmiTextTooltipWrapper(this, EmiPort.ordered(EmiPort.literal(name))));
			tooltip.add(new EmiTextTooltipWrapper(this,
				EmiPort.ordered(EmiPort.translatable("emi.tree_bookmark.count", trees.size()))));
			if (!roots.isEmpty()) {
				tooltip.add(new IngredientTooltipComponent(roots));
			}
			return tooltip;
		}
	}

	private static void applyNodeResolutions(MaterialNode node, Map<EmiIngredient, EmiRecipe> resolutions) {
		applyNodeResolutions(node, resolutions, new HashSet<>());
	}

	private static void applyNodeResolutions(MaterialNode node, Map<EmiIngredient, EmiRecipe> resolutions, Set<EmiIngredient> path) {
		if (node == null) {
			return;
		}
		if (!path.add(node.ingredient)) {
			return;
		}
		if (resolutions.containsKey(node.ingredient)) {
			node.recipe = resolutions.get(node.ingredient);
			if (node.recipe != null && (node.children == null || node.children.isEmpty())) {
				node.defineRecipe(node.recipe);
			}
		}
		if (node.children != null) {
			for (MaterialNode child : node.children) {
				applyNodeResolutions(child, resolutions, path);
			}
		}
		path.remove(node.ingredient);
	}

	private static class TreeSnapshot {
		private final MaterialNodeSnapshot root;
		private final long batches;
		private final List<ResolutionSnapshot> resolutions;

		private TreeSnapshot(MaterialNodeSnapshot root, long batches, List<ResolutionSnapshot> resolutions) {
			this.root = root;
			this.batches = Math.max(1, batches);
			this.resolutions = Collections.unmodifiableList(resolutions);
		}

		public static List<TreeSnapshot> ofAll(List<MaterialTree> trees) {
			List<TreeSnapshot> snaps = Lists.newArrayList();
			for (MaterialTree tree : trees) {
				TreeSnapshot snap = of(tree);
				if (snap != null) {
					snaps.add(snap);
				}
			}
			return snaps;
		}

		public static TreeSnapshot of(MaterialTree tree) {
			if (tree == null || tree.goal == null) {
				return null;
			}
			MaterialNodeSnapshot root = MaterialNodeSnapshot.of(tree.goal);
			if (root == null) {
				return null;
			}
			List<ResolutionSnapshot> resolutions = Lists.newArrayList();
			for (Map.Entry<EmiIngredient, EmiRecipe> entry : tree.resolutions.entrySet()) {
				ResolutionSnapshot snap = ResolutionSnapshot.of(entry.getKey(), entry.getValue());
				if (snap != null) {
					resolutions.add(snap);
				}
			}
			return new TreeSnapshot(root, tree.batches, resolutions);
		}

		public JsonObject serialize() {
			JsonObject json = new JsonObject();
			json.add("root", root.serialize());
			json.addProperty("batches", batches);
			if (!resolutions.isEmpty()) {
				JsonArray arr = new JsonArray();
				for (ResolutionSnapshot snap : resolutions) {
					JsonObject obj = snap.serialize();
					if (obj != null) {
						arr.add(obj);
					}
				}
				json.add("resolutions", arr);
			}
			return json;
		}

		public static TreeSnapshot deserialize(JsonObject json) {
			if (!JsonHelper.hasJsonObject(json, "root")) {
				return null;
			}
			MaterialNodeSnapshot root = MaterialNodeSnapshot.deserialize(JsonHelper.getObject(json, "root", new JsonObject()));
			if (root == null) {
				return null;
			}
			long batches = JsonHelper.getLong(json, "batches", 1);
			List<ResolutionSnapshot> resolutions = Lists.newArrayList();
			if (JsonHelper.hasArray(json, "resolutions")) {
				for (JsonElement el : JsonHelper.getArray(json, "resolutions")) {
					if (el.isJsonObject()) {
						ResolutionSnapshot snap = ResolutionSnapshot.deserialize(el.getAsJsonObject());
						if (snap != null) {
							resolutions.add(snap);
						}
					}
				}
			}
			return new TreeSnapshot(root, batches, resolutions);
		}

		public MaterialNode toNode() {
			return root.toNode();
		}

		public void applyResolutions(Map<EmiIngredient, EmiRecipe> map) {
			root.applyResolutions(map);
			for (ResolutionSnapshot snap : resolutions) {
				snap.apply(map);
			}
		}

		public EmiIngredient rootIngredient() {
			return root.ingredient;
		}
	}

	private static class ResolutionSnapshot {
		private final EmiIngredient ingredient;
		private final String recipeId;
		private final EmiStack stack;
		private final boolean cleared;

		private ResolutionSnapshot(EmiIngredient ingredient, String recipeId, EmiStack stack, boolean cleared) {
			this.ingredient = ingredient;
			this.recipeId = recipeId;
			this.stack = stack;
			this.cleared = cleared;
		}

		public static ResolutionSnapshot of(EmiIngredient ingredient, EmiRecipe recipe) {
			if (ingredient == null || ingredient.isEmpty()) {
				return null;
			}
			EmiIngredient copy = ingredient.copy();
			if (recipe == null) {
				return new ResolutionSnapshot(copy, null, EmiStack.EMPTY, true);
			}
			String recipeId = recipe.getId() != null ? recipe.getId().toString() : null;
			EmiStack stack = recipe instanceof EmiResolutionRecipe err ? err.stack : EmiStack.EMPTY;
			return new ResolutionSnapshot(copy, recipeId, stack, false);
		}

		public JsonObject serialize() {
			JsonObject json = new JsonObject();
			JsonElement ingredient = EmiIngredientSerializer.getSerialized(this.ingredient);
			if (ingredient != null) {
				json.add("ingredient", ingredient);
			}
			if (recipeId != null) {
				json.addProperty("recipe", recipeId);
			}
			if (!stack.isEmpty()) {
				JsonElement res = EmiIngredientSerializer.getSerialized(stack);
				if (res != null) {
					json.add("stack", res);
				}
			}
			if (cleared) {
				json.addProperty("cleared", true);
			}
			return json;
		}

		public static ResolutionSnapshot deserialize(JsonObject json) {
			EmiIngredient ingredient = EmiIngredientSerializer.getDeserialized(json.get("ingredient"));
			if (ingredient == null || ingredient.isEmpty()) {
				return null;
			}
			String recipeId = JsonHelper.getString(json, "recipe", null);
			EmiStack stack = EmiStack.EMPTY;
			if (json.has("stack")) {
				EmiIngredient res = EmiIngredientSerializer.getDeserialized(json.get("stack"));
				if (res instanceof EmiStack es) {
					stack = es;
				} else if (res != null && !res.getEmiStacks().isEmpty()) {
					stack = res.getEmiStacks().get(0);
				}
			}
			boolean cleared = JsonHelper.getBoolean(json, "cleared", false);
			return new ResolutionSnapshot(ingredient.copy(), recipeId, stack, cleared);
		}

		public void apply(Map<EmiIngredient, EmiRecipe> map) {
			EmiRecipe recipe = null;
			if (recipeId != null && Identifier.tryParse(recipeId) != null) {
				recipe = EmiApi.getRecipeManager().getRecipe(EmiPort.id(recipeId));
			} else if (!stack.isEmpty()) {
				recipe = new EmiResolutionRecipe(ingredient, stack);
			} else if (!cleared) {
				return;
			}
			map.put(ingredient, recipe);
		}
	}

	private static class MaterialNodeSnapshot {
		private final EmiIngredient ingredient;
		private final String recipeId;
		private final float consumeChance;
		private final float produceChance;
		private final long amount;
		private final long divisor;
		private final long remainderAmount;
		private final boolean catalyst;
		private final FoldState state;
		private final ProgressState progress;
		private final long neededBatches;
		private final long totalNeeded;
		private final List<MaterialNodeSnapshot> children;

		private MaterialNodeSnapshot(EmiIngredient ingredient, String recipeId, float consumeChance, float produceChance, long amount,
				long divisor, long remainderAmount, boolean catalyst, FoldState state, ProgressState progress,
				long neededBatches, long totalNeeded, List<MaterialNodeSnapshot> children) {
			this.ingredient = ingredient;
			this.recipeId = recipeId;
			this.consumeChance = consumeChance;
			this.produceChance = produceChance;
			this.amount = amount;
			this.divisor = divisor;
			this.remainderAmount = remainderAmount;
			this.catalyst = catalyst;
			this.state = state;
			this.progress = progress;
			this.neededBatches = neededBatches;
			this.totalNeeded = totalNeeded;
			this.children = children;
		}

		public static MaterialNodeSnapshot of(MaterialNode node) {
			if (node == null || node.ingredient == null || node.ingredient.isEmpty()) {
				return null;
			}
			List<MaterialNodeSnapshot> children = Lists.newArrayList();
			if (node.children != null) {
				for (MaterialNode child : node.children) {
					MaterialNodeSnapshot snap = of(child);
					if (snap != null) {
						children.add(snap);
					}
				}
			}
			String recipeId = node.recipe != null && node.recipe.getId() != null ? node.recipe.getId().toString() : null;
			return new MaterialNodeSnapshot(node.ingredient.copy(), recipeId, node.consumeChance, node.produceChance,
				node.amount, node.divisor, node.remainderAmount, node.catalyst, node.state, node.progress,
				node.neededBatches, node.totalNeeded, children);
		}

		public JsonObject serialize() {
			JsonObject json = new JsonObject();
			JsonElement ingredient = EmiIngredientSerializer.getSerialized(this.ingredient);
			if (ingredient != null) {
				json.add("ingredient", ingredient);
			}
			if (recipeId != null) {
				json.addProperty("recipe", recipeId);
			}
			json.addProperty("consume_chance", consumeChance);
			json.addProperty("produce_chance", produceChance);
			json.addProperty("amount", amount);
			json.addProperty("divisor", divisor);
			json.addProperty("remainder_amount", remainderAmount);
			json.addProperty("catalyst", catalyst);
			json.addProperty("state", state.name());
			json.addProperty("progress", progress.name());
			json.addProperty("needed_batches", neededBatches);
			json.addProperty("total_needed", totalNeeded);
			JsonArray children = new JsonArray();
			for (MaterialNodeSnapshot child : this.children) {
				children.add(child.serialize());
			}
			json.add("children", children);
			return json;
		}

		public static MaterialNodeSnapshot deserialize(JsonObject json) {
			EmiIngredient ingredient = EmiIngredientSerializer.getDeserialized(json.get("ingredient"));
			if (ingredient == null || ingredient.isEmpty()) {
				return null;
			}
			String recipeId = JsonHelper.getString(json, "recipe", null);
			float consumeChance = JsonHelper.getFloat(json, "consume_chance", 1f);
			float produceChance = JsonHelper.getFloat(json, "produce_chance", 1f);
			long amount = JsonHelper.getLong(json, "amount", ingredient.getAmount());
			long divisor = JsonHelper.getLong(json, "divisor", 1);
			long remainderAmount = JsonHelper.getLong(json, "remainder_amount", 0);
			boolean catalyst = JsonHelper.getBoolean(json, "catalyst", false);
			FoldState state = FoldState.valueOf(JsonHelper.getString(json, "state", FoldState.EXPANDED.name()));
			ProgressState progress = ProgressState.valueOf(JsonHelper.getString(json, "progress", ProgressState.UNSTARTED.name()));
			long neededBatches = JsonHelper.getLong(json, "needed_batches", 0);
			long totalNeeded = JsonHelper.getLong(json, "total_needed", 0);
			List<MaterialNodeSnapshot> children = Lists.newArrayList();
			if (JsonHelper.hasArray(json, "children")) {
				for (JsonElement el : JsonHelper.getArray(json, "children")) {
					if (el.isJsonObject()) {
						MaterialNodeSnapshot snap = deserialize(el.getAsJsonObject());
						if (snap != null) {
							children.add(snap);
						}
					}
				}
			}
			return new MaterialNodeSnapshot(ingredient, recipeId, consumeChance, produceChance, amount, divisor, remainderAmount,
				catalyst, state, progress, neededBatches, totalNeeded, children);
		}

		public MaterialNode toNode() {
			MaterialNode node = new MaterialNode(ingredient);
			node.recipe = recipeFromId();
			node.consumeChance = consumeChance;
			node.produceChance = produceChance;
			node.amount = amount;
			node.divisor = divisor;
			node.remainderAmount = remainderAmount;
			node.catalyst = catalyst;
			node.state = state;
			node.progress = progress;
			node.neededBatches = neededBatches;
			node.totalNeeded = totalNeeded;
			if (!children.isEmpty()) {
				node.children = Lists.newArrayList();
				for (MaterialNodeSnapshot child : children) {
					MaterialNode materialNode = child.toNode();
					if (materialNode != null) {
						node.children.add(materialNode);
					}
				}
			}
			if (node.recipe != null && (node.children == null || node.children.isEmpty())) {
				node.defineRecipe(node.recipe);
			}
			return node;
		}

		private EmiRecipe recipeFromId() {
			if (recipeId == null || Identifier.tryParse(recipeId) == null) {
				return null;
			}
			return EmiApi.getRecipeManager().getRecipe(EmiPort.id(recipeId));
		}

		public void applyResolutions(Map<EmiIngredient, EmiRecipe> map) {
			if (recipeId != null) {
				EmiRecipe recipe = recipeFromId();
				if (recipe != null) {
					map.put(ingredient, recipe);
				}
			}
			for (MaterialNodeSnapshot child : children) {
				child.applyResolutions(map);
			}
		}
	}
}