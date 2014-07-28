package com.lb_stuff.kataparty;

import static com.lb_stuff.kataparty.PartySettings.MemberSettings;
import com.lb_stuff.kataparty.api.*;
import static com.lb_stuff.kataparty.api.IPartySettings.IMemberSettings;
import static com.lb_stuff.kataparty.api.IPartyFactory.IMemberFactory;
import com.lb_stuff.kataparty.api.event.PartyCreateEvent;
import com.lb_stuff.kataparty.api.event.PartyDisbandEvent;
import com.lb_stuff.kataparty.api.event.PartyMemberJoinEvent;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.util.Map;
import java.util.HashMap;
import java.util.List;
import java.util.Set;
import java.util.HashSet;
import java.util.Iterator;
import java.util.UUID;

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
						p.newMember(ms, PartyMemberJoinEvent.Reason.CREATOR);
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
