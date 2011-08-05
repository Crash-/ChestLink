package com.Crash.ChestLink.listeners;

import net.minecraft.server.EntityPlayer;
import net.minecraft.server.IInventory;

import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.craftbukkit.entity.CraftPlayer;
import org.bukkit.entity.Player;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerListener;

import com.Crash.ChestLink.ChestLink;
import com.Crash.ChestLink.ChestLink.PlayerState;
import com.Crash.ChestLink.util.Link;

public class ChestLinkPlayerListener extends PlayerListener {

	private ChestLink plugin;
	
	public ChestLinkPlayerListener(ChestLink instance){
		
		plugin = instance;
		
	}
	
	@Override
	public void onPlayerInteract(PlayerInteractEvent event){
		
		if(event.isCancelled())
			return;
		
		if(event.getAction() == Action.LEFT_CLICK_BLOCK){
			
			Block b = event.getClickedBlock();
			
			if(b.getType() == Material.CHEST){
				
				Player p = event.getPlayer();
				PlayerState state = plugin.getState(event.getPlayer().getName());
				plugin.removeState(event.getPlayer().getName());
				
				if(state == PlayerState.LINK){
					
					String group = state.getGroup();
					
					plugin.getLinker().onLink(b, p, group);
					
				} else if(state == PlayerState.CHECK_LINK){
					
					plugin.getLinker().onCheckLink(b, p);
					
				} else if(state == PlayerState.UNLINK){
					
					plugin.getLinker().onRemoveLink(b, p);
						
				}
				
			}
			
		} else if(event.getAction() == Action.RIGHT_CLICK_BLOCK){
			
			Block b = event.getClickedBlock();
			
			if(b.getType() == Material.CHEST){
				
				Link link = plugin.getLinker().getLink(b.getLocation());
				
				if(link == null)
					return;
				
				EntityPlayer player = ((CraftPlayer)event.getPlayer()).getHandle();
				
				plugin.setOpenLocation(event.getPlayer(), link);
				
				link.openChest();
				
				player.a(link.getInventory());
				
				event.setCancelled(true);
				
			}
			
		}
		
	}
	
}
