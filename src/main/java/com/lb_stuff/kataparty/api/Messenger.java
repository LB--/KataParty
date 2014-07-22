package com.lb_stuff.kataparty.api;

import org.bukkit.entity.Player;

/**
 * Any class that will process requests to send messages to players.
 */
public interface Messenger
{
	/**
	 * Fetches a user-defined message from the config file.
	 * @param name The name of the message.
	 * @param parameters Zero or more parameters are require by the message.
	 * @return The message with parameters already in place.
	 */
	String getMessage(String name, Object... parameters);
	/**
	 * Tell a player a message that is not user-defined.
	 * @param p The player to tell.
	 * @param message The message to send to the player.
	 * @deprecated Exists only for testing purposes.
	 */
	@Deprecated
	void tell(Player p, String message);
	/**
	 * Tells a player a user-defined message from the config file.
	 * @param p The player to tell.
	 * @param name The name of the message.
	 * @param parameters Zero or more parameters are required by the message;
	 */
	void tellMessage(Player p, String name, Object... parameters);
}
