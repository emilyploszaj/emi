package dev.emi.emi.data;

import java.io.InputStreamReader;
import java.util.List;
import java.util.Map.Entry;

import org.apache.commons.compress.utils.Lists;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import dev.emi.emi.EmiLog;
import dev.emi.emi.EmiPort;
import dev.emi.emi.EmiStackSerializer;
import dev.emi.emi.api.stack.EmiIngredient;
import dev.emi.emi.api.stack.EmiStack;
import dev.emi.emi.bom.BoM;
import net.fabricmc.fabric.api.resource.IdentifiableResourceReloadListener;
import net.minecraft.resource.Resource;
import net.minecraft.resource.ResourceManager;
import net.minecraft.resource.SinglePreparationResourceReloader;
import net.minecraft.util.Identifier;
import net.minecraft.util.JsonHelper;
import net.minecraft.util.profiler.Profiler;

public class RecipeDefaultLoader extends SinglePreparationResourceReloader<List<RecipeDefault>>
		implements IdentifiableResourceReloadListener {
	private static final Gson GSON = new Gson();
	public static final Identifier ID = new Identifier("emi:recipe_defaults");

	@Override
	protected List<RecipeDefault> prepare(ResourceManager manager, Profiler profiler) {
		List<RecipeDefault> allDefaults = Lists.newArrayList();
		for (Identifier id : EmiPort.findResources(manager, "recipe/defaults/", i -> i.endsWith(".json"))) {
			if (!id.getNamespace().equals("emi")) {
				continue;
			}
			List<RecipeDefault> defaults = Lists.newArrayList();
			try {
				for (Resource resource : manager.getAllResources(id)) {
					InputStreamReader reader = new InputStreamReader(EmiPort.getInputStream(resource));
					JsonObject json = JsonHelper.deserialize(GSON, reader, JsonObject.class);
					if (JsonHelper.getBoolean(json, "replace", false)) {
						defaults.clear();
					}
					for (Entry<String, JsonElement> type : json.entrySet()) {
						String key = type.getKey();
						JsonElement el = type.getValue();
						if (key.equals("recipes")) {
							if (el.isJsonArray()) {
								for (JsonElement entry : el.getAsJsonArray()) {
									defaults.add(new RecipeDefault(new Identifier(entry.getAsString()), null));
								}
							}
						} else if (el.isJsonObject()) {
							Identifier kid = new Identifier(key);
							JsonObject object = el.getAsJsonObject();
							for (Entry<String, JsonElement> entry : object.entrySet()) {
								Identifier recipe = new Identifier(entry.getKey());
								EmiIngredient stack = EmiStackSerializer.deserialize(kid, object);
								for (EmiStack es : stack.getEmiStacks()) {
									defaults.add(new RecipeDefault(recipe, es));
								}
							}
						}
					}
				}
			} catch (Exception e) {
				EmiLog.error("Error loading recipe default file " + id);
				e.printStackTrace();
			}
			allDefaults.addAll(defaults);
		}
		return allDefaults;
	}

	@Override
	protected void apply(List<RecipeDefault> prepared, ResourceManager manager, Profiler profiler) {
		BoM.setDefaults(prepared);
	}
	
	@Override
	public Identifier getFabricId() {
		return ID;
	}
}
