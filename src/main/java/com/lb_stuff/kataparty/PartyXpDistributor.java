package com.lb_stuff.kataparty;

import com.lb_stuff.kataparty.api.IParty;

import org.bukkit.entity.Player;
import org.bukkit.event.Listener;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.player.PlayerExpChangeEvent;

public class PartyXpDistributor implements Listener
{
	private final KataPartyPlugin inst;
	public PartyXpDistributor(KataPartyPlugin plugin)
	{
		inst = plugin;
	}

	@EventHandler(priority = EventPriority.HIGHEST)
	public void onXp(PlayerExpChangeEvent e)
	{
		IParty.IMember m = inst.getPartySet().findMember(e.getPlayer().getUniqueId());
//		if(m != null && m.getParty().getHealth() != null)
		{
			//TODO: shared health stuff
		}
	}
}
