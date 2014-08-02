package com.lb_stuff.kataparty.command;

import com.lb_stuff.kataparty.KataPartyPlugin;
import com.lb_stuff.kataparty.PartySettings;
import com.lb_stuff.kataparty.gui.PartyCreateGui;

import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class PartyCreateCommand extends PartyCommand
{
	public PartyCreateCommand(KataPartyPlugin plugin)
	{
		super(plugin);
	}

	@Override
	public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args)
	{
		if(sender instanceof Player)
		{
			Player player = (Player)sender;
			if(args.length >= 1)
			{
				PartySettings ps = new PartySettings(args[0]);
				for(int i = 1; i < args.length; ++i)
				{
					ps.setName(ps.getName()+" "+args[i]);
				}
				if(inst.getPartySet().findParty(ps.getName()) != null)
				{
					inst.tellMessage(player, "party-already-exists", ps.getName());
				}
				else
				{
					new PartyCreateGui(inst, player, ps).show();
				}
				return true;
			}
		}
		return false;
	}
}
