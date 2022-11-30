package dev.emi.emi.config;

import java.util.List;
import java.util.stream.Collectors;

import dev.emi.emi.EmiPort;
import it.unimi.dsi.fastutil.ints.IntArrayList;
import it.unimi.dsi.fastutil.ints.IntList;
import net.minecraft.text.Text;

public class IntGroup {
	public final String baseTranslation;
	public final int size;
	public final List<String> names;
	public final IntList values;

	public IntGroup(String baseTranslation, List<String> names, IntList values) {
		this.baseTranslation = baseTranslation;
		this.size = names.size();
		this.names = names;
		this.values = new IntArrayList();
		this.values.addAll(values);
	}

	public Text getValueTranslation(int i) {
		return EmiPort.translatable(baseTranslation + names.get(i));
	}

	public String serialize() {
		return values.intStream().mapToObj(i -> "" + i).collect(Collectors.joining(", "));
	}

	public void deserialize(String text) {
		String[] parts = text.split(",");
		if (parts.length == size) {
			for (int i = 0; i < size; i++) {
				values.set(i, Integer.parseInt(parts[i].strip()));
			}
		}
	}
}
