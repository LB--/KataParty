package com.lb_stuff.kataparty;

import com.lb_stuff.kataparty.api.IMessenger;
import com.lb_stuff.kataparty.api.IParty;
import com.lb_stuff.kataparty.api.IParty.IMember;
import com.lb_stuff.kataparty.api.IPartySet;
import com.lb_stuff.kataparty.api.IPartySettings;
import com.lb_stuff.kataparty.api.IPartySettings.IMemberSettings;
import com.lb_stuff.kataparty.api.KataPartyService;
import com.lb_stuff.kataparty.api.PartyRank;
import com.lb_stuff.kataparty.api.event.PartyDisbandEvent;
import com.lb_stuff.kataparty.api.event.PartyMemberJoinEvent;
import com.lb_stuff.kataparty.api.event.PartyMemberLeaveEvent;
import com.lb_stuff.kataparty.api.event.PartySettingsChangeEvent;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.OfflinePlayer;
import org.bukkit.World;
import org.bukkit.entity.HumanEntity;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

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
			data.put("inv", true);
		}
		super.apply(PartySettings.deserialize(data));
		if(!(inventory instanceof Boolean))
		{
			ItemStack[] items = ((List<ItemStack>)inventory).toArray(new ItemStack[0]);
			enableInventory(items.length/9);
			inv.setContents(items);
		}
		List<?> mems = (List<?>)data.get("members");
		for(Object o : mems)
		{
			if(o instanceof IMember)
			{
				IMember m = (IMember)o;
				m.setParty(this);
				members.add(m);
			}
		}
		setAll(Metadatable.deserialize(data));
	}

	public Party(IPartySet ps, IPartySettings settings)
	{
		super(settings);
		parties = ps;
		messenger = ps.getMessenger();

		if(super.hasInventory())
		{
			setInventory(true);
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

	private boolean canChangeSettings(IPartySettings s)
	{
		PartySettingsChangeEvent psce = new PartySettingsChangeEvent(this, s);
		Bukkit.getPluginManager().callEvent(psce);
		return !psce.isCancelled();
	}

	@Override
	public void setName(String n)
	{
		String oldname = getName();
		PartySettings changes = new PartySettings(this);
		changes.setName(n);
		if(!canChangeSettings(changes))
		{
			return;
		}
		super.setName(n);
		informMembersMessage("party-rename-inform", oldname, getName());
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
	public Set<IMember> getMembers()
	{
		Set<IMember> mems = new HashSet<>();
		mems.addAll(members);
		return mems;
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
			disableInventory(p.getEyeLocation());
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
		if(!canChangeSettings(changes))
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
		if(!canChangeSettings(changes))
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
		if(!canChangeSettings(changes))
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

	private String getInvTitle()
	{
		return KataPartyPlugin.chopInvTitle(messenger.getMessage("party-inventory-gui-title", getName()));
	}
	@Override
	public boolean hasInventory()
	{
		return inv != null;
	}
	@Override
	public void enableInventory(int rows)
	{
		if(inv == null)
		{
			PartySettings changes = new PartySettings(this);
			changes.setInventory(true);
			if(!canChangeSettings(changes))
			{
				return;
			}
			inv = Bukkit.createInventory(this, rows*9, getInvTitle());
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
	public void resizeInventory(Location droploc, int rows)
	{
		ItemStack[] old = inv.getContents();
		for(int i = rows*9; i < inv.getSize(); ++i)
		{
			if(old[i] != null)
			{
				if(droploc != null)
				{
					droploc.getWorld().dropItem(droploc, old[i]).setPickupDelay(0);
				}
				else
				{
					World w = Bukkit.getServer().getWorlds().get(0);
					w.dropItemNaturally(w.getSpawnLocation(), old[i]);
				}
			}
		}
		ItemStack[] items = new ItemStack[rows*9];
		for(int i = 0; i < items.length && i < old.length; ++i)
		{
			items[i] = old[i];
		}
		inv.clear();
		List<HumanEntity> viewers = inv.getViewers();
		for(HumanEntity he : viewers)
		{
			he.closeInventory();
		}
		inv = Bukkit.createInventory(this, rows*9, getInvTitle());
		inv.setContents(items);
		for(HumanEntity he : viewers)
		{
			he.openInventory(inv);
		}
	}
	@Override
	public void disableInventory(Location droploc)
	{
		if(inv != null)
		{
			PartySettings changes = new PartySettings(this);
			changes.setInventory(false);
			if(!canChangeSettings(changes))
			{
				return;
			}
			for(HumanEntity he : inv.getViewers())
			{
				he.closeInventory();
			}
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
	@Override
	public void setInventory(boolean enabled)
	{
		if(enabled)
		{
			enableInventory(KataPartyPlugin.getInst().getConfig().getInt("shared-inventory-rows"));
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
		if(!canChangeSettings(changes))
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
		if(!canChangeSettings(changes))
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
		public void setRank(PartyRank r)
		{
			super.setRank(r);
			informMessage("party-rank-inform", r.getName(getParty().getPartySet().getMessenger()));
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
