package com.lb_stuff.kataparty.config;

import org.bukkit.configuration.Configuration;
import org.bukkit.configuration.file.YamlConfiguration;

import java.util.*;
import java.io.*;

public final class MainConfig
{
	private static final YamlConfiguration DEFAULTS = YamlConfiguration.loadConfiguration
	(
		new InputStreamReader(MainConfig.class.getResourceAsStream("config-defaults.yml"))
	);
	private static final String TEMPLATE = new Scanner
	(
		MainConfig.class.getResourceAsStream("config-template.yml")
	).useDelimiter("\\A").next();
	private Configuration config;
	public MainConfig(File f) throws IOException
	{
		reload(f);
	}
	public void reload(File f) throws IOException
	{
		if(!f.exists())
		{
			f.createNewFile();
		}
		config = regenConfig(f, YamlConfiguration.loadConfiguration(f));
	}
	private Configuration regenConfig(File f, Configuration current) throws IOException
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
		return YamlConfiguration.loadConfiguration(new CharArrayReader(result.toCharArray()));
	}

	public Object get(String path)
	{
		return config.get(path);
	}
}
