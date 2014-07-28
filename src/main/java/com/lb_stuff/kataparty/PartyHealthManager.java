package com.lb_stuff.kataparty;

import com.lb_stuff.kataparty.api.IHealthMeta;
import com.lb_stuff.kataparty.api.IParty;
import com.lb_stuff.kataparty.api.IPartySettings;
import com.lb_stuff.kataparty.api.IPartySettings.IMemberSettings;
import com.lb_stuff.kataparty.api.event.PartyCreateEvent;
import com.lb_stuff.kataparty.api.event.PartyMemberJoinEvent;
import com.lb_stuff.kataparty.api.event.PartyMemberLeaveEvent;
import com.lb_stuff.kataparty.api.event.PartySettingsChangeEvent;

import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByBlockEvent;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.event.entity.EntityRegainHealthEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.event.player.PlayerRespawnEvent;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

public class PartyHealthManager implements Listener
{
	private final KataPartyPlugin inst;
	public PartyHealthManager(KataPartyPlugin plugin)
	{
		inst = plugin;
	}

	private static boolean canContribute(IMemberSettings ms)
	{
		OfflinePlayer offp = Bukkit.getOfflinePlayer(ms.getUuid());
		if(offp.isOnline())
		{
			Player onp = offp.getPlayer();
			if(onp.hasPermission("KataParty.shared-health.contribute"))
			{
				return true;
			}
		}
		return false;
	}
	private static Set<IParty.IMember> contributors(IParty party)
	{
		Set<IParty.IMember> mems = party.getMembersAlive();
		Iterator<IParty.IMember> it = mems.iterator();
		while(it.hasNext())
		{
			IParty.IMember m = it.next();
			if(!canContribute(m))
			{
				it.remove();
			}
		}
		return mems;
	}
	private static void update(IParty party)
	{
		IHealthMeta hm = HealthMeta.getFrom(party);
		Set<IParty.IMember> contribs = contributors(party);
		for(IParty.IMember m : party.getMembersAlive())
		{
			Player p = Bukkit.getPlayer(m.getUuid());
			if(hm != null && contribs.contains(m))
			{
				p.setMaxHealth(20.0*contribs.size());
				p.setHealth(p.getMaxHealth()*hm.getPercent());
			}
			else
			{
				p.resetMaxHealth();
			}
		}
	}
	private void scheduleUpdate(final IParty p)
	{
		Bukkit.getScheduler().runTask(inst, new Runnable(){@Override public void run()
		{
			update(p);
		}});
	}
	private void scheduleUpdate(final IParty.IMember m, final double oldhealth)
	{
		if(!contributors(m.getParty()).contains(m))
		{
			return;
		}
		Bukkit.getScheduler().runTask(inst, new Runnable(){@Override public void run()
		{
			Player p = Bukkit.getPlayer(m.getUuid());
			double change = p.getHealth() - oldhealth;
			IHealthMeta hm = HealthMeta.getFrom(m.getParty());
			if(hm != null)
			{
				hm.setPercent(hm.getPercent() + change/p.getMaxHealth());
				update(m.getParty());
			}
		}});
	}
	private void scheduleUpdate(final IParty p, final double change)
	{
		Bukkit.getScheduler().runTask(inst, new Runnable(){@Override public void run()
		{
			IHealthMeta hm = HealthMeta.getFrom(p);
			if(hm != null)
			{
				hm.setPercent(hm.getPercent() + change);
				update(p);
			}
		}});
	}

