package com.lb_stuff.kataparty.api;

import com.lb_stuff.kataparty.api.event.PartyDisbandEvent;

import org.bukkit.configuration.serialization.ConfigurationSerializable;
import org.bukkit.entity.Player;

import java.util.UUID;
import java.util.Map;

public interface IPartySet extends Iterable<IParty>, ConfigurationSerializable
{
	Messenger getMessenger();
	IParty newParty(Player creator, IPartySettings settings);
	boolean add(IParty p);
	void remove(IParty p, PartyDisbandEvent.Reason r, Player player);
	void keepEmptyParties(boolean keep);
	boolean keepEmptyParties();
	interface IMemberSettings
	{
		String getPartyName();
		void setPartyName(String partyname);
		ChatFilterPref getPref();
		void setPref(ChatFilterPref cfp);
		void togglePref();
	}
	void addSettings(UUID uuid, String pname);
	IMemberSettings getSettings(UUID uuid);
	void removeSettings(UUID uuid);
	public Iterable<Map.Entry<UUID, IMemberSettings>> getPartyMembers();
	IParty.IMember findMember(UUID uuid);
	IParty findParty(String name);
	boolean contains(IParty p);
}
