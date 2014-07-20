package com.lb_stuff.kataparty.api.event;

import com.lb_stuff.kataparty.api.IParty;

import org.bukkit.event.HandlerList;
import org.bukkit.entity.Player;

public class PartyCreateEvent extends CancellableKataPartyEvent
{
	private final Player player;
	private IParty party;
	public PartyCreateEvent(Player creator, IParty p)
	{
		player = creator;
		party = p;
	}

	public Player getCreator()
	{
		return player;
	}
	public IParty getParty()
	{
		return party;
	}
	public void setParty(IParty p)
	{
		party = p;
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
