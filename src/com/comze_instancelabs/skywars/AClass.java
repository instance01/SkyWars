package com.comze_instancelabs.skywars;

import org.bukkit.potion.PotionEffect;

public class AClass {

	private Main m;
	public String name;
	public PotionEffect potioneffect;
	
	public AClass(Main m, String name, PotionEffect pot){
		this.m = m;
		this.name = name;
		this.potioneffect = pot;
	}
}
