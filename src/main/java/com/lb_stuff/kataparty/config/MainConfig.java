package com.lb_stuff.kataparty.config;

import org.bukkit.ChatColor;
import org.bukkit.configuration.Configuration;
import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.CharArrayReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.util.Scanner;

public final class MainConfig extends YamlConfiguration
{
	public MainConfig(File f) throws IOException, InvalidConfigurationException
	{
		reload(f);
	}
	public void reload(File f) throws IOException, InvalidConfigurationException
	{
		if(!f.exists())
		{
			f.createNewFile();
		}
		regenConfig(f, YamlConfiguration.loadConfiguration(f));
	}
	private void regenConfig(File f, Configuration current) throws IOException, InvalidConfigurationException
	{
		String t = getTemplate();
		String result = "";
		int i1, i2;
		while((i1 = t.indexOf("[%")) >= 0 && (i2 = t.indexOf("%]")) >= 0)
		{
			result += t.substring(0, i1);
			String value = t.substring(i1+2, i2);
			t = t.substring(i2+2);
			if(value.startsWith("!"))
			{
				value = value.substring(1);
				//
			}
			else
			{
				YamlConfiguration node = new YamlConfiguration();
				if(!current.contains(value))
				{
					node.set("t", getDefaultConfig().get(value));
				}
				else
				{
					node.set("t", current.get(value));
				}
				String replacement;
				if(node.saveToString().length() < 2)
				{
					replacement = "null";
				}
				else
				{
					replacement = node.saveToString().substring(2);
				}
				if(replacement.endsWith("\n"))
				{
					replacement = replacement.substring(0, replacement.length()-1);
				}
				if(result.endsWith(" "))
				{
					result = result.substring(0, result.length()-1);
				}
				result += replacement;
			}
		}
		result += t;
		f.createNewFile();
		try(PrintWriter pw = new PrintWriter(f))
		{
			pw.append(result);
		}
		load(new CharArrayReader(result.toCharArray()));
	}

	private static YamlConfiguration defconfig = null;
	private static YamlConfiguration getDefaultConfig()
	{
		if(defconfig == null)
		{
			defconfig = YamlConfiguration.loadConfiguration
			(
				new InputStreamReader(MainConfig.class.getResourceAsStream("config-defaults.yml"))
			);
		}
		return defconfig;
	}
	private static String template = null;
	private static String getTemplate()
	{
		if(template == null)
		{
			template = new Scanner
			(
				MainConfig.class.getResourceAsStream("config-template.yml")
			).useDelimiter("\\A").next();
		}
		return template;
	}

	@Override
	public String getString(String path)
	{
		String str = super.getString(path);
		if(str != null)
		{
			return ChatColor.translateAlternateColorCodes('&', str);
		}
		return str;
	}
}
