package com.lb_stuff.kataparty;

import static com.lb_stuff.kataparty.ChatFilterPref.*;

import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import java.util.*;

public class Party implements Iterable<Party.Member>
{
	private final PartySet parties;
	private final Messenger messenger;
	private String name;
	private final Set<Member> members = new HashSet<>();
	private boolean tp = true;
	private boolean pvp = false;
	private boolean visible = true;
	private Inventory inv = null;
	private Double health = null;
	private boolean potions = false;

	public Party(PartySet ps, String pname)
	{
		parties = ps;
		messenger = ps.getMessenger();
		name = pname;
	}

	@Deprecated
	public void informMembers(String message)
	{
		for(Member m : this)
		{
			m.inform(message);
		}
	}
	public void informMembersMessage(String name, Object... parameters)
	{
		for(Member m : this)
		{
			m.informMessage(name, parameters);
		}
	}

	public String getName()
	{
		return name;
	}
	public void rename(String n)
	{
		for(Map.Entry<UUID, PartySet.MemberSettings> e : parties.getPartyMembers())
		{
			if(e.getValue().getPartyName().equals(name))
			{
				e.getValue().setPartyName(n);
			}
		}
		informMembersMessage("party-rename-inform", name, n);
		name = n;
	}

	@Override
	public int hashCode()
	{
		return name.hashCode();
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
		final Party other = (Party)obj;
		if(!Objects.equals(this.name, other.name))
		{
			return false;
		}
		return true;
	}

	public Member addMember(UUID uuid)
	{
		Member m;
		while((m = parties.findMember(uuid)) != null)
		{
			m.getParty().removeMember(uuid);
		}
		members.add(m = new Member(uuid));
		parties.addSettings(uuid, name);
		OfflinePlayer offp = Bukkit.getOfflinePlayer(uuid);
		if(offp.isOnline())
		{
			informMembersMessage("party-join-inform", offp.getPlayer().getDisplayName());
		}
		else
		{
			informMembersMessage("party-join-inform", offp.getName());
		}
		parties.getSettings(uuid).setPref(parties.getJoinFilterPref());
		return m;
	}
	public void removeMember(UUID uuid)
	{
		final boolean hadmembers = (numMembers() > 0);
		for(Iterator<Member> it = members.iterator(); it.hasNext();)
		{
			Member m = it.next();
			if(m.getUuid().equals(uuid))
			{
				m.informMessage("party-left-inform");
				it.remove();
				parties.removeSettings(uuid);
				break;
			}
		}
		informMembersMessage("party-leave-inform", Bukkit.getOfflinePlayer(uuid).getName());
		if(hadmembers && numMembers() == 0 && !parties.keepEmptyParties())
		{
			parties.remove(this, Bukkit.getPlayer(uuid));
		}
	}
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
	public Iterator<Member> iterator()
	{
		return members.iterator();
	}
	public int numMembers()
	{
		return members.size();
	}
	public Set<Member> getMembersOnline()
	{
		Set<Member> mems = new HashSet<>();
		for(Member m : this)
		{
			if(Bukkit.getOfflinePlayer(m.getUuid()).isOnline())
			{
				mems.add(m);
			}
		}
		return mems;
	}
	public Set<Member> getMembersAlive()
	{
		Set<Member> mems = new HashSet<>();
		for(Member m : this)
		{
			OfflinePlayer offp = Bukkit.getOfflinePlayer(m.getUuid());
			if(offp.isOnline() && !offp.getPlayer().isDead())
			{
				mems.add(m);
			}
		}
		return mems;
	}
	public Set<Member> getMembersRanked(Rank r)
	{
		Set<Member> mems = new HashSet<>();
		for(Member m : this)
		{
			if(m.getRank().equals(r))
			{
				mems.add(m);
			}
		}
		return mems;
	}

	public boolean canTp()
	{
		return tp;
	}
	public void setTp(boolean v)
	{
		tp = v;
		if(v)
		{
			informMembersMessage("party-teleports-enabled-inform");
		}
		else
		{
			informMembersMessage("party-teleports-enabled-inform");
		}
	}

	public boolean canPvp()
	{
		return pvp;
	}
	public void setPvp(boolean v)
	{
		pvp = v;
		if(v)
		{
			informMembersMessage("party-pvp-enabled-inform");
		}
		else
		{
			informMembersMessage("party-pvp-disabled-inform");
		}
	}

	public boolean isVisible()
	{
		return visible;
	}
	public void setVisible(boolean v)
	{
		visible = v;
		if(v)
		{
			informMembersMessage("party-visibility-enabled-inform");
		}
		else
		{
			informMembersMessage("party-visibility-disabled-inform");
		}
	}

	public void enableInventory()
	{
		if(inv == null)
		{
			inv = Bukkit.createInventory(null, 4 * 9, messenger.getMessage("party-inventory-gui-title", name));
			informMembersMessage("party-inventory-enable-inform");
		}
	}
	public Inventory getInventory()
	{
		return inv;
	}
	public void disableInventory(Player p)
	{
		if(inv != null)
		{
			for(ItemStack i : inv.getContents())
			{
				if(i != null)
				{
					p.getWorld().dropItem(p.getLocation(), i).setPickupDelay(0);
				}
			}
			inv = null;
			informMembersMessage("party-inventory-disable-inform");
		}
	}

	public void disband()
	{
		for(Member m : this.members.toArray(new Member[0]))
		{
			m.informMessage("party-disband-inform");
			removeMember(m.getUuid());
		}
	}

	public Double getHealth()
	{
		return health;
	}
	public void enableHealth()
	{
		Set<Member> mems = getMembersAlive();
		health = 1.0*mems.size();
		for(Member m : mems)
		{
			Bukkit.getPlayer(m.getUuid()).setMaxHealth(20.0*mems.size());
		}
		informMembersMessage("party-shared-health-xp-enable-inform");
	}
	public void disableHealth()
	{
		health = null;
		for(Member m : getMembersOnline())
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

	public static enum Rank
	{
		ADMIN,
		MODERATOR,
		MEMBER;

		@Override
		@Deprecated
		public String toString()
		{
			return super.toString();
		}
	}
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
	public class Member
	{
		private final UUID uuid;
		private Rank rank = Rank.MEMBER;
		private boolean tp = true;

		public Member(UUID id)
		{
			uuid = id;
		}

		@Deprecated
		public void inform(String message)
		{
			OfflinePlayer offp = Bukkit.getOfflinePlayer(uuid);
			if(offp.isOnline())
			{
				offp.getPlayer().sendMessage("[KataParty] "+message);
			}
		}
		public void informMessage(String name, Object... parameters)
		{
			OfflinePlayer offp = Bukkit.getOfflinePlayer(uuid);
			if(offp.isOnline())
			{
				messenger.tellMessage(offp.getPlayer(), name, parameters);
			}
		}

		public Party getParty()
		{
			return Party.this;
		}

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

		public Rank getRank()
		{
			return rank;
		}
		public String getRankName()
		{
			return rankName(getRank());
		}
		public void setRank(Rank r)
		{
			rank = r;
			informMessage("party-rank-inform", rankName(r));
		}

		public boolean canTp()
		{
			return tp;
		}
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
