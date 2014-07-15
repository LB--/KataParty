package com.lb_stuff.kataparty;

import static com.lb_stuff.kataparty.ChatFilterPref.*;

import org.bukkit.entity.Player;

import java.util.*;
import java.util.concurrent.ConcurrentSkipListMap;

public class PartySet implements Iterable<Party>
{
	private final KataPartyPlugin inst;
	private final Set<Party> parties = new HashSet<>();
	private boolean keep_empty = false;
	public PartySet(KataPartyPlugin plugin)
	{
		inst = plugin;
	}

	public Messenger getMessenger()
	{
		return inst;
	}

	public Party add(String pname, Player creator)
	{
		if(findParty(pname) == null)
		{
			Party p = new Party(this, pname);
			parties.add(p);
			if(creator != null)
			{
				p.addMember(creator.getUniqueId()).setRank(Party.Rank.ADMIN);
				getSettings(creator.getUniqueId()).setPref(inst.getFilter().getDefaultFilterPref("on-party-create"));
			}
			return p;
		}
		return null;
	}
	public void remove(Party p, Player player)
	{
		p.disableInventory(player);
		p.disband();
		parties.remove(p);
	}
	public void keepEmptyParties(boolean keep)
	{
		keep_empty = keep;
		if(!keep)
		{
			Iterator<Party> it = iterator();
			while(it.hasNext())
			{
				Party p = it.next();
				if(p.numMembers() == 0)
				{
					it.remove();
				}
			}
		}
	}
	public boolean keepEmptyParties()
	{
		return keep_empty;
	}

	@Override
	public Iterator<Party> iterator()
	{
		return parties.iterator();
	}

	public static class MemberSettings
	{
		private String partyname;
		private ChatFilterPref pref = PREFER_PARTY;
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

		public ChatFilterPref getPref()
		{
			return pref;
		}
		public void setPref(ChatFilterPref cfp)
		{
			pref = cfp;
		}
		public void togglePref()
		{
			switch(pref)
			{
				case PREFER_PARTY:
				{
					pref = PREFER_GLOBAL;
				} break;
				case PREFER_GLOBAL:
				{
					pref = PREFER_PARTY;
				} break;
				default: throw new IllegalStateException();
			}
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

	public ChatFilterPref getJoinFilterPref()
	{
		return inst.getFilter().getDefaultFilterPref("on-party-join");
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
