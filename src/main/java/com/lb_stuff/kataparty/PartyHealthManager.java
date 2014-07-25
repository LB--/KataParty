package com.lb_stuff.kataparty;

import com.lb_stuff.kataparty.api.IParty;
import com.lb_stuff.kataparty.api.IMetadatable;
import com.lb_stuff.kataparty.api.event.PartyCreateEvent;
import com.lb_stuff.kataparty.api.event.PartySettingsChangeEvent;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.Listener;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageByBlockEvent;
import org.bukkit.event.entity.EntityRegainHealthEvent;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.event.player.PlayerRespawnEvent;
import org.bukkit.configuration.serialization.ConfigurationSerializable;

import java.util.Set;
import java.util.Iterator;
import java.util.Map;
import java.util.HashMap;

public class PartyHealthManager implements Listener
{
	private final KataPartyPlugin inst;
	public PartyHealthManager(KataPartyPlugin plugin)
	{
		inst = plugin;
	}

	private Set<IParty.IMember> contributors(IParty party)
	{
		Set<IParty.IMember> mems = party.getMembersAlive();
		Iterator<IParty.IMember> it = mems.iterator();
		while(it.hasNext())
		{
			if(!Bukkit.getPlayer(it.next().getUuid()).hasPermission("KataParty.shared-health.contribute"))
			{
				it.remove();
			}
		}
		return mems;
	}
	private void update(IParty party)
	{
		HealthMeta hm = HealthMeta.getFrom(party);
		Set<IParty.IMember> contribs = contributors(party);
		for(IParty.IMember m : party.getMembersAlive())
		{
			Player p = Bukkit.getPlayer(m.getUuid());
			if(hm != null && contribs.contains(m))
			{
				p.setMaxHealth(20.0*contribs.size());
				p.setHealth(p.getMaxHealth()*hm.percent);
			}
			else
			{
				p.resetMaxHealth();
			}
		}
	}

	@EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
	public void onPlayerJoin(PlayerJoinEvent e)
	{
		final Player p = e.getPlayer();
		IParty.IMember m = inst.getPartySet().findMember(p.getUniqueId());
		if(m != null)
		{
			update(m.getParty());
		}
	}
	@EventHandler(priority = EventPriority.MONITOR)
	public void onPlayerLeave(PlayerQuitEvent e)
	{
		IParty.IMember m = inst.getPartySet().findMember(e.getPlayer().getUniqueId());
		if(m != null)
		{
			update(m.getParty());
		}
	}
	@EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
	public void onDamage(EntityDamageEvent e)
	{
		if(e.getEntity() instanceof Player)
		{
			IParty.IMember m = inst.getPartySet().findMember(e.getEntity().getUniqueId());
			if(m != null)
			{
				HealthMeta hm = HealthMeta.getFrom(m.getParty());
				if(hm != null)
				{
					Player p = (Player)e.getEntity();
					hm.percent -= e.getDamage()/p.getMaxHealth();
				}
				update(m.getParty());
			}
		}
	}
	@EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
	public void onDamageByEntity(EntityDamageByEntityEvent e)
	{
		onDamage(e);
	}
	@EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
	public void onDamageByBlock(EntityDamageByBlockEvent e)
	{
		onDamage(e);
	}
	@EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
	public void onHeal(EntityRegainHealthEvent e)
	{
		if(e.getEntity() instanceof Player)
		{
			IParty.IMember m = inst.getPartySet().findMember(e.getEntity().getUniqueId());
			if(m != null)
			{
				HealthMeta hm = HealthMeta.getFrom(m.getParty());
				if(hm != null)
				{
					Player p = (Player)e.getEntity();
					hm.percent += e.getAmount()/p.getMaxHealth();
				}
				update(m.getParty());
			}
		}
	}
	@EventHandler(priority = EventPriority.MONITOR)
	public void onDeath(EntityDeathEvent e)
	{
		if(e.getEntity() instanceof Player)
		{
			IParty.IMember m = inst.getPartySet().findMember(e.getEntity().getUniqueId());
			if(m != null)
			{
				//...
			}
		}
	}
	@EventHandler(priority = EventPriority.MONITOR)
	public void onRespawn(PlayerRespawnEvent e)
	{
		IParty.IMember m = inst.getPartySet().findMember(e.getPlayer().getUniqueId());
		if(m != null)
		{
			update(m.getParty());
		}
	}

	@EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
	public void onPartyCreate(PartyCreateEvent e)
	{
		if(e.getSettings().isHealthShared())
		{
			HealthMeta.addTo(e.getSettings());
		}
	}
	@EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
	public void onSettingsChange(PartySettingsChangeEvent e)
	{
		if(!e.getParty().isHealthShared() && e.getChanges().isHealthShared())
		{
			HealthMeta.addTo(e.getParty());
		}
		if(e.getParty().isHealthShared() && !e.getChanges().isHealthShared())
		{
			HealthMeta.removeFrom(e.getParty());
		}
	}

	public static class HealthMeta implements ConfigurationSerializable
	{
		@Override
		public Map<String, Object> serialize()
		{
			Map<String, Object> data = new HashMap<>();
			data.put("percent", percent);
			return data;
		}
		public HealthMeta(Map<String, Object> data)
		{
			percent = (Double)data.get("percent");
		}

		public double percent = 1.0;
		public HealthMeta()
		{
		}

		public static void addTo(IMetadatable m)
		{
			m.set(HealthMeta.class, new HealthMeta());
		}
		public static HealthMeta getFrom(IMetadatable m)
		{
			return (HealthMeta)m.get(HealthMeta.class);
		}
		public static void removeFrom(IMetadatable m)
		{
			m.set(HealthMeta.class, null);
		}
	}
}
