package com.lb_stuff.kataparty.api;

import com.lb_stuff.kataparty.KataPartyPlugin;
import com.lb_stuff.kataparty.PartyTicketManager;

import org.bukkit.inventory.ItemStack;

public class KataPartyService
{
	private final KataPartyPlugin inst;
	public KataPartyService(KataPartyPlugin plugin)
	{
		inst = plugin;
	}

	public Messenger getMessenger()
	{
		return inst;
	}
	public IPartySet getPartySet()
	{
		return inst.getParties();
	}

	private PartyTicketManager getTicketManager()
	{
		return inst.getTicketManager();
	}

	public boolean isTicket(ItemStack is)
	{
		return getTicketManager().isTicket(is);
	}
	public boolean wasTicketGiven(ItemStack is)
	{
		return getTicketManager().wasTicketGiven(is);
	}

	//...
}
