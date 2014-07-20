package com.lb_stuff.kataparty.api.event;

import com.lb_stuff.kataparty.api.IParty;

import org.bukkit.event.HandlerList;
import org.bukkit.entity.Player;

public class PartyMemberJoinEvent extends CancellableKataPartyEvent
{
	public enum Reason
	{
		VOLUNTARY,
		INVITATION,
		OTHER
	}
	private final IParty party;
	private final Player player;
	private final Reason reason;
	public PartyMemberJoinEvent(IParty p, Player applicant, Reason r)
	{
		party = p;
		player = applicant;
		reason = r;
	}

	public IParty getParty()
	{
		return party;
	}
	public Player getPlayer()
	{
		return player;
	}
	public Reason getReason()
	{
		return reason;
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
