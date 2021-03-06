package com.lb_stuff.kataparty;

import com.lb_stuff.kataparty.api.IParty;

import org.bukkit.entity.AnimalTamer;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Projectile;
import org.bukkit.entity.ThrownPotion;
import org.bukkit.entity.Wolf;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityTargetEvent;
import org.bukkit.projectiles.ProjectileSource;

public class PartyPvpManager implements Listener
{
	private final KataPartyPlugin inst;
	public PartyPvpManager(KataPartyPlugin plugin)
	{
		inst = plugin;
	}

	@EventHandler(ignoreCancelled = true)
	public void onDamageBy(EntityDamageByEntityEvent e)
	{
		Entity damager = e.getDamager();
		if(damager instanceof Projectile && !(damager instanceof ThrownPotion))
		{
			Projectile proj = (Projectile)damager;
			ProjectileSource source = proj.getShooter();
			if(source instanceof Entity)
			{
				damager = (Entity)source;
			}
		}
		IParty.IMember a = inst.getPartySet().findMember(damager.getUniqueId());
		IParty.IMember b = inst.getPartySet().findMember(e.getEntity().getUniqueId());
		if(a != null && !a.getParty().canPvp())
		{
			if(b != null && a.getParty() == b.getParty())
			{
				e.setCancelled(true); //member attacks member
			}
			else if(e.getEntity() instanceof Wolf)
			{
				AnimalTamer owner = ((Wolf)e.getEntity()).getOwner();
				if(owner != null)
				{
					b = inst.getPartySet().findMember(owner.getUniqueId());
					if(a != b && a.getParty() == b.getParty())
					{
						e.setCancelled(true); //member attacks wolf of member
					}
				}
			}
		}
		else if(b != null && !b.getParty().canPvp() && e.getDamager() instanceof Wolf)
		{
			Wolf w = (Wolf)e.getDamager();
			AnimalTamer owner = w.getOwner();
			if(owner != null)
			{
				a = inst.getPartySet().findMember(owner.getUniqueId());
				if(a != null && a.getParty() == b.getParty())
				{
					e.setCancelled(true); //member's wolf attacks member
					w.setTarget(null);
				}
			}
		}
	}
	@EventHandler(ignoreCancelled = true)
	public void onTarget(EntityTargetEvent e)
	{
		if(e.getEntity() instanceof Wolf && (e.getReason() == EntityTargetEvent.TargetReason.TARGET_ATTACKED_OWNER || e.getReason() == EntityTargetEvent.TargetReason.OWNER_ATTACKED_TARGET))
		{
			AnimalTamer owner = ((Wolf)e.getEntity()).getOwner();
			if(owner != null)
			{
				IParty.IMember a = inst.getPartySet().findMember(owner.getUniqueId());
				IParty.IMember b = inst.getPartySet().findMember(e.getTarget().getUniqueId());
				if(a != null && b != null && a.getParty() == b.getParty() && !a.getParty().canPvp())
				{
					e.setCancelled(true); //member's wolf targets member
				}
			}
		}
	}
}
