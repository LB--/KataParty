package com.lb_stuff.kataparty;

import com.lb_stuff.kataparty.api.IDebugLogger;
import com.lb_stuff.kataparty.api.event.*;

import org.bukkit.event.Listener;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import static org.bukkit.ChatColor.*;

public class EventDebugLogger implements Listener, IDebugLogger
{
	private final KataPartyPlugin inst;
	public EventDebugLogger(KataPartyPlugin plugin)
	{
		inst = plugin;
	}

	private int indent = 0;
	public void log(String msg)
	{
		for(int i = 0; i < indent; ++i)
		{
			msg = "| "+msg;
		}
		if(inst.getConfig().getBoolean("debug"))
		{
			inst.tellConsole(msg);
		}
	}

	private void onEventStart(KataPartyEvent e)
	{
		log(".");
		log("{ "+DARK_BLUE+UNDERLINE+e.getClass().getSimpleName()+RESET+(e.isAsynchronous()? " (async)" : ""));
		++indent;
	}
	private void onEventEnd(KataPartyEvent e)
	{
		--indent;
		log("} "+DARK_BLUE+UNDERLINE+e.getClass().getSimpleName());
	}
	private void onCancellableEvent(CancellableKataPartyEvent e)
	{
		log("^ cancelled: "+(e.isCancelled()? DARK_RED : DARK_GREEN)+UNDERLINE+e.isCancelled());
	}

	@EventHandler(priority = EventPriority.LOWEST , ignoreCancelled = false) public void onStart(PartyCreateEvent e){ onEventStart      (e); }
	@EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = false) public void onEnd  (PartyCreateEvent e){ onEventEnd        (e); }
	@EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = false) public void on     (PartyCreateEvent e){ onCancellableEvent(e); }

	@EventHandler(priority = EventPriority.LOWEST , ignoreCancelled = false) public void onStart(PartyDisbandEvent e){ onEventStart      (e); }
	@EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = false) public void onEnd  (PartyDisbandEvent e){ onEventEnd        (e); }
	@EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = false) public void on     (PartyDisbandEvent e){ onCancellableEvent(e); }

	@EventHandler(priority = EventPriority.LOWEST , ignoreCancelled = false) public void onStart(PartyMemberJoinEvent e){ onEventStart      (e); }
	@EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = false) public void onEnd  (PartyMemberJoinEvent e){ onEventEnd        (e); }
	@EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = false) public void on     (PartyMemberJoinEvent e){ onCancellableEvent(e); }

	@EventHandler(priority = EventPriority.LOWEST , ignoreCancelled = false) public void onStart(PartyMemberLeaveEvent e){ onEventStart      (e); }
	@EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = false) public void onEnd  (PartyMemberLeaveEvent e){ onEventEnd        (e); }
	@EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = false) public void on     (PartyMemberLeaveEvent e){ onCancellableEvent(e); }

	@EventHandler(priority = EventPriority.LOWEST , ignoreCancelled = false) public void onStart(TicketInventoryEvent e){ onEventStart      (e); }
	@EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = false) public void onEnd  (TicketInventoryEvent e){ onEventEnd        (e); }
	@EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = false) public void on     (TicketInventoryEvent e){ onCancellableEvent(e); }

	@EventHandler(priority = EventPriority.LOWEST , ignoreCancelled = false) public void onStart(PartySettingsChangeEvent e){ onEventStart      (e); }
	@EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = false) public void onEnd  (PartySettingsChangeEvent e){ onEventEnd        (e); }
	@EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = false) public void on     (PartySettingsChangeEvent e){ onCancellableEvent(e); }
}
