package com.lb_stuff.kataparty.api;

import com.lb_stuff.kataparty.KataPartyPlugin;

import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;

import java.util.HashSet;
import java.util.Set;

/**
 * The main service for the KataParty API.
 */
public class KataPartyService
{
	private final KataPartyPlugin inst;
	/**
	 * Constructs the service (implementation use only).
	 * @param plugin The instance of KataParty.
	 */
	public KataPartyService(KataPartyPlugin plugin)
	{
		inst = plugin;
	}

	/**
	 * Get the {@link IMessenger}.
	 * @return The {@link IMessenger}.
	 */
	public IMessenger getMessenger()
	{
		return inst;
	}
	/**
	 * Get the set of parties.
	 * @return The {@link IPartySet} instance.
	 */
	public IPartySet getPartySet()
	{
		return inst.getPartySet();
	}
	/**
	 * Get the manager for invitation tickets.
	 * @return The {@link ITicketManager} instance.
	 */
	public IPartyTicketManager getTicketManager()
	{
		return inst.getTicketManager();
	}

	/**
	 * Returns the {@link IDebugLogger} for {@link com.lb_stuff.kataparty.api.event.KataPartyEvent}.
	 * @return The {@link IDebugLogger} for {@link com.lb_stuff.kataparty.api.event.KataPartyEvent}.
	 */
	public IDebugLogger getEventDebugLogger()
	{
		return inst.getEventDebugLogger();
	}

	public Set<IParty.IMember> getAllMembers()
	{
		Set<IParty.IMember> partiers = new HashSet<>();
		for(IParty p : getPartySet())
		{
			for(IParty.IMember m : p)
			{
				partiers.add(m);
			}
		}
		return partiers;
	}
	public static Set<OfflinePlayer> getAllPlayers(Set<IParty.IMember> partiers)
	{
		Set<OfflinePlayer> players = new HashSet<>();
		for(IParty.IMember m : partiers)
		{
			players.add(Bukkit.getOfflinePlayer(m.getUuid()));
		}
		return players;
	}
	public Set<IParty.IMember> getOnlineMembers()
	{
		Set<IParty.IMember> partiers = new HashSet<>();
		for(IParty p : getPartySet())
		{
			for(IParty.IMember m : p.getMembersOnline())
			{
				partiers.add(m);
			}
		}
		return partiers;
	}
	public static Set<Player> getOnlinePlayers(Set<IParty.IMember> partiers)
	{
		Set<Player> players = new HashSet<>();
		for(OfflinePlayer offp : getAllPlayers(partiers))
		{
			if(offp.isOnline())
			{
				players.add(offp.getPlayer());
			}
		}
		return players;
	}
}
