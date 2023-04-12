package dev.emi.emi.mixinsupport;

import java.util.Map;

import org.objectweb.asm.Opcodes;
import org.objectweb.asm.tree.AbstractInsnNode;
import org.objectweb.asm.tree.AnnotationNode;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.FieldInsnNode;
import org.objectweb.asm.tree.FieldNode;
import org.objectweb.asm.tree.MethodInsnNode;
import org.objectweb.asm.tree.MethodNode;
import org.spongepowered.asm.util.Annotations;

import com.google.common.collect.Maps;

import dev.emi.emi.mixinsupport.annotation.Transform;

public class EmiMixinTransformation {
	private static int VISIBILITY_MASK = ~(Opcodes.ACC_PRIVATE | Opcodes.ACC_PUBLIC | Opcodes.ACC_PROTECTED);

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
		AnnotationNode transform = Annotations.getInvisible(clazz, Transform.class);
		if (transform != null) {
			//String name = Annotations.getValue(transform, "name", "");
			//String desc = Annotations.getValue(transform, "desc", "");
			String visibility = Annotations.getValue(transform, "visibility", "");
			clazz.access = mutateAccess(clazz.access, visibility);
		}
	}
	
	public static boolean applyTransform(MethodNode method) {
		AnnotationNode transform = Annotations.getInvisible(method, Transform.class);
		if (transform != null) {
			String name = Annotations.getValue(transform, "name", "");
			String desc = Annotations.getValue(transform, "desc", "");
			String visibility = Annotations.getValue(transform, "visibility", "");
			if (!name.isEmpty()) {
				method.name = name;
			}
			if (!desc.isEmpty()) {
				method.desc = desc;
			}
			method.access = mutateAccess(method.access, visibility);
			return !name.isEmpty() || !desc.isEmpty();
		}
		return false;
	}
	
	public static boolean applyTransform(FieldNode field) {
		AnnotationNode transform = Annotations.getInvisible(field, Transform.class);
		if (transform != null) {
			String name = Annotations.getValue(transform, "name", "");
			String desc = Annotations.getValue(transform, "desc", "");
			String visibility = Annotations.getValue(transform, "visibility", "");
			if (!name.isEmpty()) {
				field.name = name;
			}
			if (!desc.isEmpty()) {
				field.desc = desc;
			}
			field.access = mutateAccess(field.access, visibility);
			return !name.isEmpty() || !desc.isEmpty();
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
