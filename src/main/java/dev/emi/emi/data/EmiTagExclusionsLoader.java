package dev.emi.emi.data;

import java.io.InputStreamReader;
import java.util.Set;

import com.google.common.collect.Sets;
import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import dev.emi.emi.EmiClient;
import net.fabricmc.fabric.api.resource.IdentifiableResourceReloadListener;
import net.minecraft.resource.Resource;
import net.minecraft.resource.ResourceManager;
import net.minecraft.resource.SinglePreparationResourceReloader;
import net.minecraft.util.Identifier;
import net.minecraft.util.JsonHelper;
import net.minecraft.util.profiler.Profiler;

public class EmiTagExclusionsLoader extends SinglePreparationResourceReloader<Set<Identifier>>
		implements IdentifiableResourceReloadListener {
	private static final Gson GSON = new Gson();
	private static final Identifier ID = new Identifier("emi:tag_exclusions");

	@Override
	public Set<Identifier> prepare(ResourceManager manager, Profiler profiler) {
		Set<Identifier> exclusions = Sets.newHashSet();
		try {
			for (Resource resource : manager.getAllResources(new Identifier("emi:tag_exclusions.json"))) {
				InputStreamReader reader = new InputStreamReader(resource.getInputStream());
				JsonObject json = JsonHelper.deserialize(GSON, reader, JsonObject.class);
				try {
					if (JsonHelper.getBoolean(json, "replace", false)) {
						exclusions.clear();
					}
					if (JsonHelper.hasArray(json, "exclusions")) {
						JsonArray arr = JsonHelper.getArray(json, "exclusions");
						for (JsonElement el : arr) {
							exclusions.add(new Identifier(el.getAsString()));
						}
					}
				} catch (Exception e) {
					System.err.println("[emi] Error loading tag exclusions");
					e.printStackTrace();
				}
			}
		} catch (Exception e) {
			System.err.println("[emi] Error loading tag exclusions");
			e.printStackTrace();
		}
		return exclusions;
	}

	@Override
	public void apply(Set<Identifier> exclusions, ResourceManager manager, Profiler profiler) {
		EmiClient.excludedTags = exclusions;
	}

	@Override
	public Identifier getFabricId() {
		return ID;
	}
}
