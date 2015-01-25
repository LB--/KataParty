package com.lb_stuff.kataparty;

import com.lb_stuff.bukkit.command.PluginInfoCommand;
import com.lb_stuff.bukkit.command.PluginReloadCommand;
import com.lb_stuff.eventfilterservices.EventFilterServices;
import com.lb_stuff.kataparty.PartyFactory.MemberFactory;
import com.lb_stuff.kataparty.api.IMessenger;
import com.lb_stuff.kataparty.api.IMetadatable;
import com.lb_stuff.kataparty.api.IParty;
import com.lb_stuff.kataparty.api.KataPartyService;
import com.lb_stuff.kataparty.api.PartyRank;
import com.lb_stuff.kataparty.command.PartyAdminCommand;
import com.lb_stuff.kataparty.command.PartyBackCommand;
import com.lb_stuff.kataparty.command.PartyChatToggleCommand;
import com.lb_stuff.kataparty.command.PartyCloseCommand;
import com.lb_stuff.kataparty.command.PartyCreateCommand;
import com.lb_stuff.kataparty.command.PartyDisbandCommand;
import com.lb_stuff.kataparty.command.PartyInventoryCommand;
import com.lb_stuff.kataparty.command.PartyJoinCommand;
import com.lb_stuff.kataparty.command.PartyLeaveCommand;
import com.lb_stuff.kataparty.command.PartyListCommand;
import com.lb_stuff.kataparty.command.PartyManageCommand;
import com.lb_stuff.kataparty.command.PartyMembersCommand;
import com.lb_stuff.kataparty.command.PartyPardonCommand;
import com.lb_stuff.kataparty.command.PartyTeleportCommand;
import com.lb_stuff.bukkit.config.SmartConfig;

import net.gravitydevelopment.updater.Updater;
import org.yaml.snakeyaml.error.YAMLException;

import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabExecutor;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.configuration.serialization.ConfigurationSerializable;
import org.bukkit.configuration.serialization.ConfigurationSerialization;
import org.bukkit.event.Listener;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.ServicePriority;
import org.bukkit.plugin.java.JavaPlugin;

import static org.bukkit.ChatColor.*;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.IllegalFormatException;
import java.util.List;
import java.util.Map;
import java.util.Scanner;
import java.util.UUID;
import java.util.logging.Level;
import java.util.regex.Matcher;

public final class KataPartyPlugin extends JavaPlugin implements IMessenger
{
	private void implementCommand(String name, CommandExecutor command)
	{
		getCommand(name).setExecutor(command);
		if(command instanceof Listener)
		{
			getServer().getPluginManager().registerEvents((Listener)command, this);
		}
	}
	private void implementCommand(String name, TabExecutor command)
	{
		implementCommand(name, (CommandExecutor)command);
		getCommand(name).setTabCompleter(command);
	}

	public static class SerializableDummy implements ConfigurationSerializable
	{
		private final Map<String, Object> data;

		@Override
		public Map<String, Object> serialize()
		{
			return data;
		}
		public SerializableDummy(Map<String, Object> d)
		{
			data = d;
		}
		public SerializableDummy()
		{
			data = new HashMap<>();
		}
	}

