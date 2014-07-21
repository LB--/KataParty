package com.lb_stuff.kataparty.gui;

import com.lb_stuff.kataparty.KataPartyPlugin;
import com.lb_stuff.kataparty.api.IParty;
import com.lb_stuff.kataparty.api.event.PartyMemberJoinEvent;

import org.bukkit.Material;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;

import java.util.ArrayList;

public final class PartyListGui extends PartyGui
{
	public PartyListGui(KataPartyPlugin plugin, Player plr)
	{
		super(plugin, plr, 6, plugin.getMessage("list-gui-title"));
	}

	@Override
	protected void update()
	{
		clearButtons();

		int buttons = 0;
		for(final IParty p : inst.getParties())
		{
			if(p.isVisible() || player.hasPermission("KataParty.seehidden"))
			{
				IParty.IMember mem = inst.getParties().findMember(player.getUniqueId());
				final boolean same = (mem != null && p == mem.getParty());
				addButton(buttons++, p.getName(), p.isVisible()? Material.NAME_TAG : Material.PAPER, new ArrayList<String>(){
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
					if(same)
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
			}
		}
	}

	@Override
	protected void onButton(int slot, ClickType click)
	{
		IParty p = inst.getParties().findParty(getButtonName(slot));
		if(p == null)
		{
			return;
		}
		switch(click)
		{
			case LEFT:
			{
				IParty.IMember m = inst.getParties().findMember(player.getUniqueId());
				if(m == null || m.getParty() != p)
				{
					if(!p.isInviteOnly() || player.hasPermission("KataParty.admin"))
					{
						if(p.newMember(player.getUniqueId(), PartyMemberJoinEvent.Reason.VOLUNTARY) != null)
						{
							inst.getFilter().tellFilterPref(player);
						}
						hide();
					}
				}
			} break;
			case RIGHT:
			{
				new PartyManageGui(inst, player, p).show();
			} break;
			default: break;
		}
	}
}
