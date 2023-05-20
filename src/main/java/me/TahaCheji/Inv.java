package me.TahaCheji;

import java.util.List;
import java.util.logging.Logger;


import me.TahaCheji.database.InvMysqlInterface;
import me.TahaCheji.database.MysqlSetup;
import me.TahaCheji.discordCommand.InventoryCommand;
import me.TahaCheji.events.DropItem;
import me.TahaCheji.events.InventoryClick;
import me.TahaCheji.events.PlayerJoin;
import me.TahaCheji.events.PlayerQuit;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import org.bukkit.Bukkit;
import org.bukkit.event.HandlerList;
import org.bukkit.inventory.Inventory;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;

import javax.security.auth.login.LoginException;

public class Inv extends JavaPlugin {

	//Loan the players inventory in a discord bot
	//change the players inventory while offline
	
	public static Logger log;
	public boolean useProtocolLib = false;
	public static String pluginName = "MafanaInventoryAPI";
	//public Set<String> playersSync = new HashSet<String>();
	public static boolean is19Server = true;
	public static boolean is13Server = false;
	public static boolean isDisabling = false;
	
	private static ConfigHandler configHandler;
	private static SoundHandler sH;
	private static MysqlSetup databaseManager;
	private static InvMysqlInterface invMysqlInterface;
	private static InventoryDataHandler idH;
	private static BackgroundTask bt;

	private static Inv instance;
	public JDA builder = null;
	
	@Override
    public void onEnable() {
		instance = this;
		String token = "";
		try {
			builder = JDABuilder.createDefault(token).build();
		} catch (LoginException e) {
			e.printStackTrace();
		}
		builder.addEventListener(new InventoryCommand());
		log = getLogger();
    	configHandler = new ConfigHandler(this);
    	sH = new SoundHandler(this);
    	checkDependency();
    	bt = new BackgroundTask(this);
    	databaseManager = new MysqlSetup(this);
    	invMysqlInterface = new InvMysqlInterface(this);
    	idH = new InventoryDataHandler(this);
    	//Register Listeners
    	PluginManager pm = getServer().getPluginManager();
    	pm.registerEvents(new PlayerJoin(this), this);
    	pm.registerEvents(new PlayerQuit(this), this);
    	pm.registerEvents(new DropItem(this), this);
    	pm.registerEvents(new InventoryClick(this), this);
    	pm.registerEvents(new InventoryCommand(), this);
    	log.info(pluginName + " loaded successfully!");
	}
	
	@Override
    public void onDisable() {
		isDisabling = true;
		Bukkit.getScheduler().cancelTasks(this);
		HandlerList.unregisterAll(this);
		if (databaseManager.getConnection() != null) {
			bt.onShutDownDataSave();
			databaseManager.closeConnection();
		}
		log.info(pluginName + " is disabled!");
		builder.shutdownNow();
	}
	
	public ConfigHandler getConfigHandler() {
		return configHandler;
	}
	public MysqlSetup getDatabaseManager() {
		return databaseManager;
	}
	public InvMysqlInterface getInvMysqlInterface() {
		return invMysqlInterface;
	}
	public SoundHandler getSoundHandler() {
		return sH;
	}
	public BackgroundTask getBackgroundTask() {
		return bt;
	}
	public InventoryDataHandler getInventoryDataHandler() {
		return idH;
	}
	
	private void checkDependency() {
		//Check dependency
        if (Bukkit.getPluginManager().getPlugin("ProtocolLib") != null) {
        	useProtocolLib = true;
        	log.info("ProtocolLib dependency found.");
        } else {
        	useProtocolLib = false;
        	log.warning("ProtocolLib dependency not found. No support for modded items NBT data!");
        }
	}

	public static Inv getInstance() {
		return instance;
	}
}
