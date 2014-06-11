package com.lb_stuff.kataparty;

import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.event.Listener;
import org.bukkit.event.EventHandler;
import org.bukkit.event.inventory.*;
import org.bukkit.event.player.*;
import org.bukkit.entity.Player;
import org.bukkit.inventory.*;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.Material;
import org.bukkit.material.MaterialData;
import org.bukkit.configuration.*;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.Bukkit;

import java.util.*;
import java.util.logging.Level;
import java.io.*;

public class KataParty extends JavaPlugin implements Listener
{
	@Override
	public void onEnable()
	{
		plist = Bukkit.createInventory(null, 9*6, "List of KataParties");
		File f = new File("plugins/KataParty/parties.yaml");
		if(f.exists())
		{
			YamlConfiguration conf = YamlConfiguration.loadConfiguration(f);
			ConfigurationSection cs = conf.getConfigurationSection("parties");
			for(Map.Entry<String, Object> e : cs.getValues(false).entrySet())
			{
				ConfigurationSection ps = (ConfigurationSection)e.getValue();
				Party p = new Party(e.getKey());
				parties.add(p);
				p.tp = ps.getBoolean("tp");
				p.visible = ps.getBoolean("visible");
				if(!ps.isBoolean("inventory"))
				{
					List<ItemStack> items = (List<ItemStack>)ps.getList("inventory", new ArrayList<ItemStack>());
					p.enableInventory();
					for(int i = 0; i < items.size() && i < p.inv.getSize(); ++i)
					{
						p.inv.setItem(i, items.get(i));
					}
				}
				for(Map.Entry<String, Object> me : ps.getConfigurationSection("members").getValues(false).entrySet())
				{
					ConfigurationSection ms = (ConfigurationSection)me.getValue();
					Party.Member m = p.add(UUID.fromString(me.getKey()), Rank.valueOf(ms.getString("rank")));
					m.tp = ms.getBoolean("tp");
				}
			}
		}
		updateList();
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
		ADMIN,
		MODERATOR,
		MEMBER
	}
	public static enum GuiType
	{
		NONE,
		CREATE,
		LIST,
		MANAGE,
		TP,
	}
	public class Party
	{
		public final String name;
		public boolean tp = true;
		public boolean visible = true;
		public Inventory inv = null;
		public Set<Member> members = new HashSet<>();

		public Party(String pname)
		{
			name = pname;
		}

		public Member add(UUID uuid, Rank r)
		{
			Member m;
			while((m = findMember(uuid)) != null)
			{
				m.getParty().remove(uuid);
			}
			members.add(m = new Member(uuid, r));
			for(Member mi : members)
			{
				Player p = getServer().getPlayer(mi.uuid);
				if(p != null)
				{
					p.sendMessage(getServer().getPlayer(uuid).getName()+" has joined your KataParty");
				}
			}
			return m;
		}
		public void remove(UUID uuid)
		{
			for(Iterator<Member> it = members.iterator(); it.hasNext(); )
			{
				Member m = it.next();
				if(m.uuid.equals(uuid))
				{
					it.remove();
					break;
				}
			}
			for(Member m : members)
			{
				Player p = getServer().getPlayer(m.uuid);
				if(p != null)
				{
					p.sendMessage(getServer().getOfflinePlayer(uuid).getName()+" has left your KataParty");
				}
			}
		}

		public void enableInventory()
		{
			if(inv == null)
			{
				inv = Bukkit.createInventory(null, 4*9, name+" Shared Inventory");
			}
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
			public Inventory gui = null;
			GuiType gt = GuiType.NONE;

			public Member(UUID id, Rank r)
			{
				uuid = id;
				rank = r;
			}

			public Party getParty()
			{
				return Party.this;
			}
		}
	}

	public Set<Party> parties = new HashSet<>();
	public Party.Member findMember(UUID uuid)
	{
		for(Party p : parties)
		{
			for(Party.Member m : p.members)
			{
				if(m.uuid.equals(uuid))
				{
					return m;
				}
			}
		}
		return null;
	}
	public Party findParty(String name)
	{
		for(Party p : parties)
		{
			if(p.name.equals(name))
			{
				return p;
			}
		}
		return null;
	}

	Inventory plist = null;
	public void updateList()
	{
		plist.clear();
		for(final Party p : parties)
		{
			if(p.visible)
			{
				ItemStack s = new ItemStack(Material.NAME_TAG, p.members.size());
				ItemMeta m = s.getItemMeta();
				m.setDisplayName(p.name);
				int online = 0;
				for(Party.Member mem : p.members)
				{
					if(getServer().getPlayer(mem.uuid) != null)
					{
						++online;
					}
				}
				final int online_ = online;
				m.setLore(new ArrayList<String>(){
				{
					add(online_+"/"+p.members.size()+" members online");
					add("Left click to join (you will leave your current party)");
					add("Right click for more options");
				}});
				s.setItemMeta(m);
				plist.addItem(s);
			}
		}
	}

	@EventHandler
	public void onInvClick(InventoryClickEvent e)
	{
		Party.Member m = findMember(e.getWhoClicked().getUniqueId());
		if(m != null && m.gui.equals(e.getInventory()))
		{
			e.setCancelled(true);
			ClickType click = e.getClick();
			Party p = findParty(e.getCurrentItem().getItemMeta().getDisplayName());
			if(click.equals(ClickType.LEFT))
			{
				if(m.getParty() != p)
				{
					p.add(m.uuid, Rank.MEMBER);
					e.getView().close();
				}
			}
			else if(click.equals(ClickType.RIGHT))
			{
				//
			}
			//
		}
	}
	@EventHandler
	public void onInvClose(InventoryCloseEvent e)
	{
		Party.Member m = findMember(e.getPlayer().getUniqueId());
		if(m != null)
		{
			m.gt = GuiType.NONE;
			m.gui = null;
		}
	}
	@EventHandler
	public void OnInvDrag(InventoryDragEvent e)
	{
		Party.Member m = findMember(e.getWhoClicked().getUniqueId());
		if(m != null && m.gui.equals(e.getInventory()))
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
		Party.Member m = findMember(e.getPlayer().getUniqueId());
		if(m != null)
		{
			e.getPlayer().sendMessage("You are in KataParty "+m.getParty().name);
		}
		else
		{
			e.getPlayer().sendMessage("You are not in a KataParty");
		}
	}
	@EventHandler
	public void onPlayerLeave(PlayerQuitEvent e)
	{
		//
	}
}
