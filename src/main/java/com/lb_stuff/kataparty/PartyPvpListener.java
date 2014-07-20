package com.lb_stuff.kataparty;

import org.bukkit.event.Listener;
import org.bukkit.event.EventHandler;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityTargetEvent;
import org.bukkit.entity.AnimalTamer;
import org.bukkit.entity.Wolf;

public class PartyPvpListener implements Listener
{
	private final KataPartyPlugin inst;
	public PartyPvpListener(KataPartyPlugin plugin)
	{
		inst = plugin;
	}

	@EventHandler(ignoreCancelled = true)
	public void onDamageBy(EntityDamageByEntityEvent e)
	{
		Party.Member a = inst.getParties().findMember(e.getDamager().getUniqueId());
		Party.Member b = inst.getParties().findMember(e.getEntity().getUniqueId());
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
					b = inst.getParties().findMember(owner.getUniqueId());
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
				a = inst.getParties().findMember(owner.getUniqueId());
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
				Party.Member a = inst.getParties().findMember(owner.getUniqueId());
				Party.Member b = inst.getParties().findMember(e.getTarget().getUniqueId());
				if(a != null && b != null && a.getParty() == b.getParty() && !a.getParty().canPvp())
				{
					e.setCancelled(true); //member's wolf targets member
				}
			}
		}
	}
}
