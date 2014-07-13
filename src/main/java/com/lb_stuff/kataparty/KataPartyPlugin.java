package com.lb_stuff.kataparty;

import com.lb_stuff.kataparty.command.*;
import static com.lb_stuff.kataparty.PartySet.MemberSettings;
import com.lb_stuff.kataparty.config.*;

import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.ChatColor;
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
import java.io.*;

public class KataPartyPlugin extends JavaPlugin implements Listener, Messenger
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

	public final File configFile = new File(getDataFolder(), "config.yml");
	public final File partiesFile = new File(getDataFolder(), "parties.yml");
	private MainConfig config;
	@Override
	public void onEnable()
	{
		try
		{
			config = new MainConfig(configFile);
		}
		catch(IOException e)
		{
			getLogger().info(e.toString());
		}
		if(partiesFile.exists())
		{
			YamlConfiguration conf = YamlConfiguration.loadConfiguration(partiesFile);
			ConfigurationSection cs = conf.getConfigurationSection("parties");
			for(Map.Entry<String, Object> e : cs.getValues(false).entrySet())
			{
				ConfigurationSection ps = (ConfigurationSection)e.getValue();
				Party p = getParties().add(e.getKey());
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
		YamlConfiguration conf = new YamlConfiguration();
		ConfigurationSection cp = conf.createSection("parties");
		for(Party p : getParties())
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
			conf.save(partiesFile);
		}
		catch(IOException e)
		{
			getLogger().log(Level.SEVERE, "Could not save parties!", e);
		}
	}

	public String getFilterSwap()
	{
		return config.get("chat-filtering-swap").toString();
	}
	@Override
	public String getMessage(String name, Object... parameters)
	{
		Object format = config.get("messages."+name);
		if(format != null)
		{
			try
			{
				return String.format(format.toString().replaceAll("\\&", ""+ChatColor.COLOR_CHAR), parameters);
			}
			catch(IllegalFormatException e)
			{
				getLogger().warning("Error when using translation \""+name+"\":");
				ByteArrayOutputStream baos = new ByteArrayOutputStream();
				try(PrintStream ps = new PrintStream(baos))
				{
					e.printStackTrace(ps);
				}
				getLogger().warning(baos.toString());
				return "<Broken translation \""+name+"\">";
			}
		}
		getLogger().warning("Missing translation string \""+name+"\"");
		return "<Missing translation \""+name+"\">";
	}
	@Override @Deprecated
	public void tell(Player p, String message)
	{
		p.sendMessage("[KataParty] "+message);
	}
	@Override
	public void tellMessage(Player p, String name, Object... parameters)
	{
		tell(p, getMessage(name, parameters));
	}

	private final PartySet parties = new PartySet(this);
	public PartySet getParties()
	{
		return parties;
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
			boolean useother = msg.startsWith(getFilterSwap());
			MemberSettings ms = getParties().getSettings(source.getUniqueId());
			String senderparty = (ms != null ? ms.getPartyName() : null);
			boolean talkparty = (ms != null ? ms.isPartyPreferred() : false);

			//need to manually send to console since we are cancelling the event
			getServer().getConsoleSender().sendMessage(String.format(fmt, source.getDisplayName(), msg));

			if(ms != null)
			{
				if(useother)
				{
					msg = msg.substring(getFilterSwap().length());
				}
				if(!talkparty)
				{
					useother = !useother;
				}
			}
			for(Player p : targets)
			{
				MemberSettings pms = getParties().getSettings(p.getUniqueId());
				String pn = (pms != null ? pms.getPartyName() : null);
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
		Party.Member m = getParties().findMember(p.getUniqueId());
		if(m != null)
		{
			tellMessage(p, "party-member-inform", m.getParty().getName());
			MemberSettings ms = getParties().getSettings(p.getUniqueId());
			if(ms != null)
			{
				ms.setPartyPreferred(false);
				tellMessage(p, "chat-filtering-global", getFilterSwap());
			}
			//TODO: shared health stuff
		}
		else
		{
			tellMessage(p, "party-introvert-inform");
		}
	}
	@EventHandler(priority = EventPriority.MONITOR)
	public void onPlayerLeave(PlayerQuitEvent e)
	{
		e.getPlayer().closeInventory();
		Party.Member m = getParties().findMember(e.getPlayer().getUniqueId());
		if(m != null)
		{
			//TODO: shared health stuff
		}
	}
	@EventHandler
	public void onDamageBy(EntityDamageByEntityEvent e)
	{
		Party.Member a = getParties().findMember(e.getDamager().getUniqueId());
		Party.Member b = getParties().findMember(e.getEntity().getUniqueId());
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
					b = getParties().findMember(owner.getUniqueId());
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
				a = getParties().findMember(owner.getUniqueId());
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
				Party.Member a = getParties().findMember(owner.getUniqueId());
				Party.Member b = getParties().findMember(e.getTarget().getUniqueId());
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
			Party.Member thrower = getParties().findMember(((Player)ps).getUniqueId());
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
			Party.Member m = getParties().findMember(e.getEntity().getUniqueId());
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
			Party.Member m = getParties().findMember(e.getEntity().getUniqueId());
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
			Party.Member m = getParties().findMember(e.getEntity().getUniqueId());
			if(m != null && m.getParty().getHealth() != null)
			{
				//TODO: shared health stuff
			}
		}
	}
	@EventHandler(priority = EventPriority.MONITOR)
	public void onRespawn(PlayerRespawnEvent e)
	{
		Party.Member m = getParties().findMember(e.getPlayer().getUniqueId());
		if(m != null && m.getParty().getHealth() != null)
		{
			//TODO: shared health stuff
		}
	}
	@EventHandler(priority = EventPriority.MONITOR)
	public void onXp(PlayerExpChangeEvent e)
	{
		Party.Member m = getParties().findMember(e.getPlayer().getUniqueId());
		if(m != null && m.getParty().getHealth() != null)
		{
			//TODO: shared health stuff
		}
	}
}
