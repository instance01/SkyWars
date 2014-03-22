package com.comze_instancelabs.skywars;

import java.util.ArrayList;
import java.util.Arrays;

import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffect;


public class AClass {

	private Main m;
	public String name;
	public ArrayList<ItemStack> items = new ArrayList<ItemStack>();
	
	public AClass(Main m, String name, ArrayList<ItemStack> items){
		this.m = m;
		this.name = name;
		this.items = items;
	}
	
	public ItemStack[] getItems(){
		ItemStack[] ret = new ItemStack[items.size()];
		int c = 0;
		for(ItemStack f : items){
			ret[c] = f;
			c++;
		}
		return ret;
	}
}
