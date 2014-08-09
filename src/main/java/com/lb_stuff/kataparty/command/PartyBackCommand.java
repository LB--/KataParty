package com.lb_stuff.kataparty.command;

import com.lb_stuff.kataparty.KataPartyPlugin;
import com.lb_stuff.kataparty.api.IMetadatable;
import com.lb_stuff.kataparty.api.IParty;
import com.lb_stuff.kataparty.api.IPartySettings.IMemberSettings;
import com.lb_stuff.kataparty.api.KataPartyService;
import com.lb_stuff.kataparty.api.event.PartyDisbandEvent;
import com.lb_stuff.kataparty.api.event.PartyMemberJoinEvent;
import com.lb_stuff.kataparty.api.event.PartyMemberLeaveEvent;

import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.serialization.ConfigurationSerializable;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class PartyBackCommand extends TabbablePartyCommand implements Listener
{
	public PartyBackCommand(KataPartyPlugin plugin)
	{
		super(plugin);
	}

	@EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
	public void onMemberLeave(PartyMemberLeaveEvent e)
	{
		BackMeta m = BackMeta.getFrom(inst.getServer().getOfflinePlayer(e.getMember().getUuid()));
		switch(e.getReason())
		{
			case KICKED:
			case DISBAND:
			{
				m.setInfo(e.getMember().getParty(), null);
			} break;
			default:
			{
				m.setInfo(e.getMember().getParty(), new BackMeta.Info(e.getMember()));
			} break;
		}
	}
	@EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
	public void onMemberJoin(PartyMemberJoinEvent e)
	{
		BackMeta m = BackMeta.getFrom(inst.getServer().getOfflinePlayer(e.getApplicant().getUuid()));
		m.setInfo(e.getParty(), null);
	}
	@EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
	public void onPartyDisband(PartyDisbandEvent e)
	{
		for(OfflinePlayer p : KataPartyService.getAllPlayers(e.getParty().getMembers()))
		{
			BackMeta m = BackMeta.getFrom(p);
			m.setInfo(e.getParty(), null);
		}
	}

	private long getGracePeriod(Player p)
	{
		long period = 0;
		for(String perm : ((Map<String, Object>)inst.getConfig().get("back-command-grace-periods")).keySet())
		{
			if(p.hasPermission("KataParty.back.grace-periods."+perm))
			{
				Long l = inst.getConfig().getLong("back-command-grace-periods."+perm);
				if(l == -1)
				{
					return Long.MAX_VALUE;
				}
				else if(l > period)
				{
					period = l;
				}
			}
		}
		return period;
	}

	@Override
	public List<String> onTabComplete(CommandSender sender, Command cmd, String label, String[] args)
	{
		List<String> ret = new ArrayList<>();
		if(sender instanceof Player)
		{
			Player player = (Player)sender;
			if(args.length == 1)
			{
				for(IParty p : BackMeta.getFrom(player).getParties())
				{
					if(p.getName().toLowerCase().startsWith(args[args.length-1].toLowerCase()))
					{
						ret.add(p.getName());
					}
				}
			}
		}
		return ret;
	}

	@Override
	public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args)
	{
		if(args.length > 1)
		{
			return false;
		}
		if(sender instanceof Player)
		{
			Player player = (Player)sender;
			BackMeta m = BackMeta.getFrom(player);
			IParty party = null;
			if(args.length == 1)
			{
				party = inst.getPartySet().findParty(args[0]);
			}
			else
			{
				long tick = Long.MIN_VALUE;
				for(IParty p : m.getParties())
				{
					long t = m.getInfo(p).getTick();
					if(t > tick)
					{
						tick = t;
						party = p;
					}
				}
			}
			if(party == null || !m.getParties().contains(party))
			{
				if(args.length == 1)
				{
					inst.tellMessage(player, "never-in-party", args[0]);
				}
				else
				{
					inst.tellMessage(player, "never-in-any-party");
				}
			}
			else
			{
				final long delta = KataPartyPlugin.getTick() - m.getInfo(party).getTick();
				if(delta > getGracePeriod(player))
				{
					inst.tellMessage(player, "grace-period-ended", party.getName());
					m.setInfo(party, null);
				}
				else
				{
					party.newMember(m.getInfo(party).getSettings(), PartyMemberJoinEvent.Reason.VOLUNTARY);
				}
			}
			return true;
		}
		return false;
	}

	public static class BackMeta implements ConfigurationSerializable
	{
		@Override
		public Map<String, Object> serialize()
		{
			Map<String, Object> data = new HashMap<>();
			for(Map.Entry<IParty, Info> e : leaves.entrySet())
			{
				if(KataPartyPlugin.getInst().getPartySet().contains(e.getKey()))
				{
					data.put(e.getKey().getName(), e.getValue());
				}
			}
			return data;
		}
		public BackMeta(final Map<String, Object> data)
		{
			Bukkit.getScheduler().runTask(KataPartyPlugin.getInst(), new Runnable(){@Override public void run()
			{
				for(Map.Entry<String, Object> e : data.entrySet())
				{
					leaves.put(KataPartyPlugin.getInst().getPartySet().findParty(e.getKey()), (Info)e.getValue());
				}
			}});
		}

		private Map<IParty, Info> leaves = new HashMap<>();
		public BackMeta()
		{
		}

		public static void addTo(OfflinePlayer p)
		{
			KataPartyPlugin.getInst().getPlayerMetadata(p).set(BackMeta.class, new BackMeta());
		}
		public static BackMeta getFrom(OfflinePlayer p)
		{
			IMetadatable m = KataPartyPlugin.getInst().getPlayerMetadata(p);
			if(m.get(BackMeta.class) == null)
			{
				addTo(p);
			}
			return (BackMeta)m.get(BackMeta.class);
		}
		public static void removeFrom(OfflinePlayer p)
		{
			KataPartyPlugin.getInst().getPlayerMetadata(p).set(BackMeta.class, null);
		}

		public Set<IParty> getParties()
		{
			return leaves.keySet();
		}
		public Info getInfo(IParty p)
		{
			return leaves.get(p);
		}
		public void setInfo(IParty p, Info i)
		{
			if(i == null)
			{
				leaves.remove(p);
			}
			else
			{
				leaves.put(p, i);
			}
		}

		public static class Info implements ConfigurationSerializable
		{
			@Override
			public Map<String, Object> serialize()
			{
				Map<String, Object> data = new HashMap<>();
				data.put("tick", tick);
				data.put("settings", settings);
				return data;
			}
			public Info(Map<String, Object> data)
			{
				tick = (Long)data.get("tick");
				settings = (IMemberSettings)data.get("settings");
			}

			private long tick = KataPartyPlugin.getTick();
			private IMemberSettings settings;
			public Info(IMemberSettings s)
			{
				settings = s;
			}

			public long getTick()
			{
				return tick;
			}
			public void setTick(long t)
			{
				tick = t;
			}

			public IMemberSettings getSettings()
			{
				return settings;
			}
			public void setSettings(IMemberSettings s)
			{
				settings = s;
			}
		}
	}
}
