package com.lb_stuff.kataparty.gui;

import com.lb_stuff.kataparty.KataPartyPlugin;
import com.lb_stuff.kataparty.Party;

import org.bukkit.Material;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;

import java.util.ArrayList;

public class PartyListGui extends PartyGui
{
	public PartyListGui(KataPartyPlugin plugin, Player plr)
	{
		super(plugin, plr, 6, plugin.getMessage("list-gui-title"));

		int buttons = 0;
		for(final Party p : inst.getParties())
		{
			if(p.isVisible() || player.hasPermission("KataParty.seehidden"))
			{
				Party.Member mem = inst.getParties().findMember(player.getUniqueId());
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
					if(!same)
					{
						add(inst.getMessage("list-join"));
					}
					else
					{
						add(inst.getMessage("list-member"));
					}
					if(player.hasPermission("KataParty.admin"))
					{
						add(inst.getMessage("list-admin"));
					}
					if(p.numMembers() > 0)
					{
						add("--------");
						for(Party.Member m : p)
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
		Party p = inst.getParties().findParty(getButtonName(slot));
		if(p == null)
		{
			new PartyListGui(inst, player).show();
			return;
		}
		switch(click)
		{
			case LEFT:
			{
				if(inst.getParties().findMember(player.getUniqueId()).getParty() != p)
				{
					p.addMember(player.getUniqueId());
					hide();
				}
				else
				{
					new PartyListGui(inst, player).show();
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
