package com.lb_stuff.kataparty;

import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.event.Listener;
import org.bukkit.event.EventHandler;
import org.bukkit.event.inventory.*;
import org.bukkit.event.player.*;
import org.bukkit.entity.Player;
import org.bukkit.entity.HumanEntity;
import org.bukkit.inventory.*;
import org.bukkit.inventory.meta.*;
import org.bukkit.Material;
import org.bukkit.configuration.*;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;

import java.util.*;
import java.util.logging.Level;
import java.util.concurrent.ConcurrentSkipListSet;
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
		CREATE,
		LIST,
		toMANAGE,
		MANAGE,
		toMEMBERS,
		MEMBERS,
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
			partiers.add(uuid);
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
					partiers.remove(uuid);
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
		public Member memberByName(String name)
		{
			for(Member m : members)
			{
				OfflinePlayer offp = getServer().getOfflinePlayer(m.uuid);
				if(offp.getName().equalsIgnoreCase(name))
				{
					return m;
				}
			}
			return null;
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
			public Rank rank;
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

	public ConcurrentSkipListSet<UUID> partiers = new ConcurrentSkipListSet<UUID>();

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
			if(p.name.equalsIgnoreCase(name))
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
	public Inventory partyList(final Player player)
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
					if(player.hasPermission("KataParty.admin"))
					{
						add("Right click to administrate");
					}
				}});
				s.setItemMeta(m);
				plist.addItem(s);
			}
		}
		return plist;
	}
	public Inventory partyManage(final Party party, final Player player)
	{
		Inventory inv = Bukkit.createInventory(null, 9*2, party.name+" Settings");

		final Party.Member mt = findMember(player.getUniqueId());
		boolean is_member = false;
		boolean is_admin = false;
		boolean is_partyAdmin = false;
		boolean is_partyMod = false;
		if(mt != null && mt.getParty() == party)
		{
			is_member = true;
			is_partyAdmin = (mt.rank == Rank.ADMIN);
			is_partyMod = (is_partyAdmin || mt.rank == Rank.MODERATOR);
		}
		if(player.hasPermission("KataParty.admin"))
		{
			is_admin = true;
		}
		final boolean isMember = is_member;
		final boolean isAdmin = is_admin;
		final boolean isPartyAdmin = is_partyAdmin;
		final boolean isPartyMod = is_partyMod;

		ItemStack i = new ItemStack(Material.NAME_TAG);
		ItemMeta m = i.getItemMeta();
		m.setDisplayName(party.name);
		m.setLore(new ArrayList<String>(){
		{
			if(isMember)
			{
				add("Your rank: "+mt.rank);
				add("Click to leave this KataParty");
			}
			else
			{
				add("You are not a member of this KataParty");
			}
			if(isAdmin)
			{
				add("You are managing this KataParty as a server admin");
			}
		}});
		i.setItemMeta(m);
		inv.setItem(0, i);

		int online = 0, mods = 0, onmods = 0, admins = 0, onadmins = 0;
		for(Party.Member mem : party.members)
		{
			if(mem.rank.equals(Rank.MODERATOR))
			{
				++mods;
			}
			else if(mem.rank.equals(Rank.ADMIN))
			{
				++admins;
			}
			if(getServer().getPlayer(mem.uuid) != null)
			{
				++online;
				if(mem.rank.equals(Rank.MODERATOR))
				{
					++onmods;
				}
				else if(mem.rank.equals(Rank.ADMIN))
				{
					++onadmins;
				}
			}
		}
		final int online_ = online;
		final int mods_ = mods;
		final int onmods_ = onmods;
		final int admins_ = admins;
		final int onadmins_ = onadmins;

		i = new ItemStack(Material.SKULL_ITEM, party.members.size(), (short)3);
		m = i.getItemMeta();
		m.setDisplayName("Members (submenu)");
		m.setLore(new ArrayList<String>(){
		{
			add(online_+"/"+party.members.size()+" online");
			add(onmods_+"/"+mods_+" moderators online");
			add(onadmins_+"/"+admins_+" admins online");
		}});
		i.setItemMeta(m);
		inv.setItem(1, i);

		i = new ItemStack(Material.ENDER_PEARL, (party.tp? 2 : 1));
		m = i.getItemMeta();
		m.setDisplayName("Teleportation "+(party.tp? "enabled" : "disabled"));
		m.setLore(new ArrayList<String>(){
		{
			if(isAdmin || (player.hasPermission("KataParty.teleport.disable") && isPartyMod))
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

		i = new ItemStack((party.pvp? Material.GOLD_SWORD : Material.STONE_SWORD), (party.pvp? 2 : 1));
		m = i.getItemMeta();
		m.setDisplayName("PvP "+(party.pvp? "enabled" : "disabled"));
		m.setLore(new ArrayList<String>(){
		{
			if(isAdmin || isPartyMod)
			{
				add("Click to change");
			}
			else
			{
				add("You cannot change this");
			}
		}});
		i.setItemMeta(m);
		inv.setItem(3, i);

		i = new ItemStack(Material.ENDER_CHEST, (party.inv != null? 2 : 1));
		m = i.getItemMeta();
		m.setDisplayName("Shared inventory "+(party.inv != null? "enabled" : "disabled"));
		m.setLore(new ArrayList<String>(){
		{
			if(isAdmin || (player.hasPermission("KataParty.inventory.enable") && isPartyMod))
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

		i = new ItemStack((party.visible? Material.JACK_O_LANTERN : Material.PUMPKIN), (party.visible? 2 : 1));
		m = i.getItemMeta();
		m.setDisplayName("Will"+(party.visible? "" : " not")+" be visible in list");
		m.setLore(new ArrayList<String>(){
		{
			if(isAdmin || (player.hasPermission("KataParty.hide") && isPartyAdmin))
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

		if(isAdmin || (isMember && isPartyAdmin))
		{
			i = new ItemStack(Material.TNT);
			m = i.getItemMeta();
			m.setDisplayName(isMember? "Disband your KataParty" : "Close this KataParty");
			m.setLore(new ArrayList<String>(){
			{
				if(isAdmin || (player.hasPermission("KataParty.disband") && isPartyAdmin))
				{
					add("Click to use");
				}
				else
				{
					add("You cannot use this");
				}
			}});
			i.setItemMeta(m);
			inv.setItem(9, i);
		}

		i = new ItemStack(Material.ENDER_PORTAL_FRAME);
		m = i.getItemMeta();
		m.setDisplayName("Teleport all players to yourself");
		m.setLore(new ArrayList<String>(){
		{
			if(isAdmin || (player.hasPermission("KataParty.teleport.do") && isPartyAdmin))
			{
				add("Click to use");
			}
			else
			{
				add("You cannot use this");
			}
		}});
		i.setItemMeta(m);
		inv.setItem(10, i);

		if(isMember)
		{
			i = new ItemStack(Material.EYE_OF_ENDER, (mt.tp? 2 : 1));
			m = i.getItemMeta();
			m.setDisplayName("Members are"+(mt.tp? "" : " not")+" allowed to teleport to you");
			m.setLore(new ArrayList<String>(){
			{
				if(player.hasPermission("KataParty.teleport.disallow"))
				{
					add("Click to change");
				}
				else
				{
					add("You cannot change this");
				}
			}});
			i.setItemMeta(m);
			inv.setItem(11, i);
		}

		guis.put(player, GuiType.MANAGE);

		return inv;
	}
	public Inventory partyMembers(final Party party, final Player player)
	{
		Inventory inv = Bukkit.createInventory(null, 9*6, "Manage "+party.name+" members");

		final Party.Member mt = findMember(player.getUniqueId());
		boolean is_member = false;
		boolean is_admin = false;
		boolean is_partyAdmin = false;
		boolean is_partyMod = false;
		if(mt != null && mt.getParty() == party)
		{
			is_member = true;
			is_partyAdmin = (mt.rank == Rank.ADMIN);
			is_partyMod = (is_partyAdmin || mt.rank == Rank.MODERATOR);
		}
		if(player.hasPermission("KataParty.admin"))
		{
			is_admin = true;
		}
		final boolean isMember = is_member;
		final boolean isAdmin = is_admin;
		final boolean isPartyAdmin = is_partyAdmin;
		final boolean isPartyMod = is_partyMod;

		for(final Party.Member m : party.members)
		{
			ItemStack i = new ItemStack(Material.NAME_TAG);
			ItemMeta im = i.getItemMeta();
			im.setDisplayName(party.name);
			im.setLore(new ArrayList<String>(){
			{
				add("Click to return to management");
			}});
			i.setItemMeta(im);
			inv.addItem(i);

			final OfflinePlayer offp = getServer().getOfflinePlayer(m.uuid);
			final Player onp = offp.getPlayer();
			i = new ItemStack(Material.SKULL_ITEM, (m.rank.equals(Rank.MODERATOR)? 2 : (m.rank.equals(Rank.ADMIN)? 3 : 1)), (short)3);
			im = i.getItemMeta();
			im.setDisplayName(offp.getName());
			im.setLore(new ArrayList<String>(){
			{
				if(m.uuid.equals(player.getUniqueId()))
				{
					add("(that's you!)");
				}
				add("Rank: "+m.rank);
				switch(m.rank)
				{
					case MEMBER:
					{
						if(isAdmin || (isMember && isPartyAdmin))
						{
							add("Left click to promote to moderator");
						}
						if(isAdmin || (isMember && isPartyMod))
						{
							add("Right click to kick");
						}
					} break;
					case MODERATOR:
					{
						if(isAdmin || (isMember && isPartyAdmin))
						{
							add("Left click to promote to admin");
							add("Right click to demote to member");
						}
					} break;
					case ADMIN:
					{
						if(isAdmin || (isMember && isPartyAdmin))
						{
							add("Right click to demote to moderator");
						}
					} break;
					default: break;
				}
				add("Online: "+offp.isOnline());
				add("Allows TP: "+m.tp);
				if(offp.isOnline())
				{
					add("Alive: "+!onp.isDead());
				}
			}});
			((SkullMeta)im).setOwner(offp.getName());
			i.setItemMeta(im);
			inv.addItem(i);
		}

		guis.put(player, GuiType.MEMBERS);

		return inv;
	}
	public Inventory partyTeleport(final Player player)
	{
		Inventory inv = Bukkit.createInventory(null, 9*6, "Members in your KataParty");

		Party.Member m = findMember(player.getUniqueId());
		if(m == null)
		{
			return null;
		}

		ItemStack i = new ItemStack(Material.NAME_TAG);
		ItemMeta im = i.getItemMeta();
		im.setDisplayName(m.getParty().name);
		i.setItemMeta(im);
		inv.addItem(i);

		for(final Party.Member mem : m.getParty().members)
		{
			if(mem.uuid.equals(player.getUniqueId()))
			{
				continue;
			}
			final OfflinePlayer offp = getServer().getOfflinePlayer(mem.uuid);
			final Player onp = offp.getPlayer();
			i = new ItemStack(Material.SKULL_ITEM, 1, (short)3);
			im = i.getItemMeta();
			im.setDisplayName(offp.getName());
			im.setLore(new ArrayList<String>(){
			{
				add("Rank: "+mem.rank);
				add("Online: "+offp.isOnline());
				add("Allows TP: "+mem.tp);
				if(offp.isOnline())
				{
					add("Alive: "+!onp.isDead());
				}
			}});
			((SkullMeta)im).setOwner(offp.getName());
			i.setItemMeta(im);
			inv.addItem(i);
		}

		guis.put(player, GuiType.TP);

		return inv;
	}

	@EventHandler
	public void onInvClick(InventoryClickEvent e)
	{
		if(!guis.containsKey(e.getWhoClicked()))
		{
			return;
		}
		e.setCancelled(true);
		switch(guis.get(e.getWhoClicked()))
		{
			case CREATE:
			{
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
			} break;
			case LIST:
			{
				Party p = findParty(e.getCurrentItem().getItemMeta().getDisplayName());
				HumanEntity he = e.getWhoClicked();
				if(p != null)
				{
					switch(e.getClick())
					{
						case LEFT:
						{
							p.add(he.getUniqueId(), Rank.MEMBER);
							e.getView().close();
						} break;
						case RIGHT:
						{
							if(he.hasPermission("KataParty.admin"))
							{
								Inventory inv = partyManage(p, (Player)he);
								guis.put((Player)he, GuiType.toMANAGE);
								he.openInventory(inv);
							}
						} break;
						default: break;
					}
				}
				else
				{
					e.getView().getTopInventory().setContents(partyList((Player)he).getContents());
				}
				//
			} break;
			case toMANAGE:
			case MANAGE:
			{
				HumanEntity he = e.getWhoClicked();
				Party party = findParty(e.getView().getTopInventory().getItem(0).getItemMeta().getDisplayName());
				if(party == null)
				{
					e.getView().close();
					return;
				}

				final Party.Member mt = findMember(he.getUniqueId());
				boolean is_member = false;
				boolean is_admin = false;
				boolean is_partyAdmin = false;
				boolean is_partyMod = false;
				if(mt != null && mt.getParty() == party)
				{
					is_member = true;
					is_partyAdmin = (mt.rank == Rank.ADMIN);
					is_partyMod = (is_partyAdmin || mt.rank == Rank.MODERATOR);
				}
				if(he.hasPermission("KataParty.admin"))
				{
					is_admin = true;
				}
				final boolean isMember = is_member;
				final boolean isAdmin = is_admin;
				final boolean isPartyAdmin = is_partyAdmin;
				final boolean isPartyMod = is_partyMod;

				switch(e.getSlot())
				{
					case 0: //leave
					{
						if(isMember)
						{
							party.remove(mt.uuid);
							e.getView().close();
						}
					} break;
					case 1: //manage members
					{
						Inventory inv = partyMembers(party, (Player)he);
						guis.put((Player)he, GuiType.toMEMBERS);
						he.openInventory(inv);
					} break;
					case 2: //toggle TP
					{
						if(isAdmin || (he.hasPermission("KataParty.teleport.disable") && isPartyMod))
						{
							party.tp = !party.tp;
							e.getView().getTopInventory().setContents(partyManage(party, (Player)he).getContents());
						}
					} break;
					case 3: //toggle PvP
					{
						if(isAdmin || isPartyMod)
						{
							party.pvp = !party.pvp;
							e.getView().getTopInventory().setContents(partyManage(party, (Player)he).getContents());
						}
					} break;
					case 4: //toggle shared inventory
					{
						if(isAdmin || (he.hasPermission("KataParty.inventory.enable") && isPartyMod))
						{
							if(party.inv == null)
							{
								party.enableInventory();
							}
							else
							{
								party.disableInventory((Player)he);
							}
							e.getView().getTopInventory().setContents(partyManage(party, (Player)he).getContents());
						}
					} break;
					case 5: //toggle visibility
					{
						if(isAdmin || (he.hasPermission("KataParty.hide") && isPartyAdmin))
						{
							party.visible = !party.visible;
							e.getView().getTopInventory().setContents(partyManage(party, (Player)he).getContents());
						}
					} break;
					case 9: //disband/close
					{
						if(isAdmin || (he.hasPermission("KataParty.disband") && isPartyAdmin))
						{
							parties.remove(party);
							for(Party.Member mem : party.members)
							{
								Player plr = getServer().getPlayer(mem.uuid);
								if(plr != null)
								{
									plr.sendMessage("Your KataParty was "+(isMember? "disbanded" : "closed"));
								}
							}
							if(party.inv != null)
							{
								party.disableInventory((Player)he);
							}
							e.getView().close();
						}
					} break;
					case 10: //TP all to self
					{
						if(isAdmin || (he.hasPermission("KataParty.teleport.do") && isPartyAdmin))
						{
							for(Party.Member mem : party.members)
							{
								if(!mem.uuid.equals(he.getUniqueId()) && mem.tp)
								{
									OfflinePlayer offp = getServer().getOfflinePlayer(mem.uuid);
									if(offp.isOnline())
									{
										Player onp = offp.getPlayer();
										onp.setNoDamageTicks(20*5); //inulnerable for 5 seconds
										onp.teleport(he);
										onp.sendMessage("You were teleported to "+he.getName());
									}
								}
							}
							e.getView().close();
						}
					} break;
					case 11: //toggle self TP
					{
						if(isMember && he.hasPermission("KataParty.teleport.disallow"))
						{
							mt.tp = !mt.tp;
							e.getView().getTopInventory().setContents(partyManage(party, (Player)he).getContents());
						}
					} break;
					default: break;
				}
			} break;
			case toMEMBERS:
			case MEMBERS:
			{
				HumanEntity he = e.getWhoClicked();
				Party party = findParty(e.getView().getTopInventory().getItem(0).getItemMeta().getDisplayName());
				if(party == null)
				{
					e.getView().close();
					return;
				}
				if(e.getSlot() != 0)
				{
					Party.Member target = party.memberByName(e.getCurrentItem().getItemMeta().getDisplayName());
					if(target == null || target.getParty() != party)
					{
						e.getView().getTopInventory().setContents(partyMembers(party, (Player)he).getContents());
						return;
					}

					final Party.Member mt = findMember(he.getUniqueId());
					boolean is_member = false;
					boolean is_admin = false;
					boolean is_partyAdmin = false;
					boolean is_partyMod = false;
					if(mt != null && mt.getParty() == party)
					{
						is_member = true;
						is_partyAdmin = (mt.rank == Rank.ADMIN);
						is_partyMod = (is_partyAdmin || mt.rank == Rank.MODERATOR);
					}
					if(he.hasPermission("KataParty.admin"))
					{
						is_admin = true;
					}
					final boolean isMember = is_member;
					final boolean isAdmin = is_admin;
					final boolean isPartyAdmin = is_partyAdmin;
					final boolean isPartyMod = is_partyMod;

					switch(e.getClick())
					{
						case LEFT:
						{
							switch(target.rank)
							{
								case MEMBER:
								{
									if(isAdmin || (isMember && isPartyAdmin))
									{
										target.rank = Rank.MODERATOR;
										e.getView().getTopInventory().setContents(partyMembers(party, (Player)he).getContents());
									}
								} break;
								case MODERATOR:
								{
									if(isAdmin || (isMember && isPartyAdmin))
									{
										target.rank = Rank.ADMIN;
										e.getView().getTopInventory().setContents(partyMembers(party, (Player)he).getContents());
									}
								} break;
								default: break;
							}
						} break;
						case RIGHT:
						{
							switch(target.rank)
							{
								case MEMBER:
								{
									if(isAdmin || (isMember && isPartyMod))
									{
										party.remove(target.uuid);
										e.getView().getTopInventory().setContents(partyMembers(party, (Player)he).getContents());
									}
								} break;
								case MODERATOR:
								{
									if(isAdmin || (isMember && isPartyAdmin))
									{
										target.rank = Rank.MEMBER;
										e.getView().getTopInventory().setContents(partyMembers(party, (Player)he).getContents());
									}
								} break;
								case ADMIN:
								{
									if(isAdmin || (isMember && isPartyAdmin))
									{
										target.rank = Rank.MODERATOR;
										e.getView().getTopInventory().setContents(partyMembers(party, (Player)he).getContents());
									}
								} break;
								default: break;
							}
						} break;
						default: break;
					}
				}
				else
				{
					Inventory inv = partyManage(party, (Player)he);
					guis.put((Player)he, GuiType.toMANAGE);
					he.openInventory(inv);
				}
			} break;
			case TP:
			{
				if(e.getSlot() != 0)
				{
					Party party = findParty(e.getView().getTopInventory().getItem(0).getItemMeta().getDisplayName());
					if(party == null)
					{
						e.getView().close();
						return;
					}
					HumanEntity he = e.getWhoClicked();
					Party.Member m = party.memberByName(e.getCurrentItem().getItemMeta().getDisplayName());
					if(m == null || m.getParty() != party)
					{
						e.getView().getTopInventory().setContents(partyTeleport((Player)he).getContents());
						return;
					}

					OfflinePlayer target = getServer().getOfflinePlayer(m.uuid);
					if(target.isOnline() && m.tp)
					{
						he.teleport(target.getPlayer());
						e.getView().close();
					}
				}
			} break;
			default: break;
		}
	}
	@EventHandler
	public void onInvClose(InventoryCloseEvent e)
	{
		if(guis.containsKey(e.getPlayer()))
		{
			switch(guis.get(e.getPlayer()))
			{
				case toMANAGE:
				case toMEMBERS:
					break;
				default:
				{
					guis.remove(e.getPlayer());
				} break;
			}
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
		if(!e.isCancelled())
		{
			boolean proc = false;
			for(Player p : e.getRecipients())
			{
				if(partiers.contains(p.getUniqueId()))
				{
					proc = true;
					break;
				}
			}
			if(proc || partiers.contains(e.getPlayer().getUniqueId()))
			{
				e.setCancelled(true);
				getServer().getScheduler().scheduleSyncDelayedTask(this, new FilterPartyChat(e));
			}
		}
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

	private class FilterPartyChat implements Runnable
	{
		private AsyncPlayerChatEvent e;

		private FilterPartyChat(AsyncPlayerChatEvent event)
		{
			e = event;
		}

		@Override
		public void run()
		{
			getLogger().info("########## FilterPartyChat");
		}
	}
}
