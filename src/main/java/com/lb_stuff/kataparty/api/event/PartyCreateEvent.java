package com.lb_stuff.kataparty.api.event;

import com.lb_stuff.kataparty.api.IPartySettings;

import org.bukkit.event.HandlerList;
import org.bukkit.entity.Player;

public class PartyCreateEvent extends CancellableKataPartyEvent
{
	private final Player player;
	private final IPartySettings settings;
	public PartyCreateEvent(Player creator, IPartySettings sett)
	{
		player = creator;
		settings = sett;
	}

	public Player getCreator()
	{
		return player;
	}
	public IPartySettings getSettings()
	{
		return settings;
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
