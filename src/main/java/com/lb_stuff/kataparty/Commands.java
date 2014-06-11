package com.lb_stuff.kataparty;

import org.bukkit.command.*;
import org.bukkit.entity.Player;

import java.util.*;

public class Commands implements CommandExecutor, TabCompleter
{
	private KataParty inst;

	public Commands(KataParty instance)
	{
		inst = instance;
	}

	@Override
	public List<String> onTabComplete(CommandSender sender, Command cmd, String label, String[] args)
	{
		if(sender instanceof Player)
		{
			Player player = (Player)sender;
			String cmdname = cmd.getName().toLowerCase();
			switch(cmdname)
			{
				case "kpjoin":
				{
					//
				} break;
				case "kpclose":
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
			}
		}
		return null;
	}
	@Override
	public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args)
	{
		if(sender instanceof Player)
		{
			Player player = (Player)sender;
			String cmdname = cmd.getName().toLowerCase();
			switch(cmdname)
			{
				case "kataparty":
				{
					//
				} break;
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
