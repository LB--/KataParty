package com.lb_stuff.kataparty;

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
	private final Set<Member> members = new HashSet<>();
	private Inventory inv = null;
	private Double health = null;
	private boolean potions = false;

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
		for(Member m : members)
		{
			m.inform(message);
		}
	}
	@Override
	public void informMembersMessage(String name, Object... parameters)
	{
		for(Member m : members)
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
		for(Map.Entry<UUID, PartySet.IMemberSettings> e : parties.getPartyMembers())
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
	public Member addMember(UUID uuid, PartyMemberJoinEvent.Reason r)
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
		Member m = new Member(uuid);
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
	public void removeMember(UUID uuid, PartyMemberLeaveEvent.Reason r)
	{
		final boolean hadmembers = (numMembers() > 0);
		Member m = null;
		for(Iterator<Member> it = members.iterator(); it.hasNext();)
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
	public Member findMember(UUID uuid)
	{
		for(Member m : members)
		{
			if(m.getUuid().equals(uuid))
			{
				return m;
			}
		}
		return null;
	}
	@Override
	public Member findMember(String name)
	{
		for(Member m : members)
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
		Set<IMember> mems = new HashSet<>();
		for(IMember m : this)
		{
			OfflinePlayer offp = Bukkit.getOfflinePlayer(m.getUuid());
			if(offp.isOnline() && !offp.getPlayer().isDead())
			{
				mems.add(m);
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
	public class Member implements IMember
	{
		private final UUID uuid;
		private Rank rank = Rank.MEMBER;
		private boolean tp = true;

		private Member(UUID id)
		{
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
				messenger.tellMessage(offp.getPlayer(), name, parameters);
			}
		}

		@Override
		public Party getParty()
		{
			return Party.this;
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
			if(getClass() != obj.getClass())
			{
				return false;
			}
			final Member other = (Member)obj;
			if(!Objects.equals(this.uuid, other.uuid))
			{
				return false;
			}
			return true;
		}

		@Override
		public Rank getRank()
		{
			return rank;
		}
		@Override
		public String getRankName()
		{
			return rankName(getRank());
		}
		@Override
		public void setRank(Rank r)
		{
			rank = r;
			informMessage("party-rank-inform", rankName(r));
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