	@EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
	public void onPlayerJoin(PlayerJoinEvent e)
	{
		final Player p = e.getPlayer();
		IParty.IMember m = inst.getPartySet().findMember(p.getUniqueId());
		if(m != null && contributors(m.getParty()).contains(m))
		{
			scheduleUpdate(m.getParty(), p.getHealth()/p.getMaxHealth());
		}
	}
	@EventHandler(priority = EventPriority.MONITOR)
	public void onPlayerLeave(PlayerQuitEvent e)
	{
		final Player p = e.getPlayer();
		IParty.IMember m = inst.getPartySet().findMember(p.getUniqueId());
		if(m != null)
		{
			Set<IParty.IMember> contribs = contributors(m.getParty());
			p.resetMaxHealth();
			if(contribs.contains(m))
			{
				IHealthMeta hm = HealthMeta.getFrom(m.getParty());
				if(hm != null)
				{
					p.setHealth(p.getMaxHealth()*(hm.getPercent()/contribs.size()));
				}
			}
			scheduleUpdate(m.getParty());
		}
	}
	@EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
	public void onDamage(EntityDamageEvent e)
	{
		if(e.getEntity() instanceof Player)
		{
			IParty.IMember m = inst.getPartySet().findMember(e.getEntity().getUniqueId());
			if(m != null && contributors(m.getParty()).contains(m))
			{
				Player p = (Player)e.getEntity();
				scheduleUpdate(m, p.getHealth());
			}
		}
	}
	@EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
	public void onDamageByEntity(EntityDamageByEntityEvent e)
	{
//		onDamage(e);
	}
	@EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
	public void onDamageByBlock(EntityDamageByBlockEvent e)
	{
//		onDamage(e);
	}
	@EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
	public void onHeal(EntityRegainHealthEvent e)
	{
		if(e.getEntity() instanceof Player)
		{
			IParty.IMember m = inst.getPartySet().findMember(e.getEntity().getUniqueId());
			if(m != null && contributors(m.getParty()).contains(m))
			{
				Player p = (Player)e.getEntity();
				scheduleUpdate(m, p.getHealth());
			}
		}
	}
	@EventHandler(priority = EventPriority.MONITOR)
	public void onDeath(EntityDeathEvent e)
	{
		if(e.getEntity() instanceof Player)
		{
			final IParty.IMember m = inst.getPartySet().findMember(e.getEntity().getUniqueId());
			if(m != null && canContribute(m))
			{
				final Player p = (Player)e.getEntity();
				Bukkit.getScheduler().runTask(inst, new Runnable(){@Override public void run()
				{
					p.resetMaxHealth();
					for(IParty.IMember mem : m.getParty().getMembersOnline())
					{
						if(canContribute(mem))
						{
							Player mp = Bukkit.getPlayer(mem.getUuid());
							mp.resetMaxHealth();
							if(!mp.isDead())
							{
								mp.setHealth(0.0);
							}
						}
					}
					HealthMeta.getFrom(m.getParty()).setPercent(1.0);
					//update(m.getParty());
				}});
			}
		}
	}
	@EventHandler(priority = EventPriority.MONITOR)
	public void onRespawn(PlayerRespawnEvent e)
	{
		final Player p = e.getPlayer();
		final IParty.IMember m = inst.getPartySet().findMember(p.getUniqueId());
		if(m != null)
		{
			p.resetMaxHealth();
			Bukkit.getScheduler().runTask(inst, new Runnable(){@Override public void run()
			{
				scheduleUpdate(m, 0.0);
			}});
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
			scheduleUpdate(e.getParty());
		}
		if(e.getParty().isHealthShared() && !e.getChanges().isHealthShared())
		{
			HealthMeta.removeFrom(e.getParty());
		}
	}
	@EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
	public void onMemberJoin(PartyMemberJoinEvent e)
	{
		OfflinePlayer offp = Bukkit.getOfflinePlayer(e.getApplicant().getUuid());
		Player onp = offp.getPlayer();
		if(offp.isOnline() && canContribute(e.getApplicant()))
		{
			scheduleUpdate(e.getParty(), onp.getHealth()/onp.getMaxHealth());
		}
	}
	@EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
	public void onMemberLeave(PartyMemberLeaveEvent e)
	{
		OfflinePlayer offp = Bukkit.getOfflinePlayer(e.getMember().getUuid());
		if(offp.isOnline())
		{
			Player onp = offp.getPlayer();
			onp.resetMaxHealth();
			IHealthMeta hm = HealthMeta.getFrom(e.getMember().getParty());
			Set<IParty.IMember> contribs = contributors(e.getMember().getParty());
			if(hm != null && contribs.contains(e.getMember()))
			{
				onp.setHealth(onp.getMaxHealth()*(hm.getPercent()/contribs.size()));
			}
			scheduleUpdate(e.getMember().getParty());
		}
	}

	public static class HealthMeta extends IHealthMeta
	{
		@Override
		public Map<String, Object> serialize()
		{
			Map<String, Object> data = new HashMap<>();
			data.put("percent", getPercent());
			return data;
		}
		public HealthMeta(Map<String, Object> data)
		{
			percent = (Double)data.get("percent");
		}

		private double percent = 1.0;
		public HealthMeta()
		{
		}

		public static void addTo(IPartySettings m)
		{
			m.set(IHealthMeta.class, new HealthMeta());
		}
		public static void removeFrom(IPartySettings m)
		{
			m.set(IHealthMeta.class, null);
		}

		@Override
		public double getPercent()
		{
			return percent;
		}
		@Override
		public void setPercent(double v)
		{
			KataPartyPlugin.getInst().getLogger().info(String.format("#### %1$.3f -> %2$.4f", percent, v));
			percent = v;
			if(percent <= 0.0)
			{
				percent = 0.0;
			}
			else if(percent >= 1.0)
			{
				percent = 1.0;
			}
		}
	}
}
