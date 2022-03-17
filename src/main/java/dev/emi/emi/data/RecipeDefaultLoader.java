package dev.emi.emi.data;

import java.io.InputStreamReader;
import java.util.List;
import java.util.Map.Entry;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import org.apache.commons.compress.utils.Lists;

import dev.emi.emi.api.stack.EmiStack;
import dev.emi.emi.bom.BoM;
import net.fabricmc.fabric.api.resource.IdentifiableResourceReloadListener;
import net.minecraft.resource.Resource;
import net.minecraft.resource.ResourceManager;
import net.minecraft.resource.SinglePreparationResourceReloader;
import net.minecraft.util.Identifier;
import net.minecraft.util.JsonHelper;
import net.minecraft.util.profiler.Profiler;
import net.minecraft.util.registry.Registry;

public class RecipeDefaultLoader extends SinglePreparationResourceReloader<List<RecipeDefault>> implements IdentifiableResourceReloadListener {
	private static final Gson GSON = new Gson();
	public static final Identifier ID = new Identifier("emi:recipe_defaults");

	@Override
	protected List<RecipeDefault> prepare(ResourceManager manager, Profiler profiler) {
		List<RecipeDefault> defaults = Lists.newArrayList();
		for (Identifier id : manager.findResources("recipe/defaults/", i -> i.endsWith(".json"))) {
			if (!id.getNamespace().equals("emi")) {
				continue;
			}
			try {
				for (Resource resource : manager.getAllResources(id)) {
					InputStreamReader reader = new InputStreamReader(resource.getInputStream());
					JsonObject json = JsonHelper.deserialize(GSON, reader, JsonObject.class);
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
							JsonObject object = el.getAsJsonObject();
							for (Entry<String, JsonElement> entry : object.entrySet()) {
								Identifier recipe = new Identifier(entry.getKey());
								EmiStack stack = parseEmiStack(key, entry.getValue());
								defaults.add(new RecipeDefault(recipe, stack));
							}
						}
					}
				}
			} catch (Exception e) {
				System.err.println("[emi] Error loading recipe default file " + id);
				e.printStackTrace();
			}
		}
		return defaults;
	}

	@Override
	protected void apply(List<RecipeDefault> prepared, ResourceManager manager, Profiler profiler) {
		BoM.setDefaults(prepared);
	}
	
	@Override
	public Identifier getFabricId() {
		return ID;
	}

	private EmiStack parseEmiStack(String type, JsonElement element) {
		if (type.equals("item")) {
			if (element.isJsonPrimitive()) {
				Identifier id = new Identifier(element.getAsString());
				return EmiStack.of(Registry.ITEM.get(id));
			} else {
				// TODO
			}
		} else if (type.equals("fluid")) {
			// TODO
		}
		return EmiStack.EMPTY;
	}
}
