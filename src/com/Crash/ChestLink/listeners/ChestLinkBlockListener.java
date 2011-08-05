package com.Crash.ChestLink.listeners;

import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockListener;
import org.bukkit.inventory.ItemStack;

import com.Crash.ChestLink.ChestLink;
import com.Crash.ChestLink.util.Link;

public class ChestLinkBlockListener extends BlockListener {

	private ChestLink plugin;
	
	public ChestLinkBlockListener(ChestLink instance){
		
		plugin = instance;
		
	}
	
	@Override
	public void onBlockBreak(BlockBreakEvent event){
		
		if(event.isCancelled())
			return;
		
		Block b = event.getBlock();
		
		if(b.getType() == Material.CHEST){
			
			Link link = plugin.getLinker().getLink(b.getLocation());
			Player player = event.getPlayer();
			
			if(link == null)
				return;
			
			if((link.isSmall() && link.getSize() == 1) || (!link.isSmall() && link.getSize() == 2)){
				
				plugin.getLinker().removeLink(link);
				player.sendMessage(ChatColor.GREEN + "You destroyed the link " + link.getName() + ".");
				link = null;
				return;
				
			} else {
				
				if(link.isSmall()){

					link.removeChest(b.getLocation());
					if(link.isMaster(b))
						link.transferInventory();
					
				} else {
					
					Location loc = plugin.getLinker().getLargeChest(b);
					link.removeChest(b.getLocation());
					link.removeChest(loc);
					if(link.isMaster(loc.getBlock()) || link.isMaster(b))
						link.transferInventory();
					
					loc.getBlock().setTypeId(0);
					loc.getWorld().dropItemNaturally(loc, new ItemStack(Material.CHEST, 1));
					
				}
				
				b.setTypeId(0);
				player.sendMessage(ChatColor.GREEN + "You removed the chest from the link " + link.getName() + ".");
				
				b.getWorld().dropItemNaturally(b.getLocation(), new ItemStack(Material.CHEST, 1));
				
				event.setCancelled(true);
				
				return;
				
			}
			
		}
		
	}
	
	
}
