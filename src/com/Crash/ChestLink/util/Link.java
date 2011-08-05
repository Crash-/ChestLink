package com.Crash.ChestLink.util;

import java.util.ArrayList;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.Chest;
import org.bukkit.craftbukkit.inventory.CraftInventory;

import com.Crash.ChestLink.ChestLink;

import net.minecraft.server.IInventory;
import net.minecraft.server.InventoryLargeChest;
import net.minecraft.server.ItemStack;

public class Link {

	private String name;
	private Block masterBlock;
	private boolean isSmall;
	private int numViewing;
	private IInventory masterInventory, realInventory;
	private ArrayList<Location> linkedChests = new ArrayList<Location>();
	
	public Link(String groupname, IInventory inventory, Block block ){
		
		name = groupname;
		if(inventory.getSize() == 27)
			masterInventory = new LinkChest(groupname);
		else
			masterInventory = new InventoryLargeChest(groupname, new LinkChest(), new LinkChest());
		realInventory = inventory;
		masterBlock = block;
		if(inventory.getSize() == 27)
			isSmall = true;
		else
			isSmall = false;
		numViewing = 0;
		
	}
	
	public void addChest(Location loc){
		
		linkedChests.add(loc);
		
	}
	
	public void removeChest(Location loc){
		
		linkedChests.remove(loc);
		
	}
	
	public Location getChest(int i){
		
		return linkedChests.get(i);
		
	}
	
	public void transferInventory(){
		
		Chest chest;
		
		if(getSize() == 0)
			return;
		
		if(isSmall){
			
			chest = (Chest)getChest(0).getBlock().getState();
			for(int i = 0; i < 27; i++){
				
				if(realInventory.getItem(i) == null)
					chest.getInventory().setItem(i, null);
				else
					chest.getInventory().setItem(i, new org.bukkit.inventory.ItemStack(realInventory.getItem(i).id, realInventory.getItem(i).count, (short)realInventory.getItem(i).damage));
				realInventory.setItem(i, null);
				
			}
			
			realInventory = ((CraftInventory)chest.getInventory()).getInventory();
			masterBlock = getChest(0).getBlock();
			
		} else {
			
			IInventory inventory = ChestLink.getStatic().getLinker().getInventory(name, getChest(0).getBlock());
			
			if(inventory == null){
				
				System.out.println("[ChestLink] Failed to transfer inventory.");
				return;
				
			}
			
			for(int i = 0; i < realInventory.getSize(); i++){
				
				if(realInventory.getItem(i) == null)
					inventory.setItem(i, null);
				else
					inventory.setItem(i, new ItemStack(realInventory.getItem(i).id, realInventory.getItem(i).count, (short)realInventory.getItem(i).damage));
				realInventory.setItem(i, null);
				
			}
			
			realInventory = inventory;
			masterBlock = getChest(0).getBlock();
			
		}
		
	}
	
	public Block getMasterBlock(){ return masterBlock; }
	
	public ArrayList<Location> getLocations(){ return linkedChests; }
	
	public void openChest(){
		
		numViewing++;
		
		if(numViewing > 1)
			return;
		
		for(int i = 0; i < realInventory.getSize(); i++){
			
			masterInventory.setItem(i, (realInventory.getItem(i) == null ? null : new ItemStack(realInventory.getItem(i).id, realInventory.getItem(i).count, (short)realInventory.getItem(i).damage)));
			
			realInventory.setItem(i, null);
			
		}
		
	}
	
	public void closeChest(){
		
		if(numViewing == 1)
			saveChest();
		
		numViewing--;
		
	}
	
	public void saveChest(){
		
		for(int i = 0; i < realInventory.getSize(); i++){
			
			realInventory.setItem(i, (masterInventory.getItem(i) == null ? null : new ItemStack(masterInventory.getItem(i).id, masterInventory.getItem(i).count, (short)masterInventory.getItem(i).damage)));
			
			masterInventory.setItem(i, null);
			
		}
		
	}
	
	public boolean isLinked(Location loc){
	
		if(linkedChests.contains(loc))
			return true;
		
		BlockFace faces[] = { BlockFace.NORTH, BlockFace.EAST, BlockFace.WEST, BlockFace.SOUTH };
		
		Block b = loc.getBlock();
		
		for(BlockFace face : faces){
			
			Block block = b.getRelative(face);
			
			if(block.getType() == Material.CHEST)
				return linkedChests.contains(block.getLocation());
			
			
		}
		
		return false;
		
	}
	
	public boolean isMaster(Block b){
		
		return b.getLocation().equals(masterBlock.getLocation());
		
	}
	
	public boolean isSmall(){ return isSmall; }
	
	public int getSize(){ return linkedChests.size(); }
	
	public int getCurrentViewing(){ return numViewing; }
	
	public String getName(){ return name; }
	
	public IInventory getInventory(){ return masterInventory; }
	
	public boolean equals(Object o){
		
		if(o instanceof Location)
			return isLinked((Location) o);
		else if(o instanceof Link)
			return ((Link) o).getInventory().equals(masterInventory);
		else if(o instanceof String)
			return ((String) o).equalsIgnoreCase(name);
		
		return false;
		
	}
	
}
