package com.Crash.ChestLink.util;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.Scanner;
import net.minecraft.server.IInventory;
import net.minecraft.server.InventoryLargeChest;

import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.Chest;
import org.bukkit.craftbukkit.entity.CraftPlayer;
import org.bukkit.craftbukkit.inventory.CraftInventory;
import org.bukkit.entity.Player;

import com.Crash.ChestLink.ChestLink;

public class Linker {

	private ChestLink plugin;
	private ArrayList<Link> linkList = new ArrayList<Link>();
	
	public Linker(ChestLink instance){
		
		plugin = instance;
		
	}
	
	public void addLink(Link link){
		
		linkList.add(link);
		
	}
	
	public void removeLink(Link link){
		
		linkList.remove(link);
		
	}
	
	public Link getLink(Location loc){
		
		for(Link link : linkList)
			if(link.equals(loc))
				return link;
				
		return null;
		
	}
	
	public Link getLink(String name){
		
		for(Link link : linkList)
			if(link.equals(name))
				return link;
		
		return null;
		
	}
	
	public void onLink(Block b, Player p, String group){
		
		Link link = getLink(b.getLocation());
		if(link != null){

			p.sendMessage(ChatColor.RED + "This chest is already linked!");
			return;

		}

		link = getLink(group);

		if(link == null){

			IInventory inv = getInventory(group, b);

			link = new Link(group, inv, b);
			link.addChest(b.getLocation());
			Location loc = plugin.getLinker().getLargeChest(b);
			if(loc != null)
				link.addChest(loc);
			addLink(link);

			p.sendMessage(ChatColor.GREEN + "You've created the new link, " + group + ".");

			return;
			
		} else {

			Location bigchest = getLargeChest(b);
			if(bigchest != null){

				if(link.isSmall()){

					p.sendMessage(ChatColor.RED + "Small chests can only be linked with other small chests!");
					return;

				}

				link.addChest(bigchest);

			} else {

				if(!link.isSmall()){

					p.sendMessage(ChatColor.RED + "Large chests can only be linked with other large chests!");
					return;

				}

			}

			link.addChest(b.getLocation());

			IInventory inv = getInventory(group, b);

			for(int i = 0; i < inv.getSize(); i++)
				if(inv.getItem(i) != null){

					p.sendMessage(ChatColor.RED + "Please remove all contents from the chest before linking.");
					return;

				}

			p.sendMessage(ChatColor.GREEN + "You've linked the chest to " + group + ".");

		}
		
	}
	
	public void onCheckLink(Block b, Player p){
		
		Link link = plugin.getLinker().getLink(b.getLocation());
		
		if(link == null)
			p.sendMessage(ChatColor.RED + "This is not linked to any group.");
		else {
			
			p.sendMessage(ChatColor.GREEN + "Linked to : " + link.getName());
			p.sendMessage(ChatColor.GREEN + "Total links : " + (link.isSmall() ? link.getSize() : link.getSize() / 2));
			p.sendMessage(ChatColor.GREEN + "Total viewing : " + link.getCurrentViewing());
			
		}
		
	}
	
	public void onRemoveLink(Block b, Player p){
		
		Link link = plugin.getLinker().getLink(b.getLocation());
		
		if(link == null)
			p.sendMessage(ChatColor.RED + "This is not linked to any group.");
		else {
			
			link.removeChest(b.getLocation());
			Location loc = plugin.getLinker().getLargeChest(b);
			if(loc != null)
				link.removeChest(loc);
			
			if(link.isMaster(b) || (loc != null && link.isMaster(loc.getBlock())))
				link.transferInventory();
			
			if(link.getSize() == 0){
				
				p.sendMessage(ChatColor.GREEN + "You destroyed the link " + link.getName() + ".");
				plugin.getLinker().removeLink(link);
				link = null;
				return;
				
			}
			
			p.sendMessage(ChatColor.GREEN + "You removed the link to " + link.getName() + ".");
			
		}
		
	}
	
	public Location getLargeChest(Block b){
		
		if(b.getType() != Material.CHEST)
			return null;
		
		BlockFace faces[] = { BlockFace.NORTH, BlockFace.EAST, BlockFace.WEST, BlockFace.SOUTH };
		
		for(BlockFace face : faces){
			
			Block block = b.getRelative(face);
			
			if(block.getType() == Material.CHEST)
				return block.getLocation();
			
		}
		
		return null;
		
	}
	
	public IInventory getInventory(String name, Block b){
		
		if(b.getType() != Material.CHEST)
			return null;
		
		BlockFace faces[] = { BlockFace.NORTH, BlockFace.EAST, BlockFace.WEST, BlockFace.SOUTH };
		
		IInventory inv1 = ((CraftInventory)((Chest)b.getState()).getInventory()).getInventory();
		
		for(BlockFace face : faces){
			
			Block block = b.getRelative(face);
			
			if(block.getType() == Material.CHEST){
				
				IInventory inv2 = ((CraftInventory)((Chest)block.getState()).getInventory()).getInventory();
				
				InventoryLargeChest inventory = new InventoryLargeChest(name, inv1, inv2);
				
				return inventory;
				
			}
			
		}
		
		return inv1;
		
	}
	
