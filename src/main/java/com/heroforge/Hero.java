package com.heroforge;

import net.minecraft.network.chat.Component;

/** Los ocho heroes originales de HeroForge. */
public enum Hero {
	RELAMPAGO("relampago", 400, 200, "Oleada de velocidad", "Dash fantasma"),
	CENTINELA("centinela", 40, 300, "Alternar vuelo", "Impulso ascendente"),
	TEJEDOR("tejedor", 100, 40, "Disparo de telarana", "Gancho de telarana"),
	BASTION("bastion", 200, 160, "Golpe sismico", "Salto colosal"),
	ESPECTRO("espectro", 600, 100, "Invisibilidad", "Parpadeo (teletransporte)"),
	IGNEA("ignea", 60, 240, "Bola de fuego", "Anillo de llamas"),
	GLACIAR("glaciar", 260, 100, "Nova de escarcha", "Rafaga de bolas de nieve"),
	VOLTIO("voltio", 200, 300, "Rayo dirigido", "Descarga en cadena");

	public final String id;
	public final int cooldown1;
	public final int cooldown2;
	public final String power1;
	public final String power2;

	Hero(String id, int cooldown1, int cooldown2, String power1, String power2) {
		this.id = id;
		this.cooldown1 = cooldown1;
		this.cooldown2 = cooldown2;
		this.power1 = power1;
		this.power2 = power2;
	}

	public Component displayName() {
		return Component.translatable("hero." + HeroForge.MOD_ID + "." + id);
	}

	public static Hero byId(String id) {
		for (Hero h : values()) {
			if (h.id.equals(id)) return h;
		}
		return null;
	}
}
