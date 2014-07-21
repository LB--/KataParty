package com.lb_stuff.kataparty;

import com.lb_stuff.kataparty.api.*;
import static com.lb_stuff.kataparty.api.ChatFilterPref.*;
import static com.lb_stuff.kataparty.api.IPartySet.IMemberSettings;
import com.lb_stuff.kataparty.api.event.PartyCreateEvent;
import com.lb_stuff.kataparty.api.event.PartyDisbandEvent;

import org.bukkit.entity.Player;

import java.util.*;
import java.util.concurrent.ConcurrentSkipListMap;

public class PartySet implements IPartySet
{
	private final KataPartyPlugin inst;
	private final Set<IParty> parties = new HashSet<>();
	private boolean keep_empty = false;
	public PartySet(KataPartyPlugin plugin)
	{
		inst = plugin;
	}

	public boolean defaultSelfTeleports()
	{
		return inst.getConfig().getBoolean("party-defaults.self-teleports");
	}

	public Messenger getMessenger()
	{
		return inst;
	}

	@Override
	public IParty newParty(Player creator, IPartySettings settings)
	{
		if(findParty(settings.getName()) == null)
		{
			PartyCreateEvent pce = new PartyCreateEvent(creator, settings);
			inst.getServer().getPluginManager().callEvent(pce);
			if(!pce.isCancelled())
			{
				Party p = new Party(this, settings);
				parties.add(p);
				if(creator != null)
				{
					p.addMember(creator.getUniqueId()).setRank(Party.Rank.ADMIN);
					getSettings(creator.getUniqueId()).setPref(inst.getFilter().getDefaultFilterPref("on-party-create"));
				}
				return p;
			}
		}
		return null;
	}
	@Override
	public boolean add(IParty p)
	{
		return parties.add(p);
	}
	@Override
	public void remove(IParty p, PartyDisbandEvent.Reason r, Player player)
	{
		if(p.disband(r, player))
		{
			p.disableInventory(player.getEyeLocation());
			parties.remove(p);
		}
	}
	@Override
	public void keepEmptyParties(boolean keep)
	{
		keep_empty = keep;
		if(!keep)
		{
			Iterator<IParty> it = iterator();
			while(it.hasNext())
			{
				IParty p = it.next();
				if(!p.isSticky() && p.numMembers() == 0)
				{
					remove(p, PartyDisbandEvent.Reason.AUTOMATIC_CLOSE, null);
				}
			}
		}
	}
	@Override
	public boolean keepEmptyParties()
	{
		return keep_empty;
	}

	@Override
	public Iterator<IParty> iterator()
	{
		Set<IParty> copy = new HashSet<>();
		copy.addAll(parties);
		return copy.iterator();
	}

	public static class MemberSettings implements IMemberSettings
	{
		private String partyname;
		private ChatFilterPref pref = PREFER_PARTY;
		private boolean alone = true;
		public MemberSettings(String pname)
		{
			partyname = pname;
		}

		@Override
		public String getPartyName()
		{
			return partyname;
		}
		@Override
		public void setPartyName(String partyname)
		{
			this.partyname = partyname;
		}

		@Override
		public ChatFilterPref getPref()
		{
			return pref;
		}
		@Override
		public void setPref(ChatFilterPref cfp)
		{
			pref = cfp;
		}
		@Override
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

		public boolean isAlone()
		{
			return alone;
		}
		public void setAlone(boolean isalone)
		{
			alone = isalone;
		}
	}
	private final ConcurrentSkipListMap<UUID, MemberSettings> partiers = new ConcurrentSkipListMap<>();
	@Override
	public void addSettings(UUID uuid, String pname)
	{
		partiers.put(uuid, new MemberSettings(pname));
	}
	@Override
	public MemberSettings getSettings(UUID uuid)
	{
		return partiers.get(uuid);
	}
	@Override
	public void removeSettings(UUID uuid)
	{
		partiers.remove(uuid);
	}

	public ChatFilterPref getJoinFilterPref()
	{
		return inst.getFilter().getDefaultFilterPref("on-party-join");
	}

	private class PartyMembers implements Iterable<Map.Entry<UUID, IMemberSettings>>
	{
		@Override
		public Iterator<Map.Entry<UUID, IMemberSettings>> iterator()
		{
			Map<UUID, IMemberSettings> mems = new HashMap<>();
			for(Map.Entry<UUID, MemberSettings> m : partiers.entrySet())
			{
				mems.put(m.getKey(), m.getValue());
			}
			return mems.entrySet().iterator();
		}
	}
	private final PartyMembers pms = new PartyMembers();
	@Override
	public Iterable<Map.Entry<UUID, IMemberSettings>> getPartyMembers()
	{
		return pms;
	}

	@Override
	public IParty.IMember findMember(UUID uuid)
	{
		for(IParty p : this)
		{
			for(IParty.IMember m : p)
			{
				if(m.getUuid().equals(uuid))
				{
					return m;
				}
			}
		}
		return null;
	}
	@Override
	@SuppressWarnings("element-type-mismatch")
	public IParty findParty(String name)
	{
		if(parties.contains(name))
		{
			for(IParty p : this)
			{
				if(p.getName().equalsIgnoreCase(name))
				{
					return p;
				}
			}
		}
		return null;
	}
	@Override
	public boolean contains(IParty p)
	{
		return parties.contains(p);
	}
}
