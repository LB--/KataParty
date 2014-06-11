package com.lb_stuff.kataparty;

import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.event.Listener;
import org.bukkit.event.EventHandler;
import org.bukkit.event.inventory.*;
import org.bukkit.event.player.*;
import org.bukkit.entity.Player;
import org.bukkit.inventory.*;
import org.bukkit.Material;
import org.bukkit.material.MaterialData;
import org.bukkit.configuration.*;
import org.bukkit.configuration.file.YamlConfiguration;

import java.util.*;
import java.io.*;
import java.util.logging.Level;

public class KataParty extends JavaPlugin implements Listener
{
	@Override
	public void onEnable()
	{
		File f = new File("plugins/KataParty/parties.yaml");
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
		File f = new File("plugins/KataParty");
		f.mkdir();
		f = new File("plugins/KataParty/parties.yaml");
		YamlConfiguration conf = new YamlConfiguration();
		ConfigurationSection cp = conf.createSection("parties");
		for(Party p : parties)
		{
			ConfigurationSection ps = cp.createSection(p.name);
			ps.set("tp", p.tp);
			ps.set("visible", p.visible);
			if(p.inv == null)
			{
				ps.set("inventory", false);
			}
			else
			{
				ps.set("inventory", p.inv.getContents());
			}
			ConfigurationSection pms = ps.createSection("members");
			for(Party.Member m : p.members)
			{
				ConfigurationSection ms = pms.createSection(m.uuid.toString());
				ms.set("rank", m.rank.toString());
				ms.set("tp", m.tp);
			}
		}
		try
		{
			conf.save(f);
		}
		catch(IOException e)
		{
			getLogger().log(Level.SEVERE, "Could not save parties!", e);
		}
	}

	public static enum Rank
	{
		OWNER,
		ADMIN,
		MODERATOR,
		MEMBER
	}
	public class Party
	{
		public final String name;
		public boolean tp = true;
		public boolean visible = true;
		public Inventory inv = null;
		public List<Member> members = new ArrayList<>();

		public Party(String pname)
		{
			name = pname;
		}

		public void add(Player p, Rank r)
		{
			members.add(new Member(p, r));
		}

		public KataParty getPlugin()
		{
			return KataParty.this;
		}

		public class Member
		{
			public final UUID uuid;
			public final Rank rank;
			public boolean tp = true;

			public Member(Player p, Rank r)
			{
				uuid = p.getUniqueId();
				rank = r;
			}

			public Party getParty()
			{
				return Party.this;
			}
		}
	}

	public Set<Party> parties = new HashSet<>();
	Party.Member findMember(UUID uuid)
	{
		for(Party p : parties)
		{
			for(Party.Member m : p.members)
			{
				if(m.uuid == uuid)
				{
					return m;
				}
			}
		}
		return null;
	}

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
