package com.comze_instancelabs.skywars;

import java.io.EOFException;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Random;
import java.util.Set;

import net.milkbowl.vault.economy.Economy;
import net.milkbowl.vault.economy.EconomyResponse;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Color;
import org.bukkit.DyeColor;
import org.bukkit.FireworkEffect;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.FireworkEffect.Type;
import org.bukkit.block.Block;
import org.bukkit.block.BlockState;
import org.bukkit.block.Chest;
import org.bukkit.block.Sign;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Firework;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.block.SignChangeEvent;
import org.bukkit.event.entity.FoodLevelChangeEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.FireworkMeta;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.plugin.RegisteredServiceProvider;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.potion.PotionEffect;
import org.bukkit.scheduler.BukkitTask;
import org.bukkit.scoreboard.DisplaySlot;
import org.bukkit.scoreboard.Objective;
import org.bukkit.scoreboard.Scoreboard;
import org.bukkit.scoreboard.ScoreboardManager;
import org.bukkit.util.io.BukkitObjectInputStream;
import org.bukkit.util.io.BukkitObjectOutputStream;


public class Main extends JavaPlugin implements Listener {
	public static Economy econ = null;

	public static HashMap<String, Boolean> ingame = new HashMap<String, Boolean>(); // arena -> whether arena is ingame or not
	public static HashMap<String, BukkitTask> tasks = new HashMap<String, BukkitTask>(); // arena -> task/ task
	public static HashMap<Player, String> arenap = new HashMap<Player, String>(); // player -> arena
	public static HashMap<String, String> arenap_ = new HashMap<String, String>(); // player -> arena
	public static HashMap<Player, ItemStack[]> pinv = new HashMap<Player, ItemStack[]>(); // player -> inventory
	public static HashMap<Player, String> lost = new HashMap<Player, String>(); // player -> whether lost or not
	public static HashMap<Player, Integer> xpsecp = new HashMap<Player, Integer>();
	public static HashMap<String, Integer> a_round = new HashMap<String, Integer>();
	public static HashMap<String, Integer> a_n = new HashMap<String, Integer>();
	public static HashMap<String, Integer> a_currentw = new HashMap<String, Integer>();
	public static HashMap<String, AClass> pclass = new HashMap<String, AClass>(); // player -> class
	public static HashMap<String, AClass> aclasses = new HashMap<String, AClass>(); // classname -> class

	int rounds_per_game = 10;
	//int minplayers = 4;
	int default_max_players = 4;
	int default_min_players = 3;
	
	boolean economy = true;
	int reward = 30;
	int itemid = 264;
	int itemamount = 1;
	boolean command_reward = false;
	String cmd = "";
	boolean start_announcement = false;
	boolean winner_announcement = false;
	
	int start_countdown = 5;

	public String saved_arena = "";
	public String saved_lobby = "";
	public String saved_setup = "";
	public String saved_mainlobby = "";
	public String not_in_arena = "";
	public String reloaded = "";
	public String arena_ingame = "";
	public String arena_invalid = "";
	public String arena_invalid_sign = "";
	public String you_fell = "";
	public String arena_invalid_component = "";
	public String you_won = "";
	public String starting_in = "";
	public String starting_in2 = "";
	public String arena_full = "";
	public String removed_arena = "";
	public String winner_an = "";
	public String join_announcement = "";

	// anouncements
	public String starting = "";
	public String started = "";
	

	
	@Override
	public void onEnable() {
		getServer().getPluginManager().registerEvents(this, this);

		getConfig().options().header("I recommend you to set auto_updating to true for possible future bugfixes. If use_economy is set to false, the winner will get the item reward.");
		getConfig().addDefault("config.auto_updating", true);
		getConfig().addDefault("config.rounds_per_game", 10);
		getConfig().addDefault("config.start_countdown", 5);
		getConfig().addDefault("config.default_max_players", 4);
		getConfig().addDefault("config.default_min_players", 3);
		getConfig().addDefault("config.use_economy_reward", false);
		getConfig().addDefault("config.money_reward_per_game", 30);
		getConfig().addDefault("config.itemid", 264); // diamond
		getConfig().addDefault("config.itemamount", 1);
		getConfig().addDefault("config.use_command_reward", false);
		getConfig().addDefault("config.command_reward", "pex user <player> group set ColorPro");
		getConfig().addDefault("config.start_announcement", false);
		getConfig().addDefault("config.winner_announcement", false);
		getConfig().addDefault("config.game_on_join", false);

		getConfig().addDefault("config.kits.default.name", "default");
		getConfig().addDefault("config.kits.default.items", "276#1");
		getConfig().addDefault("config.kits.default.lore", "The default class.");

		getConfig().addDefault("strings.saved.arena", "&aSuccessfully saved arena.");
		getConfig().addDefault("strings.saved.lobby", "&aSuccessfully saved lobby.");
		getConfig().addDefault("strings.saved.setup", "&6Successfully saved spawn. Now setting up, might &2lag&6 a little bit.");
		getConfig().addDefault("strings.removed_arena", "&cSuccessfully removed arena.");
		getConfig().addDefault("strings.not_in_arena", "&cYou don't seem to be in an arena right now.");
		getConfig().addDefault("strings.config_reloaded", "&6Successfully reloaded config.");
		getConfig().addDefault("strings.arena_is_ingame", "&cThe arena appears to be ingame.");
		getConfig().addDefault("strings.arena_invalid", "&cThe arena appears to be invalid.");
		getConfig().addDefault("strings.arena_invalid_sign", "&cThe arena appears to be invalid, because a join sign is missing.");
		getConfig().addDefault("strings.arena_invalid_component", "&2The arena appears to be invalid (missing components or misstyped arena)!");
		getConfig().addDefault("strings.you_fell", "&3You fell! Type &6/sw leave &3to leave.");
		getConfig().addDefault("strings.you_won", "&aYou won this round, awesome man! Here, enjoy your reward.");
		getConfig().addDefault("strings.starting_in", "&aStarting in &6");
		getConfig().addDefault("strings.starting_in2", "&a seconds.");
		getConfig().addDefault("strings.arena_full", "&cThis arena is full!");
		getConfig().addDefault("strings.starting_announcement", "&aStarting a new SkyWars Game in &6");
		getConfig().addDefault("strings.started_announcement", "&aA new SkyWars Round has started!");
		getConfig().addDefault("strings.winner_announcement", "&6<player> &awon the game on arena &6<arena>!");
		getConfig().addDefault("strings.join_announcement", "&6<player> joined the game (&a<count>)!");

		//TODO sign option
		getConfig().addDefault("config.sign_join.line0", "&6MobEscape");
		getConfig().addDefault("config.sign_join.line1", "");
		getConfig().addDefault("config.sign_join.line2", "");
		getConfig().addDefault("config.sign_join.line3", "");
		getConfig().addDefault("config.sign_ingame.line0", "&6MobEscape");
		getConfig().addDefault("config.sign_ingame.line1", "");
		getConfig().addDefault("config.sign_ingame.line2", "");
		getConfig().addDefault("config.sign_ingame.line3", "");
		getConfig().addDefault("config.sign_restart.line0", "&6MobEscape");
		getConfig().addDefault("config.sign_restart.line1", "");
		getConfig().addDefault("config.sign_restart.line2", "");
		getConfig().addDefault("config.sign_restart.line3", "");
		//getConfig().addDefault("config.sign_second_line_join", "&a[Join]");
		//getConfig().addDefault("config.sign_second_line_ingame", "&c[Ingame]");
		//getConfig().addDefault("config.sign_second_line_restarting", "&6[Restarting]");

		
		getConfig().options().copyDefaults(true);
		if(getConfig().isSet("config.min_players")){
			getConfig().set("config.min_players", null);
		}
		this.saveConfig();
		
		getConfigVars();

		if (economy) {
			if (!setupEconomy()) {
				getLogger().severe(String.format("[%s] - No iConomy dependency found! Disabling Economy.", getDescription().getName()));
				economy = false;
			}
		}
		
		loadClasses();
		
	}

	private boolean setupEconomy() {
		if (getServer().getPluginManager().getPlugin("Vault") == null) {
			return false;
		}
		RegisteredServiceProvider<Economy> rsp = getServer().getServicesManager().getRegistration(Economy.class);
		if (rsp == null) {
			return false;
		}
		econ = rsp.getProvider();
		return econ != null;
	}

