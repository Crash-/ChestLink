package com.Crash.ChestLink.listeners;

import org.getspout.spoutapi.event.inventory.InventoryCloseEvent;
import org.getspout.spoutapi.event.inventory.InventoryListener;

import com.Crash.ChestLink.ChestLink;
import com.Crash.ChestLink.util.Link;

public class ChestLinkInventoryListener extends InventoryListener {

	private ChestLink plugin;
	
	public ChestLinkInventoryListener(ChestLink instance){
		
		plugin = instance;
		
	}
	
	@Override
	public void onInventoryClose(InventoryCloseEvent event){
		
		if(event.isCancelled())
			return;

		Link link = plugin.getOpenLocation(event.getPlayer());
		
		if(link != null)
			link.closeChest();
		
	}
	
}
