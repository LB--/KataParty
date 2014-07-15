package com.lb_stuff.kataparty.config;

import org.bukkit.configuration.Configuration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.ChatColor;

import java.util.*;
import java.io.*;

public final class MainConfig extends YamlConfiguration
{
	private static final YamlConfiguration DEFAULTS = YamlConfiguration.loadConfiguration
	(
		new InputStreamReader(MainConfig.class.getResourceAsStream("config-defaults.yml"))
	);
	private static final String TEMPLATE = new Scanner
	(
		MainConfig.class.getResourceAsStream("config-template.yml")
	).useDelimiter("\\A").next();
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
		String template = TEMPLATE;
		String result = "";
		int i1, i2;
		while((i1 = template.indexOf("[%")) >= 0 && (i2 = template.indexOf("%]")) >= 0)
		{
			result += template.substring(0, i1);
			String value = template.substring(i1+2, i2);
			template = template.substring(i2+2);
			Object v = current.get(value);
			if(v == null)
			{
				v = DEFAULTS.get(value);
			}
			result += v.toString().replaceAll("\"", "\\\\\"");
		}
		result += template;
		f.createNewFile();
		try(PrintWriter pw = new PrintWriter(f))
		{
			pw.append(result);
		}
		load(new CharArrayReader(result.toCharArray()));
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
