package com.lb_stuff.kataparty;

import com.lb_stuff.command.PluginInfoCommand;
import com.lb_stuff.command.PluginReloadCommand;
import com.lb_stuff.kataparty.command.*;
import com.lb_stuff.kataparty.config.MainConfig;
import com.lb_stuff.kataparty.api.Messenger;
import com.lb_stuff.kataparty.api.KataPartyService;
import com.lb_stuff.kataparty.api.IParty;

import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.TabExecutor;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;
import static org.bukkit.ChatColor.*;
import org.bukkit.plugin.ServicePriority;

import java.util.*;
import java.io.*;

public final class KataPartyPlugin extends JavaPlugin implements Messenger
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
	private MainConfig config;
	private final PartyPvpListener pvp = new PartyPvpListener(this);
	private final PartyPotionListener potions = new PartyPotionListener(this);
	private final PartyHealthXpListener shxp = new PartyHealthXpListener(this);
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
				Party p = addParty(e.getKey(), null);
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
				if(ps.contains("stickied"))
				{
					p.setSticky(ps.getBoolean("stickied"));
				}
				else
				{
					p.setSticky(false);
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

		getServer().getServicesManager().register(KataPartyService.class, service, this, ServicePriority.Highest);
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
		for(IParty p : getParties())
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
			ps.set("stickied", p.isSticky());
//			if(p.getHealth() == null)
			{
				ps.set("health", false);
			}
//			else
			{
//				ps.set("health", p.getHealth());
			}
			ps.set("potions", false/*p.arePotionsSmart()*/);
			ConfigurationSection pms = ps.createSection("members");
			for(IParty.IMember m : p)
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
	public Party addParty(String pname, Player creator)
	{
		if(getParties().findParty(pname) == null)
		{
			Party p = new Party(getParties(), pname);
			getParties().add(p);
			if(creator != null)
			{
				p.addMember(creator.getUniqueId()).setRank(Party.Rank.ADMIN);
				getParties().getSettings(creator.getUniqueId()).setPref(getFilter().getDefaultFilterPref("on-party-create"));
			}
			return p;
		}
		return null;
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
}
