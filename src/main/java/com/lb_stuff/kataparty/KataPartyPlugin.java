package com.lb_stuff.kataparty;

import com.lb_stuff.command.*;
import com.lb_stuff.kataparty.command.*;
import static com.lb_stuff.kataparty.PartySet.MemberSettings;
import com.lb_stuff.kataparty.config.*;
import com.lb_stuff.kataparty.api.PartyTicketService;

import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.InvalidConfigurationException;
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
import static org.bukkit.ChatColor.*;
import org.bukkit.plugin.ServicePriority;

import java.util.*;
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

	private final File configFile = new File(getDataFolder(), "config.yml");
	private final File partiesFile = new File(getDataFolder(), "parties.yml");
	private MainConfig config;
	private final PartyPvpListener pvp = new PartyPvpListener(this);
	@Override
	public void onEnable()
	{
		try
		{
			getDataFolder().mkdirs();
			config = new MainConfig(configFile);
		}
		catch(IOException|InvalidConfigurationException e)
		{
			throw new RuntimeException(e);
		}
		if(partiesFile.exists())
		{
			YamlConfiguration conf = YamlConfiguration.loadConfiguration(partiesFile);
			ConfigurationSection cs = conf.getConfigurationSection("parties");
			for(Map.Entry<String, Object> e : cs.getValues(false).entrySet())
			{
				ConfigurationSection ps = (ConfigurationSection)e.getValue();
				Party p = getParties().add(e.getKey(), null);
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
				if(ps.contains("invite-only"))
				{
					p.setInviteOnly(ps.getBoolean("invite-only"));
				}
				else
				{
					p.setInviteOnly(false);
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

		parties.keepEmptyParties(!config.getBoolean("remove-empty-parties"));

		getCommand("kataparty").setExecutor(new PluginInfoCommand(this));
		getCommand("kpreload").setExecutor(new PluginReloadCommand(this));
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
		getServer().getPluginManager().registerEvents(pvp, this);
		getServer().getPluginManager().registerEvents(filter, this);
		getServer().getPluginManager().registerEvents(tickets, this);

		getServer().getServicesManager().register(PartyTicketService.class, ticketserv, this, ServicePriority.Highest);
	}
	@Override
	public FileConfiguration getConfig()
	{
		return config;
	}
	@Override
	public void reloadConfig()
	{
		try
		{
			config.reload(configFile);
		}
		catch(IOException|InvalidConfigurationException e)
		{
			throw new RuntimeException(e);
		}
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
			ps.set("invite-only", p.isInviteOnly());
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
			throw new RuntimeException(e);
		}
	}

	private final PartySet parties = new PartySet(this);
	public PartySet getParties()
	{
		return parties;
	}

	private final PartyChatFilter filter = new PartyChatFilter(this);
	public PartyChatFilter getFilter()
	{
		return filter;
	}

	private final PartyTicketManager tickets = new PartyTicketManager(this);
	public PartyTicketManager getTicketManager()
	{
		return tickets;
	}

	private final PartyTicketService ticketserv = new PartyTicketService(this);
	public PartyTicketService getTicketService()
	{
		return ticketserv;
	}

	@Override
	public String getMessage(String name, Object... parameters)
	{
		String format = config.getString("messages."+name);
		if(format != null)
		{
			try
			{
				return String.format(format, parameters);
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
		p.sendMessage(""+AQUA+"[KataParty] "+RESET+message);
	}
	@Override
	public void tellMessage(Player p, String name, Object... parameters)
	{
		tell(p, getMessage(name, parameters));
	}

	@EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
	public void onPlayerJoin(PlayerJoinEvent e)
	{
		final Player p = e.getPlayer();
		Party.Member m = getParties().findMember(p.getUniqueId());
		if(m != null)
		{
			tellMessage(p, "party-member-inform", m.getParty().getName());
			MemberSettings ms = getParties().getSettings(p.getUniqueId());
			if(ms != null)
			{
				ms.setPref(filter.getDefaultFilterPref("on-join-server"));
				filter.tellFilterPref(p);
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
	@EventHandler(ignoreCancelled = true)
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
	@EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
	public void onDamage(EntityDamageEvent e)
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
	@EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
	public void onHeal(EntityRegainHealthEvent e)
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
