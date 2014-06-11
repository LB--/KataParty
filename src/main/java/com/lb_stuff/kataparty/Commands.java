package com.lb_stuff.kataparty;

import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.Command;
import org.bukkit.entity.Player;

public class Commands implements CommandExecutor
{
	private KataParty inst;

	public Commands(KataParty instance)
	{
		inst = instance;
	}

	@Override
	public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args)
	{
		if(sender instanceof Player)
		{
			String cmdname = cmd.getName().toLowerCase();
			switch(cmdname)
			{
				case "kpcreate":
				{
					//
				} break;
				case "kplist":
				{
					//
				} break;
				case "kpjoin":
				{
					//
				} break;
				case "kpleave":
				{
					//
				} break;
				case "kpdisband":
				{
					//
				} break;
				case "kpclose":
				{
					//
				} break;
				case "kpmanage":
				{
					//
				} break;
				case "kpadmin":
				{
					//
				} break;
				case "kptp":
				{
					//
				} break;
				case "kpshare":
				{
					//
				} break;
			}
		}
		return false;
	}
}
