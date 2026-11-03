package dev.emi.emi.data;

import java.io.InputStreamReader;
import net.minecraft.resources.Identifier;
import net.minecraft.server.packs.resources.Resource;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.server.packs.resources.SimplePreparableReloadListener;
import net.minecraft.util.GsonHelper;
import net.minecraft.util.profiling.ProfilerFiller;
import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;

import dev.emi.emi.EmiPort;
import dev.emi.emi.bom.BoM;
import dev.emi.emi.runtime.EmiLog;

public class RecipeDefaultLoader extends SimplePreparableReloadListener<RecipeDefaults>
		implements EmiResourceReloadListener {
	private static final Gson GSON = new Gson();
	public static final Identifier ID = EmiPort.id("emi:recipe_defaults");

	@Override
	protected RecipeDefaults prepare(ResourceManager manager, ProfilerFiller profiler) {
		RecipeDefaults defaults = new RecipeDefaults();
		for (Identifier id : EmiPort.findResources(manager, "recipe/defaults", i -> i.endsWith(".json"))) {
			if (!id.getNamespace().equals("emi")) {
				continue;
			}
			try {
				for (Resource resource : manager.getResourceStack(id)) {
					InputStreamReader reader = new InputStreamReader(EmiPort.getInputStream(resource));
					JsonObject json = GsonHelper.fromJson(GSON, reader, JsonObject.class);
					loadDefaults(defaults, json);
				}
			} catch (Exception e) {
				EmiLog.error("Error loading recipe default file " + id, e);
			}
		}
		return defaults;
	}

	@Override
	protected void apply(RecipeDefaults prepared, ResourceManager manager, ProfilerFiller profiler) {
		BoM.setDefaults(prepared);
	}
	
	@Override
	public Identifier getEmiId() {
		return ID;
	}

	public static void loadDefaults(RecipeDefaults defaults, JsonObject json) {
		if (GsonHelper.getAsBoolean(json, "replace", false)) {
			defaults.clear();
		}
		JsonArray disabled = GsonHelper.getAsJsonArray(json, "disabled", new JsonArray());
		for (JsonElement el : disabled) {
			Identifier id = EmiPort.id(el.getAsString());
			defaults.remove(id);
		}
		JsonArray added = GsonHelper.getAsJsonArray(json, "added", new JsonArray());
		if (GsonHelper.isArrayNode(json, "recipes")) {
			added.addAll(GsonHelper.getAsJsonArray(json, "recipes"));
		}
		for (JsonElement el : added) {
			Identifier id = EmiPort.id(el.getAsString());
			defaults.add(id);
		}
		JsonObject resolutions = GsonHelper.getAsJsonObject(json, "resolutions", new JsonObject());
		for (String key : resolutions.keySet()) {
			Identifier id = EmiPort.id(key);
			if (GsonHelper.isArrayNode(resolutions, key)) {
				defaults.add(id, GsonHelper.getAsJsonArray(resolutions, key));
			}
		}
		JsonObject addedTags = GsonHelper.getAsJsonObject(json, "tags", new JsonObject());
		for (String key : addedTags.keySet()) {
			defaults.addTag(new JsonPrimitive(key), addedTags.get(key));
		}
	}
}
