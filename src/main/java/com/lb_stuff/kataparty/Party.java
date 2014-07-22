package com.lb_stuff.kataparty;

import com.lb_stuff.kataparty.api.KataPartyService;
import com.lb_stuff.kataparty.api.Messenger;
import com.lb_stuff.kataparty.api.IParty;
import static com.lb_stuff.kataparty.api.IParty.IMember;
import com.lb_stuff.kataparty.api.IPartySettings;
import com.lb_stuff.kataparty.api.event.PartyDisbandEvent;
import com.lb_stuff.kataparty.api.event.PartyMemberJoinEvent;
import com.lb_stuff.kataparty.api.event.PartyMemberLeaveEvent;

import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.Location;
import org.bukkit.World;

import java.util.*;

public final class Party extends PartySettings implements IParty
{
	private final PartySet parties;
	private final Messenger messenger;
	private final Set<IMember> members = new HashSet<>();
	private Inventory inv = null;
	private Double health = null;
	private boolean potions = false;

	@Override
	public Map<String, Object> serialize()
	{
		Map<String, Object> data = super.serialize();
		if(inv != null)
		{
			data.put("inv", inv.getContents());
		}
		data.put("members", members.toArray(new IMember[0]));
		return data;
	}
	public Party(Map<String, Object> data)
	{
		KataPartyPlugin plugin = (KataPartyPlugin)Bukkit.getServicesManager().getRegistration(KataPartyService.class).getPlugin();
		parties = plugin.getParties();
		messenger = parties.getMessenger();
		Object inventory = data.get("inv");
		if(!(inventory instanceof Boolean))
		{
			enableInventory();
			inv.setContents(((List<ItemStack>)inventory).toArray(new ItemStack[0]));
			data.put("inv", true);
		}
		super.apply(PartySettings.deserialize(data));
		List<IMember> mems = (List<IMember>)data.get("members");
		for(IMember m : mems)
		{
			m.setParty(this);
			add(m);
		}
	}

	public Party(PartySet ps, IPartySettings settings)
	{
		super(settings);
		parties = ps;
		messenger = ps.getMessenger();

		if(super.hasInventory())
		{
			enableInventory();
		}
	}

	@Override @Deprecated
	public void informMembers(String message)
	{
		for(IMember m : members)
		{
			m.inform(message);
		}
	}
	@Override
	public void informMembersMessage(String name, Object... parameters)
	{
		for(IMember m : members)
		{
			m.informMessage(name, parameters);
		}
	}

	@Override
	public PartySet getPartySet()
	{
		return parties;
	}

	@Override
	public void setName(String n)
	{
		for(Map.Entry<UUID, PartySet.IAsyncMemberSettings> e : parties.getPartyMembers())
		{
			if(e.getValue().getPartyName().equals(getName()))
			{
				e.getValue().setPartyName(n);
			}
		}
		informMembersMessage("party-rename-inform", getName(), n);
		super.setName(n);
	}

	@Override
	public int hashCode()
	{
		return getName().toLowerCase().hashCode();
	}
	@Override
	public boolean equals(Object obj)
	{
		if(obj == null)
		{
			return false;
		}
		if(obj instanceof IPartySettings)
		{
			return getName().equalsIgnoreCase(((IPartySettings)obj).getName());
		}
		else if(obj instanceof String)
		{
			return getName().equalsIgnoreCase((String)obj);
		}
		return false;
	}

