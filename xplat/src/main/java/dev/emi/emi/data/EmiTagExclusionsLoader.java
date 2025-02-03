package dev.emi.emi.data;

import java.io.InputStreamReader;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import dev.emi.emi.EmiPort;
import dev.emi.emi.registry.EmiTags;
import dev.emi.emi.runtime.EmiLog;
import net.minecraft.resource.Resource;
import net.minecraft.resource.ResourceManager;
import net.minecraft.resource.SinglePreparationResourceReloader;
import net.minecraft.util.Identifier;
import net.minecraft.util.JsonHelper;
import net.minecraft.util.profiler.Profiler;

public class EmiTagExclusionsLoader extends SinglePreparationResourceReloader<TagExclusions>
		implements EmiResourceReloadListener {
	private static final Gson GSON = new Gson();
	private static final Identifier ID = EmiPort.id("emi:tag_exclusions");

	@Override
	public TagExclusions prepare(ResourceManager manager, Profiler profiler) {
		TagExclusions exclusions = new TagExclusions();
		for (Identifier id : EmiPort.findResources(manager, "tag/exclusions", i -> i.endsWith(".json"))) {
			if (!id.getNamespace().equals("emi")) {
				continue;
			}
			try {
				for (Resource resource : manager.getAllResources(id)) {
					InputStreamReader reader = new InputStreamReader(EmiPort.getInputStream(resource));
					JsonObject json = JsonHelper.deserialize(GSON, reader, JsonObject.class);
					try {
						if (JsonHelper.getBoolean(json, "replace", false)) {
							exclusions.clear();
						}
						for (String key : json.keySet()) {
							Identifier type = EmiPort.id(key);
							if (JsonHelper.hasArray(json, key)) {
								JsonArray arr = JsonHelper.getArray(json, key);
								for (JsonElement el : arr) {
									Identifier eid = EmiPort.id(el.getAsString());
									if (key.equals("exclusions")) {
										exclusions.add(eid);
										if (eid.getNamespace().equals("c")) {
											exclusions.add(EmiPort.id("forge", eid.getPath()));
										}
									} else {
										exclusions.add(type, eid);
										if (eid.getNamespace().equals("c")) {
											exclusions.add(type, EmiPort.id("forge", eid.getPath()));
										}
									}
								}
							}
						}
					} catch (Exception e) {
						EmiLog.error("Error loading tag exclusions");
						e.printStackTrace();
					}
				}
			} catch (Exception e) {
				EmiLog.error("Error loading tag exclusions");
				e.printStackTrace();
			}
		}
		return exclusions;
	}

	@Override
	public void apply(TagExclusions exclusions, ResourceManager manager, Profiler profiler) {
		EmiTags.exclusions = exclusions;
	}

	@Override
	public Identifier getEmiId() {
		return ID;
	}
}
