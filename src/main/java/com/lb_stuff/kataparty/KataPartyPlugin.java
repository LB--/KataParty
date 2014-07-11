package com.lb_stuff.kataparty;

import com.lb_stuff.kataparty.command.*;

import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.event.Listener;
import org.bukkit.event.EventHandler;
import org.bukkit.event.inventory.*;
import org.bukkit.event.inventory.InventoryType.SlotType;
import org.bukkit.event.player.*;
import org.bukkit.event.entity.*;
import org.bukkit.event.entity.EntityTargetEvent.TargetReason;
import org.bukkit.event.EventPriority;
import org.bukkit.entity.*;
import org.bukkit.inventory.*;
import org.bukkit.inventory.meta.*;
import org.bukkit.Material;
import org.bukkit.configuration.*;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.projectiles.ProjectileSource;

import java.util.*;
import java.util.logging.Level;
import java.util.concurrent.ConcurrentSkipListMap;
import java.io.*;

public class KataPartyPlugin extends JavaPlugin implements Listener
{
	private void implementCommand(String name, PartyCommand command)
	{
		getCommand(name).setExecutor(command);
	}
	private void implementCommand(String name, TabbablePartyCommand command)
	{
		getCommand(name).setTabCompleter(command);
		getCommand(name).setExecutor(command);
	}