	public void getConfigVars() {
		rounds_per_game = getConfig().getInt("config.rounds_per_game");
	    default_max_players = getConfig().getInt("config.default_max_players");
	    default_min_players = getConfig().getInt("config.default_min_players");
		reward = getConfig().getInt("config.money_reward");
		itemid = getConfig().getInt("config.itemid");
		itemamount = getConfig().getInt("config.itemamount");
		economy = getConfig().getBoolean("config.use_economy_reward");
		command_reward = getConfig().getBoolean("config.use_command_reward");
		cmd = getConfig().getString("config.command_reward");
		start_countdown = getConfig().getInt("config.start_countdown");
		start_announcement = getConfig().getBoolean("config.start_announcement");
		winner_announcement = getConfig().getBoolean("config.winner_announcement");
		
		saved_arena = getConfig().getString("strings.saved.arena").replaceAll("&", "§");
		saved_lobby = getConfig().getString("strings.saved.lobby").replaceAll("&", "§");
		saved_setup = getConfig().getString("strings.saved.setup").replaceAll("&", "§");
		saved_mainlobby = "§aSuccessfully saved main lobby";
		not_in_arena = getConfig().getString("strings.not_in_arena").replaceAll("&", "§");
		reloaded = getConfig().getString("strings.config_reloaded").replaceAll("&", "§");
		arena_ingame = getConfig().getString("strings.arena_is_ingame").replaceAll("&", "§");
		arena_invalid = getConfig().getString("strings.arena_invalid").replaceAll("&", "§");
		arena_invalid_sign = getConfig().getString("strings.arena_invalid_sign").replaceAll("&", "§");
		you_fell = getConfig().getString("strings.you_fell").replaceAll("&", "§");
		arena_invalid_component = getConfig().getString("strings.arena_invalid_component").replace("&", "§");
		you_won = getConfig().getString("strings.you_won").replaceAll("&", "§");
		starting_in = getConfig().getString("strings.starting_in").replaceAll("&", "§");
		starting_in2 = getConfig().getString("strings.starting_in2").replaceAll("&", "§");
		arena_full = getConfig().getString("strings.arena_full").replaceAll("&", "§");
		starting = getConfig().getString("strings.starting_announcement").replaceAll("&", "§");
		started = getConfig().getString("strings.started_announcement").replaceAll("&", "§");
		removed_arena = getConfig().getString("strings.removed_arena").replaceAll("&", "§");
		winner_an = getConfig().getString("strings.winner_announcement").replaceAll("&", "§");
		join_announcement = getConfig().getString("strings.join_announcement").replaceAll("&", "§");
	}

