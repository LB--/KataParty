package com.lb_stuff.kataparty.api.event;

import com.lb_stuff.kataparty.api.IParty;

import org.bukkit.event.HandlerList;

public class PartyMemberLeaveEvent extends CancellableKataPartyEvent
{
	public enum Reason
	{
		VOLUNTARY,
		KICKED,
		DISBAND,
		OTHER
	}
	private final IParty.IMember member;
	private final Reason reason;
	public PartyMemberLeaveEvent(IParty.IMember m, Reason r)
	{
		member = m;
		reason = r;
	}

	public IParty.IMember getMember()
	{
		return member;
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