	@Override
	public Member newMember(UUID uuid, PartyMemberJoinEvent.Reason r)
	{
		if(disbanded)
		{
			return null;
		}

		if(r != null)
		{
			PartyMemberJoinEvent pmje = new PartyMemberJoinEvent(this, uuid, r);
			Bukkit.getServer().getPluginManager().callEvent(pmje);
			if(pmje.isCancelled())
			{
				return null;
			}
		}

		{
			IMember m;
			while((m = parties.findMember(uuid)) != null)
			{
				m.getParty().removeMember(uuid, PartyMemberLeaveEvent.Reason.SWITCH_PARTIES);
			}
		}
		Member m = new Member(this, uuid);
		members.add(m);
		parties.addSettings(uuid, getName());
		OfflinePlayer offp = Bukkit.getOfflinePlayer(uuid);
		if(offp.isOnline())
		{
			informMembersMessage("party-join-inform", offp.getPlayer().getDisplayName());
			m.informMessage("manage-hint");
		}
		else
		{
			informMembersMessage("party-join-inform", offp.getName());
		}
		parties.getSettings(uuid).setPref(parties.getJoinFilterPref());
		m.setTp(parties.defaultSelfTeleports());
		return m;
	}
	@Override
	public void add(IMember m)
	{
		members.add(m);
		m.setParty(this);
		parties.addSettings(m.getUuid(), getName());
	}
	@Override
	public void removeMember(UUID uuid, PartyMemberLeaveEvent.Reason r)
	{
		final boolean hadmembers = (numMembers() > 0);
		IMember m = null;
		for(Iterator<IMember> it = members.iterator(); it.hasNext();)
		{
			m = it.next();
			if(m.getUuid().equals(uuid))
			{
				PartyMemberLeaveEvent pmle = new PartyMemberLeaveEvent(m, r);
				Bukkit.getServer().getPluginManager().callEvent(pmle);
				if(pmle.isCancelled())
				{
					continue;
				}
				if(!disbanded)
				{
					m.informMessage("party-left-inform");
				}
				it.remove();
				parties.removeSettings(uuid);
				break;
			}
		}
		if(!disbanded)
		{
			informMembersMessage("party-leave-inform", Bukkit.getOfflinePlayer(uuid).getName());
			if(hadmembers && numMembers() == 0 && !parties.keepEmptyParties() && !isSticky())
			{
				parties.remove(this, PartyDisbandEvent.Reason.AUTOMATIC_CLOSE, Bukkit.getPlayer(uuid));
				if(m  != null)
				{
					m.informMessage("party-closed-on-leave-inform", getName());
				}
			}
		}
	}
	@Override
	public IMember findMember(UUID uuid)
	{
		for(IMember m : members)
		{
			if(m.getUuid().equals(uuid))
			{
				return m;
			}
		}
		return null;
	}
	@Override
	public IMember findMember(String name)
	{
		for(IMember m : members)
		{
			OfflinePlayer offp = Bukkit.getOfflinePlayer(m.getUuid());
			if(offp != null && offp.getName() != null && offp.getName().equalsIgnoreCase(name))
			{
				return m;
			}
		}
		return null;
	}
	@Override
	public Iterator<IMember> iterator()
	{
		Set<IMember> mems = new HashSet<IMember>();
		mems.addAll(members);
		return mems.iterator();
	}
	@Override
	public int numMembers()
	{
		return members.size();
	}
	@Override
	public Set<IMember> getMembersOnline()
	{
		Set<IMember> mems = new HashSet<>();
		for(IMember m : this)
		{
			if(Bukkit.getOfflinePlayer(m.getUuid()).isOnline())
			{
				mems.add(m);
			}
		}
		return mems;
	}
	@Override
	public Set<IMember> getMembersAlive()
	{
		Set<IMember> mems = getMembersOnline();
		for(Iterator<IMember> it = mems.iterator(); it.hasNext(); )
		{
			IMember m = it.next();
			Player p = Bukkit.getPlayer(m.getUuid());
			if(p.getPlayer().isDead())
			{
				it.remove();
			}
		}
		return mems;
	}
	@Override
	public Set<IMember> getMembersRanked(Rank r)
	{
		Set<IMember> mems = new HashSet<>();
		for(IMember m : this)
		{
			if(m.getRank().equals(r))
			{
				mems.add(m);
			}
		}
		return mems;
	}

	private boolean disbanded = false;
	@Override
	public boolean disband(PartyDisbandEvent.Reason r, Player p)
	{
		PartyDisbandEvent pde = new PartyDisbandEvent(this, r, p);
		Bukkit.getServer().getPluginManager().callEvent(pde);
		if(!pde.isCancelled())
		{
			disbanded = true;
			for(Member m : this.members.toArray(new Member[0]))
			{
				m.informMessage("party-disband-inform");
				removeMember(m.getUuid(), PartyMemberLeaveEvent.Reason.DISBAND);
			}
			return true;
		}
		return false;
	}
	@Override
	public boolean isDisbanded()
	{
		return disbanded;
	}

	@Override
	public void setTp(boolean enabled)
	{
		if(enabled)
		{
			informMembersMessage("party-teleports-enabled-inform");
		}
		else
		{
			informMembersMessage("party-teleports-disabled-inform");
		}
		super.setTp(enabled);
	}

	@Override
	public void setPvp(boolean enabled)
	{
		if(enabled)
		{
			informMembersMessage("party-pvp-enabled-inform");
		}
		else
		{
			informMembersMessage("party-pvp-disabled-inform");
		}
		super.setPvp(enabled);
	}

	@Override
	public void setVisible(boolean enabled)
	{
		if(enabled)
		{
			informMembersMessage("party-visibility-enabled-inform");
		}
		else
		{
			informMembersMessage("party-visibility-disabled-inform");
		}
		super.setVisible(enabled);
	}