	public static final String CONFIG_DIR = "plugins/KataParty/";
	public static final String CONFIG_MAIN = null;
	public static final String CONFIG_PARTIES = "parties.yml";
	@Override
	public void onEnable()
	{
		File f = new File(CONFIG_DIR+CONFIG_PARTIES);
		if(f.exists())
		{
			YamlConfiguration conf = YamlConfiguration.loadConfiguration(f);
			ConfigurationSection cs = conf.getConfigurationSection("parties");
			for(Map.Entry<String, Object> e : cs.getValues(false).entrySet())
			{
				ConfigurationSection ps = (ConfigurationSection)e.getValue();
				Party p = new Party(this, e.getKey());
				parties.add(p);
				p.setTp(ps.getBoolean("tp"));
				p.setPvp(ps.getBoolean("pvp"));
				p.setVisible(ps.getBoolean("visible"));
				if(!ps.isBoolean("inventory"))
				{
					List<ItemStack> items = (List<ItemStack>)ps.getList("inventory", new ArrayList<ItemStack>());
					p.enableInventory();
					for(int i = 0; i < items.size() && i < p.getInventory().getSize(); ++i)
					{
						p.getInventory().setItem(i, items.get(i));
					}
				}
				if(!ps.isBoolean("health"))
				{
					p.setHealth(ps.getDouble("health"));
				}
				p.setPotionsSmart(ps.getBoolean("potions"));
				for(Map.Entry<String, Object> me : ps.getConfigurationSection("members").getValues(false).entrySet())
				{
					ConfigurationSection ms = (ConfigurationSection)me.getValue();
					Party.Member m = p.addMember(UUID.fromString(me.getKey()));
					m.setRank(Party.Rank.valueOf(ms.getString("rank")));
					m.setTp(ms.getBoolean("tp"));
				}
			}
		}
		getCommand("kataparty").setExecutor(new PluginInfoCommand(this));
		implementCommand("kpcreate", new PartyCreateCommand(this));
		implementCommand("kplist", new PartyListCommand(this));
		implementCommand("kpjoin", new PartyJoinCommand(this));
		implementCommand("kpleave", new PartyLeaveCommand(this));
		implementCommand("kpmanage", new PartyManageCommand(this));
		implementCommand("kpdisband", new PartyDisbandCommand(this));
		implementCommand("kpadmin", new PartyAdminCommand(this));
		implementCommand("kpclose", new PartyCloseCommand(this));
		implementCommand("kptp", new PartyTeleportCommand(this));
		implementCommand("kpshare", new PartyInventoryCommand(this));
		implementCommand("kptoggle", new PartyChatToggleCommand(this));
		getServer().getPluginManager().registerEvents(this, this);
	}
	@Override
	public void onDisable()
	{
		File f = new File(CONFIG_DIR);
		f.mkdir();
		f = new File(CONFIG_DIR+CONFIG_PARTIES);
		YamlConfiguration conf = new YamlConfiguration();
		ConfigurationSection cp = conf.createSection("parties");
		for(Party p : parties)
		{
			ConfigurationSection ps = cp.createSection(p.getName());
			ps.set("tp", p.canTp());
			ps.set("pvp", p.canPvp());
			ps.set("visible", p.isVisible());
			if(p.getInventory() == null)
			{
				ps.set("inventory", false);
			}
			else
			{
				ps.set("inventory", p.getInventory().getContents());
			}
			if(p.getHealth() == null)
			{
				ps.set("health", false);
			}
			else
			{
				ps.set("health", p.getHealth());
			}
			ps.set("potions", p.arePotionsSmart());
			ConfigurationSection pms = ps.createSection("members");
			for(Party.Member m : p)
			{
				ConfigurationSection ms = pms.createSection(m.getUuid().toString());
				ms.set("rank", m.getRank().toString());
				ms.set("tp", m.canTp());
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

	public static class MemberSettings
	{
		public String partyname;
		public boolean talkparty = true;
		public MemberSettings(String pname)
		{
			partyname = pname;
		}
	}
	public ConcurrentSkipListMap<UUID, MemberSettings> partiers = new ConcurrentSkipListMap<>();

	public Set<Party> parties = new HashSet<>();
	public Party.Member findMember(UUID uuid)
	{
		for(Party p : parties)
		{
			for(Party.Member m : p)
			{
				if(m.getUuid().equals(uuid))
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
			if(p.getName().equalsIgnoreCase(name))
			{
				return p;
			}
		}
		return null;
	}

	public static enum GuiType
	{
		CREATE,
		LIST,
		toMANAGE,
		MANAGE,
		toRENAME,
		RENAME,
		toMEMBERS,
		MEMBERS,
		TP,
	}
	public Map<UUID, GuiType> guis = new HashMap<>();
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

		guis.put(player.getUniqueId(), GuiType.CREATE);

		return inv;
	}
	public void partyCreate(InventoryClickEvent e)
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
				Party p = new Party(this, name);
				p.addMember(e.getWhoClicked().getUniqueId()).setRank(Party.Rank.ADMIN);
				p.setTp(e.getInventory().getItem(2).getAmount() != 1);
				p.setPvp(e.getInventory().getItem(3).getAmount() != 1);
				if(e.getInventory().getItem(4).getAmount() != 1)
				{
					p.enableInventory();
				}
				p.setVisible(e.getInventory().getItem(5).getAmount() != 1);
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
	public Inventory partyList(final Player player)
	{
		Inventory inv = Bukkit.createInventory(null, 9*6, "List of KataParties");
		for(final Party p : parties)
		{
			if(p.isVisible() || player.hasPermission("KataParty.seehidden"))
			{
				ItemStack s = new ItemStack(p.isVisible()? Material.NAME_TAG : Material.PAPER, p.numMembers());
				ItemMeta m = s.getItemMeta();
				m.setDisplayName(p.getName());
				int online = 0;
				for(Party.Member mem : p)
				{
					if(getServer().getPlayer(mem.getUuid()) != null)
					{
						++online;
					}
				}
				final int online_ = online;
				Party.Member mem = findMember(player.getUniqueId());
				final boolean same = (mem != null && p == mem.getParty());
				m.setLore(new ArrayList<String>(){
				{
					if(!p.isVisible())
					{
						add("(invisible)");
					}
					add(online_+"/"+p.numMembers()+" members online");
					add("PvP: "+p.canPvp());
					add("TP: "+p.canTp());
					add("Shared Inv: "+(p.getInventory() != null? true : false));
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
				inv.addItem(s);
			}
		}

		guis.put(player.getUniqueId(), GuiType.LIST);

		return inv;
	}
	public void partyList(InventoryClickEvent e)
	{
		if(e.getCurrentItem() == null)
		{
			return;
		}
		Party p = findParty(e.getCurrentItem().getItemMeta().getDisplayName());
		HumanEntity he = e.getWhoClicked();
		if(p != null)
		{
			switch(e.getClick())
			{
				case LEFT:
				{
					p.addMember(he.getUniqueId());
					e.getView().close();
				} break;
				case RIGHT:
				{
					if(he.hasPermission("KataParty.admin"))
					{
						Inventory inv = partyManage(p, (Player)he);
						guis.put(he.getUniqueId(), GuiType.toMANAGE);
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
	}
	public Inventory partyManage(final Party party, final Player player)
	{
		Inventory inv = Bukkit.createInventory(null, 9*2, party.getName()+" Settings");

		final Party.Member mt = findMember(player.getUniqueId());
		boolean is_member = false;
		boolean is_admin = false;
		boolean is_partyAdmin = false;
		boolean is_partyMod = false;
		if(mt != null && mt.getParty() == party)
		{
			is_member = true;
			is_partyAdmin = (mt.getRank() == Party.Rank.ADMIN);
			is_partyMod = (is_partyAdmin || mt.getRank() == Party.Rank.MODERATOR);
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
		m.setDisplayName(party.getName());
		m.setLore(new ArrayList<String>(){
		{
			if(isMember)
			{
				add("Your rank: "+mt.getRank());
				add("Left click to leave this KataParty");
			}
			else
			{
				add("You are not a member of this KataParty");
			}
			if(isAdmin || isPartyAdmin)
			{
				add("Right click to rename this KataParty");
			}
			if(isAdmin)
			{
				add("You are managing this KataParty as a server admin");
			}
		}});
		i.setItemMeta(m);
		inv.setItem(0, i);

		int online = 0, mods = 0, onmods = 0, admins = 0, onadmins = 0;
		for(Party.Member mem : party)
		{
			if(mem.getRank().equals(Party.Rank.MODERATOR))
			{
				++mods;
			}
			else if(mem.getRank().equals(Party.Rank.ADMIN))
			{
				++admins;
			}
			if(getServer().getPlayer(mem.getUuid()) != null)
			{
				++online;
				if(mem.getRank().equals(Party.Rank.MODERATOR))
				{
					++onmods;
				}
				else if(mem.getRank().equals(Party.Rank.ADMIN))
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

		i = new ItemStack(Material.SKULL_ITEM, party.numMembers(), (short)3);
		m = i.getItemMeta();
		m.setDisplayName("Members (submenu)");
		m.setLore(new ArrayList<String>(){
		{
			add(online_+"/"+party.numMembers()+" online");
			add(onmods_+"/"+mods_+" moderators online");
			add(onadmins_+"/"+admins_+" admins online");
		}});
		i.setItemMeta(m);
		inv.setItem(1, i);

		i = new ItemStack(Material.ENDER_PEARL, (party.canTp()? 2 : 1));
		m = i.getItemMeta();
		m.setDisplayName("Teleportation "+(party.canTp()? "enabled" : "disabled"));
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

		i = new ItemStack((party.canPvp()? Material.GOLD_SWORD : Material.STONE_SWORD), (party.canPvp()? 2 : 1));
		m = i.getItemMeta();
		m.setDisplayName("PvP "+(party.canPvp()? "enabled" : "disabled"));
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

		i = new ItemStack(Material.ENDER_CHEST, (party.getInventory() != null? 2 : 1));
		m = i.getItemMeta();
		m.setDisplayName("Shared inventory "+(party.getInventory() != null? "enabled" : "disabled"));
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

		i = new ItemStack((party.isVisible()? Material.JACK_O_LANTERN : Material.PUMPKIN), (party.isVisible()? 2 : 1));
		m = i.getItemMeta();
		m.setDisplayName("Will"+(party.isVisible()? "" : " not")+" be visible in list");
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
			i = new ItemStack(Material.EYE_OF_ENDER, (mt.canTp()? 2 : 1));
			m = i.getItemMeta();
			m.setDisplayName("Members are"+(mt.canTp()? "" : " not")+" allowed to teleport to you");
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

		guis.put(player.getUniqueId(), GuiType.MANAGE);

		return inv;
	}
	public void partyManage(InventoryClickEvent e)
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
			is_partyAdmin = (mt.getRank() == Party.Rank.ADMIN);
			is_partyMod = (is_partyAdmin || mt.getRank() == Party.Rank.MODERATOR);
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
			case 0: //leave/rename
			{
				if(isMember && e.isLeftClick())
				{
					party.removeMember(mt.getUuid());
					e.getView().close();
				}
				else if(e.isRightClick() && (isAdmin || isPartyAdmin))
				{
					Inventory inv = partyRename(party, (Player)he);
					guis.put(he.getUniqueId(), GuiType.toRENAME);
					e.getView().close(); //temporary until Bukkit supports opening Anvil inventories
					he.openInventory(inv);
				}
			} break;
			case 1: //manage members
			{
				Inventory inv = partyMembers(party, (Player)he);
				guis.put(he.getUniqueId(), GuiType.toMEMBERS);
				he.openInventory(inv);
			} break;
			case 2: //toggle TP
			{
				if(isAdmin || (he.hasPermission("KataParty.teleport.disable") && isPartyMod))
				{
					party.setTp(!party.canTp());
					e.getView().getTopInventory().setContents(partyManage(party, (Player)he).getContents());
				}
			} break;
			case 3: //toggle PvP
			{
				if(isAdmin || isPartyMod)
				{
					party.setPvp(!party.canPvp());
					e.getView().getTopInventory().setContents(partyManage(party, (Player)he).getContents());
				}
			} break;
			case 4: //toggle shared inventory
			{
				if(isAdmin || (he.hasPermission("KataParty.inventory.enable") && isPartyMod))
				{
					if(party.getInventory() == null)
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
					party.setVisible(!party.isVisible());
					e.getView().getTopInventory().setContents(partyManage(party, (Player)he).getContents());
				}
			} break;
			case 9: //disband/close
			{
				if(isAdmin || (he.hasPermission("KataParty.disband") && isPartyAdmin))
				{
					parties.remove(party);
					party.disband();
					party.disableInventory((Player)he);
					e.getView().close();
				}
			} break;
			case 10: //TP all to self
			{
				if(isAdmin || (he.hasPermission("KataParty.teleport.do") && isPartyAdmin))
				{
					for(Party.Member mem : party)
					{
						if(!mem.getUuid().equals(he.getUniqueId()) && mem.canTp())
						{
							OfflinePlayer offp = getServer().getOfflinePlayer(mem.getUuid());
							if(offp.isOnline())
							{
								Player onp = offp.getPlayer();
								onp.setNoDamageTicks(20*5); //inulnerable for 5 seconds
								onp.teleport(he);
								onp.sendMessage("[KataParty] You were teleported to "+he.getName());
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
					mt.setTp( ! mt.canTp());
					e.getView().getTopInventory().setContents(partyManage(party, (Player)he).getContents());
				}
			} break;
			default: break;
		}
	}
	public Inventory partyRename(final Party party, final Player player)
	{
		Inventory inv = Bukkit.createInventory(null, InventoryType.ANVIL, "Rename Party");

		ItemStack i = new ItemStack(Material.NAME_TAG);
		ItemMeta im = i.getItemMeta();
		im.setDisplayName(party.getName());
		im.setLore(new ArrayList<String>(){
		{
			add("Party name must be unique and not contain spaces");
		}});
		i.setItemMeta(im);
		inv.setItem(0, i);

		guis.put(player.getUniqueId(), GuiType.RENAME);

		return inv;
	}
	public void partyRename(InventoryClickEvent e)
	{
		HumanEntity he = e.getWhoClicked();
		Party party = findParty(e.getView().getTopInventory().getItem(0).getItemMeta().getDisplayName());
		if(party == null)
		{
			e.getView().close();
			return;
		}

		if(!e.getSlotType().equals(SlotType.RESULT))
		{
			e.setCancelled(true);
		}
		else
		{
			String name = e.getCurrentItem().getItemMeta().getDisplayName();
			if(!name.contains(" "))
			{
				if(findParty(name) != null)
				{
					e.setCancelled(true);
				}
				else
				{
					e.setCancelled(true);
					party.rename(name);
					e.getView().close();
				}
			}
		}
	}
	public Inventory partyMembers(final Party party, final Player player)
	{
		Inventory inv = Bukkit.createInventory(null, 9*6, "Manage "+party.getName()+" members");

		final Party.Member mt = findMember(player.getUniqueId());
		boolean is_member = false;
		boolean is_admin = false;
		boolean is_partyAdmin = false;
		boolean is_partyMod = false;
		if(mt != null && mt.getParty() == party)
		{
			is_member = true;
			is_partyAdmin = (mt.getRank() == Party.Rank.ADMIN);
			is_partyMod = (is_partyAdmin || mt.getRank() == Party.Rank.MODERATOR);
		}
		if(player.hasPermission("KataParty.admin"))
		{
			is_admin = true;
		}
		final boolean isMember = is_member;
		final boolean isAdmin = is_admin;
		final boolean isPartyAdmin = is_partyAdmin;
		final boolean isPartyMod = is_partyMod;

		for(final Party.Member m : party)
		{
			ItemStack i = new ItemStack(Material.NAME_TAG);
			ItemMeta im = i.getItemMeta();
			im.setDisplayName(party.getName());
			im.setLore(new ArrayList<String>(){
			{
				add("Click to return to management");
			}});
			i.setItemMeta(im);
			inv.addItem(i);

			final OfflinePlayer offp = getServer().getOfflinePlayer(m.getUuid());
			final Player onp = offp.getPlayer();
			i = new ItemStack(Material.SKULL_ITEM, (m.getRank().equals(Party.Rank.MODERATOR)? 2 : (m.getRank().equals(Party.Rank.ADMIN)? 3 : 1)), (short)3);
			im = i.getItemMeta();
			im.setDisplayName(offp.getName() != null ? offp.getName() : m.getUuid().toString());
			im.setLore(new ArrayList<String>(){
			{
				if(offp.getName() == null)
				{
					add("§4(missing player file; unknown name)");
				}
				if(m.getUuid().equals(player.getUniqueId()))
				{
					add("(that's you!)");
				}
				add("Rank: "+m.getRank());
				switch(m.getRank())
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
				add("Online: "+(offp.isOnline() && player.canSee(onp)));
				add("Allows TP: "+m.canTp());
				if(offp.isOnline() && player.canSee(onp))
				{
					add("Alive: "+!onp.isDead());
				}
			}});
			((SkullMeta)im).setOwner(offp.getName());
			i.setItemMeta(im);
			inv.addItem(i);
		}

		guis.put(player.getUniqueId(), GuiType.MEMBERS);

		return inv;
	}
	public void partyMembers(InventoryClickEvent e)
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
			Party.Member target = party.findMember(e.getCurrentItem().getItemMeta().getDisplayName());
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
				is_partyAdmin = (mt.getRank() == Party.Rank.ADMIN);
				is_partyMod = (is_partyAdmin || mt.getRank() == Party.Rank.MODERATOR);
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
					switch(target.getRank())
					{
						case MEMBER:
						{
							if(isAdmin || (isMember && isPartyAdmin))
							{
								target.setRank(Party.Rank.MODERATOR);
								e.getView().getTopInventory().setContents(partyMembers(party, (Player)he).getContents());
							}
						} break;
						case MODERATOR:
						{
							if(isAdmin || (isMember && isPartyAdmin))
							{
								target.setRank(Party.Rank.ADMIN);
								e.getView().getTopInventory().setContents(partyMembers(party, (Player)he).getContents());
							}
						} break;
						default: break;
					}
				} break;
				case RIGHT:
				{
					switch(target.getRank())
					{
						case MEMBER:
						{
							if(isAdmin || (isMember && isPartyMod))
							{
								party.removeMember(target.getUuid());
								e.getView().getTopInventory().setContents(partyMembers(party, (Player)he).getContents());
							}
						} break;
						case MODERATOR:
						{
							if(isAdmin || (isMember && isPartyAdmin))
							{
								target.setRank(Party.Rank.MEMBER);
								e.getView().getTopInventory().setContents(partyMembers(party, (Player)he).getContents());
							}
						} break;
						case ADMIN:
						{
							if(isAdmin || (isMember && isPartyAdmin))
							{
								target.setRank(Party.Rank.MODERATOR);
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
			guis.put(he.getUniqueId(), GuiType.toMANAGE);
			he.openInventory(inv);
		}
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
		im.setDisplayName(m.getParty().getName());
		i.setItemMeta(im);
		inv.addItem(i);

		for(final Party.Member mem : m.getParty())
		{
			if(mem.getUuid().equals(player.getUniqueId()))
			{
				continue;
			}
			final OfflinePlayer offp = getServer().getOfflinePlayer(mem.getUuid());
			final Player onp = offp.getPlayer();
			i = new ItemStack(Material.SKULL_ITEM, 1, (short)3);
			im = i.getItemMeta();
			im.setDisplayName(offp.getName());
			im.setLore(new ArrayList<String>(){
			{
				add("Rank: "+mem.getRank());
				add("Online: "+(offp.isOnline() && player.canSee(onp)));
				add("Allows TP: "+mem.canTp());
				if(offp.isOnline() && player.canSee(onp))
				{
					add("Alive: "+!onp.isDead());
				}
			}});
			((SkullMeta)im).setOwner(offp.getName());
			i.setItemMeta(im);
			inv.addItem(i);
		}

		guis.put(player.getUniqueId(), GuiType.TP);

		return inv;
	}
	public void partyTeleport(InventoryClickEvent e)
	{
		if(e.getSlot() != 0 && e.getCurrentItem() != null)
		{
			Party party = findParty(e.getView().getTopInventory().getItem(0).getItemMeta().getDisplayName());
			if(party == null)
			{
				e.getView().close();
				return;
			}
			HumanEntity he = e.getWhoClicked();
			Party.Member m = party.findMember(e.getCurrentItem().getItemMeta().getDisplayName());
			if(m == null || m.getParty() != party)
			{
				e.getView().getTopInventory().setContents(partyTeleport((Player)he).getContents());
				return;
			}

			OfflinePlayer target = getServer().getOfflinePlayer(m.getUuid());
			if(target.isOnline() && m.canTp() && ((Player)he).canSee(target.getPlayer()))
			{
				he.teleport(target.getPlayer());
				e.getView().close();
				((Player)he).sendMessage("[KataParty] You were teleported to "+target.getName());
				target.getPlayer().sendMessage("[KataParty] "+he.getName()+" telepoted to you");
			}
		}
	}

	@EventHandler
	public void onInvClick(InventoryClickEvent e)
	{
		UUID uuid = e.getWhoClicked().getUniqueId();
		GuiType gt = guis.get(uuid);
		if(gt == null)
		{
			return;
		}
		e.setCancelled(true);
		switch(gt)
		{
			case CREATE:
			{
				partyCreate(e);
			} break;
			case LIST:
			{
				partyList(e);
			} break;
			case toMANAGE:
			case MANAGE:
			{
				guis.put(uuid, GuiType.MANAGE);
				partyManage(e);
			} break;
			case toRENAME:
			case RENAME:
			{
				guis.put(uuid, GuiType.RENAME);
				partyRename(e);
			} break;
			case toMEMBERS:
			case MEMBERS:
			{
				guis.put(uuid, GuiType.MEMBERS);
				partyMembers(e);
			} break;
			case TP:
			{
				partyTeleport(e);
			} break;
			default: break;
		}
	}
	@EventHandler
	public void onInvClose(InventoryCloseEvent e)
	{
		GuiType gt = guis.get(e.getPlayer().getUniqueId());
		if(gt != null)
		{
			switch(gt)
			{
				case toMANAGE:
				case toRENAME:
				case toMEMBERS:
					break;
				default:
				{
					guis.remove(e.getPlayer().getUniqueId());
				} break;
			}
		}
	}
	@EventHandler
	public void OnInvDrag(InventoryDragEvent e)
	{
		if(guis.containsKey(e.getWhoClicked().getUniqueId()))
		{
			e.setCancelled(true);
		}
	}
	@EventHandler(priority = EventPriority.HIGHEST) //highest executed last
	public void onPlayerChat(AsyncPlayerChatEvent e)
	{
		if(!e.isCancelled())
		{
			e.setCancelled(true);
			String msg = e.getMessage();
			String fmt = e.getFormat();
			Player source = e.getPlayer();
			Set<Player> targets = e.getRecipients();
			boolean useother = msg.startsWith("!");
			MemberSettings ms = partiers.get(source.getUniqueId());
			String senderparty = (ms != null ? ms.partyname : null);
			boolean talkparty = (ms != null ? ms.talkparty : false);

			//need to manually send to console since we are cancelling the event
			getServer().getConsoleSender().sendMessage(String.format(fmt, source.getDisplayName(), msg));

			if(ms != null)
			{
				if(useother)
				{
					msg = msg.substring(1);
				}
				if(!talkparty)
				{
					useother = !useother;
				}
			}
			for(Player p : targets)
			{
				MemberSettings pms = partiers.get(p.getUniqueId());
				String pn = (pms != null ? pms.partyname : null);
				if(pn != null) //receiver is in a party
				{
					if(senderparty != null) //sender /is/ in a party
					{
						if(useother) //send to other
						{
							p.sendMessage(String.format
							(
								fmt,
								source.getDisplayName(),
								(talkparty?"§7§o":"")+msg
							));
						}
						else if(pn.equals(senderparty)) //send to preferred
						{
							p.sendMessage(String.format
							(
								(talkparty?"§l":"§o")+"{%3$s}§r"+fmt,
								source.getDisplayName(),
								(talkparty?"":"§7§o")+msg,
								senderparty
							));
						}
						else {} //different parties
					}
					else //sender is /not/ in a party
					{
						p.sendMessage(String.format
						(
							fmt,
							source.getDisplayName(),
							(talkparty?"§7§o":"")+msg
						));
					}
				}
				else //receiver is /not/ in a party
				{
					if(senderparty == null) //sender is /not/ in a party
					{
						p.sendMessage(String.format
						(
							fmt,
							source.getDisplayName(),
							msg
						));
					}
					else //sender /is/ in a party
					{
						if(useother) //send to other
						{
							p.sendMessage(String.format
							(
								fmt,
								source.getDisplayName(),
								msg
							));
						}
						else {} //send to preferred
					}
				}
			}
		}
	}
	@EventHandler(priority = EventPriority.MONITOR)
	public void onPlayerJoin(PlayerJoinEvent e)
	{
		Player p = e.getPlayer();
		Party.Member m = findMember(p.getUniqueId());
		if(m != null)
		{
			p.sendMessage("[KataParty] You are in KataParty §n"+m.getParty().getName()+"§r");
			MemberSettings ms = partiers.get(p.getUniqueId());
			if(ms != null)
			{
				ms.talkparty = false;
				p.sendMessage("[KataParty] You talk in global chat, start message with ! to speak in party");
			}
			//TODO: shared health stuff
		}
		else
		{
			e.getPlayer().sendMessage("[KataParty] You are §nnot§r in a KataParty");
		}
	}
	@EventHandler(priority = EventPriority.MONITOR)
	public void onPlayerLeave(PlayerQuitEvent e)
	{
		guis.remove(e.getPlayer().getUniqueId());
		Party.Member m = findMember(e.getPlayer().getUniqueId());
		if(m != null)
		{
			//TODO: shared health stuff
		}
	}
	@EventHandler
	public void onDamageBy(EntityDamageByEntityEvent e)
	{
		Party.Member a = findMember(e.getDamager().getUniqueId());
		Party.Member b = findMember(e.getEntity().getUniqueId());
		if(a != null && !a.getParty().canPvp())
		{
			if(b != null && a.getParty() == b.getParty())
			{
				e.setCancelled(true); //member attacks member
			}
			else if(e.getEntity() instanceof Wolf)
			{
				AnimalTamer owner = ((Wolf)e.getEntity()).getOwner();
				if(owner != null)
				{
					b = findMember(owner.getUniqueId());
					if(a != b && a.getParty() == b.getParty())
					{
						e.setCancelled(true); //member attacks wolf of member
					}
				}
			}
		}
		else if(b != null && !b.getParty().canPvp() && e.getDamager() instanceof Wolf)
		{
			Wolf w = (Wolf)e.getDamager();
			AnimalTamer owner = w.getOwner();
			if(owner != null)
			{
				a = findMember(owner.getUniqueId());
				if(a != null && a.getParty() == b.getParty())
				{
					e.setCancelled(true); //member's wolf attacks member
					w.setTarget(null);
				}
			}
		}
	}
	@EventHandler
	public void onTarget(EntityTargetEvent e)
	{
		if(e.getEntity() instanceof Wolf && (e.getReason() == TargetReason.TARGET_ATTACKED_OWNER || e.getReason() == TargetReason.OWNER_ATTACKED_TARGET))
		{
			AnimalTamer owner = ((Wolf)e.getEntity()).getOwner();
			if(owner != null)
			{
				Party.Member a = findMember(owner.getUniqueId());
				Party.Member b = findMember(e.getTarget().getUniqueId());
				if(a != null && b != null && a.getParty() == b.getParty() && !a.getParty().canPvp())
				{
					e.setCancelled(true); //member's wolf targets member
				}
			}
		}
	}
	@EventHandler
	public void onSplash(PotionSplashEvent e)
	{
		ProjectileSource ps = e.getPotion().getShooter();
		if(ps instanceof Player)
		{
			Party.Member thrower = findMember(((Player)ps).getUniqueId());
			if(thrower != null && thrower.getParty().arePotionsSmart())
			{
				//
			}
		}
	}
	@EventHandler(priority = EventPriority.MONITOR)
	public void onDamage(EntityDamageEvent e)
	{
		if(!e.isCancelled() && e.getEntity() instanceof Player)
		{
			Party.Member m = findMember(e.getEntity().getUniqueId());
			if(m != null && m.getParty().getHealth() != null)
			{
				//TODO: shared health stuff
			}
		}
	}
	@EventHandler(priority = EventPriority.MONITOR)
	public void onDeath(EntityDeathEvent e)
	{
		if(e.getEntity() instanceof Player)
		{
			Party.Member m = findMember(e.getEntity().getUniqueId());
			if(m != null && m.getParty().getHealth() != null)
			{
				//TODO: shared health stuff
			}
		}
	}
	@EventHandler(priority = EventPriority.MONITOR)
	public void onHeal(EntityRegainHealthEvent e)
	{
		if(!e.isCancelled() && e.getEntity() instanceof Player)
		{
			Party.Member m = findMember(e.getEntity().getUniqueId());
			if(m != null && m.getParty().getHealth() != null)
			{
				//TODO: shared health stuff
			}
		}
	}
	@EventHandler(priority = EventPriority.MONITOR)
	public void onRespawn(PlayerRespawnEvent e)
	{
		Party.Member m = findMember(e.getPlayer().getUniqueId());
		if(m != null && m.getParty().getHealth() != null)
		{
			//TODO: shared health stuff
		}
	}
	@EventHandler(priority = EventPriority.MONITOR)
	public void onXp(PlayerExpChangeEvent e)
	{
		Party.Member m = findMember(e.getPlayer().getUniqueId());
		if(m != null && m.getParty().getHealth() != null)
		{
			//TODO: shared health stuff
		}
	}
}
