package com.lb_stuff.kataparty;

import static com.lb_stuff.kataparty.PartySettings.MemberSettings;
import com.lb_stuff.kataparty.api.*;
import static com.lb_stuff.kataparty.api.IPartySettings.IMemberSettings;
import static com.lb_stuff.kataparty.api.IPartyFactory.IMemberFactory;
import static com.lb_stuff.kataparty.api.ChatFilterPref.*;
import static com.lb_stuff.kataparty.api.IPartySet.IAsyncMemberSettings;
import com.lb_stuff.kataparty.api.event.PartyCreateEvent;
import com.lb_stuff.kataparty.api.event.PartyDisbandEvent;
import com.lb_stuff.kataparty.api.event.PartyMemberJoinEvent;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.util.*;
import java.util.concurrent.ConcurrentSkipListMap;

public class PartySet implements IPartySet
{
	private final KataPartyPlugin inst;
	private final Set<IParty> parties = new HashSet<>();
	private boolean keep_empty = false;

	@Override
	public Map<String, Object> serialize()
	{
		Map<String, Object> data = new HashMap<>();
		data.put("parties", parties.toArray(new IParty[0]));
		return data;
	}
	public static PartySet deserialize(Map<String, Object> data)
	{
		KataPartyPlugin plugin = (KataPartyPlugin)Bukkit.getServicesManager().getRegistration(KataPartyService.class).getPlugin();
		PartySet ps = plugin.getPartySet();
		List<IParty> plist = (List<IParty>)data.get("parties");
		ps.parties.addAll(plist);

		return ps;
	}

	public PartySet(KataPartyPlugin plugin)
	{
		inst = plugin;
	}

	@Override
	public IMessenger getMessenger()
	{
		return inst;
	}

	private final Map<Class<? extends IPartySettings>, IPartyFactory> pfacts = new HashMap<>();
	@Override
	public IPartyFactory registerPartyFactory(Class<? extends IPartySettings> clazz, IPartyFactory factory)
	{
		if(clazz == null)
		{
			throw new IllegalArgumentException("Class cannot be null");
		}
		if(factory == null)
		{
			return pfacts.remove(clazz);
		}
		return pfacts.put(clazz, factory);
	}
	@Override
	public IPartyFactory getPartyFactory(Class<? extends IPartySettings> clazz)
	{
		IPartyFactory fact = pfacts.get(clazz);
		if(fact == null)
		{
			throw new IllegalArgumentException("No IPartyFactory for "+clazz);
		}
		return fact;
	}
	private final Map<Class<? extends IMemberSettings>, IMemberFactory> mfacts = new HashMap<>();
	@Override
	public IMemberFactory registerMemberFactory(Class<? extends IMemberSettings> clazz, IMemberFactory factory)
	{
		if(clazz == null)
		{
			throw new IllegalArgumentException("Class cannot be null");
		}
		if(factory == null)
		{
			return mfacts.remove(clazz);
		}
		return mfacts.put(clazz, factory);
	}
	@Override
	public IMemberFactory getMemberFactory(Class<? extends IMemberSettings> clazz)
	{
		IMemberFactory fact = mfacts.get(clazz);
		if(fact == null)
		{
			throw new IllegalArgumentException("No IMemberFactory for "+clazz);
		}
		return fact;
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
				IParty p = getPartyFactory(settings.getClass()).create(this, settings);
				if(p != null)
				{
					parties.add(p);
					if(creator != null)
					{
						MemberSettings ms = new MemberSettings(creator.getUniqueId());
						ms.setRank(PartyRank.ADMIN);
						if(p.newMember(ms, PartyMemberJoinEvent.Reason.CREATOR) != null)
						{
							getSettings(ms.getUuid()).setPref(inst.getFilter().getDefaultFilterPref("on-party-create"));
						}
					}
				}
				return p;
			}
		}
		return null;
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

	public static class AsyncMemberSettings implements IAsyncMemberSettings
	{
		private String partyname;
		private ChatFilterPref pref = PREFER_PARTY;
		private boolean alone = true;
		public AsyncMemberSettings(String pname)
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
			pref = pref.opposite();
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
	private final ConcurrentSkipListMap<UUID, AsyncMemberSettings> partiers = new ConcurrentSkipListMap<>();
	@Override
	public void addSettings(UUID uuid, String pname)
	{
		partiers.put(uuid, new AsyncMemberSettings(pname));
	}
	@Override
	public AsyncMemberSettings getSettings(UUID uuid)
	{
		return partiers.get(uuid);
	}
	@Override
	public void removeSettings(UUID uuid)
	{
		partiers.remove(uuid);
	}

	private class PartyMembers implements Iterable<Map.Entry<UUID, IAsyncMemberSettings>>
	{
		@Override
		public Iterator<Map.Entry<UUID, IAsyncMemberSettings>> iterator()
		{
			Map<UUID, IAsyncMemberSettings> mems = new HashMap<>();
			for(Map.Entry<UUID, AsyncMemberSettings> m : partiers.entrySet())
			{
				mems.put(m.getKey(), m.getValue());
			}
			return mems.entrySet().iterator();
		}
	}
	private final PartyMembers pms = new PartyMembers();
	@Override
	public Iterable<Map.Entry<UUID, IAsyncMemberSettings>> getPartyMembers()
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
	@SuppressWarnings("IncompatibleEquals")
	public IParty findParty(String name)
	{
		for(IParty p : this)
		{
			if(p.equals(name))
			{
				return p;
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
