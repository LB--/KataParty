package com.lb_stuff.kataparty;

import com.lb_stuff.kataparty.command.*;

import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.event.Listener;
import org.bukkit.event.EventHandler;
import org.bukkit.event.player.*;
import org.bukkit.event.entity.*;
import org.bukkit.event.entity.EntityTargetEvent.TargetReason;
import org.bukkit.event.EventPriority;
import org.bukkit.entity.*;
import org.bukkit.inventory.ItemStack;
import org.bukkit.configuration.*;
import org.bukkit.configuration.file.YamlConfiguration;
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
		e.getPlayer().closeInventory();
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
