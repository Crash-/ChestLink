package com.Crash.ChestLink;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

import net.minecraft.server.IInventory;

import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.block.Block;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.*;

import com.Crash.ChestLink.listeners.ChestLinkBlockListener;
import com.Crash.ChestLink.listeners.ChestLinkEntityListener;
import com.Crash.ChestLink.listeners.ChestLinkInventoryListener;
import com.Crash.ChestLink.listeners.ChestLinkPlayerListener;
import com.Crash.ChestLink.util.Link;
import com.Crash.ChestLink.util.Linker;
import com.nijiko.permissions.PermissionHandler;

public class ChestLink extends JavaPlugin {

	private static ChestLink plugin = null;
	private Linker LinkManager = new Linker(this);
	private PermissionHandler Permissions = null;
	private ChestLinkPlayerListener PlayerListener = new ChestLinkPlayerListener(this);
	private ChestLinkBlockListener BlockListener = new ChestLinkBlockListener(this);
	private ChestLinkEntityListener EntityListener = new ChestLinkEntityListener(this);
	private ChestLinkInventoryListener InventoryListener = new ChestLinkInventoryListener(this);
	private Map<String, PlayerState> PlayerStates = new HashMap<String, PlayerState>();
	private Map<Player, Link> PlayerOpenLinks = new HashMap<Player, Link>();
	private File saveFile = new File("plugins/ChestLink/save.txt");
	
	@Override
	public void onDisable() {

		LinkManager.closeAllLinks();
		LinkManager.saveLinks(saveFile);
		
		System.out.println("ChestLink disabled.");

	}

	@Override
	public void onEnable() {

		plugin = this;

		if(getDataFolder().mkdir())
			System.out.println("[ChestLink] Created ChestLink folder.");
		
		if(!saveFile.exists()){
			
			try {
				saveFile.createNewFile();
			} catch(Exception e){
				System.out.println("[ChestLink] Error creating save file.");
			}
			
		}
		
		Plugin PermissionsPlugin = getServer().getPluginManager().getPlugin("Permissions");
		
		if(PermissionsPlugin == null)
			System.out.println("ChestLink is unable to find Permissions using OP only now.");
		else
			Permissions = ((com.nijikokun.bukkit.Permissions.Permissions)PermissionsPlugin).getHandler();
		
		getServer().getPluginManager().registerEvent(Event.Type.PLAYER_INTERACT, PlayerListener, Event.Priority.Normal, this);
		getServer().getPluginManager().registerEvent(Event.Type.BLOCK_BREAK, BlockListener, Event.Priority.Normal, this);
		getServer().getPluginManager().registerEvent(Event.Type.ENTITY_EXPLODE, EntityListener, Event.Priority.Normal, this);
		getServer().getPluginManager().registerEvent(Event.Type.CUSTOM_EVENT, InventoryListener, Event.Priority.Normal, this);
		
		LinkManager.loadLinks(saveFile);
		
		System.out.println("ChestLink v" + getDescription().getVersion() + " enabled, by Crash");
		
	}

	@Override
	public boolean onCommand(CommandSender s, Command Cmd, String Name, String[] Args){

		if(!(s instanceof Player))
			return false;

		Player p = (Player)s;
		
		if(Cmd.getName().equalsIgnoreCase("unlink")){

			if(!p.hasPermission("chestlink.unlink")){
				
				p.sendMessage(ChatColor.RED + "You don't have permission to unlink!");
				return false;
				
			}
			
			Block b = p.getTargetBlock(null, 69);

			if(b != null && b.getType() == Material.CHEST){
				
				getLinker().onRemoveLink(b, p);
				return true;

			}

			PlayerStates.put(p.getName(), PlayerState.UNLINK);

			p.sendMessage(ChatColor.GREEN + "Click a chest to remove the link.");

		} else if(Cmd.getName().equalsIgnoreCase("link")) {

			if(Args.length == 0)
				return false;

			if(!p.hasPermission("chestlink.link")){
				
				p.sendMessage(ChatColor.RED + "You don't have permission to link!");
				return false;
				
			}
			
			Block b = p.getTargetBlock(null, 69);

			if(b != null && b.getType() == Material.CHEST){

				getLinker().onLink(b, p, Args[0]);
				return true;

			}

			PlayerState state = PlayerState.LINK;
			state.setGroup(Args[0]);

			PlayerStates.put(p.getName(), state);

			p.sendMessage(ChatColor.GREEN + "Click a chest to create the link.");

		} else if(Cmd.getName().equalsIgnoreCase("getlink")){

			if(!p.hasPermission("chestlink.unlink")){
				
				p.sendMessage(ChatColor.RED + "You don't have permission to check links!");
				return false;
				
			}
			
			Block b = p.getTargetBlock(null, 69);

			if(b != null && b.getType() == Material.CHEST){

				getLinker().onCheckLink(b, p);
				return true;

			}

			PlayerStates.put(p.getName(), PlayerState.CHECK_LINK);

			p.sendMessage(ChatColor.GREEN + "Click a chest to check the link.");

		} else if(Cmd.getName().equalsIgnoreCase("savelinks")){
			
			if(!p.hasPermission("chestlink.save")){
				
				p.sendMessage(ChatColor.RED + "You don't have permission to save links!");
				return false;
				
			}
			
			LinkManager.saveLinks(saveFile);
			p.sendMessage(ChatColor.GREEN + "Saved all links.");
			
		} else if(Cmd.getName().equalsIgnoreCase("loadlinks")){
			
			if(!p.hasPermission("chestlink.load")){
				
				p.sendMessage(ChatColor.RED + "You don't have permission to load links!");
				return false;
				
			}
			
			PlayerOpenLinks = new HashMap<Player, Link>();
			LinkManager = new Linker(this);
			
			LinkManager.loadLinks(saveFile);
			p.sendMessage(ChatColor.GREEN + "Loaded all links.");
			
		}

		return true;

	}

	public void setOpenLocation(Player player, Link link){
		
		PlayerOpenLinks.put(player, link);
		
	}
	
	public Link getOpenLocation(Player player){
		
		return PlayerOpenLinks.remove(player);
		
	}
	
	public boolean has(Player player, String perm){
		
		return usingPermissions() ? Permissions.has(player, perm) : player.isOp();
		
	}
	
	public boolean usingPermissions(){ return Permissions == null; }
	
	public Map<Player, Link> getOpenLocations(){ return PlayerOpenLinks; }
	
	public PlayerState getState(String name){ return PlayerStates.get(name); }

	public void removeState(String name){ PlayerStates.remove(name); }

	public Linker getLinker(){ return LinkManager; }

	public static ChestLink getStatic(){ return plugin; }

	public enum PlayerState {

		NONE,
		LINK,
		UNLINK,
		CHECK_LINK;

		String group;

		public void setGroup(String val){ group = val; }

		public String getGroup(){ return group; }

	}

}
