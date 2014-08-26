package com.lb_stuff.bukkit.config;


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
	private final File file;
	private final String template;
	private final Configuration defconf;
	public SmartConfig(File f, String templ, Configuration def) throws IOException, InvalidConfigurationException
	{
		if(f == null)
		{
			throw new IllegalArgumentException("File cannot be null");
		}
		file = f;
		if(templ == null)
		{
			throw new IllegalArgumentException("Template cannot be null");
		}
		template = templ;
		if(def == null)
		{
			defconf = new YamlConfiguration();
		}
		else
		{
			defconf = def;
		}
	}
	public SmartConfig(File f, Class<?> clazz) throws IOException, InvalidConfigurationException
	{
		this
		(
			f,
			new Scanner(clazz.getResourceAsStream("config-template.yml")).useDelimiter("\\A").next(),
			YamlConfiguration.loadConfiguration(new InputStreamReader(clazz.getResourceAsStream("config-defaults.yml")))
		);
	}

	public interface NodeProcessor
	{
		Object process(SmartConfig sc, Configuration current, Configuration defaults, String node);
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

	public void reload() throws IOException, InvalidConfigurationException
	{
		file.createNewFile();
		Configuration current = YamlConfiguration.loadConfiguration(file);
		String t = template;
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
				temp.set("t", procs.get(node).process(this, current, defconf, node));
			}
			else
			{
				if(!current.contains(node))
				{
					temp.set("t", defconf.get(node));
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
		file.createNewFile();
		try(PrintWriter pw = new PrintWriter(file))
		{
			pw.append(result);
		}
		load(new CharArrayReader(result.toString().toCharArray()));
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
