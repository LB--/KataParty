package com.lb_stuff.kataparty.api;

import org.bukkit.event.Listener;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.event.Cancellable;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;

import java.util.Set;
import java.util.HashSet;

public final class ChatFilterService implements Listener
{
	@EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
	public void onPlayerChat(AsyncPlayerChatEvent e)
	{
		Set<CommandSender> targets = new HashSet<>();
		targets.add(Bukkit.getConsoleSender());
		targets.addAll(e.getRecipients());
		for(CommandSender target : targets)
		{
			Bukkit.getPluginManager().callEvent(new AsyncMessage
			(
				e.getFormat(),
				e.getPlayer().getDisplayName(),
				e.getMessage(),
				e.getPlayer(),
				target
			));
		}
		e.setCancelled(true);
	}
	@EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
	public void onMessage(AsyncMessage m)
	{
		m.getTarget().sendMessage(String.format(m.getFormat(), m.getSourceDisplayName(), m.getMessage()));
	}

	public static final class AsyncMessage extends Event implements Cancellable
	{
		private String format;
		private String srcdisp;
		private String message;
		private final CommandSender source;
		private final CommandSender target;
		public AsyncMessage(String fmt, String dispname, String msg, CommandSender src, CommandSender dest)
		{
			format = fmt;
			srcdisp = dispname;
			message = msg;
			source = src;
			target = dest;
		}

		public ChatFilterService getService()
		{
			return Bukkit.getServicesManager().getRegistration(ChatFilterService.class).getProvider();
		}

		public String getFormat()
		{
			return format;
		}
		public void setFormat(String fmt)
		{
			format = fmt;
		}

		public String getSourceDisplayName()
		{
			return srcdisp;
		}
		public void setSourceDisplayName(String dispname)
		{
			srcdisp = dispname;
		}

		public String getMessage()
		{
			return message;
		}
		public void setMessage(String msg)
		{
			message = msg;
		}

		public CommandSender getSource()
		{
			return source;
		}
		public CommandSender getTarget()
		{
			return target;
		}

		private boolean cancelled = false;
		@Override
		public boolean isCancelled()
		{
			return cancelled;
		}
		@Override
		public void setCancelled(boolean c)
		{
			cancelled = c;
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
}
