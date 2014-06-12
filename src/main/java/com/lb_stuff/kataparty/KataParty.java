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
				p.pvp = ps.getBoolean("pvp");
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
			ps.set("pvp", p.pvp);
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
		public boolean pvp = false;
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
		public void disableInventory(Player p)
		{
			if(inv != null)
			{
				for(ItemStack i : inv.getContents())
				{
					if(i != null)
					{
						p.getWorld().dropItem(p.getLocation(), i).setPickupDelay(0);
					}
				}
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
			if(p.name.toLowerCase().equals(name.toLowerCase()))
			{
				return p;
			}
		}
		return null;
	}

	public Map<Player, GuiType> guis = new HashMap<>();
	public Inventory partyCreate(final Player player, String name)
	{
		Inventory inv = Bukkit.createInventory(null, 9*1, "Create KataParty "+name);

		ItemStack i = new ItemStack(Material.NAME_TAG);
		ItemMeta m = i.getItemMeta();
		m.setDisplayName(name);
		m.setLore(new ArrayList<String>(){
		{
			add("Click to create with these settings");
			add("Close this inventory screen to cancel");
		}});
		i.setItemMeta(m);
		inv.setItem(0, i);

		i = new ItemStack(Material.ENDER_PEARL, 2);
		m = i.getItemMeta();
		m.setDisplayName("Teleportation enabled");
		m.setLore(new ArrayList<String>(){
		{
			if(player.hasPermission("KataParty.teleport.disable"))
			{
				add("Click to change");
			}
			else
			{
				add("You cannot change this");
			}
		}});
		i.setItemMeta(m);
		inv.setItem(2, i);

		i = new ItemStack(Material.STONE_SWORD, 1); //Material.GOLD_SWORD
		m = i.getItemMeta();
		m.setDisplayName("PvP disabled");
		m.setLore(new ArrayList<String>(){
		{
			add("Click to change");
		}});
		i.setItemMeta(m);
		inv.setItem(3, i);

		i = new ItemStack(Material.ENDER_CHEST, 1);
		m = i.getItemMeta();
		m.setDisplayName("Shared inventory disabled");
		m.setLore(new ArrayList<String>(){
		{
			if(player.hasPermission("KataParty.inventory.enable"))
			{
				add("Click to change");
			}
			else
			{
				add("You cannot change this");
			}
		}});
		i.setItemMeta(m);
		inv.setItem(4, i);

		i = new ItemStack(Material.JACK_O_LANTERN, 2); //Material.PUMPKIN
		m = i.getItemMeta();
		m.setDisplayName("Will be visible in list");
		m.setLore(new ArrayList<String>(){
		{
			if(player.hasPermission("KataParty.hide"))
			{
				add("Click to change");
			}
			else
			{
				add("You cannot change this");
			}
		}});
		i.setItemMeta(m);
		inv.setItem(5, i);

		guis.put(player, GuiType.CREATE);

		return inv;
	}
	public Inventory partyList(Player player)
	{
		Inventory plist = Bukkit.createInventory(null, 9*6, "List of KataParties");
		for(final Party p : parties)
		{
			if(p.visible || player.hasPermission("KataParty.seehidden"))
			{
				ItemStack s = new ItemStack(p.visible? Material.NAME_TAG : Material.PAPER, p.members.size());
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
				Party.Member mem = findMember(player.getUniqueId());
				final boolean same = (mem != null && p == mem.getParty());
				m.setLore(new ArrayList<String>(){
				{
					if(!p.visible)
					{
						add("(invisible)");
					}
					add(online_+"/"+p.members.size()+" members online");
					add("PvP: "+p.pvp);
					add("TP: "+p.tp);
					add("Shared Inv: "+(p.inv != null? true : false));
					if(!same)
					{
						add("Left click to join (you will leave your current KataParty)");
					}
					else
					{
						add("You are in this KataParty");
					}
					add("Right click for more options");
				}});
				s.setItemMeta(m);
				plist.addItem(s);
			}
		}
		return plist;
	}

	@EventHandler
	public void onInvClick(InventoryClickEvent e)
	{
		if(!guis.containsKey(e.getWhoClicked()))
		{
			return;
		}
		if(guis.get(e.getWhoClicked()).equals(GuiType.LIST))
		{
			e.setCancelled(true);
			ClickType click = e.getClick();
			Party p = findParty(e.getCurrentItem().getItemMeta().getDisplayName());
			if(p != null)
			{
				if(click.equals(ClickType.LEFT))
				{
					p.add(e.getWhoClicked().getUniqueId(), Rank.MEMBER);
					e.getView().close();
				}
				else if(click.equals(ClickType.RIGHT))
				{
					//
				}
			}
			else
			{
				e.getView().close();
			}
			//
		}
		else if(guis.get(e.getWhoClicked()).equals(GuiType.CREATE))
		{
			e.setCancelled(true);
			switch(e.getSlot())
			{
				case 0: //create
				{
					String name = e.getCurrentItem().getItemMeta().getDisplayName();
					if(findParty(name) != null)
					{
						e.getWhoClicked().openInventory(Bukkit.createInventory(null, 9*1, "Name taken: "+name));
						break;
					}
					Party p = new Party(name);
					p.add(e.getWhoClicked().getUniqueId(), Rank.ADMIN);
					p.tp = (e.getInventory().getItem(2).getAmount() != 1);
					p.pvp = (e.getInventory().getItem(3).getAmount() != 1);
					if(e.getInventory().getItem(4).getAmount() != 1)
					{
						p.enableInventory();
					}
					p.visible = (e.getInventory().getItem(5).getAmount() != 1);
					parties.add(p);
					e.getWhoClicked().closeInventory();
				} break;
				case 2: //toggle TP
				{
					if(e.getWhoClicked().hasPermission("KataParty.teleport.disable"))
					{
						ItemStack i = e.getCurrentItem();
						ItemMeta d = i.getItemMeta();
						if(i.getAmount() != 1)
						{
							i.setAmount(1);
							d.setDisplayName("Teleportation disabled");
						}
						else
						{
							i.setAmount(2);
							d.setDisplayName("Teleportation enabled");
						}
						i.setItemMeta(d);
					}
				} break;
				case 3: //toggle PvP
				{
					ItemStack i = e.getCurrentItem();
					ItemMeta d = i.getItemMeta();
					if(i.getAmount() != 1)
					{
						i.setAmount(1);
						i.setType(Material.STONE_SWORD);
						d.setDisplayName("PvP disabled");
					}
					else
					{
						i.setAmount(2);
						i.setType(Material.GOLD_SWORD);
						d.setDisplayName("PvP enabled");
					}
					i.setItemMeta(d);
				} break;
				case 4: //toggle shared inventory
				{
					if(e.getWhoClicked().hasPermission("KataParty.inventory.enable"))
					{
						ItemStack i = e.getCurrentItem();
						ItemMeta d = i.getItemMeta();
						if(i.getAmount() != 1)
						{
							i.setAmount(1);
							d.setDisplayName("Shared inventory disabled");
						}
						else
						{
							i.setAmount(2);
							d.setDisplayName("Shared inventory enabled");
						}
						i.setItemMeta(d);
					}
				} break;
				case 5: //toggle visibility
				{
					if(e.getWhoClicked().hasPermission("KataParty.hide"))
					{
						ItemStack i = e.getCurrentItem();
						ItemMeta d = i.getItemMeta();
						if(i.getAmount() != 1)
						{
							i.setAmount(1);
							i.setType(Material.PUMPKIN);
							d.setDisplayName("Will not be visible in list");
						}
						else
						{
							i.setAmount(2);
							i.setType(Material.JACK_O_LANTERN);
							d.setDisplayName("Will be visible in list");
						}
						i.setItemMeta(d);
					}
				} break;
				default: break;
			}
		}
	}
	@EventHandler
	public void onInvClose(InventoryCloseEvent e)
	{
		if(guis.containsKey(e.getPlayer()))
		{
			guis.remove(e.getPlayer());
		}
	}
	@EventHandler
	public void OnInvDrag(InventoryDragEvent e)
	{
		if(guis.containsKey(e.getWhoClicked()))
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
		guis.remove(e.getPlayer());
	}
}
