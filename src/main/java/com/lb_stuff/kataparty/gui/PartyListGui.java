package com.lb_stuff.kataparty.gui;

import com.lb_stuff.kataparty.KataPartyPlugin;
import static com.lb_stuff.kataparty.PartySettings.MemberSettings;
import com.lb_stuff.kataparty.api.IParty;
import com.lb_stuff.kataparty.api.event.PartyMemberJoinEvent;

import org.bukkit.Material;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.event.inventory.ClickType;

import java.util.ArrayList;

public final class PartyListGui extends PartyGui
{
	public PartyListGui(KataPartyPlugin plugin, Player plr)
	{
		super(plugin, plr, 6, plugin.getMessage("list-gui-title"));
	}

	private class ListedPartyButton extends GenericGuiButton
	{
		private final IParty p;
		public ListedPartyButton(IParty party)
		{
			super(party.isVisible()? Material.NAME_TAG : Material.PAPER);
			p = party;
		}
		@Override
		public ItemStack display()
		{
			if(inst.getPartySet().contains(p))
			{
				setName(p.getName());
				setLore(new ArrayList<String>(){
				{
					if(!p.isVisible())
					{
						add(inst.getMessage("list-invisible"));
					}
					add(inst.getMessage("list-members-online", p.getMembersOnline().size(), p.numMembers()));
					add(inst.getMessage("list-pvp", p.canPvp()));
					add(inst.getMessage("list-teleports", p.canTp()));
					add(inst.getMessage("list-inventory", (p.getInventory() != null)));
					add(inst.getMessage("list-invite-only", p.isInviteOnly()));
					if(inst.getPartySet().findMember(player.getUniqueId()) == p)
					{
						add(inst.getMessage("list-member"));
					}
					else if(!p.isInviteOnly() || player.hasPermission("KataParty.admin"))
					{
						add(inst.getMessage("list-join"));
					}
					if(player.hasPermission("KataParty.admin"))
					{
						add(inst.getMessage("list-admin"));
					}
					if(p.numMembers() > 0)
					{
						add("--------");
						for(IParty.IMember m : p)
						{
							OfflinePlayer offp = inst.getServer().getOfflinePlayer(m.getUuid());
							Player onp = offp.getPlayer();
							if(offp.isOnline())
							{
								add(onp.getDisplayName());
							}
							else
							{
								add(offp.getName());
							}
						}
					}
				}});
				return super.display();
			}
			return null;
		}
		@Override
		public boolean onClick(ClickType click)
		{
			switch(click)
			{
				case LEFT:
				{
					IParty.IMember m = inst.getPartySet().findMember(player.getUniqueId());
					if(m == null || m.getParty() != p)
					{
						if(!p.isInviteOnly() || player.hasPermission("KataParty.admin"))
						{
							p.newMember(new MemberSettings(player.getUniqueId()), PartyMemberJoinEvent.Reason.VOLUNTARY);
							hide();
							return true;
						}
					}
				} break;
				case RIGHT:
				{
					new PartyManageGui(inst, player, p).show();
					return true;
				} //break;
				default: break;
			}
			return false;
		}
	}

	@Override
	protected void onUpdate()
	{
		clearButtons();

		int buttons = 0;
		for(final IParty p : inst.getPartySet())
		{
			if(p.isVisible() || player.hasPermission("KataParty.seehidden"))
			{
				IParty.IMember mem = inst.getPartySet().findMember(player.getUniqueId());
				final boolean same = (mem != null && p == mem.getParty());
				int slotn = buttons++;
				addButton(slotn, new ListedPartyButton(p));
			}
		}
	}
}
