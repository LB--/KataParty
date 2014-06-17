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

	public KataParty getPlugin()
	{
		return inst;
	}

	public void informMembers(String message)
	{
		for(Member m : this)
		{
			m.inform(message);
		}
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
		informMembers("Your KataParty was renamed from §n"+name+"§r to §n"+n+"§r");
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
		informMembers("§n"+inst.getServer().getOfflinePlayer(uuid).getName()+"§r has joined your KataParty");
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
		informMembers("§n"+inst.getServer().getOfflinePlayer(uuid).getName()+"§r has left your KataParty");
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
		if(v)
		{
			informMembers("Teleportation has been §nenabled§r for your KataParty");
		}
		else
		{
			informMembers("Teleportation has been §ndisabled§r for your KataParty");
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
			informMembers("PvP has been §nenabled§r for your KataParty");
		}
		else
		{
			informMembers("PvP has been §ndisabled§r for your KataParty");
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
			informMembers("Visibility has been §nenabled§r for your KataParty");
		}
		else
		{
			informMembers("Visibility has been §ndisabled§r for your KataParty");
		}
	}

	public void enableInventory()
	{
		if(inv == null)
		{
			inv = Bukkit.createInventory(null, 4 * 9, name + " Shared Inventory");
			informMembers("Shared Inventory has been §nenabled§r for your KataParty");
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
			informMembers("Shared Inventory has been §ndisabled§r for your KataParty");
		}
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

		public void inform(String message)
		{
			OfflinePlayer offp = inst.getServer().getOfflinePlayer(uuid);
			if(offp.isOnline())
			{
				offp.getPlayer().sendMessage("[KataParty] "+message);
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
		public void setRank(Rank r)
		{
			rank = r;
			inform("Your rank has been set to §n"+r+"§r");
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
				inform("Teleportation has been personally §nenabled§r for you");
			}
			else
			{
				inform("Teleportation has been personally §ndisabled§r for you");
			}
		}
	}
}
