package com.lb_stuff.kataparty;

import com.lb_stuff.command.PluginInfoCommand;
import com.lb_stuff.command.PluginReloadCommand;
import com.lb_stuff.kataparty.command.*;
import com.lb_stuff.kataparty.config.MainConfig;
import static com.lb_stuff.kataparty.PartySettings.MemberSettings;
import com.lb_stuff.kataparty.PartyFactory.MemberFactory;
import com.lb_stuff.kataparty.api.*;
import com.lb_stuff.service.ChatFilterService;

import net.gravitydevelopment.updater.Updater;

import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.TabExecutor;
import org.bukkit.Bukkit;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.configuration.serialization.ConfigurationSerialization;
import org.bukkit.inventory.ItemStack;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;
import static org.bukkit.ChatColor.*;
import org.bukkit.plugin.ServicePriority;
import org.bukkit.command.CommandSender;

import java.util.*;
import java.io.*;

public final class KataPartyPlugin extends JavaPlugin implements IMessenger
{
	private void implementCommand(String name, CommandExecutor command)
	{
		getCommand(name).setExecutor(command);
	}
	private void implementCommand(String name, TabExecutor command)
	{
		getCommand(name).setTabCompleter(command);
		getCommand(name).setExecutor(command);
	}

	private final File configFile = new File(getDataFolder(), "config.yml");
	private final File partiesFile = new File(getDataFolder(), "parties.yml");
	private final PartyFactory pfact = new PartyFactory();
	private final PartyFactory.MemberFactory mfact = pfact.new MemberFactory();
	private final PartyPvpListener pvp = new PartyPvpListener(this);
	private final PartyPotionListener potions = new PartyPotionListener(this);
	private final PartyHealthXpListener shxp = new PartyHealthXpListener(this);
	private MainConfig config;
	private Updater updater = null;
	@Override
	public void onEnable()
	{
		new ChatFilterService(ServicePriority.Normal).start();
		getServer().getServicesManager().register(KataPartyService.class, service, this, ServicePriority.Highest);

		ConfigurationSerialization.registerClass(Party.class);
		ConfigurationSerialization.registerClass(Party.Member.class);
		ConfigurationSerialization.registerClass(PartySet.class);

		getPartySet().registerPartyFactory(PartySettings.class, pfact);
		getPartySet().registerMemberFactory(MemberSettings.class, mfact);

		implementCommand("kataparty", new PluginInfoCommand(this));
		implementCommand("kpreload", new PluginReloadCommand(this));
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

		getServer().getPluginManager().registerEvents(pvp, this);
		getServer().getPluginManager().registerEvents(potions, this);
		getServer().getPluginManager().registerEvents(shxp, this);
		getServer().getPluginManager().registerEvents(filter, this);
		getServer().getPluginManager().registerEvents(tickets, this);
		getServer().getPluginManager().registerEvents(edl, this);

		boolean firstrun = !configFile.exists();

		try
		{
			getDataFolder().mkdirs();
			config = new MainConfig(configFile);
		}
		catch(IOException|InvalidConfigurationException e)
		{
			throw new RuntimeException(e);
		}
		if(!firstrun)
		{
			Updater.UpdateType type = Updater.UpdateType.DEFAULT;
			if(!config.getBoolean("auto-updater"))
			{
				type = Updater.UpdateType.NO_DOWNLOAD;
				getLogger().info("Automatic update downloading disabled in config");
			}
			updater = new KataPartyUpdater(this, getFile(), type);
		}
		else
		{
			getLogger().warning("This plugin supports auto-updating - you may disable it in the config.");
		}

		if(partiesFile.exists())
		{
			final YamlConfiguration conf = YamlConfiguration.loadConfiguration(partiesFile);
			if(conf.contains("party-set"))
			{
				Bukkit.getScheduler().runTask(this, new Runnable(){@Override public void run()
				{
					getLogger().info("Loading parties...");
					conf.get("party-set");
					getLogger().info("Done loading parties.");
				}});
			}
			else //compatibiity with pre-v1.2.3
			{
				ConfigurationSection cs = conf.getConfigurationSection("parties");
				for(Map.Entry<String, Object> e : cs.getValues(false).entrySet())
				{
					ConfigurationSection ps = (ConfigurationSection)e.getValue();
					PartySettings p = new PartySettings();
					p.setName(e.getKey());
					p.setTp(ps.getBoolean("tp"));
					p.setPvp(ps.getBoolean("pvp"));
					p.setVisible(ps.getBoolean("visible"));
					if(ps.contains("invite-only"))
					{
						p.setInviteOnly(ps.getBoolean("invite-only"));
					}
					else
					{
						p.setInviteOnly(false);
					}
					if(ps.contains("stickied"))
					{
						p.setSticky(ps.getBoolean("stickied"));
					}
					else
					{
						p.setSticky(false);
					}
					IParty party = getPartySet().newParty(null, p);
					if(!ps.isBoolean("inventory"))
					{
						List<ItemStack> items = (List<ItemStack>)ps.getList("inventory", new ArrayList<ItemStack>());
						party.enableInventory();
						for(int i = 0; i < items.size() && i < party.getInventory().getSize(); ++i)
						{
							party.getInventory().setItem(i, items.get(i));
						}
					}
					for(Map.Entry<String, Object> me : ps.getConfigurationSection("members").getValues(false).entrySet())
					{
						ConfigurationSection ms = (ConfigurationSection)me.getValue();
						IParty.IMember m = party.newMember(new MemberSettings(UUID.fromString(me.getKey())), null);
						m.setRank(PartyRank.valueOf(ms.getString("rank")));
						m.setTp(ms.getBoolean("tp"));
					}
				}
			}
		}

		parties.keepEmptyParties(!config.getBoolean("remove-empty-parties"));
	}
	@Override
	public void onDisable()
	{
		YamlConfiguration conf = new YamlConfiguration();
		conf.set("party-set", parties);
		try
		{
			conf.save(partiesFile);
		}
		catch(IOException e)
		{
			throw new RuntimeException(e);
		}
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
	public void saveDefaultConfig()
	{
		reloadConfig();
	}

	public static KataPartyPlugin getInst()
	{
		return (KataPartyPlugin)Bukkit.getServicesManager().getRegistration(KataPartyService.class).getPlugin();
	}

	private final PartySet parties = new PartySet(this);
	public PartySet getPartySet()
	{
		return parties;
	}

	private final PartyChatFilter filter = new PartyChatFilter(this);
	public PartyChatFilter getFilter()
	{
		return filter;
	}

	public ChatFilterPref getJoinFilterPref()
	{
		return getFilter().getDefaultFilterPref("on-party-join");
	}

	private final PartyTicketManager tickets = new PartyTicketManager(this);
	public PartyTicketManager getTicketManager()
	{
		return tickets;
	}

	private final KataPartyService service = new KataPartyService(this);
	public KataPartyService getService()
	{
		return service;
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
	private static final String PREFIX = ""+AQUA+"[KataParty] "+RESET;
	@Override
	public void tell(CommandSender p, String message)
	{
		for(String line : message.split("\\n"))
		{
			p.sendMessage(PREFIX+line);
		}
	}
	@Override
	public void tellMessage(CommandSender p, String name, Object... parameters)
	{
		tell(p, getMessage(name, parameters));
	}
	@Override
	public void tellConsole(String message)
	{
		tell(getServer().getConsoleSender(), message);
	}

	private final EventDebugLogger edl = new EventDebugLogger(this);
	public EventDebugLogger getEventDebugLogger()
	{
		return edl;
	}
}
