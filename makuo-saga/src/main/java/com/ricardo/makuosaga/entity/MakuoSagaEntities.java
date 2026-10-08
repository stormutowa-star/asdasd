package com.ricardo.makuosaga.entity;

import com.dragonminez.common.init.entities.sagas.DBSagasEntity;
import com.ricardo.makuosaga.ModEntities;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.level.Level;

/**
 * The villains of the Makuo saga, built on DragonMineZ's saga enemy (same AI, ki skills, combos and
 * mid-fight transformations as the official villains). Stats come from the quest that spawns them.
 */
public final class MakuoSagaEntities {
	private static final int VIOLET = 0x8A3CF0;
	private static final int VIOLET_DARK = 0x4A157F;
	private static final int LILAC = 0xE3A6FF;
	private static final int WHITE_VIOLET = 0xF2E6FF;

	private MakuoSagaEntities() {
	}

	private static void speed(DBSagasEntity entity, double value) {
		entity.getAttribute(Attributes.MOVEMENT_SPEED).setBaseValue(value);
		entity.setDefaultMovementSpeed(value);
	}

	/** Egg-born foot soldier. Uses Slug's armored model with black and violet armor. */
	public static class Zhar extends DBSagasEntity {
		public Zhar(EntityType<? extends Monster> type, Level level) {
			super(type, level);
			this.setCanFly(true);
			this.setAuraColor(VIOLET);
			this.setKiBlastSpeed(1.4F);
			this.setZanzoken(true, 160);
			this.setAllowedCombos(180, ComboType.BASIC);
			this.addKiSkill(KiSkillType.KI_SMALL, 50, 1.4F, LILAC, VIOLET);
			speed(this, 0.28D);
		}

		@Override
		public String getGeckolibModelName() {
			return "saga_slug";
		}
	}

	/** Zhar that survived the Void Heart; drops Void Shards (loot table). */
	public static class ZharElite extends DBSagasEntity {
		public ZharElite(EntityType<? extends Monster> type, Level level) {
			super(type, level);
			this.setCanFly(true);
			this.setAuraColor(LILAC);
			this.setKiBlastSpeed(1.6F);
			this.setZanzoken(true, 120);
			this.setEvade(true, 140);
			this.setAllowedCombos(160, ComboType.BASIC, ComboType.AIR);
			this.addKiSkill(KiSkillType.KI_SMALL, 40, 1.5F, WHITE_VIOLET, VIOLET);
			this.addKiSkill(KiSkillType.GENERIC_KI_WAVE, 260, 1.0F, LILAC, VIOLET_DARK);
			speed(this, 0.3D);
		}

		@Override
		public String getGeckolibModelName() {
			return "saga_slug";
		}
	}

	/** First Herald: slow brute with crystal fists. */
	public static class Garuk extends DBSagasEntity {
		public Garuk(EntityType<? extends Monster> type, Level level) {
			super(type, level);
			this.setCanFly(true);
			this.setAuraColor(VIOLET);
			this.setScaleVal(1.3F);
			this.setKiBlastSpeed(1.6F);
			this.setWildSense(true, 120);
			this.setAllowedCombos(120, ComboType.BASIC, ComboType.AIR, ComboType.KI_CHARGE_ATTACK, ComboType.METEOR_COMBINATION);
			this.addKiSkill(KiSkillType.KI_EXPLOSION, 300, 1.4F, LILAC, VIOLET);
			this.addKiSkill(KiSkillType.GENERIC_KI_WAVE, 400, 1.2F, LILAC, VIOLET_DARK);
			speed(this, 0.26D);
			this.getAttribute(Attributes.KNOCKBACK_RESISTANCE).setBaseValue(0.8D);
		}

		@Override
		public String getGeckolibModelName() {
			return "saga_piccolo";
		}
	}

	/** Second Herald: sorcerer that keeps its distance and floods the area with ki. */
	public static class Seiryn extends DBSagasEntity {
		public Seiryn(EntityType<? extends Monster> type, Level level) {
			super(type, level);
			this.setCanFly(true);
			this.setAuraColor(VIOLET_DARK);
			this.setKiBlastSpeed(2.2F);
			this.setZanzoken(true, 100);
			this.setAllowedCombos(220, ComboType.BASIC);
			this.addKiSkill(KiSkillType.KI_VOLLEY, 80, 1.3F, WHITE_VIOLET, VIOLET);
			this.addKiSkill(KiSkillType.KI_AIR_VOLLEY, 180, 1.2F, LILAC, VIOLET_DARK);
			this.addKiSkill(KiSkillType.MASENKO, 240, 1.8F, LILAC, VIOLET);
			this.addKiSkill(KiSkillType.MAKANKOSAPPO, 320, 1.0F, WHITE_VIOLET, VIOLET);
			this.addKiSkill(KiSkillType.KI_BARRIER, 400, 1.0F, LILAC, VIOLET_DARK);
			speed(this, 0.3D);
		}

		@Override
		public String getGeckolibModelName() {
			return "saga_piccolo";
		}
	}

	/** Third Herald: the fastest of the army, always teleporting and dodging. */
	public static class Vokkar extends DBSagasEntity {
		public Vokkar(EntityType<? extends Monster> type, Level level) {
			super(type, level);
			this.setCanFly(true);
			this.setAuraColor(0xC81E3A);
			this.setKiBlastSpeed(2.4F);
			this.setZanzoken(true, 40);
			this.setEvade(true, 60);
			this.setWildSense(true, 80);
			this.setAllowedCombos(90, ComboType.BASIC, ComboType.AIR, ComboType.RAPID_KICKS);
			this.addKiSkill(KiSkillType.KI_LASER, 120, 1.0F, 0xFF6B81, 0xC81E3A);
			this.addKiSkill(KiSkillType.KIENZAN, 260, 1.2F, LILAC, VIOLET);
			speed(this, 0.38D);
		}

