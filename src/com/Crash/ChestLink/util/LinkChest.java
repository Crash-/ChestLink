package com.Crash.ChestLink.util;

import net.minecraft.server.EntityHuman;
import net.minecraft.server.TileEntityChest;

public class LinkChest extends TileEntityChest {

	private String name; 
	public LinkChest(String chestname){
		
		super();
		name = chestname;
		
	}
	
	public LinkChest(){
		super();
	}
	
	@Override
	public boolean a_(EntityHuman e){ return true; }
	
	@Override
	public String getName(){ return name; }
	
}
