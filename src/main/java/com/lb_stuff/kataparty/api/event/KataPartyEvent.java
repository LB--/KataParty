package com.lb_stuff.kataparty.api.event;

import com.lb_stuff.kataparty.api.KataPartyService;

import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.bukkit.Bukkit;

public class KataPartyEvent extends Event
{
	public final KataPartyService getService()
	{
		return Bukkit.getServicesManager().getRegistration(KataPartyService.class).getProvider();
	}

	private static final HandlerList handlers = new HandlerList();
	@Override
	public HandlerList getHandlers()
	{
		return handlers;
	}
	public static HandlerList getHandlerList()
	{
		return handlers;
	}
}
