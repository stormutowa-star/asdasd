package com.keinloree.entity;

import net.minecraft.network.chat.Component;

/** Rangos de la Legion del Nexo. Cambian nombre, vida, dano y armadura (los modelos son los de siempre). */
public enum Rank {
    NINGUNO(1.0F, 1.0F, 0),
    SOLDADO(1.0F, 1.0F, 0),
    PILOTO(0.9F, 1.0F, 0),
    TECNICO(0.8F, 0.7F, 0),
    SARGENTO(1.4F, 1.2F, 2),
    TENIENTE(1.7F, 1.35F, 3),
    COMANDANTE(2.0F, 1.5F, 4),
    ALMIRANTE(2.2F, 1.4F, 4),
    GUARDIA_NEXO(1.6F, 1.5F, 8),
    SENOR_GUERRA(2.0F, 1.6F, 6);

    public final float health;
    public final float damage;
    public final int armor;

    Rank(float health, float damage, int armor) {
        this.health = health;
        this.damage = damage;
        this.armor = armor;
    }

    public Component title() {
        return Component.translatable("rank.keinloree." + name().toLowerCase(java.util.Locale.ROOT));
    }

    public boolean isOfficer() {
        return ordinal() >= SARGENTO.ordinal() && this != GUARDIA_NEXO;
    }

    public static Rank of(int i) {
        Rank[] v = values();
        return i >= 0 && i < v.length ? v[i] : NINGUNO;
    }
}
