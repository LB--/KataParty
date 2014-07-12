package com.lb_stuff.kataparty.gui;

import com.lb_stuff.kataparty.KataPartyPlugin;
import com.lb_stuff.kataparty.Party;

import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;

import java.util.ArrayList;

public class PartyListGui extends PartyGui
{
	public PartyListGui(KataPartyPlugin plugin, Player plr)
	{
		super(plugin, plr, 6, "List of KataParties");

		int buttons = 0;
		for(final Party p : inst.getParties())
		{
			if(p.isVisible() || player.hasPermission("KataParty.seehidden"))
			{
				int online = 0;
				for(Party.Member mem : p)
				{
					if(inst.getServer().getPlayer(mem.getUuid()) != null)
					{
						++online;
					}
				}
				final int online_ = online;
				Party.Member mem = inst.getParties().findMember(player.getUniqueId());
				final boolean same = (mem != null && p == mem.getParty());
				addButton(buttons++, p.getName(), p.isVisible()? Material.NAME_TAG : Material.PAPER, new ArrayList<String>(){
				{
					if(!p.isVisible())
					{
						add("(invisible)");
					}
					add(online_+"/"+p.numMembers()+" members online");
					add("PvP: "+p.canPvp());
					add("TP: "+p.canTp());
					add("Shared Inv: "+(p.getInventory() != null? true : false));
					if(!same)
					{
						add("Left click to join (you will leave your current KataParty)");
					}
					else
					{
						add("You are in this KataParty");
					}
					if(player.hasPermission("KataParty.admin"))
					{
						add("Right click to administrate");
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
				p.addMember(player.getUniqueId());
				hide();
			} break;
			case RIGHT:
			{
				new PartyManageGui(inst, player, p).show();
			} break;
			default: break;
		}
	}
}