	@Override
	public boolean hasInventory()
	{
		return inv != null;
	}
	@Override
	public void enableInventory()
	{
		if(inv == null)
		{
			inv = Bukkit.createInventory(null, 4 * 9, messenger.getMessage("party-inventory-gui-title", getName()));
			informMembersMessage("party-inventory-enable-inform");
		}
		super.setInventory(true);
	}
	@Override
	public Inventory getInventory()
	{
		return inv;
	}
	@Override
	public void disableInventory(Location droploc)
	{
		if(inv != null)
		{
			for(ItemStack i : inv.getContents())
			{
				if(i != null)
				{
					if(droploc != null)
					{
						droploc.getWorld().dropItem(droploc, i).setPickupDelay(0);
					}
					else
					{
						World w = Bukkit.getServer().getWorlds().get(0);
						w.dropItemNaturally(w.getSpawnLocation(), i);
					}
				}
			}
			inv = null;
			informMembersMessage("party-inventory-disable-inform");
		}
		super.setInventory(false);
	}
	@Override @Deprecated
	public void setInventory(boolean enabled)
	{
		if(enabled)
		{
			enableInventory();
		}
		else
		{
			disableInventory(null);
		}
	}

	@Override
	public void setInviteOnly(boolean only)
	{
		if(only)
		{
			informMembersMessage("party-invite-only-inform");
		}
		else
		{
			informMembersMessage("party-public-inform");
		}
		super.setInviteOnly(only);
	}

	public Double getHealth()
	{
		return health;
	}
	public void enableHealth()
	{
		Set<IMember> mems = getMembersAlive();
		health = 1.0*mems.size();
		for(IMember m : mems)
		{
			Bukkit.getPlayer(m.getUuid()).setMaxHealth(20.0*mems.size());
		}
		informMembersMessage("party-shared-health-xp-enable-inform");
	}
	public void disableHealth()
	{
		health = null;
		for(IMember m : getMembersOnline())
		{
			Bukkit.getPlayer(m.getUuid()).resetMaxHealth();
		}
		informMembersMessage("party-shared-health-xp-disable-inform");
	}
	public void setHealth(double v)
	{
		if(health == null)
		{
			enableHealth();
		}
		health = v;
	}

	public boolean arePotionsSmart()
	{
		return potions;
	}
	public void setPotionsSmart(boolean v)
	{
		potions = v;
		if(v)
		{
			informMembersMessage("party-smart-potions-enable-inform");
		}
		else
		{
			informMembersMessage("party-smart-potions-disable-inform");
		}
	}

	@Override
	public String rankName(Rank r)
	{
		switch(r)
		{
			case ADMIN: return messenger.getMessage("party-rank-admin");
			case MODERATOR: return messenger.getMessage("party-rank-moderator");
			case MEMBER: return messenger.getMessage("party-rank-member");
			default: throw new IllegalStateException();
		}
	}
	public static class Member implements IMember
	{
		private IParty p;
		private final UUID uuid;
		private Rank rank = Rank.MEMBER;
		private boolean tp = true;

		@Override
		public Map<String, Object> serialize()
		{
			Map<String, Object> data = new HashMap<>();
			data.put("uuid", uuid.toString());
			data.put("rank", ""+rank);
			data.put("tp", tp);
			return data;
		}
		public Member(Map<String, Object> data)
		{
			uuid = UUID.fromString((String)data.get("uuid"));
			rank = Rank.valueOf((String)data.get("rank"));
			tp = (Boolean)data.get("tp");
		}
		@Override @Deprecated
		public void setParty(IParty party)
		{
			p = party;
		}

		private Member(Party party, UUID id)
		{
			p = party;
			uuid = id;
		}

		@Override @Deprecated
		public void inform(String message)
		{
			OfflinePlayer offp = Bukkit.getOfflinePlayer(uuid);
			if(offp.isOnline())
			{
				offp.getPlayer().sendMessage("[KataParty] "+message);
			}
		}
		@Override
		public void informMessage(String name, Object... parameters)
		{
			OfflinePlayer offp = Bukkit.getOfflinePlayer(uuid);
			if(offp.isOnline())
			{
				p.getPartySet().getMessenger().tellMessage(offp.getPlayer(), name, parameters);
			}
		}

		@Override
		public IParty getParty()
		{
			return p;
		}

		@Override
		public UUID getUuid()
		{
			return uuid;
		}
		@Override
		public int hashCode()
		{
			return uuid.hashCode();
		}
		@Override
		public boolean equals(Object obj)
		{
			if(obj == null)
			{
				return false;
			}
			if(obj instanceof IMember)
			{
				return uuid.equals(((IMember)obj).getUuid());
			}
			if(obj instanceof UUID)
			{
				return uuid.equals((UUID)obj);
			}
			return false;
		}

		@Override
		public Rank getRank()
		{
			return rank;
		}
		@Override
		public String getRankName()
		{
			return p.rankName(getRank());
		}
		@Override
		public void setRank(Rank r)
		{
			rank = r;
			informMessage("party-rank-inform", p.rankName(r));
		}

		@Override
		public boolean canTp()
		{
			return tp;
		}
		@Override
		public void setTp(boolean v)
		{
			tp = v;
			if(v)
			{
				informMessage("party-self-teleports-enable-inform");
			}
			else
			{
				informMessage("party-self-teleports-disable-inform");
			}
		}
	}
}
