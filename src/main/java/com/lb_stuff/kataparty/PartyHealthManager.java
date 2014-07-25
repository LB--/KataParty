package com.lb_stuff.kataparty;

import com.lb_stuff.kataparty.api.IParty;

import org.bukkit.entity.Player;
import org.bukkit.event.Listener;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityRegainHealthEvent;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.event.player.PlayerRespawnEvent;

public class PartyHealthManager implements Listener
{
	private final KataPartyPlugin inst;
	public PartyHealthManager(KataPartyPlugin plugin)
	{
		inst = plugin;
	}

	@EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
	public void onPlayerJoin(PlayerJoinEvent e)
	{
		final Player p = e.getPlayer();
		IParty.IMember m = inst.getPartySet().findMember(p.getUniqueId());
		if(m != null)
		{
			//TODO: shared health stuff
		}
	}
	@EventHandler(priority = EventPriority.MONITOR)
	public void onPlayerLeave(PlayerQuitEvent e)
	{
		IParty.IMember m = inst.getPartySet().findMember(e.getPlayer().getUniqueId());
		if(m != null)
		{
			//TODO: shared health stuff
		}
	}
	@EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
	public void onDamage(EntityDamageEvent e)
	{
		if(e.getEntity() instanceof Player)
		{
			IParty.IMember m = inst.getPartySet().findMember(e.getEntity().getUniqueId());
//			if(m != null && m.getParty().getHealth() != null)
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
			IParty.IMember m = inst.getPartySet().findMember(e.getEntity().getUniqueId());
//			if(m != null && m.getParty().getHealth() != null)
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
			IParty.IMember m = inst.getPartySet().findMember(e.getEntity().getUniqueId());
//			if(m != null && m.getParty().getHealth() != null)
			{
				//TODO: shared health stuff
			}
		}
	}
	@EventHandler(priority = EventPriority.MONITOR)
	public void onRespawn(PlayerRespawnEvent e)
	{
		IParty.IMember m = inst.getPartySet().findMember(e.getPlayer().getUniqueId());
//		if(m != null && m.getParty().getHealth() != null)
		{
			//TODO: shared health stuff
		}
	}
}
