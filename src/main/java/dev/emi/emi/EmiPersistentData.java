package dev.emi.emi;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;

import com.google.gson.Gson;
import com.google.gson.JsonObject;

import net.minecraft.util.JsonHelper;

public class EmiPersistentData {
	public static final File FILE = new File("emi.json");
	public static final Gson GSON = new Gson().newBuilder().setPrettyPrinting().create();
	
	public static void save() {
		JsonObject json = new JsonObject();
		json.add("favorites", EmiFavorites.save());
		try {
			FileWriter writer = new FileWriter(FILE);
			GSON.toJson(json, writer);
			writer.close();
		} catch (Exception e) {
			System.err.println("[emi] Failed to write config");
			e.printStackTrace();
		}
	}

	public static void load() {
		if (!FILE.exists()) {
			return;
		}
		try {
			JsonObject json = GSON.fromJson(new FileReader(FILE), JsonObject.class);
			if (JsonHelper.hasArray(json, "favorites")) {
				EmiFavorites.load(JsonHelper.getArray(json, "favorites"));
			}
		} catch (Exception e) {
			System.err.println("[emi] Failed to parse config");
			e.printStackTrace();
		}
	}
}
