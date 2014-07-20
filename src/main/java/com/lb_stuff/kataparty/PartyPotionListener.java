package com.lb_stuff.kataparty;

import com.lb_stuff.kataparty.api.IParty;

import org.bukkit.entity.Player;
import org.bukkit.event.Listener;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.entity.PotionSplashEvent;
import org.bukkit.projectiles.ProjectileSource;

public class PartyPotionListener implements Listener
{
	private final KataPartyPlugin inst;
	public PartyPotionListener(KataPartyPlugin plugin)
	{
		inst = plugin;
	}

	@EventHandler(priority = EventPriority.NORMAL, ignoreCancelled = true)
	public void onSplash(PotionSplashEvent e)
	{
		ProjectileSource ps = e.getPotion().getShooter();
		if(ps instanceof Player)
		{
			IParty.IMember thrower = inst.getParties().findMember(((Player)ps).getUniqueId());
//			if(thrower != null && thrower.getParty().arePotionsSmart())
			{
				//
			}
		}
	}
}
