/*
 *   Wormhole X-Treme Plugin for Bukkit
 *   Copyright (C) 2011  Ben Echols
 *                       Dean Bailey
 *
 *   This program is free software: you can redistribute it and/or modify
 *   it under the terms of the GNU General Public License as published by
 *   the Free Software Foundation, either version 3 of the License, or
 *   (at your option) any later version.
 *
 *   This program is distributed in the hope that it will be useful,
 *   but WITHOUT ANY WARRANTY; without even the implied warranty of
 *   MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *   GNU General Public License for more details.
 *
 *   You should have received a copy of the GNU General Public License
 *   along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package com.wormhole_xtreme.wormhole; 
 
import java.util.ArrayList;
import java.util.HashMap; 
import java.util.logging.Logger;
import java.util.logging.Level;

import org.bukkit.entity.Player; 
import org.bukkit.event.Event; 
import org.bukkit.event.Event.Priority; 
import org.bukkit.plugin.PluginDescriptionFile; 
import org.bukkit.plugin.PluginManager; 
import org.bukkit.plugin.java.JavaPlugin; 
import org.bukkit.scheduler.BukkitScheduler;

import com.nijiko.coelho.iConomy.iConomy;
import com.nijiko.permissions.PermissionHandler;

import com.wormhole_xtreme.wormhole.command.*;
import com.wormhole_xtreme.wormhole.config.ConfigManager;
import com.wormhole_xtreme.wormhole.config.Configuration;
import com.wormhole_xtreme.wormhole.logic.StargateHelper;
import com.wormhole_xtreme.wormhole.model.Stargate;
import com.wormhole_xtreme.wormhole.model.StargateDBManager;
import com.wormhole_xtreme.wormhole.model.StargateManager;
import com.wormhole_xtreme.wormhole.permissions.PermissionsManager;
import com.wormhole_xtreme.wormhole.plugin.HelpSupport;
import com.wormhole_xtreme.wormhole.plugin.IConomySupport;
import com.wormhole_xtreme.wormhole.plugin.PermissionsSupport;
import com.wormhole_xtreme.wormhole.utils.DBUpdateUtil;

import me.taylorkelly.help.Help;

// TODO: Auto-generated Javadoc
/**
 * WormholeXtreme for Bukkit.
 *
 * @author Ben Echols (Lologarithm)
 * @author Dean Bailey (alron)
 */ 
public class WormholeXTreme extends JavaPlugin
{

	/** The player listener. */
	private final WormholeXTremePlayerListener playerListener = new WormholeXTremePlayerListener();
	/** The block listener. */
	private final WormholeXTremeBlockListener blockListener = new WormholeXTremeBlockListener();
	/** The vehicle listener. */
	private final WormholeXTremeVehicleListener vehicleListener = new WormholeXTremeVehicleListener();
	/** The entity listener. */
	private final WormholeXTremeEntityListener entityListener = new WormholeXTremeEntityListener();
	/** The server listener. */
	private final WormholeXTremeServerListener serverListener = new WormholeXTremeServerListener();
	
	/** The debugees. */
	private final HashMap<Player, Boolean> debugees = new HashMap<Player, Boolean>();

	
	/** The Permissions. */
	public static volatile PermissionHandler permissions = null;
	
	/** The Iconomy. */
	public static volatile iConomy iconomy = null;
	
	/** The Help. */
	public static volatile Help help = null;
	
	/** The Scheduler. */
	public static BukkitScheduler scheduler = null;
	
	/** The This plugin. */
	public static WormholeXTreme thisPlugin = null;
	 
	/** The log. */
//	private static Logger log;
	private static Logger log = null;
	
	/* (non-Javadoc)
	 * @see org.bukkit.plugin.java.JavaPlugin#onLoad()
	 */
	@Override
	public void onLoad()
	{
	   log = this.getServer().getLogger(); 
	   thisPlugin = this; 
	   scheduler = getServer().getScheduler();
	   PluginDescriptionFile pdfFile = this.getDescription();
	   
	   prettyLog(Level.INFO,true, pdfFile.getAuthors() + "Load Beginning." );
	   // Load our config files and set logging level right away.
	   ConfigManager.setupConfigs(pdfFile);
	   this.setPrettyLogLevel(ConfigManager.getLogLevel());
	   // Make sure DB is up to date with latest SCHEMA
	   DBUpdateUtil.updateDB();
	   // Load our shapes, stargates, and internal permissions.
	   StargateHelper.loadShapes();	 
	   StargateDBManager.LoadStargates(getServer());
	   PermissionsManager.LoadPermissions();
	   prettyLog(Level.INFO,true, "Load Completed.");
	}
	
	/* (non-Javadoc)
	 * @see org.bukkit.plugin.Plugin#onEnable()
	 */
	@Override
    public void onEnable()
	{ 
		prettyLog(Level.INFO,true,"Enable Beginning.");
		// Try and attach to Permissions and iConomy and Help
		try
		{
			PermissionsSupport.enablePermissions();
			IConomySupport.enableIconomy();
			HelpSupport.enableHelp();
		}
		catch ( Exception e)
		{
			
		}
		// Register our events and commands
		registerEvents();
		registerCommands();
		HelpSupport.registerHelpCommands();
		prettyLog(Level.INFO, true, "Enable Completed.");
	}
	