	private final File configFile = new File(getDataFolder(), "config.yml");
	private final File partiesFile = new File(getDataFolder(), "parties.yml");
	private final PartyFactory pfact = new PartyFactory();
	private final PartyFactory.MemberFactory mfact = pfact.new MemberFactory();
	private final PartyPvpManager pvp = new PartyPvpManager(this);
	private final PartyPotionFilter potions = new PartyPotionFilter(this);
	//private final PartyHealthManager shxp = new PartyHealthManager(this);
	private final PartyNametagManager nametags = new PartyNametagManager(this);
	private SmartConfig config;
	private Updater updater = null;
	@Override
	public void onEnable()
	{
		EventFilterServices.ChatFilter.depend(this, EventFilterServices.DependencyType.REQUIRED);
		EventFilterServices.PotionFilter.depend(this, EventFilterServices.DependencyType.REQUIRED);
		getServer().getServicesManager().register(KataPartyService.class, service, this, ServicePriority.Highest);

		ConfigurationSerialization.registerClass(SerializableDummy.class);
		ConfigurationSerialization.registerClass(Metadatable.class);
		ConfigurationSerialization.registerClass(Metadatable.EntrySerializer.class);
		ConfigurationSerialization.registerClass(PartySettings.class);
		ConfigurationSerialization.registerClass(PartySettings.MemberSettings.class);
		ConfigurationSerialization.registerClass(Party.class);
		ConfigurationSerialization.registerClass(Party.Member.class);
		ConfigurationSerialization.registerClass(PartySet.class);
		ConfigurationSerialization.registerClass(PartyHealthManager.HealthMeta.class);
		ConfigurationSerialization.registerClass(PartyXpManager.XpMeta.class);
		ConfigurationSerialization.registerClass(PartyBackCommand.BackMeta.class);
		ConfigurationSerialization.registerClass(PartyBackCommand.BackMeta.Info.class);
		ConfigurationSerialization.registerClass(PartyPardonCommand.PardonMeta.class);
		ConfigurationSerialization.registerClass(PartyMembersCommand.ScoreboardMeta.class);

		getPartySet().registerPartyFactory(PartySettings.class, pfact);
		getPartySet().registerPartyFactory(Party.class, pfact);
		getPartySet().registerMemberFactory(PartySettings.MemberSettings.class, mfact);
		getPartySet().registerMemberFactory(Party.Member.class, mfact);

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
		implementCommand("kpback", new PartyBackCommand(this));
		implementCommand("kppardon", new PartyPardonCommand(this));
		implementCommand("kpmembers", new PartyMembersCommand(this));

		getServer().getPluginManager().registerEvents(pvp, this);
		getServer().getPluginManager().registerEvents(potions, this);
		//getServer().getPluginManager().registerEvents(shxp, this);
		getServer().getPluginManager().registerEvents(filter, this);
		getServer().getPluginManager().registerEvents(tickets, this);
		getServer().getPluginManager().registerEvents(edl, this);

		boolean firstrun = !configFile.exists();
		try
		{
			getDataFolder().mkdirs();
			config = new SmartConfig(configFile, KataPartyPlugin.class);
			config.reload();
		}
		catch(IOException|InvalidConfigurationException e)
		{
			throw new RuntimeException(e);
		}
		reloadSettings(firstrun);

		if(partiesFile.exists())
		{
			Bukkit.getScheduler().runTask(this, new Runnable(){@Override public void run()
			{
				tellConsole("Loading parties...");
				final YamlConfiguration conf = new YamlConfiguration();
				boolean reload = true;
				String pf = "";
				try
				{
					pf = new Scanner(partiesFile).useDelimiter("\\Z").next();
				}
				catch(FileNotFoundException e)
				{
					getLogger().log(Level.SEVERE, "Could not find parties.yml: ", e);
					return;
				}
				while(reload) try
				{
					reload = false;
					conf.load(new StringReader(pf));
				}
				catch(IOException e)
				{
					getLogger().log(Level.SEVERE, "Could not load parties.yml: ", e);
					return;
				}
				catch(InvalidConfigurationException e)
				{
					reload = false;
					if(e.getCause() instanceof YAMLException)
					{
						Throwable t = e.getCause().getCause();
						if(t instanceof IllegalArgumentException && t.getMessage().startsWith("Specified class does not exist ('"))
						{
							final int t1 = t.getMessage().indexOf('\'');
							final int t2 = t.getMessage().lastIndexOf('\'');
							final String missingclass = t.getMessage().substring(t1+1, t2);
							final String dummyclass = SerializableDummy.class.getName();
							tellConsole(RED+missingclass+RESET+" could not be found, using dummy class instead");
							pf = pf.replaceAll("\\Q"+missingclass+"\\E", Matcher.quoteReplacement(dummyclass));
							reload = true;
						}
					}
					if(!reload)
					{
						getLogger().log(Level.SEVERE, "Could not load parties.yml: ", e);
						return;
					}
				}
				if(!conf.contains("party-set") && conf.isConfigurationSection("parties")) //compatibiity with pre-v1.3.0
				{
					tellConsole("Upgrading parties...");
					ConfigurationSection cs = conf.getConfigurationSection("parties");
					for(Map.Entry<String, Object> e : cs.getValues(false).entrySet())
					{
						if(!(e.getValue() instanceof ConfigurationSection))
						{
							continue;
						}
						ConfigurationSection ps = (ConfigurationSection)e.getValue();
						PartySettings p = new PartySettings();
						p.setName(e.getKey());
						if(ps.isBoolean("tp"))
						{
							p.setTp(ps.getBoolean("tp"));
						}
						if(ps.isBoolean("pvp"))
						{
							p.setPvp(ps.getBoolean("pvp"));
						}
						if(ps.isBoolean("visible"))
						{
							p.setVisible(ps.getBoolean("visible"));
						}
						if(ps.isBoolean("invite-only"))
						{
							p.setInviteOnly(ps.getBoolean("invite-only"));
						}
						if(ps.isBoolean("stickied"))
						{
							p.setSticky(ps.getBoolean("stickied"));
						}
						IParty party = getPartySet().newParty(null, p);
						if(ps.isList("inventory"))
						{
							List<?> items = ps.getList("inventory", new ArrayList<>());
							party.setInventory(true);
							for(int i = 0; i < items.size() && i < party.getInventory().getSize(); ++i)
							{
								if(items.get(i) instanceof ItemStack)
								{
									party.getInventory().setItem(i, (ItemStack)items.get(i));
								}
							}
						}
						if(ps.isConfigurationSection("members"))
						{
							for(Map.Entry<String, Object> me : ps.getConfigurationSection("members").getValues(false).entrySet())
							{
								if(!(me.getValue() instanceof ConfigurationSection))
								{
									continue;
								}
								ConfigurationSection ms = (ConfigurationSection)me.getValue();
								try
								{
									IParty.IMember m = party.newMember(new PartySettings.MemberSettings(UUID.fromString(me.getKey())), null);
									m.setRank(PartyRank.valueOf(ms.getString("rank")));
									if(ms.isBoolean("tp"))
									{
										m.setTp(ms.getBoolean("tp"));
									}
								}
								catch(IllegalArgumentException ex)
								{
								}
							}
						}
					}
					tellConsole("Done upgrading parties.");
				}
				if(conf.contains("player-metadata"))
				{
					ConfigurationSection pmetas = (ConfigurationSection)conf.get("player-metadata");
					for(Map.Entry<String, Object> e : pmetas.getValues(false).entrySet())
					{
						pmeta.put(UUID.fromString(e.getKey()), (IMetadatable)e.getValue());
					}
				}
				tellConsole("Done loading parties.");
			}});
		}

		parties.keepEmptyParties(!config.getBoolean("remove-empty-parties"));
	}
	@Override
	public void onDisable()
	{
		tellConsole("Saving parties...");
		YamlConfiguration conf = new YamlConfiguration();
		conf.set("party-set", parties);
		Map<String, Object> pmetas = new HashMap<>();
		for(Map.Entry<UUID, IMetadatable> e : pmeta.entrySet())
		{
			pmetas.put(e.getKey().toString(), e.getValue());
		}
		conf.set("player-metadata", pmetas);
		try
		{
			conf.save(partiesFile);
		}
		catch(IOException e)
		{
			throw new RuntimeException(e);
		}
		tellConsole("Done saving parties.");
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
			config.reload();
		}
		catch(IOException|InvalidConfigurationException e)
		{
			throw new RuntimeException(e);
		}
		if(isEnabled())
		{
			reloadSettings(false);
		}
	}
	@Override
	public void saveDefaultConfig()
	{
		reloadConfig();
	}

	private void reloadSettings(boolean firstrun)
	{
		if(!firstrun)
		{
			if(config.getBoolean("auto-updater"))
			{
				tellConsole("Automatic update downloading is "+GREEN+"enabled"+RESET+" in config");
				if(updater == null)
				{
					updater = new KataPartyUpdater(this, getFile(), Updater.UpdateType.DEFAULT);
				}
			}
			else
			{
				tellConsole("Automatic update downloading is "+RED+"disabled"+RESET+" in config");
			}
		}
		else
		{
			tellConsole(YELLOW+"Gravity's Updater may be disabled in the config.");
		}

		if(config.getBoolean("color-nametags"))
		{
			if(nametags.hasTagAPI())
			{
				tellConsole("Found TagAPI and using it!");
				nametags.start();
			}
			else
			{
				tellConsole(YELLOW+"This plugin requires TagAPI to be installed in order to use the \"color-nametags\" feature (disable in config)");
			}
		}
		else
		{
			nametags.stop();
		}

		parties.keepEmptyParties(!config.getBoolean("remove-empty-parties"));
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

	public static String chopInvTitle(String name)
	{
		if(name.length() >= 32)
		{
			return name.substring(0, 32);
		}
		return name;
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
				getLogger().log(Level.WARNING, "Error when using translation \""+name+"\": ", e);
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

	private final Map<UUID, IMetadatable> pmeta = new HashMap<>();
	public IMetadatable getPlayerMetadata(OfflinePlayer p)
	{
		if(!pmeta.containsKey(p.getUniqueId()))
		{
			pmeta.put(p.getUniqueId(), new Metadatable());
		}
		return pmeta.get(p.getUniqueId());
	}

	public static long getTick()
	{
		return System.currentTimeMillis()/50;
	}
}
