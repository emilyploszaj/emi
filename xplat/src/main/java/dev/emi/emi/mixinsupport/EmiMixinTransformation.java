package dev.emi.emi.mixinsupport;

import java.util.Map;
import java.util.Set;

import org.objectweb.asm.Opcodes;
import org.objectweb.asm.tree.AbstractInsnNode;
import org.objectweb.asm.tree.AnnotationNode;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.FieldInsnNode;
import org.objectweb.asm.tree.FieldNode;
import org.objectweb.asm.tree.MethodInsnNode;
import org.objectweb.asm.tree.MethodNode;
import org.spongepowered.asm.mixin.transformer.meta.MixinMerged;
import org.spongepowered.asm.util.Annotations;

import com.google.common.collect.Maps;
import com.google.common.collect.Sets;

import dev.emi.emi.mixinsupport.annotation.Transform;
import dev.emi.emi.runtime.EmiLog;

public class EmiMixinTransformation {
	private static int VISIBILITY_MASK = ~(Opcodes.ACC_PRIVATE | Opcodes.ACC_PUBLIC | Opcodes.ACC_PROTECTED);
	private static Map<String, Set<String>> cache = Maps.newHashMap();
	public static boolean speakTheGoodWord = false;

	private static void dictate(String targetClassName, Set<String> tainters) {
		if (tainters.isEmpty()) {
			return;
		}
		if (speakTheGoodWord) {
			String warning = targetClassName + " is tainted by";
			for (String tainter : tainters) {
				warning += "\n\t * " + tainter;
			}
			EmiLog.warn(warning);
		}
	}

	public static void preach() {
		if (cache.isEmpty()) {
			return; // God's in his heaven, all's right with the world
		}
		EmiLog.warn("The following EMI classes have mixins applied to them, which could fundamentally alter behavior and cause issues.");
		speakTheGoodWord = true;
		for (Map.Entry<String, Set<String>> entry : cache.entrySet()) {
			dictate(entry.getKey(), entry.getValue());
		}
	}

	public static void exposeSinners(String targetClassName, ClassNode targetClass, String mixinClassName) {
		if (targetClassName.startsWith("dev.emi.emi")) {
			Set<String> tainters = cache.computeIfAbsent(targetClassName, k -> Sets.newHashSet());
			for (var m : targetClass.methods) {
				AnnotationNode annot = Annotations.getVisible(m, MixinMerged.class);
				String tainter = Annotations.getValue(annot, "mixin", (String) null);
				if (tainter != null && !tainter.startsWith("dev.emi.emi.mixin")) {
					tainters.add(tainter);
				}
			}
			if (!tainters.isEmpty()) {
				String sinners = "Mixins:";
				for (String tainter : tainters) {
					sinners += "\n\t\t * " + tainter;
				}
				sinners += "\n\t\t";
				dictate(targetClassName, tainters);
				targetClass.sourceFile = sinners;
				targetClass.sourceDebug = sinners;
			}
		}
	}

	public static void relinkTransforms(ClassNode clazz) {
		Map<String, MethodNode> changedMethods = Maps.newHashMap();
		for (MethodNode method : clazz.methods) {
			String target = method.name + method.desc;
			if (EmiMixinTransformation.applyTransform(method)) {
				changedMethods.put(target, method);
			}
		}
		Map<String, FieldNode> changedFields = Maps.newHashMap();
		for (FieldNode field : clazz.fields) {
			String target = field.name + ":" + field.desc;
			if (EmiMixinTransformation.applyTransform(field)) {
				changedFields.put(target, field);
			}
		}
		for (MethodNode method : clazz.methods) {
			for (AbstractInsnNode node : method.instructions) {
				if (node instanceof MethodInsnNode min) {
					String target = min.name + min.desc;
					if (changedMethods.containsKey(target)) {
						MethodNode changed = changedMethods.get(target);
						min.name = changed.name;
						min.desc = changed.desc;
					}
				} else if (node instanceof FieldInsnNode fin) {
					String target = fin.name + ":" + fin.desc;
					if (changedFields.containsKey(target)) {
						FieldNode changed = changedFields.get(target);
						fin.name = changed.name;
						fin.desc = changed.desc;
					}
				}
			}
		}
	}
	
	public static void applyTransform(ClassNode clazz) {
		AnnotationNode transform = EmiMixinPlugin.popInvisible(clazz, Transform.class);
		if (transform != null) {
			//String name = Annotations.getValue(transform, "name", "");
			//String desc = Annotations.getValue(transform, "desc", "");
			String visibility = Annotations.getValue(transform, "visibility", "");
			int flags = Annotations.getValue(transform, "flags", 0);
			clazz.access = mutateAccess(clazz.access, visibility);
			clazz.access |= flags;
		}
	}
	
	public static boolean applyTransform(MethodNode method) {
		AnnotationNode transform = EmiMixinPlugin.popInvisible(method, Transform.class);
		if (transform != null) {
			String name = Annotations.getValue(transform, "name", "");
			String desc = Annotations.getValue(transform, "desc", "");
			String visibility = Annotations.getValue(transform, "visibility", "");
			int flags = Annotations.getValue(transform, "flags", 0);
			if (!name.isEmpty()) {
				method.name = name;
			}
			if (!desc.isEmpty()) {
				method.desc = desc;
			}
			method.access = mutateAccess(method.access, visibility);
			method.access |= flags;
			return true;
		}
		return false;
	}
	
	public static boolean applyTransform(FieldNode field) {
		AnnotationNode transform = EmiMixinPlugin.popInvisible(field, Transform.class);
		if (transform != null) {
			String name = Annotations.getValue(transform, "name", "");
			String desc = Annotations.getValue(transform, "desc", "");
			String visibility = Annotations.getValue(transform, "visibility", "");
			int flags = Annotations.getValue(transform, "flags", 0);
			if (!name.isEmpty()) {
				field.name = name;
			}
			if (!desc.isEmpty()) {
				field.desc = desc;
			}
			field.access = mutateAccess(field.access, visibility);
			field.access |= flags;
			return true;
		}
		return false;
	}

	private static int mutateAccess(int access, String visibility) {
		return switch (visibility) {
			case "PRIVATE" -> (access & VISIBILITY_MASK) | Opcodes.ACC_PRIVATE;
			case "PROTECTED" -> (access & VISIBILITY_MASK) | Opcodes.ACC_PROTECTED;
			case "PUBLIC" -> (access & VISIBILITY_MASK) | Opcodes.ACC_PUBLIC;
			case "PACKAGE" -> access & VISIBILITY_MASK;
			default -> access;
		};
	}
}