		@Override
		public String getGeckolibModelName() {
			return "saga_piccolo";
		}
	}

	/** Makuo's firstborn. Turns giant when wounded, like the Namekians of legend. */
	public static class Drakhul extends DBSagasEntity {
		public Drakhul(EntityType<? extends Monster> type, Level level) {
			super(type, level);
			this.setCanFly(true);
			this.setAuraColor(VIOLET);
			this.setScaleVal(1.25F);
			this.setKiBlastSpeed(1.8F);
			this.setZanzoken(true, 120);
			this.setWildSense(true, 100);
			this.setAllowedCombos(130, ComboType.BASIC, ComboType.AIR, ComboType.KI_CHARGE_ATTACK);
			this.addKiSkill(KiSkillType.GENERIC_KI_WAVE, 300, 1.2F, LILAC, VIOLET);
			this.addKiSkill(KiSkillType.MASENKO, 280, 1.8F, LILAC, VIOLET_DARK);
			speed(this, 0.3D);
			this.getAttribute(Attributes.KNOCKBACK_RESISTANCE).setBaseValue(0.6D);
		}

		@Override
		protected boolean hasTransformation() {
			return true;
		}

		@Override
		public EntityType<? extends DBSagasEntity> getNextTransform() {
			return ModEntities.DRAKHUL_GIANT.get();
		}

		@Override
		public String getGeckolibModelName() {
			return "saga_piccolo";
		}
	}

	public static class DrakhulGiant extends DBSagasEntity {
		public DrakhulGiant(EntityType<? extends Monster> type, Level level) {
			super(type, level);
			this.setCanFly(false);
			this.setAuraColor(VIOLET);
			this.setScaleVal(4.0F);
			this.setKiBlastSpeed(2.0F);
			this.setAllowedCombos(150, ComboType.BASIC, ComboType.KI_CHARGE_ATTACK);
			this.addKiSkill(KiSkillType.OOZARU_ROAR, 500, 1.0F);
			this.addKiSkill(KiSkillType.KI_EXPLOSION, 260, 2.4F, LILAC, VIOLET);
			this.addKiSkill(KiSkillType.GENERIC_KI_WAVE, 320, 3.0F, LILAC, VIOLET_DARK);
			speed(this, 0.3D);
			this.getAttribute(Attributes.KNOCKBACK_RESISTANCE).setBaseValue(1.0D);
		}

		@Override
		public String getGeckolibModelName() {
			return "saga_piccolo";
		}
	}

	/** Makuo, the Exile. At the quest's trigger health he spreads the Wings of the Void. */
	public static class Makuo extends DBSagasEntity {
		public Makuo(EntityType<? extends Monster> type, Level level) {
			super(type, level);
			this.setCanFly(true);
			this.setDBZStyle(0);
			this.setAuraColor(VIOLET);
			this.setKiBlastSpeed(2.2F);
			this.setZanzoken(true, 70);
			this.setEvade(true, 100);
			this.setWildSense(true, 90);
			this.setAllowedCombos(110, ComboType.BASIC, ComboType.AIR, ComboType.KI_CHARGE_ATTACK, ComboType.METEOR_COMBINATION);
			this.addKiSkill(KiSkillType.MAKANKOSAPPO, 260, 1.2F, WHITE_VIOLET, VIOLET);
			this.addKiSkill(KiSkillType.GENERIC_KI_WAVE, 300, 1.4F, LILAC, VIOLET_DARK);
			this.addKiSkill(KiSkillType.KI_VOLLEY, 120, 1.4F, LILAC, VIOLET);
			speed(this, 0.32D);
		}

		@Override
		protected boolean hasTransformation() {
			return true;
		}

		@Override
		public EntityType<? extends DBSagasEntity> getNextTransform() {
			return ModEntities.MAKUO_WINGS.get();
		}

		@Override
		public String getGeckolibModelName() {
			return "saga_piccolo";
		}
	}

	/** Final form: violet skin and Heilig-Flügel-style wings of light blades. */
	public static class MakuoWings extends DBSagasEntity {
		public MakuoWings(EntityType<? extends Monster> type, Level level) {
			super(type, level);
			this.setCanFly(true);
			this.setDBZStyle(0);
			this.setAuraColor(LILAC);
			this.setLightning(true);
			this.setLightningColor(WHITE_VIOLET);
			this.setScaleVal(1.1F);
			this.setKiBlastSpeed(2.6F);
			this.setZanzoken(true, 45);
			this.setEvade(true, 70);
			this.setWildSense(true, 60);
			this.setAllowedCombos(90, ComboType.BASIC, ComboType.AIR, ComboType.KI_CHARGE_ATTACK, ComboType.METEOR_COMBINATION, ComboType.RAPID_KICKS);
			this.addKiSkill(KiSkillType.KI_AIR_VOLLEY, 110, 1.4F, WHITE_VIOLET, VIOLET);
			this.addKiSkill(KiSkillType.TRIPLE_LASER, 200, 1.2F, WHITE_VIOLET, VIOLET);
			this.addKiSkill(KiSkillType.MAKANKOSAPPO, 220, 1.4F, WHITE_VIOLET, VIOLET_DARK);
			this.addKiSkill(KiSkillType.KI_EXPLOSION, 320, 2.0F, LILAC, VIOLET);
			this.addKiSkill(KiSkillType.BIG_BANG, 500, 2.2F, LILAC, VIOLET_DARK);
			speed(this, 0.36D);
		}

		@Override
		public String getGeckolibModelName() {
			return "saga_makuo_wings";
		}
	}
}
