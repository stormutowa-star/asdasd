package com.keinloree.client.ui;

import java.util.regex.Matcher;
import java.util.regex.Pattern;
import net.minecraft.client.resources.language.I18n;

/** Utilidades de texto del cliente: sustitucion de [[clave]] por su traduccion. */
public final class KlText {
    private static final Pattern KEY = Pattern.compile("\\[\\[([^\\]]+)\\]\\]");

    private KlText() {}

    /** Cambia cada [[clave]] por I18n.get(clave); si el texto entero es una clave conocida, la traduce. */
    public static String resolve(String text) {
        if (text == null || text.isEmpty()) {
            return "";
        }
        if (text.indexOf("[[") >= 0) {
            Matcher m = KEY.matcher(text);
            StringBuilder sb = new StringBuilder();
            while (m.find()) {
                String key = m.group(1).trim();
                m.appendReplacement(sb, Matcher.quoteReplacement(I18n.exists(key) ? I18n.get(key) : key));
            }
            m.appendTail(sb);
            text = sb.toString();
        }
        if (text.indexOf(' ') < 0 && text.indexOf('.') > 0 && I18n.exists(text)) {
            return I18n.get(text);
        }
        return text;
    }

    /** Color RGB con alfa (0..1) en formato ARGB. */
    public static int argb(int rgb, float alpha) {
        int a = Math.max(0, Math.min(255, (int) (alpha * 255.0F)));
        return (a << 24) | (rgb & 0xFFFFFF);
    }

    /** Las fuentes de Minecraft tratan alfa &lt; 4 como opaco: no hay que dibujar texto con tan poco alfa. */
    public static boolean visible(float alpha) {
        return alpha * 255.0F >= 5.0F;
    }
}
