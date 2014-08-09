package com.lb_stuff.kataparty.command;

import com.lb_stuff.kataparty.KataPartyPlugin;
import com.lb_stuff.kataparty.PartySettings.MemberSettings;
import com.lb_stuff.kataparty.api.IParty;
import com.lb_stuff.kataparty.api.IPartySettings;
import com.lb_stuff.kataparty.api.event.PartyMemberJoinEvent;
import com.lb_stuff.kataparty.api.event.PartyMemberLeaveEvent;

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
import java.util.UUID;

public class PartyPardonCommand extends TabbablePartyCommand implements Listener
{
	public PartyPardonCommand(KataPartyPlugin plugin)
	{
		super(plugin);
	}

	@EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
	public void onMemberLeave(PartyMemberLeaveEvent e)
	{
		PardonMeta m = PardonMeta.getFrom(e.getMember().getParty());
		switch(e.getReason())
		{
			case KICKED:
			{
				m.setKickTick(e.getMember().getUuid(), KataPartyPlugin.getTick());
			} break;
			default:
			{
				m.setKickTick(e.getMember().getUuid(), null);
			} break;
		}
	}

	@Override
	public List<String> onTabComplete(CommandSender sender, Command cmd, String label, String[] args)
	{
		List<String> ret = new ArrayList<>();
		if(args.length == 1)
		{
			for(Player p : inst.getServer().getOnlinePlayers())
			{
				if((!(sender instanceof Player) || ((Player)sender).canSee(p)) && p.getName().startsWith(args[0].toLowerCase()))
				{
					ret.add(p.getName());
				}
			}
		}
		else if(args.length == 2)
		{
			for(IParty p : inst.getPartySet())
			{
				if((sender.hasPermission("KataParty.admin") || p.isVisible()) && p.getName().startsWith(args[0].toLowerCase()))
				{
					ret.add(p.getName());
				}
			}
		}
		return ret;
	}

	@Override
	public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args)
	{
		if(sender instanceof Player)
		{
			Player player = (Player)sender;
			if(args.length >= 1)
			{
				//
				return true;
			}
		}
		return false;
	}

	public static class PardonMeta implements ConfigurationSerializable
	{
		@Override
		public Map<String, Object> serialize()
		{
			Map<String, Object> data = new HashMap<>();
			for(Map.Entry<UUID, Long> e : leaves.entrySet())
			{
				data.put(e.getKey().toString(), e.getValue());
			}
			return data;
		}
		public PardonMeta(Map<String, Object> data)
		{
			for(Map.Entry<String, Object> e : data.entrySet())
			{
				leaves.put(UUID.fromString(e.getKey()), (Long)e.getValue());
			}
		}

		private Map<UUID, Long> leaves = new HashMap<>();
		public PardonMeta()
		{
		}

		public static void addTo(IPartySettings m)
		{
			m.set(PardonMeta.class, new PardonMeta());
		}
		public static PardonMeta getFrom(IPartySettings m)
		{
			if(m.get(PardonMeta.class) == null)
			{
				addTo(m);
			}
			return (PardonMeta)m.get(PardonMeta.class);
		}
		public static void removeFrom(IPartySettings m)
		{
			m.set(PardonMeta.class, null);
		}

		public Long getKickTick(UUID uuid)
		{
			return leaves.get(uuid);
		}
		public void setKickTick(UUID uuid, Long tick)
		{
			if(tick == null)
			{
				leaves.remove(uuid);
			}
			else
			{
				leaves.put(uuid, tick);
			}
		}
	}
}
