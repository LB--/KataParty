package com.lb_stuff.kataparty.api;

import com.lb_stuff.kataparty.KataPartyPlugin;
import com.lb_stuff.kataparty.PartyTicketManager;

import org.bukkit.inventory.ItemStack;

public class PartyTicketService
{
	private final KataPartyPlugin inst;
	public PartyTicketService(KataPartyPlugin plugin)
	{
		inst = plugin;
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