	private Location makeLocation(String[] split){
		
		String worldname;
		int x, y, z;
		
		worldname = split[0];
		try {
			
			x = Integer.parseInt(split[1]);
			y = Integer.parseInt(split[2]);
			z = Integer.parseInt(split[3]);
			
		} catch(Exception e){
			
			System.out.println("[ChestLink] Error getting location of chest.");
			return null;
			
		}
		
		World world = plugin.getServer().getWorld(worldname);
		
		return new Location(world, x, y, z);
		
	}
	
	public void closeAllLinks(){
		
		Iterator<Player> itp = plugin.getOpenLocations().keySet().iterator();
		Iterator<Link> itl = plugin.getOpenLocations().values().iterator();
		
		while(itp.hasNext()){
			
			Player p = itp.next();
			Link link = itl.next();
			((CraftPlayer)p).getHandle().y();
			link.closeChest();
			
		}
		
	}
	
	public void loadLinks(File file){
		
		Scanner s = null;
		
		try {
			s = new Scanner(file);
		} catch (FileNotFoundException e) {
			System.out.println("[ChestLink] Unable to find save file.");
			return;
		}
		
		String name;
		Link link = null;
		
		while(s.hasNextLine()){
		
			String line = s.nextLine();
			
			try {
			
				if(line.charAt(0) == ' '){
					
					link = null;
					
					line = line.substring(1);

					String[] split = line.split(new Character(' ').toString());
					
					name = split[0];
					String[] pos = split[1].split(",");
					
					Location loc = makeLocation(pos);
					if(loc == null)
						continue;
					
					Block block = loc.getBlock();
					
					if(block.getType() != Material.CHEST){
						
						System.out.println("[ChestLink] The master block for " + name + " is no longer a chest!");
						continue;
						
					}
					
					IInventory inv = getInventory(name, block);
					link = new Link(name, inv, block);
					link.addChest(block.getLocation());
					if(!link.isSmall())
						link.addChest(getLargeChest(block));
					addLink(link);
					
				} else {
					
					if(link == null)
						continue;
					
					String[] locs = line.split(";");
					
					for(String strloc : locs){
						
						String[] pos = strloc.split(",");
						
						Location loc = makeLocation(pos);
						
						if(loc == null)
							continue;
						
						if(loc.getBlock().getType() != Material.CHEST){
							
							System.out.println("[ChestLink] One of the links for " + link.getName() + " is no longer a chest!");
							continue;
							
						}
						link.addChest(loc);
						
						if(!link.isSmall())
							link.addChest(getLargeChest(loc.getBlock()));
						
					}
					
				}
				
			} catch(Exception e){
				
			}
				
		}
		
		s.close();
		
	}
	
	public void saveLinks(File file){
		
		BufferedWriter o = null;
		
		try {
			o = new BufferedWriter(new FileWriter(file));
		} catch (IOException e) {
			System.out.println("[ChestLink] Unable to open save file.");
			return;
		} 
		
		for(Link link : linkList){
			
			StringBuilder builder = new StringBuilder();
			Block block = link.getMasterBlock();
			builder.append(' ').append(link.getName()).append(' ').append(block.getWorld().getName()).append(",").append(block.getX()).append(",").append(block.getY()).append(",").append(block.getZ()).append("\r\n");
			try {
				o.write(builder.toString());
			} catch (IOException e) {
				System.out.println("[ChestLink] Error writing link " + link.getName() + ".");
				continue;
			}
			
			if(link.isSmall()){
			
				for(int i = 1; i < link.getLocations().size(); i++){
				
					Location loc = link.getChest(i);
				
					builder = new StringBuilder(loc.getWorld().getName());
					builder.append(",").append(loc.getBlockX()).append(",").append(loc.getBlockY()).append(",").append(loc.getBlockZ()).append("\r\n");
					try {
						o.write(builder.toString());
					} catch (IOException e) {
						System.out.println("[ChestLink] Error writing link " + link.getName() + ".");
						continue;
					}
				
				}
				
			} else {
				
				for(int i = 2; i < link.getLocations().size(); i+=2){
					
					Location loc = link.getChest(i);
					
					builder = new StringBuilder(loc.getWorld().getName());
					builder.append(",").append(loc.getBlockX()).append(",").append(loc.getBlockY()).append(",").append(loc.getBlockZ()).append("\r\n");
					try {
						o.write(builder.toString());
					} catch (IOException e) {
						System.out.println("[ChestLink] Error writing link " + link.getName() + ".");
						continue;
					}
					
				}
				
			}
			
		}
		
		try {
			o.close();
		} catch (IOException e) {

		}
		
	}
	
}
