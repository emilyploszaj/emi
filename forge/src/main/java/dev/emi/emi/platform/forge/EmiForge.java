package dev.emi.emi.platform.forge;

import dev.emi.emi.platform.EmiAgnos;
import dev.emi.emi.platform.EmiMod;
import net.minecraftforge.fml.common.Mod;

@Mod("emi")
public class EmiForge {

	public EmiForge() {
		EmiMod.init();
		System.out.println(EmiAgnos.getAllModNames());
		System.out.println(EmiAgnos.getAllModAuthors());
	}
}