	/**
	 * Register commands.
	 */
	private void registerCommands()
	{
	    getCommand("wxforce").setExecutor(new Force());
		getCommand("wxidc").setExecutor(new WXIDC(this));
		getCommand("wxcompass").setExecutor(new Compass());
		getCommand("wxcomplete").setExecutor(new Complete());
		getCommand("wxremove").setExecutor(new WXRemove(this));
		getCommand("wxlist").setExecutor(new WXList(this));
		getCommand("wxgo").setExecutor(new Go());
		getCommand("dial").setExecutor(new Dial());
		getCommand("wxbuild").setExecutor(new Build());
		getCommand("wormhole").setExecutor(new Wormhole(this));
	}

    /**
     * Register events.
     */
    private void registerEvents() 
    {
		final PluginManager pm = getServer().getPluginManager(); 
		
		//Listen for Interact, Physics, Break, Flow, and RightClick events. Pass to blockListener
		
		pm.registerEvent(Event.Type.BLOCK_PHYSICS, blockListener, Priority.Highest, this);
		pm.registerEvent(Event.Type.BLOCK_BREAK, blockListener, Priority.High, this);
		pm.registerEvent(Event.Type.BLOCK_FROMTO, blockListener, Priority.Highest, this);
		pm.registerEvent(Event.Type.BLOCK_IGNITE, blockListener, Priority.High, this);
		pm.registerEvent(Event.Type.BLOCK_BURN, blockListener, Priority.High, this);
		pm.registerEvent(Event.Type.BLOCK_DAMAGE, blockListener, Priority.High, this);
		
		// To handle teleporting when walking into a gate.
		pm.registerEvent(Event.Type.PLAYER_MOVE, playerListener, Priority.High, this);
		pm.registerEvent(Event.Type.PLAYER_INTERACT, playerListener, Priority.High, this);
		pm.registerEvent(Event.Type.PLAYER_BUCKET_FILL, playerListener, Priority.High, this);
		pm.registerEvent(Event.Type.PLAYER_BUCKET_EMPTY, playerListener, Priority.High, this);
		// Handle removing player data
		// pm.registerEvent(Event.Type.PLAYER_QUIT, playerListener, Priority.Normal, this);

		// Handle minecarts going through portal
		pm.registerEvent(Event.Type.VEHICLE_MOVE, vehicleListener, Priority.High, this);
		pm.registerEvent(Event.Type.VEHICLE_DAMAGE, vehicleListener, Priority.High, this);
		// Handle player walking through the lava.
		pm.registerEvent(Event.Type.ENTITY_DAMAGE, entityListener, Priority.High, this);
		// Handle Creeper explosions damaging Gate components.
		pm.registerEvent(Event.Type.ENTITY_EXPLODE, entityListener, Priority.High, this);
		
        // Listen for enable events.
		pm.registerEvent(Event.Type.PLUGIN_ENABLE, serverListener, Priority.Monitor, this);
		// Listen for disable events.
		pm.registerEvent(Event.Type.PLUGIN_DISABLE, serverListener, Priority.Monitor, this);
	}

    
	/* (non-Javadoc)
	 * @see org.bukkit.plugin.Plugin#onDisable()
	 */
	@Override
    public void onDisable() 
	{  
		try
		{
			Configuration.writeFile(this.getDescription());
			ArrayList<Stargate> gates = StargateManager.GetAllGates();
			// Store all our gates
			for ( Stargate gate : gates )
			{
				gate.ShutdownStargate(false);
				StargateDBManager.StargateToSQL(gate);
			}
			
			StargateDBManager.Shutdown();
			prettyLog(Level.INFO, true, "Successfully shutdown.");
		}
		catch ( Exception e)
		{
			prettyLog(Level.SEVERE,false,"Caught exception while shutting down: " + e.getMessage());
			e.printStackTrace();
		}
	} 


	/**
	 * Checks if is debugging.
	 *
	 * @param player the player
	 * @return true, if is debugging
	 */
	public boolean isDebugging(final Player player) 
	{ 
		return debugees.containsKey(player) && debugees.get(player).booleanValue();
	}
	
	/**
	 * Sets the debugging.
	 *
	 * @param player the player
	 * @param value the value
	 */
	public void setDebugging(final Player player, final boolean value) 
	{
		debugees.put(player, Boolean.valueOf(value)); 
	}
	
	/**
	 * 
	 * prettyLog: A quick and dirty way to make log output clean, unified, and with versioning as needed.
	 * 
	 * @param severity Level of severity in the form of INFO, WARNING, SEVERE, etc.
	 * @param version true causes version display in log entries.
	 * @param message to prettyLog.
	 * 
	 */
	public void prettyLog(Level severity, boolean version, String message) 
	{
		final String prettyName = (String)("[" + this.getDescription().getName() + "]");
		final String prettyVersion = (String)("[v" + this.getDescription().getVersion() + "]");
		String prettyLogLine = prettyName;
		if (version)
		{
			prettyLogLine += prettyVersion;
			log.log(severity,prettyLogLine + message);
		} 
		else
		{
			log.log(severity,prettyLogLine + message);
		}
	}
	
	/**
	 * Sets the pretty log level.
	 *
	 * @param level the new pretty log level
	 */
	public void setPrettyLogLevel(Level level)
	{
		log.setLevel(level);
		this.prettyLog(Level.CONFIG, false, "Logging set to: " + level );
	}
} 
 
