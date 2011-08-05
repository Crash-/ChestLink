package com.Crash.ChestLink.listeners;

import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.entity.EntityExplodeEvent;
import org.bukkit.event.entity.EntityListener;

import com.Crash.ChestLink.ChestLink;
import com.Crash.ChestLink.util.Link;

public class ChestLinkEntityListener extends EntityListener {

	private ChestLink plugin;

	public ChestLinkEntityListener(ChestLink instance){

		plugin = instance;

	}

	@Override
	public void onEntityExplode(EntityExplodeEvent event){

		if(event.isCancelled())
			return;

		for(Block b : event.blockList()){

			if(b.getType() == Material.CHEST){

				Link link = plugin.getLinker().getLink(b.getLocation());

				if(link == null)
					continue;
				
				if(link.getSize() == 1){

					plugin.getLinker().removeLink(link);
					if(event.getEntity() instanceof Player)
						((Player)event.getEntity()).sendMessage(ChatColor.GREEN + "You destroyed the link " + link.getName() + ".");
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

					}

					b.setTypeId(0);
					if(event.getEntity() instanceof Player)
						((Player)event.getEntity()).sendMessage(ChatColor.GREEN + "You removed the chest from the link " + link.getName() + ".");

					return;

				}

			}

		}

	}

}
