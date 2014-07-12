package com.lb_stuff.kataparty;

import org.bukkit.entity.Player;

import java.util.*;
import java.util.concurrent.ConcurrentSkipListMap;

public class PartySet implements Iterable<Party>
{
	private final Set<Party> parties = new HashSet<>();
	public PartySet()
	{
	}

	public Party add(String pname)
	{
		if(findParty(pname) == null)
		{
			Party p = new Party(this, pname);
			parties.add(p);
			return p;
		}
		return null;
	}
	public void remove(Party p, Player player)
	{
		p.disableInventory(player);
		p.disband();
	}

	@Override
	public Iterator<Party> iterator()
	{
		return parties.iterator();
	}

	public static class MemberSettings
	{
		private String partyname;
		private boolean talkparty = true;
		public MemberSettings(String pname)
		{
			partyname = pname;
		}

		public String getPartyName()
		{
			return partyname;
		}
		public void setPartyName(String partyname)
		{
			this.partyname = partyname;
		}

		public boolean isPartyPreferred()
		{
			return talkparty;
		}
		public void setPartyPreferred(boolean preferred)
		{
			talkparty = preferred;
		}
	}
	private final ConcurrentSkipListMap<UUID, MemberSettings> partiers = new ConcurrentSkipListMap<>();
	public void addSettings(UUID uuid, String pname)
	{
		partiers.put(uuid, new MemberSettings(pname));
	}
	public MemberSettings getSettings(UUID uuid)
	{
		return partiers.get(uuid);
	}
	public void removeSettings(UUID uuid)
	{
		partiers.remove(uuid);
	}
	private class PartyMembers implements Iterable<Map.Entry<UUID, MemberSettings>>
	{
		@Override
		public Iterator<Map.Entry<UUID, MemberSettings>> iterator()
		{
			return partiers.entrySet().iterator();
		}
	}
	private PartyMembers pms = new PartyMembers();
	public Iterable<Map.Entry<UUID, MemberSettings>> getPartyMembers()
	{
		return pms;
	}

	public Party.Member findMember(UUID uuid)
	{
		for(Party p : parties)
		{
			for(Party.Member m : p)
			{
				if(m.getUuid().equals(uuid))
				{
					return m;
				}
			}
		}
		return null;
	}
	public Party findParty(String name)
	{
		for(Party p : parties)
		{
			if(p.getName().equalsIgnoreCase(name))
			{
				return p;
			}
		}
		return null;
	}
	public boolean contains(Party p)
	{
		return parties.contains(p);
	}
}