	public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
		if (cmd.getName().equalsIgnoreCase("sw") || cmd.getName().equalsIgnoreCase("skywars")) {
			if (args.length > 0) {
				String action = args[0];
				if (action.equalsIgnoreCase("createarena")) {
					// create arena
					if (args.length > 1) {
						if (sender.hasPermission("skywars.setup")) {
							String arenaname = args[1];
							getConfig().set(arenaname + ".name", arenaname);
							this.saveConfig();
							sender.sendMessage(saved_arena);
						}
					}
				} else if (action.equalsIgnoreCase("removearena")) {
					// remove arena
					if (args.length > 1) {
						if (sender.hasPermission("skywars.setup")) {
							String arenaname = args[1];
							if(isValidArena(arenaname)){
								sender.sendMessage("§cRemoving " + arenaname + ". This will §6lag §ca little bit.");
								try{
									getSignFromArena(arenaname).getBlock().setType(Material.AIR);
								}catch(Exception e){
									
								}
								getConfig().set(arenaname, null);
								this.saveConfig();
								sender.sendMessage(removed_arena);
							}else{
								sender.sendMessage(arena_invalid);
							}
						}
					}
				}  else if (action.equalsIgnoreCase("savearena")) {
					if (args.length > 1) {
						if (!(sender instanceof Player)) {
							return true;
						}

						Player p = (Player) sender;

						if (isValidArena(args[1])) {
							File f = new File(this.getDataFolder() + "/" + args[1]);
							f.delete();

							sender.sendMessage("" + ChatColor.GREEN + "Arena is now saving, " + ChatColor.GOLD + "this might take a while" + ChatColor.GREEN + ".");
							saveArenaToFile(p.getName(), args[1]);
						} else {
							sender.sendMessage("" + ChatColor.RED + "The arena appears to be invalid (missing components)!");
						}
					} else {
						sender.sendMessage("" + ChatColor.RED + "Usage: " + ChatColor.DARK_GREEN + "/etm savearena [name]");
					}
				} else if (action.equalsIgnoreCase("setbounds")) {
					if (sender.hasPermission("mobescape.setup")) {
						if (args.length > 2) {
							String arena = args[1];
							String count = args[2];
							if (!count.equalsIgnoreCase("low") && !count.equalsIgnoreCase("high")) {
								sender.sendMessage("" + ChatColor.RED + "Second parameter invalid. Usage: /etm setbounds [arena] [low/high]");
								return true;
							}
							if (!getConfig().isSet(arena)) {
								sender.sendMessage("" + ChatColor.RED + "Could not find this arena.");
								return true;
							}

							if (!(sender instanceof Player)) {
								return true;
							}

							Player p = (Player) sender;

							getConfig().set(arena + ".boundary" + count + ".world", p.getWorld().getName());
							getConfig().set(arena + ".boundary" + count + ".loc.x", p.getLocation().getBlockX());
							if (count.equalsIgnoreCase("low")) {
								getConfig().set(arena + ".boundary" + count + ".loc.y", p.getLocation().getBlockY() - 1);
							} else {
								getConfig().set(arena + ".boundary" + count + ".loc.y", p.getLocation().getBlockY());
							}
							getConfig().set(arena + ".boundary" + count + ".loc.z", p.getLocation().getBlockZ());
							this.saveConfig();

							sender.sendMessage("" + ChatColor.YELLOW + "Successfully saved " + count + " boundary!");
						} else {
							sender.sendMessage("" + ChatColor.RED + "Usage: /etm setbounds [arena] [count].");
						}
					} else {
						sender.sendMessage(ChatColor.RED + "You don't have permission.");
					}
				} else if (action.equalsIgnoreCase("setlobby")) {
					if (args.length > 1) {
						if (sender.hasPermission("skywars.setup")) {
							Player p = (Player) sender;
							String arenaname = args[1];
							getConfig().set(arenaname + ".lobby.world", p.getWorld().getName());
							getConfig().set(arenaname + ".lobby.loc.x", p.getLocation().getBlockX());
							getConfig().set(arenaname + ".lobby.loc.y", p.getLocation().getBlockY());
							getConfig().set(arenaname + ".lobby.loc.z", p.getLocation().getBlockZ());
							this.saveConfig();
							sender.sendMessage(saved_lobby);
						}
					}
				} else if (action.equalsIgnoreCase("setchest")) {
					if (args.length > 2) {
						if (sender.hasPermission("skywars.setup")) {
							Player p = (Player) sender;
							String arenaname = args[1];
							String type = args[2];
							
							boolean t = false;
							
							if(type.equalsIgnoreCase("default")){
								t = false;
							}else if (type.equalsIgnoreCase("op")){
								t = true;
							}else{
								sender.sendMessage(ChatColor.RED + "Possible types: default, op. Use /sw setchest [arena] [type].");
								return true;
							}
							
							this.setChest(arenaname, p.getLocation(), t);
							
							sender.sendMessage(ChatColor.GREEN + "Saved a chest.");
						}
					}
				} else if (action.equalsIgnoreCase("setspawn")) {
					if (args.length > 1) {
						if (sender.hasPermission("mobescape.setup")) {
							Player p = (Player) sender;
							String arenaname = args[1];
							
							String count = Integer.toString(this.getCurrentSpawnIndex(arenaname));

							getConfig().set(arenaname + ".spawn." + count + ".world", p.getWorld().getName());
							getConfig().set(arenaname + ".spawn." + count + ".loc.x", p.getLocation().getBlockX());
							getConfig().set(arenaname + ".spawn." + count + ".loc.y", p.getLocation().getBlockY());
							getConfig().set(arenaname + ".spawn." + count + ".loc.z", p.getLocation().getBlockZ());
							getConfig().set(arenaname + ".spawn." + count + ".loc.yaw", p.getLocation().getYaw());
							getConfig().set(arenaname + ".spawn." + count + ".loc.pitch", p.getLocation().getPitch());
							this.saveConfig();
							sender.sendMessage(ChatColor.GREEN + "Saved spawn: " + " Count: " + count);
						} else {
							sender.sendMessage(ChatColor.RED + "You don't have permission.");
						}
					}
				}  else if (action.equalsIgnoreCase("setmainlobby")) {
					if (sender.hasPermission("skywars.setup")) {
						Player p = (Player) sender;
						getConfig().set("mainlobby.world", p.getWorld().getName());
						getConfig().set("mainlobby.loc.x", p.getLocation().getBlockX());
						getConfig().set("mainlobby.loc.y", p.getLocation().getBlockY());
						getConfig().set("mainlobby.loc.z", p.getLocation().getBlockZ());
						this.saveConfig();
						sender.sendMessage(saved_mainlobby);
					}
				} else if (action.equalsIgnoreCase("spawnsetup") || action.equalsIgnoreCase("getbeacon")) {
					if (sender.hasPermission("skywars.setup")) {
						if(args.length > 1){
							String arena = args[1];
							Player p = (Player) sender;
							ItemStack beacon = new ItemStack(Material.BEACON);
							ItemMeta im = beacon.getItemMeta();
							im.setDisplayName(arena);
							beacon.setItemMeta(im);
							p.getInventory().addItem(beacon);
							p.updateInventory();
						}else{
							sender.sendMessage(ChatColor.RED + "Usage: /sw getbeacon [arena]");
						}
						
					}
				} else if (action.equalsIgnoreCase("leave")) {
					Player p = (Player) sender;
					if (arenap.containsKey(p)) {
						leaveArena(p, true, false);
					} else {
						p.sendMessage(not_in_arena);
					}
				} else if (action.equalsIgnoreCase("endall")) {
					if (sender.hasPermission("skywars.end")) {
						for (String arena : tasks.keySet()) {
							try {
								tasks.get(arena).cancel();
							} catch (Exception e) {

							}
						}
						ingame.clear();
						Bukkit.getScheduler().cancelTasks(this);
					}
				} else if (action.equalsIgnoreCase("setmaxplayers")) {
					if (sender.hasPermission("skywars.setup")) {
						if (args.length > 2) {
							String arena = args[1];
							String playercount = args[2];
							if(!isNumeric(playercount)){
								playercount = Integer.toString(default_max_players);
								sender.sendMessage("§cPlayercount is invalid. Setting to default value.");
							}
							if(!getConfig().isSet(arena)){
								sender.sendMessage("§cCould not find this arena.");
								return true;
							}
							this.setArenaMaxPlayers(arena, Integer.parseInt(playercount));
							sender.sendMessage("§eSuccessfully set!");
						}else{
							sender.sendMessage("§cUsage: /sw setmaxplayers [arena] [count].");
						}
					}
				} else if (action.equalsIgnoreCase("setminplayers")) {
					if (sender.hasPermission("skywars.setup")) {
						if (args.length > 2) {
							String arena = args[1];
							String playercount = args[2];
							if(!isNumeric(playercount)){
								playercount = Integer.toString(default_min_players);
								sender.sendMessage("§cPlayercount is invalid. Setting to default value.");
							}
							if(!getConfig().isSet(arena)){
								sender.sendMessage("§cCould not find this arena.");
								return true;
							}
							this.setArenaMinPlayers(arena, Integer.parseInt(playercount));
							sender.sendMessage("§eSuccessfully set!");
						}else{
							sender.sendMessage("§cUsage: /sw setminplayers [arena] [count].");
						}
					}
				} else if (action.equalsIgnoreCase("join")) {
					if (args.length > 1) {
						if (isValidArena(args[1])) {
							Sign s = null;
							try {
								s = this.getSignFromArena(args[1]);
							} catch (Exception e) {
								getLogger().warning("No sign found for arena " + args[1] + ". May lead to errors.");
							}
							if (s != null) {
								if (s.getLine(1).equalsIgnoreCase("§2[join]")) {
									joinLobby((Player) sender, args[1]);
								} else {
									sender.sendMessage(arena_ingame);
								}
							} else {
								sender.sendMessage(arena_invalid_sign);
							}
						} else {
							sender.sendMessage(arena_invalid);
						}
					}
				} else if (action.equalsIgnoreCase("start")) {
					if (args.length > 1) {
						if (sender.hasPermission("skywars.start")) {
							final String arena = args[1];
							if (!ingame.containsKey(arena)) {
								ingame.put(arena, false);
							}
							int count = 0;
							for (Player p : arenap.keySet()) {
								if (arenap.get(p).equalsIgnoreCase(arena)) {
									count++;
								}
							}
							if(count < 1){
								sender.sendMessage("§cNoone is in this arena.");
								return true;
							}
							if (!ingame.get(arena)) {
								ingame.put(arena, true);
								for (Player p_ : arenap.keySet()) {
									if (arenap.get(p_).equalsIgnoreCase(arena)) {
										final Player p__ = p_;
										Bukkit.getScheduler().runTaskLater(this, new Runnable() {
											public void run() {
												p__.teleport(getSpawnForPlayer(p__, arena).add(0.5D, 7D, 0.5D));
											}
										}, 5);
									}
								}
								Bukkit.getScheduler().runTaskLater(this, new Runnable() {
									public void run() {
										start(arena);
									}
								}, 10);
							}
						}
					}
				} else if(action.equalsIgnoreCase("changekit")){
					Player p = (Player)sender;
					if(args.length > 1){
						if(arenap.containsKey(p)){
							if(aclasses.containsKey(args[1])){
								if(args[1].equalsIgnoreCase("default")){
									this.setClass(args[1], p.getName());
									sender.sendMessage("§aKit successfully set!");
									return true;
								}
								if(p.hasPermission("skywars.kit." + args[1])){
									this.setClass(args[1], p.getName());
									sender.sendMessage("§aKit successfully set!");
								}
							}else{
								String all = "  ";
								for(String class_ : aclasses.keySet()){
									all += class_ + ", ";
								}
								sender.sendMessage("§cThis is not a valid kit. Possible ones:§3" + all.substring(0, all.length() - 2));
							}
						}else{
							sender.sendMessage("§cYou are not in an arena right now.");
						}
					}else{
						sender.sendMessage("§cUsage: §a/sw changekit [name].");
					}
				} else if (action.equalsIgnoreCase("kitgui")) {
					Player p = (Player)sender;
					if(arenap.containsKey(p)){
						openGUI(this, p.getName());
					}else{
						sender.sendMessage("§cYou are not in an arena right now.");
					}
				} else if (action.equalsIgnoreCase("reload")) {
					if (sender.hasPermission("skywars.reload")) {
						this.reloadConfig();
						getConfigVars();
						sender.sendMessage(reloaded);
					}
				} else if (action.equalsIgnoreCase("list")) {
					if (sender.hasPermission("skywars.list")) {
						sender.sendMessage("§6-= Arenas =-");
						for (String arena : getConfig().getKeys(false)) {
							if (!arena.equalsIgnoreCase("mainlobby") && !arena.equalsIgnoreCase("strings") && !arena.equalsIgnoreCase("config")) {
								sender.sendMessage("§2" + arena);
							}
						}
					}
				} else {
					sender.sendMessage("§6-= SkyWars §2help: §6=-");
					sender.sendMessage("§2To §6setup the main lobby §2, type in §c/sw setmainlobby");
					sender.sendMessage("§2To §6setup §2a new arena, type in the following commands:");
					sender.sendMessage("§2/sw createarena [name]");
					sender.sendMessage("§2/sw setlobby [name] §6 - for the waiting lobby");
					sender.sendMessage("§2/sw getbeacon [name] §6 - set a few player spawns");
					//sender.sendMessage("§2/sw setchest [name] [type] §6 - set a few chests");
					sender.sendMessage("§2/sw setbounds [name] [low/high] §6 - set the bounds");
					sender.sendMessage("§2/sw savearena [name]");
					sender.sendMessage("");
					sender.sendMessage("§2You can join with §c/sw join [name] §2and leave with §c/sw leave§2.");
					sender.sendMessage("§2You can force an arena to start with §c/sw start [name]§2.");
				}
			} else {
				sender.sendMessage("§6-= SkyWars §2help: §6=-");
				sender.sendMessage("§2To §6setup the main lobby §2, type in §c/sw setmainlobby");
				sender.sendMessage("§2To §6setup §2a new arena, type in the following commands:");
				sender.sendMessage("§2/sw createarena [name]");
				sender.sendMessage("§2/sw setlobby [name] §6 - for the waiting lobby");
				sender.sendMessage("§2/sw getbeacon [name] §6 - set a few player spawns");
				//sender.sendMessage("§2/sw setchest [name] [type] §6 - set a few chests");
				sender.sendMessage("§2/sw setbounds [name] [low/high] §6 - set the bounds");
				sender.sendMessage("§2/sw savearena [name]");
				sender.sendMessage("");
				sender.sendMessage("§2You can join with §c/sw join [name] §2and leave with §c/sw leave§2.");
				sender.sendMessage("§2You can force an arena to start with §c/sw start [name]§2.");
			}
			return true;
		}
		return false;
	}

	public ArrayList<String> left_players = new ArrayList<String>();

	@EventHandler
	public void onPlayerLeave(PlayerQuitEvent event) {
		if (arenap.containsKey(event.getPlayer())) {
			String arena = arenap.get(event.getPlayer());
			getLogger().info(arena);
			int count = 0;
			for (Player p_ : arenap.keySet()) {
				if (arenap.get(p_).equalsIgnoreCase(arena)) {
					count++;
				}
			}

			try {
				Sign s = this.getSignFromArena(arena);
				if (s != null) {
					s.setLine(1, "§2[Join]");
					s.setLine(3, Integer.toString(count - 1) + "/" + Integer.toString(getArenaMaxPlayers(arena)));
					s.update();
				}
			} catch (Exception e) {
				getLogger().warning("You forgot to set a sign for arena " + arena + "! This might lead to errors.");
			}

			leaveArena(event.getPlayer(), true, true);
			left_players.add(event.getPlayer().getName());
		}
	}

	@EventHandler
	public void onPlayerJoin(PlayerJoinEvent event) {
		final Player p = event.getPlayer();
		if (left_players.contains(event.getPlayer().getName())) {
			Bukkit.getScheduler().runTaskLater(this, new Runnable() {
				public void run() {
					p.teleport(getMainLobby());
					p.setFlying(false);
				}
			}, 5);
			left_players.remove(event.getPlayer().getName());
		}
		
		
		if(getConfig().getBoolean("config.game_on_join")){
			int c = 0;
			final List<String> arenas = new ArrayList<String>();
			for (String arena : getConfig().getKeys(false)) {
				if (!arena.equalsIgnoreCase("mainlobby") && !arena.equalsIgnoreCase("strings") && !arena.equalsIgnoreCase("config")) {
					c++;
					arenas.add(arena);
				}
			}
			if(c < 1){
				getLogger().severe("Couldn't find any arena even though game_on_join was turned on. Please setup an arena to fix this!");
				return;
			}
			
			Bukkit.getScheduler().runTaskLater(this, new Runnable(){
				public void run(){
					joinLobby(p, arenas.get(0));
				}
			}, 30L);
		}
	}

	@EventHandler
	public void onHunger(FoodLevelChangeEvent event) {
		if (event.getEntity() instanceof Player) {
			Player p = (Player) event.getEntity();
			if (arenap_.containsKey(p.getName())) {
				event.setCancelled(true);
			}
		}
	}


	@EventHandler
	public void onPlayerDeath(PlayerDeathEvent event){
		if(arenap_.containsKey(event.getEntity().getName())){
			event.getEntity().setHealth(20D);
			Player p = event.getEntity();
			
			lost.put(p, arenap.get(p));
			final Player p__ = p;
			final String arena = arenap.get(p);
			Bukkit.getScheduler().runTaskLater(this, new Runnable() {
				public void run() {
					try {
						Location l = getSpawn(arena);
						p__.teleport(new Location(l.getWorld(), l.getBlockX(), l.getBlockY() + 30, l.getBlockZ()));
						p__.setAllowFlight(true);
						p__.setFlying(true);
					} catch (Exception e) {
						e.printStackTrace();
					}
				}
			}, 5);

			int count = 0;

			for (Player p_ : arenap.keySet()) {
				if (arenap.get(p_).equalsIgnoreCase(arena)) {
					if (!lost.containsKey(p_)) {
						count++;
					}
				}
			}

			if (count < 2) {
				// last man standing!
				stop(h.get(arena), arena);
			}
		}
	}
	
	@EventHandler
	public void onPlayerMove(PlayerMoveEvent event) {
		if(arenap.containsKey(event.getPlayer()) && !arenap_.containsKey(event.getPlayer().getName())){
			final String arena_ = arenap.get(event.getPlayer());
			//getLogger().info(astarted.get(arena_).toString());
			if(ingame.get(arena_)){
				final Player p = event.getPlayer();
				final Location temp = getSpawn(arena_, pspawn.get(p));
				if (p.getLocation().getBlockZ() > temp.getBlockZ() + 1 || p.getLocation().getBlockZ() < temp.getBlockZ() - 1 || p.getLocation().getBlockX() > temp.getBlockX() + 1 || p.getLocation().getBlockX() < temp.getBlockX() - 1) {
					Bukkit.getScheduler().scheduleSyncDelayedTask(this, new Runnable() {
						public void run() {
							p.teleport(temp);
						}
					}, 5);
				}
			}
		}
		if (arenap_.containsKey(event.getPlayer().getName())) {
			if (lost.containsKey(event.getPlayer())) {
				Location l = getSpawn(lost.get(event.getPlayer()));
				final Location spectatorlobby = new Location(l.getWorld(), l.getBlockX(), l.getBlockY() + 30, l.getBlockZ());
				if (event.getPlayer().getLocation().getBlockY() < spectatorlobby.getBlockY() || event.getPlayer().getLocation().getBlockY() > spectatorlobby.getBlockY()) {
					final Player p = event.getPlayer();
					final float b = p.getLocation().getYaw();
					final float c = p.getLocation().getPitch();
					final String arena = arenap.get(event.getPlayer());
					Bukkit.getServer().getScheduler().scheduleSyncDelayedTask(this, new Runnable() {
						@Override
						public void run() {
							try {
								p.setAllowFlight(true);
								p.setFlying(true);
								p.teleport(new Location(p.getWorld(), p.getLocation().getBlockX(), spectatorlobby.getBlockY(), p.getLocation().getBlockZ(), b, c));
							} catch (Exception e) {
								e.printStackTrace();
							}
						}
					}, 5);
					p.sendMessage(you_fell);
				}
			}
			if (event.getPlayer().getLocation().getBlockY() < getSpawn(arenap_.get(event.getPlayer().getName())).getBlockY() - 2) {
				lost.put(event.getPlayer(), arenap.get(event.getPlayer()));
				final Player p__ = event.getPlayer();
				final String arena = arenap.get(event.getPlayer());
				Bukkit.getScheduler().runTaskLater(this, new Runnable() {
					public void run() {
						try {
							Location l = getSpawn(arena);
							p__.teleport(new Location(l.getWorld(), l.getBlockX(), l.getBlockY() + 30, l.getBlockZ()));
							p__.setAllowFlight(true);
							p__.setFlying(true);
						} catch (Exception e) {
							e.printStackTrace();
						}
					}
				}, 5);

				int count = 0;

				for (Player p : arenap.keySet()) {
					if (arenap.get(p).equalsIgnoreCase(arena)) {
						if (!lost.containsKey(p)) {
							count++;
						}
					}
				}
				
				m.updateScoreboard();

				if (count < 2) {
					// last man standing!
					stop(h.get(arena), arena);
				}
			}
		}
	}

	@EventHandler
	public void onSignUse(PlayerInteractEvent event) {
		if (event.hasBlock()) {
			if (event.getClickedBlock().getType() == Material.SIGN_POST || event.getClickedBlock().getType() == Material.WALL_SIGN) {
				final Sign s = (Sign) event.getClickedBlock().getState();
				if (s.getLine(0).toLowerCase().contains("skywars")) {
					if (s.getLine(1).equalsIgnoreCase("§2[join]")) {
						if(isValidArena(s.getLine(2))){
							joinLobby(event.getPlayer(), s.getLine(2));
						}else{
							event.getPlayer().sendMessage(arena_invalid);
						}
					}
				}
			}
		}
	}

	@EventHandler
	public void onSignChange(SignChangeEvent event) {
		Player p = event.getPlayer();
		if (event.getLine(0).toLowerCase().equalsIgnoreCase("skywars")) {
			if (event.getPlayer().hasPermission("skywars.sign") || event.getPlayer().hasPermission("skywars.sign") || event.getPlayer().isOp()) {
				event.setLine(0, "§6§lSkyWars");
				if (!event.getLine(2).equalsIgnoreCase("")) {
					String arena = event.getLine(2);
					if (isValidArena(arena)) {
						getConfig().set(arena + ".sign.world", p.getWorld().getName());
						getConfig().set(arena + ".sign.loc.x", event.getBlock().getLocation().getBlockX());
						getConfig().set(arena + ".sign.loc.y", event.getBlock().getLocation().getBlockY());
						getConfig().set(arena + ".sign.loc.z", event.getBlock().getLocation().getBlockZ());
						this.saveConfig();
						p.sendMessage("§2Successfully created arena sign.");
					} else {
						p.sendMessage(arena_invalid_component);
						event.getBlock().breakNaturally();
					}
					event.setLine(1, "§2[Join]");
					event.setLine(2, arena);
					event.setLine(3, "0/" + Integer.toString(getArenaMaxPlayers(arena)));
				}
			}
		}
	}

	
	@EventHandler
	public void onBlockPlace(BlockPlaceEvent event){
		if(event.getBlock().getType() == Material.BEACON){
			//TODO try out
			Player p = event.getPlayer();
			String arenaname = event.getItemInHand().getItemMeta().getDisplayName();
			
			if(arenaname == null)
				return;
			
			if(!getConfig().isSet(arenaname)){
				p.sendMessage(ChatColor.RED + "Could not find the arena.");
				return;
			}
			String count = Integer.toString(this.getCurrentSpawnIndex(arenaname));

			getConfig().set(arenaname + ".spawn." + count + ".world", event.getBlock().getLocation().getWorld().getName());
			getConfig().set(arenaname + ".spawn." + count + ".loc.x", event.getBlock().getLocation().getBlockX());
			getConfig().set(arenaname + ".spawn." + count + ".loc.y", event.getBlock().getLocation().getBlockY());
			getConfig().set(arenaname + ".spawn." + count + ".loc.z", event.getBlock().getLocation().getBlockZ());
			getConfig().set(arenaname + ".spawn." + count + ".loc.yaw", event.getBlock().getLocation().getYaw());
			getConfig().set(arenaname + ".spawn." + count + ".loc.pitch", event.getBlock().getLocation().getPitch());
			this.saveConfig();
			p.sendMessage(ChatColor.GREEN + "Saved spawn: " + " Count: " + count);
			Location l = event.getBlock().getLocation();
			l.clone().add(0D, 5D, 0D).getBlock().setType(Material.GLASS);
			l.clone().add(0D, 6D, 1D).getBlock().setType(Material.GLASS);
			l.clone().add(0D, 6D, -1D).getBlock().setType(Material.GLASS);
			l.clone().add(1D, 6D, 0D).getBlock().setType(Material.GLASS);
			l.clone().add(-1D, 6D, 0D).getBlock().setType(Material.GLASS);
			l.clone().add(0D, 7D, 1D).getBlock().setType(Material.GLASS);
			l.clone().add(0D, 7D, -1D).getBlock().setType(Material.GLASS);
			l.clone().add(1D, 7D, 0D).getBlock().setType(Material.GLASS);
			l.clone().add(-1D, 7D, 0D).getBlock().setType(Material.GLASS);
			l.clone().add(0D, 8D, 1D).getBlock().setType(Material.GLASS);
			l.clone().add(0D, 8D, -1D).getBlock().setType(Material.GLASS);
			l.clone().add(1D, 8D, 0D).getBlock().setType(Material.GLASS);
			l.clone().add(-1D, 8D, 0D).getBlock().setType(Material.GLASS);
			l.clone().add(0D, 9D, 0D).getBlock().setType(Material.GLASS);
			event.setCancelled(true);
		}
	}
	
	@EventHandler
   	public void onPlayerCommandPreprocessEvent(PlayerCommandPreprocessEvent event) {
       	if(arenap.containsKey(event.getPlayer()) && !event.getPlayer().isOp()){
       		if(!event.getMessage().startsWith("/sw") && !event.getMessage().startsWith("/skywars")){
       			event.getPlayer().sendMessage("§cPlease use §6/sw leave §cto leave this minigame.");
        		event.setCancelled(true);
       			return;
        	}
       	}
    }
	

	
	public Sign getSignFromArena(String arena) {
		Location b_ = new Location(getServer().getWorld(getConfig().getString(arena + ".sign.world")), getConfig().getInt(arena + ".sign.loc.x"), getConfig().getInt(arena + ".sign.loc.y"), getConfig().getInt(arena + ".sign.loc.z"));
		BlockState bs = b_.getBlock().getState();
		Sign s_ = null;
		if (bs instanceof Sign) {
			s_ = (Sign) bs;
		} else {
		}
		return s_;
	}

	public Location getLobby(String arena) {
		Location ret = null;
		if (isValidArena(arena)) {
			ret = new Location(Bukkit.getWorld(getConfig().getString(arena + ".lobby.world")), getConfig().getInt(arena + ".lobby.loc.x"), getConfig().getInt(arena + ".lobby.loc.y"), getConfig().getInt(arena + ".lobby.loc.z"));
		}
		return ret;
	}

	public Location getMainLobby() {
		Location ret;
		if(getConfig().isSet("mainlobby")){
			ret = new Location(Bukkit.getWorld(getConfig().getString("mainlobby.world")), getConfig().getInt("mainlobby.loc.x"), getConfig().getInt("mainlobby.loc.y"), getConfig().getInt("mainlobby.loc.z"));
		}else{
			ret = null;
			getLogger().warning("A Mainlobby could not be found. This will lead to errors, please fix this with /sw setmainlobby.");
		}
		return ret;
	}

	public Location getSpawn(String arena, int count) {
		Location ret = null;
		if (isValidArena(arena)) {
			String entry = ".spawn." + Integer.toString(count) + ".";
			if(!getConfig().isSet(arena + entry)){
				entry = ".spawn.";
			}
			ret = new Location(Bukkit.getWorld(getConfig().getString(arena + entry + "world")), getConfig().getInt(arena + entry + "loc.x"), getConfig().getInt(arena + entry + "loc.y"), getConfig().getInt(arena + entry + "loc.z"), getConfig().getInt(arena + entry + "loc.yaw"), getConfig().getInt(arena + entry + "loc.pitch"));
		}
		return ret;
	}
	
	public HashMap<String, Integer> spawncount = new HashMap<String, Integer>();
	public HashMap<Player, Integer> pspawn = new HashMap<Player, Integer>();
	
	public Location getSpawn(String arena) {
		return getSpawn(arena, 0);
	}
	
	public Location getSpawnForPlayer(Player p, String arena) {
		System.out.println("f");
		if(!spawncount.containsKey(arena)){
			spawncount.put(arena, 0);
			pspawn.put(p, 0);
			spawncount.put(arena, spawncount.get(arena) + 1);
			return getSpawn(arena, 0);
		}

		System.out.println("[A]" + p.getName() + " at spawn " + spawncount.get(arena));
		
		if(spawncount.get(arena) < this.getCurrentSpawnIndex(arena)){
			Location ret = getSpawn(arena, spawncount.get(arena));
			pspawn.put(p, spawncount.get(arena));
			spawncount.put(arena, spawncount.get(arena) + 1);
			
			System.out.println("[B]" + p.getName() + " at spawn " + spawncount.get(arena));
			
			return ret;
		}else{
			spawncount.put(arena, 0);
		}
		pspawn.put(p, 0);
		return getSpawn(arena, 0);
	}
	
	public Location getSpawnForPlayerRAW(Player p, String arena) {
		return getSpawn(arena, pspawn.get(p));
	}
	
	public int getCurrentSpawnIndex(String arena) {
		if (!getConfig().isSet(arena + ".spawn.")) {
			return 0;
		} else {
			int count = 0;
			Set<String> f = getConfig().getConfigurationSection(arena + ".spawn").getKeys(false);
			for (String key : f) {
				if(!key.equalsIgnoreCase("world") && !key.equalsIgnoreCase("loc")){
					count++;
				}
			}
			return count;
		}
	}
	

	public int getCurrentChestIndex(String arena) {
		if (!getConfig().isSet(arena + ".chest.")) {
			return 0;
		} else {
			int count = 0;
			Set<String> f = getConfig().getConfigurationSection(arena + ".chest").getKeys(false);
			for (String key : f) {
				if(!key.equalsIgnoreCase("world") && !key.equalsIgnoreCase("loc")){
					count++;
				}
			}
			return count;
		}
	}

	public boolean isValidArena(String arena) {
		if (getConfig().isSet(arena + ".spawn") && getConfig().isSet(arena + ".lobby")) {
			return true;
		}
		return false;
	}

	public HashMap<Player, Boolean> winner = new HashMap<Player, Boolean>();

	public void leaveArena(final Player p, boolean flag, boolean hmmthisbug) {
		try {
			Bukkit.getScheduler().runTaskLater(this, new Runnable() {
				public void run() {
					if (p.isOnline()) {
						p.teleport(getMainLobby());
						//p.setFlying(false);
						for(PotionEffect pe : p.getActivePotionEffects()){
							try{
								if(p.hasPotionEffect(pe.getType())){
									p.removePotionEffect(pe.getType());
								}
							}catch(Exception e){	
							}
						}
					}
				}
			}, 5);

			if(lost.containsKey(p)){
				lost.remove(p);
			}

			Bukkit.getScheduler().runTaskLater(this, new Runnable() {
				public void run() {
					if (p.isOnline()) {
						p.setAllowFlight(false);
						p.setFlying(false);
					}
				}
			}, 10);
			
			/*if (p.isOnline()) {
				p.setAllowFlight(false);
				p.setFlying(false);
			}*/
			
			final String arena = arenap.get(p);

			if (flag) {
				if (arenap.containsKey(p)) {
					arenap.remove(p);
				}
				if (xpsecp.containsKey(p)) {
					xpsecp.remove(p);
				}
			}
			if (arenap_.containsKey(p.getName())) {
				arenap_.remove(p.getName());
			}
			if (p.isOnline()) {
				p.getInventory().setContents(pinv.get(p));
				p.updateInventory();
				removeScoreboard(arena, p);
			}

			if (winner.containsKey(p)) {
				if (economy) {
					EconomyResponse r = econ.depositPlayer(p.getName(), getConfig().getDouble("config.money_reward_per_game"));
					if (!r.transactionSuccess()) {
						getServer().getPlayer(p.getName()).sendMessage(String.format("An error occured: %s", r.errorMessage));
					}
				} else {
					p.getInventory().addItem(new ItemStack(Material.getMaterial(itemid), itemamount));
					p.updateInventory();
				}

				// command reward
				if (command_reward) {
					Bukkit.getServer().dispatchCommand(Bukkit.getConsoleSender(), cmd.replaceAll("<player>", p.getName()));
				}
			}

			int count = 0;
			for (Player p_ : arenap.keySet()) {
				if (arenap.get(p_).equalsIgnoreCase(arena)) {
					count++;
				}
			}

			if (hmmthisbug && count > 0) {
				getLogger().info("Sorry, I could not fix the game. Stopping now.");
				stop(h.get(arena), arena);
			}

			if (count < 2) {
				if (flag) {
					stop(h.get(arena), arena);
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}

	}

	public void joinLobby(final Player p, final String arena) {
		// check first if max players are reached.
		int count_ = 0;
		for (Player p_ : arenap.keySet()) {
			if (arenap.get(p_).equalsIgnoreCase(arena)) {
				count_++;
			}
		}
		if (count_ > getArenaMaxPlayers(arena) - 1 && !p.hasPermission("skywars.vip")) {
			p.sendMessage(arena_full);
			return;
		}

		
		// continue
		arenap.put(p, arena);
		pinv.put(p, p.getInventory().getContents());
		p.setGameMode(GameMode.SURVIVAL);
		p.getInventory().clear();
		p.updateInventory();
		Bukkit.getScheduler().runTaskLater(this, new Runnable() {
			public void run() {
				p.teleport(getLobby(arena));
				p.setFoodLevel(20);
			}
		}, 4);

		int count = 0;
		for (Player p_ : arenap.keySet()) {
			if (arenap.get(p_).equalsIgnoreCase(arena)) {
				count++;
			}
		}
		
		for (Player p_ : arenap.keySet()) {
			if (arenap.get(p_).equalsIgnoreCase(arena)) {
				p_.sendMessage(join_announcement.replace("<player>", p.getName()).replace("<count>", Integer.toString(count) + "/" + Integer.toString(getArenaMaxPlayers(arena))));
			}
		}
		
		if (count > getArenaMinPlayers(arena) - 1) {
			for (Player p_ : arenap.keySet()) {
				final Player p__ = p_;
				if (arenap.get(p_).equalsIgnoreCase(arena)) {
					final Location l_ = getSpawnForPlayer(p__, arena).add(0D, 5D, 0D);
					l_.getBlock().setType(Material.GLASS);
					Bukkit.getScheduler().runTaskLater(this, new Runnable() {
						public void run() {
							p__.teleport(l_.add(0.5D, 1D, 0.5D));
						}
					}, 7);
				}
			}
			Bukkit.getScheduler().runTaskLater(this, new Runnable() {
				public void run() {
					if (!ingame.containsKey(arena)) {
						ingame.put(arena, false);
					}
					if (!ingame.get(arena)) {
						start(arena);
					}
				}
			}, 10);
		}
		
		if (!ingame.containsKey(arena)) {
			ingame.put(arena, false);
		}
		if(ingame.get(arena)){
			Bukkit.getScheduler().runTaskLater(this, new Runnable() {
				public void run() {
					p.teleport(getSpawnForPlayer(p, arena));
				}
			}, 7);
		}

		try {
			Sign s = this.getSignFromArena(arena);
			if (s != null) {
				s.setLine(3, Integer.toString(count) + "/" + Integer.toString(getArenaMaxPlayers(arena)));
				s.update();
			}
		} catch (Exception e) {
			getLogger().warning("You forgot to set a sign for arena " + arena + "! This may lead to errors.");
		}

	}

	

	
	
	final Main m = this;

	static ArrayList<Integer> ints = new ArrayList<Integer>();
	static ArrayList<DyeColor> colors = new ArrayList<DyeColor>(Arrays.asList(DyeColor.BLUE, DyeColor.RED, DyeColor.CYAN, DyeColor.BLACK, DyeColor.GREEN, DyeColor.YELLOW, DyeColor.ORANGE, DyeColor.PURPLE));
	static Random r = new Random();

	final public HashMap<String, BukkitTask> h = new HashMap<String, BukkitTask>();
	final public HashMap<String, Integer> countdown_count = new HashMap<String, Integer>();
	final public HashMap<String, Integer> countdown_id = new HashMap<String, Integer>();

	public void start(final String arena) {
		ingame.put(arena, true);

		// setup arena
		a_round.put(arena, 0);
		a_n.put(arena, 0);
		a_currentw.put(arena, 0);


		// start countdown timer
		if(start_announcement){
			Bukkit.getServer().broadcastMessage(starting + " " + Integer.toString(start_countdown));
		}
		
		Bukkit.getServer().getScheduler().runTaskLater(this, new Runnable(){
			public void run(){
				// clear hostile mobs on start:
				for(Player p : arenap.keySet()){
        			p.playSound(p.getLocation(), Sound.CAT_MEOW, 1, 0);
					if(arenap.get(p).equalsIgnoreCase(arena)){
						for(Entity t : p.getNearbyEntities(64, 64, 64)){
							if(t.getType() == EntityType.ZOMBIE || t.getType() == EntityType.SKELETON || t.getType() == EntityType.CREEPER || t.getType() == EntityType.CAVE_SPIDER || t.getType() == EntityType.SPIDER || t.getType() == EntityType.WITCH || t.getType() == EntityType.GIANT){
								t.remove();
							}
						}
						break;
					}
				}
			}
		}, 20L);
		
		refillAllChests(arena);
		
		int t = Bukkit.getServer().getScheduler().runTaskTimerAsynchronously(m, new Runnable() {
			public void run() {
				if (!countdown_count.containsKey(arena)) {
					countdown_count.put(arena, start_countdown);
				}
				int count = countdown_count.get(arena);
				for (Player p : arenap.keySet()) {
					if (arenap.get(p).equalsIgnoreCase(arena)) {
						p.sendMessage(starting_in + count + starting_in2);
					}
				}
				count--;
				countdown_count.put(arena, count);
				if (count < 0) {
					countdown_count.put(arena, start_countdown);
					
					if(start_announcement){
						Bukkit.getServer().broadcastMessage(started);
					}
					
					for (Player p_ : arenap.keySet()) {
						if (arenap.get(p_).equalsIgnoreCase(arena)) {
							final Player p__ = p_;
							Bukkit.getScheduler().runTaskLater(m, new Runnable() {
								public void run() {
									Location l = getSpawnForPlayerRAW(p__, arena).add(0D, 5D, 0D);
									System.out.println(l);
									l.getBlock().setType(Material.AIR);
									//p__.teleport(getSpawnForPlayer(p__, arena));
								}
							}, 5);
						}
					}
					
					// update sign
					Bukkit.getServer().getScheduler().runTask(m, new Runnable(){
						public void run(){
							Sign s = getSignFromArena(arena);
							if (s != null) {
								s.setLine(1, "§4[Ingame]");
								s.update();
							}
						}
					});
					
					for (Player p : arenap.keySet()) {
						if (arenap.get(p).equalsIgnoreCase(arena)) {
							if(pclass.containsKey(p.getName())){
								m.getClass(p.getName());
							}else{
								//setClass("default", p.getName());
							}
							arenap_.put(p.getName(), arena);
						}
					}
					
					m.updateScoreboard();
					
					Bukkit.getServer().getScheduler().cancelTask(countdown_id.get(arena));
				}
			}
		}, 0, 20).getTaskId();
		countdown_id.put(arena, t);


	}

	

	public void stop(BukkitTask t, final String arena) {
		ingame.put(arena, false);
		try {
			t.cancel();
		} catch (Exception e) {

		}

		
		// runs all that stuff later, that fixes the
		// "players are stuck in arena" bug!
		Bukkit.getScheduler().runTaskLater(this, new Runnable() {

			public void run() {
				countdown_count.put(arena, start_countdown);
				try {
					Bukkit.getServer().getScheduler().cancelTask(countdown_id.get(arena));
				} catch (Exception e) {
				}

				determineWinners(arena);
				
				ArrayList<Player> torem = new ArrayList<Player>();
				determineWinners(arena);
				for (Player p : arenap.keySet()) {
					if (arenap.get(p).equalsIgnoreCase(arena)) {
						leaveArena(p, false, false);
						torem.add(p);
					}
				}

				for (Player p : torem) {
					arenap.remove(p);
				}
				torem.clear();

				winner.clear();

				Sign s = getSignFromArena(arena);
				if (s != null) {
					s.setLine(1, "§2[Join]");
					s.setLine(3, "0/" + Integer.toString(getArenaMaxPlayers(arena)));
					s.update();
				}

				h.remove(arena);

				// reset arena
				for (Player p : xpsecp.keySet()) {
					xpsecp.put(p, 1);
				}
				a_round.put(arena, 0);
				a_n.put(arena, 0);
				a_currentw.put(arena, 0);

				
				if(spawncount.containsKey(arena)){
					spawncount.remove(arena);
				}
				
				
				m.reset(arena);
				
				clean();
			}

		}, 20); // 1 second

	}

	public void clean() {
		for (Player p : arenap.keySet()) {
			if (!p.isOnline()) {
				leaveArena(p, false, false);
			}
		}
	}

	public void determineWinners(String arena) {
		for (Player p : arenap.keySet()) {
			if (arenap.get(p).equalsIgnoreCase(arena)) {
				if (!lost.containsKey(p)) {
					// this player is a winner
					p.sendMessage(you_won);
					
					if(winner_announcement){
						getServer().broadcastMessage(winner_an.replaceAll("<player>", p.getName()).replaceAll("<arena>", arena));
					}
					
					winner.put(p, true);
				} else {
					lost.remove(p);
				}
			}
		}
	}


	public int getArenaMaxPlayers(String arena) {
		if(!getConfig().isSet(arena + ".max_players")){
			setArenaMaxPlayers(arena, default_max_players);
		}
		return getConfig().getInt(arena + ".max_players");
	}
	
	public void setArenaMaxPlayers(String arena, int players) {
		getConfig().set(arena + ".max_players", players);
		this.saveConfig();
	}
	
	public int getArenaMinPlayers(String arena) {
		if(!getConfig().isSet(arena + ".min_players")){
			setArenaMinPlayers(arena, default_min_players);
		}
		return getConfig().getInt(arena + ".min_players");
	}
	
	public void setArenaMinPlayers(String arena, int players) {
		getConfig().set(arena + ".min_players", players);
		this.saveConfig();
	}
	
	
	public boolean isNumeric(String s) {  
	    return s.matches("[-+]?\\d*\\.?\\d+");  
	}
	

	public void getClass(String player){
		AClass c = pclass.get(player);
		getServer().getPlayer(player).getInventory().clear();
		getServer().getPlayer(player).getInventory().setArmorContents(null);
		getServer().getPlayer(player).updateInventory();
		getServer().getPlayer(player).getInventory().setContents(c.getItems());
		getServer().getPlayer(player).updateInventory();
	}
	
	public void setClass(String classname, String player){
		pclass.put(player, aclasses.get(classname));
	}
	
	public void loadClasses(){
		if(getConfig().isSet("config.kits")){
			for(String aclass : getConfig().getConfigurationSection("config.kits.").getKeys(false)){
				AClass n = new AClass(this, aclass, parseItems(getConfig().getString("config.kits." + aclass + ".items")));
				aclasses.put(aclass, n);
				if(!getConfig().isSet("config.kits." + aclass + ".items") || !getConfig().isSet("config.kits." + aclass + ".lore")){
					getLogger().warning("One of the classes found in the config file is invalid: " + aclass + ". Missing itemid or lore!");
				}
			}
		}
	}
	
	
	public void openGUI(final Main m, String p) {
		IconMenu iconm = new IconMenu("Shop", 18, new IconMenu.OptionClickEventHandler() {
			@Override
			public void onOptionClick(IconMenu.OptionClickEvent event) {
				String d = event.getName();
				Player p = event.getPlayer();
				if(aclasses.containsKey(d)){
					m.setClass(d, p.getName());
				}
				event.setWillClose(true);
			}
		}, m);
		
		int c = 0;
		for(String ac : aclasses.keySet()){
			iconm.setOption(c, new ItemStack(Material.SLIME_BALL), ac, m.getConfig().getString("config.kits." + ac + ".lore"));
			c++;
		}
		
		iconm.open(Bukkit.getPlayerExact(p));
	}
	
	
	public void reset(final String arena) {
		Runnable r = new Runnable() {
			public void run() {
				loadArenaFromFileSYNC(arena);
			}
		};
		new Thread(r).start();
	}
	
	
	public void loadArenaFromFileSYNC(final String arena) {
		int failcount = 0;
		final ArrayList<ArenaBlock> failedblocks = new ArrayList<ArenaBlock>();

		File f = new File(this.getDataFolder() + "/" + arena);
		FileInputStream fis = null;
		BukkitObjectInputStream ois = null;
		try {
			fis = new FileInputStream(f);
			ois = new BukkitObjectInputStream(fis);
		} catch (IOException e) {
			e.printStackTrace();
		}

		try {
			while (true) {
				Object b = null;
				try {
					b = ois.readObject();
				} catch (EOFException e) {
					getLogger().info("Finished restoring map for " + arena + ".");
				}

				if (b != null) {
					//if(b instanceof ArenaBlock){
						ArenaBlock ablock = (ArenaBlock) b;
						try {
							Block b_ = ablock.getBlock().getWorld().getBlockAt(ablock.getBlock().getLocation());
							if (!b_.getType().toString().equalsIgnoreCase(ablock.getMaterial().toString())) {
								b_.setType(ablock.getMaterial());
								b_.setData(ablock.getData());
								// .setTypeIdAndData(ablock.getMaterial().getId(), ablock.getData(), false);
							}
							if(b_.getType() == Material.CHEST){
								((Chest)b_.getState()).getInventory().setContents(ablock.getInventory());
								((Chest)b_.getState()).update();
							}
						} catch (IllegalStateException e) {
							failcount += 1;
							failedblocks.add(ablock);
						}
					/*}else {
						ChestBlock ablock = (ChestBlock) b;
						try {
							Block b_ = ablock.getBlock().getWorld().getBlockAt(ablock.getBlock().getLocation());
							if (!b_.getType().toString().equalsIgnoreCase(ablock.getMaterial().toString())) {
								b_.setType(ablock.getMaterial());
								b_.setData(ablock.getData());
								((Chest)b_.getState()).getInventory().setContents(ablock.getInventory());
								// .setTypeIdAndData(ablock.getMaterial().getId(), ablock.getData(), false);
							}
						} catch (IllegalStateException e) {
						}
					}*/
					
				} else {
					break;
				}
			}

		} catch (IOException e) {
			e.printStackTrace();
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
		}

		try {
			ois.close();
		} catch (IOException e) {
			e.printStackTrace();
		}

		getLogger().info("Failed to update " + Integer.toString(failcount) + " blocks due to spigots async exception.");
		getServer().getScheduler().scheduleSyncDelayedTask(this, new Runnable() {
			public void run() {
				// restore spigot blocks!
				//getLogger().info("Trying to restore blocks affected by spigot exception..");
				for (ArenaBlock ablock : failedblocks) {
					getServer().getWorld(ablock.world).getBlockAt(new Location(getServer().getWorld(ablock.world), ablock.x, ablock.y, ablock.z)).setType(Material.WOOL);
					getServer().getWorld(ablock.world).getBlockAt(new Location(getServer().getWorld(ablock.world), ablock.x, ablock.y, ablock.z)).getTypeId();
					getServer().getWorld(ablock.world).getBlockAt(new Location(getServer().getWorld(ablock.world), ablock.x, ablock.y, ablock.z)).setType(ablock.getMaterial());
					Block b_ = getServer().getWorld(ablock.world).getBlockAt(new Location(getServer().getWorld(ablock.world), ablock.x, ablock.y, ablock.z));
					System.out.println(ablock.getMaterial() + " " + b_.getType());
					if(b_.getType() == Material.CHEST){
						System.out.println(ablock.getInventory().length);
						((Chest)b_.getState()).getInventory().setContents(ablock.getInventory());
						((Chest)b_.getState()).update();
					}
				}
				getLogger().info("Successfully finished!");

				Sign s = getSignFromArena(arena);
				if (s != null) {
					s.setLine(1, "§2[Join]");
					s.setLine(3, "0/" + Integer.toString(getArenaMaxPlayers(arena)));
					s.update();
				}
			}
		}, 40L);

		return;
	}
	
	public void saveArenaToFile(String player, String arena) {
		File f = new File(this.getDataFolder() + "/" + arena);
		Cuboid c = new Cuboid(getLowBoundary(arena), getHighBoundary(arena));
		Location start = c.getLowLoc();
		Location end = c.getHighLoc();

		int width = end.getBlockX() - start.getBlockX();
		int length = end.getBlockZ() - start.getBlockZ();
		int height = end.getBlockY() - start.getBlockY();

		getLogger().info("BOUNDS: " + Integer.toString(width) + " " + Integer.toString(height) + " " + Integer.toString(length));
		getLogger().info("BLOCKS TO SAVE: " + Integer.toString(width * height * length));

		FileOutputStream fos;
		ObjectOutputStream oos = null;
		try {
			fos = new FileOutputStream(f);
			oos = new BukkitObjectOutputStream(fos);
		} catch (IOException e) {
			e.printStackTrace();
		}

		for (int i = 0; i <= width; i++) {
			for (int j = 0; j <= height; j++) {
				for (int k = 0; k <= length; k++) {
					Block change = c.getWorld().getBlockAt(start.getBlockX() + i, start.getBlockY() + j, start.getBlockZ() + k);

					ArenaBlock bl;
					if(change.getType() == Material.CHEST){
						bl = new ArenaBlock(change, true);
					}else{
						bl = new ArenaBlock(change, false);
					}

					
					/*if(change.getType() == Material.CHEST){
						ChestBlock bl_ = new ChestBlock(change);
						try {
							oos.writeObject(bl_);
						} catch (IOException e) {
							e.printStackTrace();
						}
					}*/

					try {
						oos.writeObject(bl);
					} catch (IOException e) {
						e.printStackTrace();
					}

				}
			}
		}

		try {
			oos.close();
		} catch (IOException e) {
			e.printStackTrace();
		}

		Bukkit.getPlayerExact(player).sendMessage("" + ChatColor.GREEN + "Successfully saved arena to file.");
	}

	public void saveArenaToFile(String arena) {
		File f = new File(this.getDataFolder() + "/" + arena);
		Cuboid c = new Cuboid(getLowBoundary(arena), getHighBoundary(arena));
		Location start = c.getLowLoc();
		Location end = c.getHighLoc();

		int width = end.getBlockX() - start.getBlockX();
		int length = end.getBlockZ() - start.getBlockZ();
		int height = end.getBlockY() - start.getBlockY();

		getLogger().info("BOUNDS: " + Integer.toString(width) + " " + Integer.toString(height) + " " + Integer.toString(length));
		getLogger().info("BLOCKS TO SAVE: " + Integer.toString(width * height * length));

		FileOutputStream fos;
		ObjectOutputStream oos = null;
		try {
			fos = new FileOutputStream(f);
			oos = new BukkitObjectOutputStream(fos);
		} catch (IOException e) {
			e.printStackTrace();
		}

		for (int i = 0; i <= width; i++) {
			for (int j = 0; j <= height; j++) {
				for (int k = 0; k <= length; k++) {
					Block change = c.getWorld().getBlockAt(start.getBlockX() + i, start.getBlockY() + j, start.getBlockZ() + k);

					// if(change.getType() != Material.AIR){
					ArenaBlock bl;
					if(change.getType() == Material.CHEST){
						bl = new ArenaBlock(change, true);
					}else{
						bl = new ArenaBlock(change, false);
					}

					try {
						oos.writeObject(bl);
					} catch (IOException e) {
						e.printStackTrace();
					}
					// }

				}
			}
		}

		try {
			oos.close();
		} catch (IOException e) {
			e.printStackTrace();
		}

		getLogger().info("saved");
	}
	
	
	public Location getLowBoundary(String arena) {
		Location ret = null;
		if (isValidArena(arena)) {
			ret = new Location(Bukkit.getWorld(getConfig().getString(arena + ".boundarylow.world")), getConfig().getInt(arena + ".boundarylow.loc.x"), getConfig().getInt(arena + ".boundarylow.loc.y") + 2, getConfig().getInt(arena + ".boundarylow.loc.z"));
		}
		return ret;
	}

	public Location getHighBoundary(String arena) {
		Location ret = null;
		if (isValidArena(arena)) {
			ret = new Location(Bukkit.getWorld(getConfig().getString(arena + ".boundaryhigh.world")), getConfig().getInt(arena + ".boundaryhigh.loc.x"), getConfig().getInt(arena + ".boundaryhigh.loc.y") + 2, getConfig().getInt(arena + ".boundaryhigh.loc.z"));
		}
		return ret;
	}


	public void refillAllChests(String arena){
		if(getAllNormalChests(arena) != null){
			for(Location l : getAllNormalChests(arena)){
		        Block bl = l.getWorld().getBlockAt(l);
		        bl.setType(Material.CHEST);
		        Chest c = (Chest) bl.getState();
		        
		        c.getInventory().clear();
		        c.getInventory().addItem(new ItemStack(Material.IRON_SWORD));
		        //c.getBlockInventory().getItem(0).setTypeId(377);
		        c.update();
			}	
		}
		
		if(getAllOPChests(arena) != null){
			for(Location l : getAllOPChests(arena)){
		        Block bl = l.getWorld().getBlockAt(l);
		        bl.setType(Material.CHEST);
		        Chest c = (Chest) bl.getState();
		        
		        c.getInventory().clear();
		        c.getInventory().addItem(new ItemStack(Material.DIAMOND_SWORD));
		        c.update();
			}	
		}
		
	}
	
	public void setChest(String arena, Location l, boolean op){
		String count = Integer.toString(this.getCurrentChestIndex(arena));

		getConfig().set(arena + ".chest." + count + ".world", l.getWorld().getName());
		getConfig().set(arena + ".chest." + count + ".loc.x", l.getBlockX());
		getConfig().set(arena + ".chest." + count + ".loc.y", l.getBlockY());
		getConfig().set(arena + ".chest." + count + ".loc.z", l.getBlockZ());
		getConfig().set(arena + ".chest." + count + ".loc.yaw", l.getYaw());
		getConfig().set(arena + ".chest." + count + ".loc.pitch", l.getPitch());
		getConfig().set(arena + ".chest." + count + ".op", op);
		this.saveConfig();
		
		l.getWorld().getBlockAt(l).setType(Material.CHEST);
	}
	
	public ArrayList<Location> getAllNormalChests(String arena) {
		ArrayList<Location> ret = new ArrayList<Location>();
		if (!getConfig().isSet(arena + ".chest.")) {
			return null;
		} else {
			Set<String> f = getConfig().getConfigurationSection(arena + ".chest").getKeys(false);
			for (String key : f) {
				if(!key.equalsIgnoreCase("world") && !key.equalsIgnoreCase("loc")){
					if(!getConfig().getBoolean(arena + ".chest." + key + ".op")){
						ret.add(new Location(Bukkit.getWorld(getConfig().getString(arena + ".chest." + key + ".world")), getConfig().getInt(arena + ".chest." + key + ".loc.x"), getConfig().getInt(arena + ".chest." + key + ".loc.y"), getConfig().getInt(arena + ".chest." + key + ".loc.z")));
					}
				}
			}
			return ret;
		}
	}
	
	public ArrayList<Location> getAllOPChests(String arena) {
		ArrayList<Location> ret = new ArrayList<Location>();
		if (!getConfig().isSet(arena + ".chest.")) {
			return null;
		} else {
			Set<String> f = getConfig().getConfigurationSection(arena + ".chest").getKeys(false);
			for (String key : f) {
				if(!key.equalsIgnoreCase("world") && !key.equalsIgnoreCase("loc")){
					if(getConfig().getBoolean(arena + ".chest." + key + ".op")){
						ret.add(new Location(Bukkit.getWorld(getConfig().getString(arena + ".chest." + key + ".world")), getConfig().getInt(arena + ".chest." + key + ".loc.x"), getConfig().getInt(arena + ".chest." + key + ".loc.y"), getConfig().getInt(arena + ".chest." + key + ".loc.z")));
					}
				}
			}
			return ret;
		}
	}


	
	// example items: 267#1;3#64;3#64
	@SuppressWarnings("unused")
	public ArrayList<ItemStack> parseItems(String rawitems){
		ArrayList<ItemStack> ret = new ArrayList<ItemStack>();
		
		String[] a = rawitems.split(";");
		for(String b : a){
			String[] c = b.split("#");
			String itemid = c[0];
			String itemamount = c[1];
			ItemStack nitem = new ItemStack(Integer.parseInt(itemid), Integer.parseInt(itemamount));
			ret.add(nitem);
		}
		if(ret == null){
			getLogger().severe("Found invalid class in config!");
		}
		return ret;
	}
	
	

	Scoreboard board;
	Objective objective;
	public HashMap<String, Integer> currentscore = new HashMap<String, Integer>();

	
	public void updateScoreboard() {

		for (Player pl : arenap.keySet()) {
			Player p = pl;
			if (board == null) {
				board = Bukkit.getScoreboardManager().getNewScoreboard();
			}
			if (objective == null) {
				objective = board.registerNewObjective("test", "dummy");
			}

			objective.setDisplaySlot(DisplaySlot.SIDEBAR);

			objective.setDisplayName("[" + arenap.get(p) + "]");

			for (Player pl_ : arenap.keySet()) {
				Player p_ = pl_;
				if (!lost.containsKey(pl_)) {
					int score = 0;
					if (currentscore.containsKey(pl_.getName())) {
						int oldscore = currentscore.get(pl_.getName());
						if (score > oldscore) {
							currentscore.put(pl_.getName(), score);
						} else {
							score = oldscore;
						}
					} else {
						currentscore.put(pl_.getName(), score);
					}
					try{
						if(p_.getName().length() < 15){
							objective.getScore(Bukkit.getOfflinePlayer("§a" + p_.getName())).setScore(score);
						}else{
							objective.getScore(Bukkit.getOfflinePlayer("§a" + p_.getName().substring(0, p_.getName().length() - 3))).setScore(score);
						}
					}catch(Exception e){
					}
				} else if (lost.containsKey(pl_)){
					if (currentscore.containsKey(pl_.getName())) {
						int score = currentscore.get(pl_.getName());
						try{
							if(p_.getName().length() < 15){
								board.resetScores(Bukkit.getOfflinePlayer("§a" + p_.getName()));
								objective.getScore(Bukkit.getOfflinePlayer("§c" + p_.getName())).setScore(score);
							}else{
								board.resetScores(Bukkit.getOfflinePlayer("§a" + p_.getName().substring(0, p_.getName().length() - 3)));
								objective.getScore(Bukkit.getOfflinePlayer("§c" + p_.getName().substring(0, p_.getName().length() - 3))).setScore(score);
							}
						}catch(Exception e){
						}
					}
				}
			}

			p.setScoreboard(board);
		}
	}

	public void removeScoreboard(String arena, Player p) {
		try {
			ScoreboardManager manager = Bukkit.getScoreboardManager();
			Scoreboard sc = manager.getNewScoreboard();
			try{
				if(p.getName().length() < 15){
					board.resetScores(Bukkit.getOfflinePlayer("§c" + p.getName()));
					board.resetScores(Bukkit.getOfflinePlayer("§a" + p.getName()));
				}else{
					board.resetScores(Bukkit.getOfflinePlayer("§c" + p.getName().substring(0, p.getName().length() - 3)));
					board.resetScores(Bukkit.getOfflinePlayer("§a" + p.getName().substring(0, p.getName().length() - 3)));
				}
				
			}catch(Exception e){}

			sc.clearSlot(DisplaySlot.SIDEBAR);
			p.setScoreboard(sc);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public void spawnFirework(Player p) {
		Firework fw = (Firework) p.getWorld().spawnEntity(p.getLocation(), EntityType.FIREWORK);
		FireworkMeta fwm = fw.getFireworkMeta();
		FireworkEffect effect = FireworkEffect.builder().flicker(r.nextBoolean()).withColor(Color.AQUA).withFade(Color.ORANGE).with(Type.STAR).trail(r.nextBoolean()).build();
		fwm.addEffect(effect);
		int rp = r.nextInt(2) + 1;
		fwm.setPower(rp);
		fw.setFireworkMeta(fwm);
	}
	
}
