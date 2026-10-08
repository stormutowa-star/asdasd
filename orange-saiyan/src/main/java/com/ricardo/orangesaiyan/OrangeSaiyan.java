package com.ricardo.orangesaiyan;

import net.minecraftforge.fml.common.Mod;

/**
 * Orange Saiyan: a new Saiyan transformation branch for DragonMineZ. First form: Super Saiyan Orange.
 */
@Mod(OrangeSaiyan.MOD_ID)
public class OrangeSaiyan {
	public static final String MOD_ID = "orangesaiyan";

	public OrangeSaiyan() {
		FormInstaller.install();
	}
}
