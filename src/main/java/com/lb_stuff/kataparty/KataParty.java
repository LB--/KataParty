package com.lb_stuff.kataparty;

import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.event.Listener;
import org.bukkit.event.EventHandler;
import org.bukkit.event.inventory.*;
import org.bukkit.event.player.*;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.Material;
import org.bukkit.material.MaterialData;
import org.bukkit.configuration.file.YamlConfiguration;

import java.util.*;
import java.io.*;

public class KataParty extends JavaPlugin implements Listener
{
	@Override
	public void onEnable()
	{
		File f = new File("plugins/KataParty");
		f.mkdir();
		f = new File("plugins/KataParty/parties.yaml");
		if(f.exists())
		{
			YamlConfiguration conf = YamlConfiguration.loadConfiguration(f);
			for(Object o : conf.getList("parties", new ArrayList<Object>()))
			{
				//
			}
		}
		Commands c = new Commands(this);
		getCommand("kataparty").setExecutor(c);
		getCommand("kpcreate").setExecutor(c);
		getCommand("kplist").setExecutor(c);
		getCommand("kpjoin").setExecutor(c);
		getCommand("kpleave").setExecutor(c);
		getCommand("kpdisband").setExecutor(c);
		getCommand("kpclose").setExecutor(c);
		getCommand("kpmanage").setExecutor(c);
		getCommand("kpadmin").setExecutor(c);
		getCommand("kptp").setExecutor(c);
		getCommand("kpshare").setExecutor(c);
		getServer().getPluginManager().registerEvents(this, this);
	}
	@Override
	public void onDisable()
	{
		//
	}

	public class Party
	{
		public class Member
		{
			//
		}
	}

	private Set<Party> parties = new HashSet<>();

	@EventHandler
	public void onInvClick(InventoryClickEvent e)
	{
		//
	}
	@EventHandler
	public void onInvClose(InventoryCloseEvent e)
	{
		//
	}
	@EventHandler
	public void OnInvDrag(InventoryDragEvent e)
	{
		if(false)
		{
			//
		}
		else
		{
			e.setCancelled(true);
		}
	}
	@EventHandler
	public void onPlayerChat(AsyncPlayerChatEvent e)
	{
		//
	}
	@EventHandler
	public void onPlayerJoin(PlayerJoinEvent e)
	{
		//
	}
	@EventHandler
	public void onPlayerLeave(PlayerQuitEvent e)
	{
		//
	}
}
