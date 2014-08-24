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
import java.util.HashMap;
import java.util.Map;
import java.util.Scanner;

public final class SmartConfig extends YamlConfiguration
{
	public SmartConfig(File f) throws IOException, InvalidConfigurationException
	{
		reload(f);
	}

	public interface NodeProcessor
	{
		Object process(Configuration current, Configuration defaults, String node);
	}

	private final Map<String, NodeProcessor> procs = new HashMap<>();
	public NodeProcessor setProcessor(String node, NodeProcessor processor)
	{
		if(node == null)
		{
			throw new IllegalArgumentException("node cannot be null");
		}
		if(processor == null)
		{
			return procs.remove(node);
		}
		return procs.put(node, processor);
	}

	public void reload(File f) throws IOException, InvalidConfigurationException
	{
		if(f == null)
		{
			throw new IllegalArgumentException("File cannot be null");
		}
		if(!f.exists())
		{
			f.createNewFile();
		}
		regenConfig(f, YamlConfiguration.loadConfiguration(f));
	}
	private void regenConfig(File f, Configuration current) throws IOException, InvalidConfigurationException
	{
		String t = getTemplate();
		StringBuilder result = new StringBuilder();
		int i1, i2;
		while((i1 = t.indexOf("[%")) >= 0 && (i2 = t.indexOf("%]")) >= 0)
		{
			result.append(t.substring(0, i1));
			String node = t.substring(i1+2, i2);
			t = t.substring(i2+2);
			YamlConfiguration temp = new YamlConfiguration();
			if(procs.containsKey(node))
			{
				temp.set("t", procs.get(node).process(current, getDefaultConfig(), node));
			}
			else
			{
				if(!current.contains(node))
				{
					temp.set("t", getDefaultConfig().get(node));
				}
				else
				{
					temp.set("t", current.get(node));
				}
			}
			String replacement;
			if(temp.saveToString().length() < 2)
			{
				replacement = "null";
			}
			else
			{
				replacement = temp.saveToString().substring(2);
			}
			if(replacement.endsWith("\n"))
			{
				replacement = replacement.substring(0, replacement.length()-1);
			}
			if(result.toString().endsWith(" "))
			{
				result.deleteCharAt(result.length()-1);
			}
			result.append(replacement);
		}
		result.append(t);
		f.createNewFile();
		try(PrintWriter pw = new PrintWriter(f))
		{
			pw.append(result);
		}
		load(new CharArrayReader(result.toString().toCharArray()));
	}

	private static YamlConfiguration defconfig = null;
	private static YamlConfiguration getDefaultConfig()
	{
		if(defconfig == null)
		{
			defconfig = YamlConfiguration.loadConfiguration
			(
				new InputStreamReader(SmartConfig.class.getResourceAsStream("config-defaults.yml"))
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
				SmartConfig.class.getResourceAsStream("config-template.yml")
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
