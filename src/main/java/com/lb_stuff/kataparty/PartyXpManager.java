package com.lb_stuff.kataparty;

import com.lb_stuff.kataparty.api.IParty;
import com.lb_stuff.kataparty.api.IPartySettings;
import com.lb_stuff.kataparty.api.KataPartyService;
import com.lb_stuff.kataparty.api.event.PartyCreateEvent;
import com.lb_stuff.kataparty.api.event.PartyDisbandEvent;
import com.lb_stuff.kataparty.api.event.PartyMemberLeaveEvent;

import org.bukkit.Bukkit;
import org.bukkit.configuration.serialization.ConfigurationSerializable;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerExpChangeEvent;
import org.bukkit.event.player.PlayerQuitEvent;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

public class PartyXpManager implements Listener
{
	private final KataPartyPlugin inst;
	public PartyXpManager(KataPartyPlugin plugin)
	{
		inst = plugin;
	}

	private void distribute(IParty p)
	{
		Set<Player> online = KataPartyService.getOnlinePlayers(p.getMembersOnline());
		for(Iterator<Player> it = online.iterator(); it.hasNext(); )
		{
			if(!it.next().hasPermission("KataParty.shared-xp.benefit"))
			{
				it.remove();
			}
		}
		XpMeta xp = XpMeta.getFrom(p);
		while(xp.getBuildup() >= online.size())
		{
			xp.setBuildup(xp.getBuildup() - online.size());
			for(Player player : online)
			{
				player.giveExp(1);
			}
		}
	}
	private void scheduleDistribution(final IParty p)
	{
		Bukkit.getScheduler().runTask(inst, new Runnable(){@Override public void run()
		{
			distribute(p);
		}});
	}

	@EventHandler(priority = EventPriority.HIGHEST)
	public void onXp(PlayerExpChangeEvent e)
	{
		if(!inst.getConfig().getBoolean("share-xp-gain"))
		{
			return;
		}
		if(!e.getPlayer().hasPermission("KataParty.shared-xp.contribute"))
		{
			return;
		}
		if(e.getAmount() < 1)
		{
			return;
		}

		IParty.IMember m = inst.getPartySet().findMember(e.getPlayer().getUniqueId());
		if(m != null)
		{
			XpMeta xp = XpMeta.getFrom(m.getParty());
			if(xp == null)
			{
				XpMeta.addTo(m.getParty());
				xp = XpMeta.getFrom(m.getParty());
			}
			xp.setBuildup(xp.getBuildup()+e.getAmount());
			e.setAmount(0);
			distribute(m.getParty());
		}
	}
	@EventHandler(priority = EventPriority.MONITOR)
	public void onPartyCreate(PartyCreateEvent e)
	{
		XpMeta.addTo(e.getSettings());
	}
	@EventHandler(priority = EventPriority.MONITOR)
	public void onPartyDisband(PartyDisbandEvent e)
	{
		if(e.getCloser() != null)
		{
			e.getCloser().giveExp(XpMeta.getFrom(e.getParty()).getBuildup());
		}
	}
	@EventHandler(priority = EventPriority.MONITOR)
	public void onMemberLeave(PartyMemberLeaveEvent e)
	{
		scheduleDistribution(e.getMember().getParty());
	}
	@EventHandler(priority = EventPriority.MONITOR)
	public void onPlayerQuit(PlayerQuitEvent e)
	{
		IParty.IMember m = inst.getPartySet().findMember(e.getPlayer().getUniqueId());
		if(m != null)
		{
			scheduleDistribution(m.getParty());
		}
	}

	public static class XpMeta implements ConfigurationSerializable
	{
		@Override
		public Map<String, Object> serialize()
		{
			Map<String, Object> data = new HashMap<>();
			data.put("buildup", getBuildup());
			return data;
		}
		public XpMeta(Map<String, Object> data)
		{
			buildup = (Integer)data.get("buildup");
		}

		private int buildup;
		public XpMeta()
		{
		}

		public static void addTo(IPartySettings m)
		{
			m.set(XpMeta.class, new XpMeta());
		}
		public static XpMeta getFrom(IPartySettings m)
		{
			return (XpMeta)m.get(XpMeta.class);
		}
		public static void removeFrom(IPartySettings m)
		{
			m.set(XpMeta.class, null);
		}

		public int getBuildup()
		{
			return buildup;
		}
		public void setBuildup(int v)
		{
			buildup = v;
			if(buildup <= 0)
			{
				buildup = 0;
			}
		}
	}
}
