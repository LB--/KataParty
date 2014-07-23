package com.lb_stuff.service;

import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.plugin.ServicePriority;
import org.bukkit.plugin.RegisteredServiceProvider;
import org.bukkit.event.Listener;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.event.Cancellable;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.command.ConsoleCommandSender;

import java.util.Collection;
import java.util.Set;
import java.util.HashSet;

/**
 * This service listens to {@link org.bukkit.event.player.AsyncPlayerChatEvent}
 * and cancels the event after other plugins have processed it, instead splitting
 * it into multiple events ({@link ChatFilterService.AsyncMessage}) where each
 * event is from the source to one of the targets. Two of the targets are always
 * the console and the source. This allows each target to see a different message
 * (or no message at all) based on their preferences. This class listens to its own
 * events that have not been cancelled with {@link org.bukkit.event.EventPriority#MONITOR}
 * priority and manually sends the message to each target based on the event.
 */
public final class ChatFilterService implements Listener
{
	/**
	 * Doesn't do anything - allows you to register the service as you wish.
	 */
	public ChatFilterService()
	{
	}
	/**
	 * Registers the service with the given {@link ServicePriority}, but does not start it.
	 * @param priority The desired {@link ServicePriority}.
	 */
	public ChatFilterService(ServicePriority priority)
	{
		JavaPlugin plugin = JavaPlugin.getProvidingPlugin(ChatFilterService.class);
		Bukkit.getServicesManager().register(ChatFilterService.class, this, plugin, priority);
	}

	/**
	 * Start intercepting {@link org.bukkit.event.player.AsyncPlayerChatEvent}.
	 */
	public void start()
	{
		JavaPlugin plugin = JavaPlugin.getProvidingPlugin(ChatFilterService.class);
		Bukkit.getPluginManager().registerEvents(this, plugin);
	}
	/**
	 * Stop intercepting {@link org.bukkit.event.player.AsyncPlayerChatEvent}.
	 */
	public void stop()
	{
		HandlerList.unregisterAll(this);
	}

	/**
	 * Convenience method for {@link org.bukkit.plugin.ServicesManager#getRegistration(java.lang.Class)}.
	 * @return The {@link RegisteredServiceProvider} for this class.
	 */
	public static RegisteredServiceProvider<ChatFilterService> getService()
	{
		return Bukkit.getServicesManager().getRegistration(ChatFilterService.class);
	}
	/**
	 * Convenience method for {@link org.bukkit.plugin.ServicesManager#getRegistrations(java.lang.Class)}.
	 * @return The {@link RegisteredServiceProvider} for this class.
	 */
	public static Collection<RegisteredServiceProvider<ChatFilterService>> getServices()
	{
		return Bukkit.getServicesManager().getRegistrations(ChatFilterService.class);
	}

	/**
	 * See class description for more information.
	 * @param e The {@link org.bukkit.event.player.AsyncPlayerChatEvent}.
	 */
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
	/**
	 * See class description for more information.
	 * @param m The {@link AsyncMesage}.
	 */
	@EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
	public void onMessage(AsyncMessage m)
	{
		m.getTarget().sendMessage(String.format(m.getFormat(), m.getSourceDisplayName(), m.getMessage()));
	}

	/**
	 * Listen to this event to customize message formatting for different
	 * players or the console, or even cancel messages being sent to certain
	 * players. See {@link ChatFilterService} for more information. This
	 * event will always be asynchronous.
	 */
	public static final class AsyncMessage extends Event implements Cancellable
	{
		private String format;
		private String srcdisp;
		private String message;
		private final CommandSender source;
		private final CommandSender target;
		private AsyncMessage(String fmt, String dispname, String msg, CommandSender src, CommandSender dest)
		{
			super(true); //set asynchronous
			format = fmt;
			srcdisp = dispname;
			message = msg;
			source = src;
			target = dest;
		}

		/**
		 * Returns the {@link ChatFilterService} instance.
		 * @return The {@link ChatFilterService} instance.
		 */
		public ChatFilterService getService()
		{
			return Bukkit.getServicesManager().getRegistration(ChatFilterService.class).getProvider();
		}

		/**
		 * Returns the format initially from {@link AsyncPlayerChatEvent#getFormat()}.
		 * @return the format of this message.
		 */
		public String getFormat()
		{
			return format;
		}
		/**
		 * Replaces the format of this message.
		 * @param fmt The new format of this message.
		 */
		public void setFormat(String fmt)
		{
			if(fmt == null)
			{
				throw new IllegalArgumentException("Format cannot be null");
			}
			format = fmt;
		}

		/**
		 * Returns the display name that will be included in the final message.
		 * @return The display name that will be included in the final message.
		 */
		public String getSourceDisplayName()
		{
			return srcdisp;
		}
		/**
		 * Sets the display name that will be included in the final message.
		 * @param dispname The display name that will be included in the final message.
		 */
		public void setSourceDisplayName(String dispname)
		{
			if(dispname == null)
			{
				throw new IllegalArgumentException("Display name cannot be null");
			}
			srcdisp = dispname;
		}

		/**
		 * Returns the message initially from {@link AsyncPlayerChatEvent#getMessage()}.
		 * @return
		 */
		public String getMessage()
		{
			return message;
		}
		/**
		 * Replaces the message. If <code>msg</code> is null, cancels the event without
		 * changing the return value of {@link #getMessage()}.
		 * @param msg The new message.
		 */
		public void setMessage(String msg)
		{
			if(msg == null)
			{
				setCancelled(true);
				return;
			}
			message = msg;
		}

		/**
		 * Returns the {@link org.bukkit.command.CommandSender} that sent this message.
		 * @return The {@link org.bukkit.command.CommandSender} that sent this message.
		 */
		public CommandSender getSource()
		{
			return source;
		}
		/**
		 * Returns the {@link org.bukkit.command.CommandSender} that will receive this message.
		 * May be a {@link org.bukkit.command.ConsoleCommandSender}.
		 * @return The {@link org.bukkit.command.CommandSender} that will receive this message.
		 */
		public CommandSender getTarget()
		{
			return target;
		}

		private boolean cancelled = false;
		/**
		 * Returns whether this message will not be sent.
		 * @return Whether this message will not be sent.
		 */
		@Override
		public boolean isCancelled()
		{
			return cancelled;
		}
		/**
		 * Cancel or uncancel sending of this message. You cannot cancel this message if
		 * the target is a {@link org.bukkit.command.ConsoleCommandSender}.
		 * @param c Whether this message will not be sent.
		 */
		@Override
		public void setCancelled(boolean c)
		{
			if(source instanceof ConsoleCommandSender)
			{
				cancelled = false;
			}
			else
			{
				cancelled = c;
			}
		}

		private static final HandlerList handlers = new HandlerList();
		/**
		 * See {@link org.bukkit.event.Event#getHandlers()}.
		 * @return The {@link org.bukkit.event.HandlerList}.
		 */
		@Override
		public HandlerList getHandlers()
		{
			return handlers;
		}
		/**
		 * See {@link org.bukkit.event.Event#getHandlers()}.
		 * @return The {@link org.bukkit.event.HandlerList}.
		 */
		public static HandlerList getHandlerList()
		{
			return handlers;
		}
	}
}
