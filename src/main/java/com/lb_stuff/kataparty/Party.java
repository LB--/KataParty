package com.lb_stuff.kataparty;

import com.lb_stuff.kataparty.api.IPartySet;
import com.lb_stuff.kataparty.api.KataPartyService;
import com.lb_stuff.kataparty.api.IMessenger;
import com.lb_stuff.kataparty.api.IParty;
import static com.lb_stuff.kataparty.api.IParty.IMember;
import com.lb_stuff.kataparty.api.PartyRank;
import com.lb_stuff.kataparty.api.IPartySettings;
import static com.lb_stuff.kataparty.api.IPartySettings.IMemberSettings;
import com.lb_stuff.kataparty.api.event.PartyDisbandEvent;
import com.lb_stuff.kataparty.api.event.PartyMemberJoinEvent;
import com.lb_stuff.kataparty.api.event.PartyMemberLeaveEvent;
import com.lb_stuff.kataparty.api.event.PartySettingsChangeEvent;

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
	private final IPartySet parties;
	private final IMessenger messenger;
	private final Set<IMember> members = new HashSet<>();
	private Inventory inv = null;

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
		parties = plugin.getPartySet();
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
			members.add(m);
		}
		setAll(Metadatable.deserialize(data));
	}

	public Party(IPartySet ps, IPartySettings settings)
	{
		super(settings);
		parties = ps;
		messenger = ps.getMessenger();
		cloneAll(settings);

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
	public IPartySet getPartySet()
	{
		return parties;
	}

	private boolean changeSettings(IPartySettings s)
	{
		PartySettingsChangeEvent psce = new PartySettingsChangeEvent(this, s);
		Bukkit.getPluginManager().callEvent(psce);
		return !psce.isCancelled();
	}

	@Override
	public void setName(String n)
	{
		PartySettings changes = new PartySettings(this);
		changes.setName(n);
		if(!changeSettings(changes))
		{
			return;
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
	public IMember newMember(IMemberSettings settings, PartyMemberJoinEvent.Reason r)
	{
		if(disbanded)
		{
			return null;
		}

		{
			IMember m;
			while((m = getPartySet().findMember(settings.getUuid())) != null)
			{
				if(!m.getParty().removeMember(settings.getUuid(), PartyMemberLeaveEvent.Reason.SWITCH_PARTIES))
				{
					return null;
				}
			}
		}
		if(r != null)
		{
			PartyMemberJoinEvent pmje = new PartyMemberJoinEvent(this, settings, r);
			Bukkit.getServer().getPluginManager().callEvent(pmje);
			if(pmje.isCancelled())
			{
				return null;
			}
		}
		IMember m = getPartySet().getMemberFactory(settings.getClass()).create(this, settings);
		if(m != null)
		{
			members.add(m);
			OfflinePlayer offp = Bukkit.getOfflinePlayer(settings.getUuid());
			if(offp.isOnline())
			{
				informMembersMessage("party-join-inform", offp.getPlayer().getDisplayName());
				m.informMessage("manage-hint");
			}
			else
			{
				informMembersMessage("party-join-inform", offp.getName());
			}
			m.setTp(m.canTp()); //shows message
		}
		return m;
	}
	@Override
	public boolean removeMember(UUID uuid, PartyMemberLeaveEvent.Reason r)
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
					return false;
				}
				if(!disbanded)
				{
					m.informMessage("party-left-inform");
				}
				it.remove();
				break;
			}
			m = null;
		}
		if(!disbanded)
		{
			informMembersMessage("party-leave-inform", Bukkit.getOfflinePlayer(uuid).getName());
			if(hadmembers && numMembers() == 0 && !parties.keepEmptyParties() && !isSticky())
			{
				parties.remove(this, PartyDisbandEvent.Reason.AUTOMATIC_CLOSE, Bukkit.getPlayer(uuid));
				if(m != null)
				{
					m.informMessage("party-closed-on-leave-inform", getName());
				}
			}
		}
		if(m != null)
		{
			m.setParty(null);
		}
		return true;
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
	public Set<IMember> getMembersRanked(PartyRank r)
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
		PartySettings changes = new PartySettings(this);
		changes.setTp(enabled);
		if(!changeSettings(changes))
		{
			return;
		}
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
		PartySettings changes = new PartySettings(this);
		changes.setPvp(enabled);
		if(!changeSettings(changes))
		{
			return;
		}
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
		PartySettings changes = new PartySettings(this);
		changes.setVisible(enabled);
		if(!changeSettings(changes))
		{
			return;
		}
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
		PartySettings changes = new PartySettings(this);
		changes.setInventory(true);
		if(!changeSettings(changes))
		{
			return;
		}
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
		PartySettings changes = new PartySettings(this);
		changes.setInventory(false);
		if(!changeSettings(changes))
		{
			return;
		}
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
	public void setInviteOnly(boolean enabled)
	{
		PartySettings changes = new PartySettings(this);
		changes.setInviteOnly(enabled);
		if(!changeSettings(changes))
		{
			return;
		}
		if(enabled)
		{
			informMembersMessage("party-invite-only-inform");
		}
		else
		{
			informMembersMessage("party-public-inform");
		}
		super.setInviteOnly(enabled);
	}

	@Override
	public void setHealthShared(boolean enabled)
	{
		PartySettings changes = new PartySettings(this);
		changes.setHealthShared(enabled);
		if(!changeSettings(changes))
		{
			return;
		}
		if(enabled)
		{
			//...
			informMembersMessage("party-shared-health-enable-inform");
		}
		else
		{
			//...
			informMembersMessage("party-shared-health-disable-inform");
		}
		super.setHealthShared(enabled);
	}

	@Override
	public String rankName(PartyRank r)
	{
		switch(r)
		{
			case ADMIN: return messenger.getMessage("party-rank-admin");
			case MODERATOR: return messenger.getMessage("party-rank-moderator");
			case MEMBER: return messenger.getMessage("party-rank-member");
			default: throw new IllegalStateException();
		}
	}
	public static class Member extends MemberSettings implements IMember
	{
		private IParty p;

		@Override
		public Map<String, Object> serialize()
		{
			Map<String, Object> data = super.serialize();
			return data;
		}
		public Member(Map<String, Object> data)
		{
			super(MemberSettings.deserialize(data));
			setAll(Metadatable.deserialize(data));
		}
		@Override @Deprecated
		public void setParty(IParty party)
		{
			p = party;
		}

		public Member(IParty party, IMemberSettings settings)
		{
			super(settings);
			p = party;
			cloneAll(settings);
		}

		@Override @Deprecated
		public void inform(String message)
		{
			OfflinePlayer offp = Bukkit.getOfflinePlayer(getUuid());
			if(offp.isOnline())
			{
				offp.getPlayer().sendMessage("[KataParty] "+message);
			}
		}
		@Override
		public void informMessage(String name, Object... parameters)
		{
			OfflinePlayer offp = Bukkit.getOfflinePlayer(getUuid());
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
		public String getRankName()
		{
			return p.rankName(getRank());
		}
		@Override
		public void setRank(PartyRank r)
		{
			super.setRank(r);
			informMessage("party-rank-inform", p.rankName(r));
		}

		@Override
		public void setTp(boolean v)
		{
			if(v)
			{
				informMessage("party-self-teleports-enable-inform");
			}
			else
			{
				informMessage("party-self-teleports-disable-inform");
			}
			super.setTp(v);
		}
	}
}
