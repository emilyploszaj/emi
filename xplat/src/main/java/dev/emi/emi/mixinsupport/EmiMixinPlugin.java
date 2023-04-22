package dev.emi.emi.mixinsupport;

import java.util.List;
import java.util.Map;
import java.util.Set;

import org.objectweb.asm.Opcodes;
import org.objectweb.asm.tree.AbstractInsnNode;
import org.objectweb.asm.tree.AnnotationNode;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.FieldInsnNode;
import org.objectweb.asm.tree.InsnNode;
import org.objectweb.asm.tree.MethodInsnNode;
import org.objectweb.asm.tree.MethodNode;
import org.objectweb.asm.tree.TypeInsnNode;
import org.spongepowered.asm.mixin.extensibility.IMixinConfigPlugin;
import org.spongepowered.asm.mixin.extensibility.IMixinInfo;
import org.spongepowered.asm.util.Annotations;

import com.google.common.collect.Maps;

import dev.emi.emi.mixinsupport.annotation.Extends;
import dev.emi.emi.mixinsupport.annotation.InvokeTarget;
import dev.emi.emi.mixinsupport.annotation.StripConstructors;

public class EmiMixinPlugin implements IMixinConfigPlugin {
	private static final String MIXIN_PLACEHOLDER = MixinPlaceholder.class.getName().replace(".", "/");

	@Override
	public void onLoad(String mixinPackage) {
	}

	@Override
	public String getRefMapperConfig() {
		return null;
	}

	@Override
	public boolean shouldApplyMixin(String targetClassName, String mixinClassName) {
		return true;
	}

	@Override
	public void acceptTargets(Set<String> myTargets, Set<String> otherTargets) {
	}

	@Override
	public List<String> getMixins() {
		return null;
	}

	@Override
	public void preApply(String targetClassName, ClassNode targetClass, String mixinClassName, IMixinInfo mixinInfo) {
	}

	@Override
	public void postApply(String targetClassName, ClassNode targetClass, String mixinClassName, IMixinInfo mixinInfo) {
		processClassAnnotations(targetClassName, targetClass, mixinClassName, mixinInfo);
		EmiMixinTransformation.relinkTransforms(targetClass);
		processMethodAnnotations(targetClassName, targetClass, mixinClassName, mixinInfo);
	}

	private void processClassAnnotations(String targetClassName, ClassNode targetClass, String mixinClassName, IMixinInfo mixinInfo) {
		//RemapperChain remapper = MixinEnvironment.getCurrentEnvironment().getRemappers();
		EmiMixinTransformation.applyTransform(targetClass);
		AnnotationNode extendsAnnot = Annotations.getInvisible(targetClass, Extends.class);
		if (extendsAnnot != null) {
			targetClass.superName = Annotations.getValue(extendsAnnot, "value", targetClass.superName);
		}
		AnnotationNode stripConstructors = Annotations.getInvisible(targetClass, StripConstructors.class);
		if (stripConstructors != null) {
			for (int i = 0; i < targetClass.methods.size(); i++) {
				if (targetClass.methods.get(i).name.equals("<init>")) {
					targetClass.methods.remove(i--);
				}
			}
		}
	}

	private void processMethodAnnotations(String targetClassName, ClassNode targetClass, String mixinClassName, IMixinInfo mixinInfo) {
		//RemapperChain remapper = MixinEnvironment.getCurrentEnvironment().getRemappers();
		String thisOwner = targetClassName.replace(".", "/");
		ClassNode mixinClass = mixinInfo.getClassNode(0);
		Map<String, InvokeTargetInfo> targets = Maps.newHashMap();
		for (MethodNode method : mixinClass.methods) {
			AnnotationNode invokeTarget = Annotations.getInvisible(method, InvokeTarget.class);
			if (invokeTarget != null) {
				String owner = Annotations.getValue(invokeTarget, "owner", targetClassName);
				owner = switch(owner) {
					case "this" -> thisOwner;
					case "super" -> targetClass.superName;
					default -> owner;
				};
				String name = Annotations.getValue(invokeTarget, "name", method.name);
				String desc = Annotations.getValue(invokeTarget, "desc", method.desc);
				int type = switch (Annotations.getValue(invokeTarget, "type", "")) {
					case "VIRTUAL" -> Opcodes.INVOKEVIRTUAL;
					case "SPECIAL" -> Opcodes.INVOKESPECIAL;
					case "STATIC" -> Opcodes.INVOKESTATIC;
					case "INTERFACE" -> Opcodes.INVOKEINTERFACE;
					case "NEW" -> Opcodes.NEW;
					default -> -1;
				};
				targets.put(method.name + method.desc, new InvokeTargetInfo(owner, name, type, desc));
			}
		}
		for (int i = 0; i < targetClass.methods.size(); i++) {
			MethodNode method = targetClass.methods.get(i);
			if (Annotations.getInvisible(method, InvokeTarget.class) != null) {
				targetClass.methods.remove(i--);
			}
		}
		for (MethodNode method : targetClass.methods) {
			FieldInsnNode lastNewDup = null;
			for (int i = 0; i < method.instructions.size(); i++) {
				AbstractInsnNode node = method.instructions.get(i);
				if (node instanceof MethodInsnNode min && thisOwner.equals(min.owner)) {
					String desc = min.name + min.desc;
					if (targets.containsKey(desc)) {
						InvokeTargetInfo info = targets.get(desc);
						int type = info.type;
						if (type == -1) {
							if (info.name.equals("<init>")) {
								type = Opcodes.INVOKESPECIAL;
							} else {
								type = min.getOpcode();
							}
						}
						if (type == Opcodes.NEW) {
							if (lastNewDup != null) {
								method.instructions.insertBefore(lastNewDup, new TypeInsnNode(Opcodes.NEW, info.owner));
								method.instructions.insertBefore(lastNewDup, new InsnNode(Opcodes.DUP));
								method.instructions.remove(lastNewDup);
								lastNewDup = null;
								i += 2;
							}
							type = Opcodes.INVOKESPECIAL;
						}
						method.instructions.set(min, new MethodInsnNode(type, info.owner, info.name, info.desc));
						if (info.name.equals("<init>")) {
						}
					}
				} else if (node instanceof FieldInsnNode field) {
					if (MIXIN_PLACEHOLDER.equals(field.owner) && field.name.equals("NEW_DUP")) {
						lastNewDup = field;
					}
				}
			}
		}
	}

	private static record InvokeTargetInfo(String owner, String name, int type, String desc) {
	}
}
