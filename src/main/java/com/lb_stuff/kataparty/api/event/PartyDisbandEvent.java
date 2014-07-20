package com.lb_stuff.kataparty.api.event;

import com.lb_stuff.kataparty.api.IParty;

import org.bukkit.event.HandlerList;
import org.bukkit.entity.Player;

public class PartyDisbandEvent extends CancellableKataPartyEvent
{
	public enum Reason
	{
		PARTY_ADMIN_DISBAND,
		SERVER_ADMIN_CLOSE,
		AUTOMATIC_CLOSE,
		OTHER
	}
	private final IParty party;
	private final Reason reason;
	private final Player player;
	public PartyDisbandEvent(IParty p, Reason r, Player plr)
	{
		party = p;
		reason = r;
		player = plr;
	}

	public IParty getParty()
	{
		return party;
	}
	public Reason getReason()
	{
		return reason;
	}
	public Player getCloser()
	{
		return player;
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
