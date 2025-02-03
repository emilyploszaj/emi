package dev.emi.emi.data;

import java.io.InputStreamReader;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;

import dev.emi.emi.EmiPort;
import dev.emi.emi.bom.BoM;
import dev.emi.emi.runtime.EmiLog;
import net.minecraft.resource.Resource;
import net.minecraft.resource.ResourceManager;
import net.minecraft.resource.SinglePreparationResourceReloader;
import net.minecraft.util.Identifier;
import net.minecraft.util.JsonHelper;
import net.minecraft.util.profiler.Profiler;

public class RecipeDefaultLoader extends SinglePreparationResourceReloader<RecipeDefaults>
		implements EmiResourceReloadListener {
	private static final Gson GSON = new Gson();
	public static final Identifier ID = EmiPort.id("emi:recipe_defaults");

	@Override
	protected RecipeDefaults prepare(ResourceManager manager, Profiler profiler) {
		RecipeDefaults defaults = new RecipeDefaults();
		for (Identifier id : EmiPort.findResources(manager, "recipe/defaults", i -> i.endsWith(".json"))) {
			if (!id.getNamespace().equals("emi")) {
				continue;
			}
			try {
				for (Resource resource : manager.getAllResources(id)) {
					InputStreamReader reader = new InputStreamReader(EmiPort.getInputStream(resource));
					JsonObject json = JsonHelper.deserialize(GSON, reader, JsonObject.class);
					loadDefaults(defaults, json);
				}
			} catch (Exception e) {
				EmiLog.error("Error loading recipe default file " + id);
				e.printStackTrace();
			}
		}
		return defaults;
	}

	@Override
	protected void apply(RecipeDefaults prepared, ResourceManager manager, Profiler profiler) {
		BoM.setDefaults(prepared);
	}
	
	@Override
	public Identifier getEmiId() {
		return ID;
	}

	public static void loadDefaults(RecipeDefaults defaults, JsonObject json) {
		if (JsonHelper.getBoolean(json, "replace", false)) {
			defaults.clear();
		}
		JsonArray disabled = JsonHelper.getArray(json, "disabled", new JsonArray());
		for (JsonElement el : disabled) {
			Identifier id = EmiPort.id(el.getAsString());
			defaults.remove(id);
		}
		JsonArray added = JsonHelper.getArray(json, "added", new JsonArray());
		if (JsonHelper.hasArray(json, "recipes")) {
			added.addAll(JsonHelper.getArray(json, "recipes"));
		}
		for (JsonElement el : added) {
			Identifier id = EmiPort.id(el.getAsString());
			defaults.add(id);
		}
		JsonObject resolutions = JsonHelper.getObject(json, "resolutions", new JsonObject());
		for (String key : resolutions.keySet()) {
			Identifier id = EmiPort.id(key);
			if (JsonHelper.hasArray(resolutions, key)) {
				defaults.add(id, JsonHelper.getArray(resolutions, key));
			}
		}
		JsonObject addedTags = JsonHelper.getObject(json, "tags", new JsonObject());
		for (String key : addedTags.keySet()) {
			defaults.addTag(new JsonPrimitive(key), addedTags.get(key));
		}
	}
}
