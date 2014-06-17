package com.lb_stuff.kataparty;

import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import java.util.*;

public class Party implements Iterable<Party.Member>
{
	private final KataParty inst;
	private String name;
	private final Set<Member> members = new HashSet<>();
	private boolean tp = true;
	private boolean pvp = false;
	private boolean visible = true;
	private Inventory inv = null;

	public Party(final KataParty inst, String pname)
	{
		this.inst = inst;
		name = pname;
	}

	public String getName()
	{
		return name;
	}
	public void rename(String n)
	{
		for(Map.Entry<UUID, String> e : inst.partiers.entrySet())
		{
			if(e.getValue().equals(name))
			{
				e.setValue(n);
			}
		}
		name = n;
	}

	public Member addMember(UUID uuid)
	{
		Member m;
		while((m = inst.findMember(uuid)) != null)
		{
			m.getParty().removeMember(uuid);
		}
		members.add(m = new Member(uuid));
		inst.partiers.put(uuid, name);
		for(Member mi : members)
		{
			Player p = inst.getServer().getPlayer(mi.getUuid());
			if(p != null)
			{
				p.sendMessage(inst.getServer().getPlayer(uuid).getName() + " has joined your KataParty");
			}
		}
		return m;
	}
	public void removeMember(UUID uuid)
	{
		for(Iterator<Member> it = members.iterator(); it.hasNext();)
		{
			Member m = it.next();
			if(m.getUuid().equals(uuid))
			{
				it.remove();
				inst.partiers.remove(uuid);
				break;
			}
		}
		for(Member m : members)
		{
			Player p = inst.getServer().getPlayer(m.getUuid());
			if(p != null)
			{
				p.sendMessage(inst.getServer().getOfflinePlayer(uuid).getName() + " has left your KataParty");
			}
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
			OfflinePlayer offp = inst.getServer().getOfflinePlayer(m.getUuid());
			if(offp.getName().equalsIgnoreCase(name))
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

	public boolean canTp()
	{
		return tp;
	}
	public void setTp(boolean v)
	{
		tp = v;
	}

	public boolean canPvp()
	{
		return pvp;
	}
	public void setPvp(boolean v)
	{
		pvp = v;
	}

	public boolean isVisible()
	{
		return visible;
	}
	public void setVisible(boolean v)
	{
		visible = v;
	}

	public void enableInventory()
	{
		if(inv == null)
		{
			inv = Bukkit.createInventory(null, 4 * 9, name + " Shared Inventory");
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
		}
	}

	public KataParty getPlugin()
	{
		return inst;
	}

	public static enum Rank
	{
		ADMIN,
		MODERATOR,
		MEMBER;
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
		public void setRank(Rank r)
		{
			rank = r;
		}

		public boolean canTp()
		{
			return tp;
		}
		public void setTp(boolean v)
		{
			tp = v;
		}
	}
}
