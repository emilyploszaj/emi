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

import dev.emi.emi.EmiPort;
import dev.emi.emi.registry.EmiTags;
import dev.emi.emi.runtime.EmiLog;

public class EmiTagExclusionsLoader extends SimplePreparableReloadListener<TagExclusions>
		implements EmiResourceReloadListener {
	private static final Gson GSON = new Gson();
	private static final Identifier ID = EmiPort.id("emi:tag_exclusions");

	@Override
	public TagExclusions prepare(ResourceManager manager, ProfilerFiller profiler) {
		TagExclusions exclusions = new TagExclusions();
		for (Identifier id : EmiPort.findResources(manager, "tag/exclusions", i -> i.endsWith(".json"))) {
			if (!id.getNamespace().equals("emi")) {
				continue;
			}
			try {
				for (Resource resource : manager.getResourceStack(id)) {
					InputStreamReader reader = new InputStreamReader(EmiPort.getInputStream(resource));
					JsonObject json = GsonHelper.fromJson(GSON, reader, JsonObject.class);
					try {
						if (GsonHelper.getAsBoolean(json, "replace", false)) {
							exclusions.clear();
						}
						for (String key : json.keySet()) {
							Identifier type = EmiPort.id(key);
							if (GsonHelper.isArrayNode(json, key)) {
								JsonArray arr = GsonHelper.getAsJsonArray(json, key);
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
						EmiLog.error("Error loading tag exclusions", e);
					}
				}
			} catch (Exception e) {
				EmiLog.error("Error loading tag exclusions", e);
			}
		}
		return exclusions;
	}

	@Override
	public void apply(TagExclusions exclusions, ResourceManager manager, ProfilerFiller profiler) {
		EmiTags.exclusions = exclusions;
	}

	@Override
	public Identifier getEmiId() {
		return ID;
	}
}
