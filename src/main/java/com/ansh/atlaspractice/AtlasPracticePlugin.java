/*
 * AtlasPractice - Open-source Minecraft Practice plugin.
 * Copyright (C) 2026 Ansh Sharma (Modular Boy Ansh)
 *
 * This file is part of AtlasPractice.
 *
 * AtlasPractice is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * AtlasPractice is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with AtlasPractice. If not, see <https://www.gnu.org/licenses/>.
 *
 * Project: https://github.com/AnshSharma07/AtlasPractice
 * Documentation: https://modularboyansh.xyz/atlas_docs
 */

package com.ansh.atlaspractice;

import com.ansh.atlaspractice.adapters.PlaceholderAPIHook;
import com.ansh.atlaspractice.arena.Arena;
import com.ansh.atlaspractice.arena.ArenaManager;
import com.ansh.atlaspractice.arena.RuntimeArena;
import com.ansh.atlaspractice.arena.ArenaRepository;
import com.ansh.atlaspractice.arena.SharedArenaService;
import com.ansh.atlaspractice.bots.BotManager;
import com.ansh.atlaspractice.commands.*;
import com.ansh.atlaspractice.config.ExplosionConfig;
import com.ansh.atlaspractice.cosmetics.CosmeticManager;
import com.ansh.atlaspractice.cosmetics.Cosmetics;
import com.ansh.atlaspractice.cosmetics.listener.ProjectileTrailListener;
import com.ansh.atlaspractice.cosmetics.task.LobbyCosmeticTask;
import com.ansh.atlaspractice.cosmetics.command.CosmeticsCommand;
import com.ansh.atlaspractice.cosmetics.listener.CosmeticListener;
import com.ansh.atlaspractice.database.SQLiteDatabase;
import com.ansh.atlaspractice.duel.DuelManager;
import com.ansh.atlaspractice.explosion.ExplosionManager;
import com.ansh.atlaspractice.kit.KitManager;
import com.ansh.atlaspractice.leaderboard.LeaderboardManager;
import com.ansh.atlaspractice.listeners.*;
import com.ansh.atlaspractice.managers.CooldownManager;
import com.ansh.atlaspractice.progression.LevelManager;
import com.ansh.atlaspractice.progression.command.CoinsCommand;
import com.ansh.atlaspractice.progression.command.XPCommand;
import com.ansh.atlaspractice.match.MatchManager;
import com.ansh.atlaspractice.menus.MenuManager;
import com.ansh.atlaspractice.menus.SettingsMenu;
import com.ansh.atlaspractice.party.PartyManager;
import com.ansh.atlaspractice.preset.command.PresetKitCreateCommand;
import com.ansh.atlaspractice.preset.command.PresetKitMenuCommand;
import com.ansh.atlaspractice.profile.ProfileManager;
import com.ansh.atlaspractice.queue.QueueManager;
import com.ansh.atlaspractice.replay.ReplayManager;
import com.ansh.atlaspractice.replay.ReplayCommand;
import com.ansh.atlaspractice.replay.ReplayViewCommand;
import com.ansh.atlaspractice.scoreboard.PracticeScoreboardAdapter;
import com.ansh.atlaspractice.settings.PostMatchManager;
import com.ansh.atlaspractice.tasks.QueueMatchmakingTask;
import com.ansh.atlaspractice.tasks.ScoreboardUpdateTask;
import com.ansh.atlaspractice.update.UpdateListener;
import com.ansh.atlaspractice.update.VersionChecker;
import com.ansh.atlaspractice.util.InventoryUtil;
import com.ansh.atlaspractice.world.SlimeWorldService;
import com.ansh.atlaspractice.world.WorldService;
import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.PluginCommand;
import org.bukkit.command.TabCompleter;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import java.io.File;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.logging.Level;

public final class AtlasPracticePlugin extends JavaPlugin {

    private static AtlasPracticePlugin instance;
    private boolean fullyLoaded;
    private CosmeticManager cosmeticManager;
    private ProfileManager profileManager;
    private MatchManager matchManager;
    private PartyManager partyManager;
    private KitManager kitManager;
    private MenuManager menuManager;
    private PlayerKitLayoutListener playerKitLayoutListener;
    private QueueManager queueManager;
    private ArenaManager arenaManager;
    private ArenaRepository arenaRepository;
    private ArenaAdminCommand arenaAdminCommand;
    private SharedArenaService sharedArenaService;
    private InventoryUtil inventoryUtil;
    private KitEditorListener kitEditorListener;
    private DuelManager duelManager;
    private ScoreboardUpdateTask scoreboardUpdateTask;
    private SQLiteDatabase database;
    private SettingsMenu settingsMenu;
    private LeaderboardManager leaderboardManager;
    private PostMatchManager postMatchManager;
    private ExplosionConfig explosionConfig;
    private FileConfiguration scoreboardConfig;
    private CooldownManager cooldownManager;
    private ExplosionManager explosionManager;
    private BotManager botManager;
    private LevelManager levelManager;
    private ReplayManager replayManager;
    private WorldService worldService;
    private VersionChecker versionChecker;
    private com.ansh.atlaspractice.config.RankedConfig rankedConfig;
    @Override
    public void onEnable() {
        instance = this;
        fullyLoaded = false;
        long start = System.currentTimeMillis();

        try {

            saveDefaultConfig();
            this.rankedConfig = new com.ansh.atlaspractice.config.RankedConfig(this);
            this.sharedArenaService = new SharedArenaService(this);
            this.sharedArenaService.load();
            saveResource("scoreboard.yml", false);
            saveResource("bot.yml", false);
            saveResource("shop.yml", false);
            saveResource("level-colors.yml", false);
            saveResource("kill-messages.yml", false);
            File scoreboardFile = new File(getDataFolder(), "scoreboard.yml");
            scoreboardConfig = YamlConfiguration.loadConfiguration(scoreboardFile);
            this.database = new SQLiteDatabase(this);
            this.database.connect();
            profileManager = new ProfileManager(this);
            levelManager = new LevelManager(this);
            matchManager = new MatchManager(this);
            partyManager = new PartyManager(this);
            kitManager = new KitManager(this);
            menuManager = new MenuManager(this);
            settingsMenu = new SettingsMenu(this);
            queueManager = new QueueManager(this);
            replayManager = new ReplayManager(this);
            worldService = new SlimeWorldService(this);
            arenaManager = new ArenaManager();
            this.inventoryUtil = new InventoryUtil(this);
            kitManager.loadKitsFromDisk();
            kitEditorListener = new KitEditorListener(this);
            this.playerKitLayoutListener = new PlayerKitLayoutListener(this);
            getServer().getPluginManager().registerEvents(playerKitLayoutListener, this);

            this.explosionConfig = new ExplosionConfig(this);
            this.cooldownManager = new CooldownManager();
            this.explosionManager = new ExplosionManager(this);

            registerCommand("kitlayout", new KitLayoutCommand(this));
            getServer().getPluginManager().registerEvents(
                    new PlayerConnectionListener(this, profileManager),
                    this
            );
            getServer().getPluginManager().registerEvents(
                    new PartyItemListener(this),
                    this
            );
            getServer().getPluginManager().registerEvents(
                    new MatchDeathListener(this),
                    this
            );
            getServer().getPluginManager().registerEvents(
                    new QueueListener(this),
                    this
            );
            getServer().getPluginManager().registerEvents(
                    new ArenaBreakableListener(this),
                    this
            );
            registerCommand("ranked", new RankedCommand(this));
            registerCommand("unranked", new UnrankedCommand(this));
            getServer().getPluginManager().registerEvents(
                    new LobbyItemInteractListener(this),
                    this
            );
            getServer().getPluginManager().registerEvents(
                    new DurabilityListener(),
                    this
            );
            getServer().getPluginManager().registerEvents(
                    new MatchFreezeListener(),
                    this
            );
            getServer().getPluginManager().registerEvents(
                    new PlayerCombatListener(
                            profileManager,
                            matchManager
                    ),
                    this
            );
            getServer().getPluginManager().registerEvents(
                    new PlayerSumoListener(
                            profileManager,
                            matchManager
                    ),
                    this
            );
            getCommand("randomqueue").setExecutor(new RandomQueueCommand(this));
            getServer().getPluginManager().registerEvents(new HealthSoupListener(), this);
            getServer().getPluginManager().registerEvents(new BattleRushListener(this), this);
            getServer().getPluginManager().registerEvents(new GoldenHeadListener(this), this);
            getServer().getPluginManager().registerEvents(new PlayerHungerListener(this), this);
            getServer().getPluginManager().registerEvents(new WorldInteractionListener(profileManager), this);
            getServer().getPluginManager().registerEvents(
                    new ScoreboardListener(this),
                    this
            );
            getServer().getPluginManager().registerEvents(
                    menuManager,
                    this
            );
            getServer().getPluginManager().registerEvents(
                    new PotionBottleListener(this), this);
            getServer().getPluginManager().registerEvents(
                    new MatchBlockListener(this), this);
            getServer().getPluginManager().registerEvents(kitEditorListener, this);

            getServer().getPluginManager().registerEvents(new TNTListener(this), this);
            getServer().getPluginManager().registerEvents(new FireballListener(this), this);
            getServer().getPluginManager().registerEvents(new BridgeListener(this), this);
            Cosmetics.registerAll();
            cosmeticManager = new CosmeticManager();
            getServer().getPluginManager().registerEvents(new UpdateListener(this), this);
            getServer().getPluginManager().registerEvents(new CosmeticListener(), this);
            getServer().getPluginManager().registerEvents(new ProjectileTrailListener(this), this);
            new LobbyCosmeticTask(this).runTaskTimer(this, 20L, 2L);

            getCommand("cosmetics").setExecutor(new CosmeticsCommand());
            registerCommand("duel", new DuelCommand(this, profileManager));
            registerCommand("party", new PartyCommand(this));
            registerCommand("xp", new XPCommand(this));
            registerCommand("bot", new BotFightCommand());
            registerCommand("coins", new CoinsCommand(this));
            getServer().getPluginManager().registerEvents(new PortalListener(), this);
            registerCommand(
                    "stats",
                    new StatsCommand(profileManager)
            );
            registerCommand(
                    "spec",
                    new SpecCommand(this, profileManager)
            );
            registerCommand("resetlayout", new ResetLayoutCommand(this));
            registerCommand("atlasleaderboard", new LeaderboardCommand(this));
            this.arenaRepository =
                    new ArenaRepository(this);
            this.arenaAdminCommand =
                    new ArenaAdminCommand(
                            arenaManager,
                            this.arenaRepository
                    );
            registerCommand("arena", arenaAdminCommand);
            getCommand("toolazytocreatekits").setExecutor(new PresetKitMenuCommand());
            getCommand("presetkitcreate").setExecutor(new PresetKitCreateCommand());
            getCommand("arenaautosetup").setExecutor(new com.ansh.atlaspractice.arena.setup.ArenaAutoSetupCommand(this));
            if (getCommand("arena") != null) {
                getCommand("arena").setTabCompleter(
                        new ArenaTabCompleter(this)
                );
            }

            PracticeScoreboardAdapter scoreboardAdapter = new PracticeScoreboardAdapter(this);
            this.scoreboardUpdateTask = new ScoreboardUpdateTask(scoreboardAdapter);
            this.scoreboardUpdateTask.runTaskTimer(this, 20L, 20L);
            this.leaderboardManager = new LeaderboardManager(this);
            this.leaderboardManager.start();
            this.botManager = new BotManager(this);

            this.duelManager = new DuelManager(this);
            SpawnCommand spawnCommandExecutor = new SpawnCommand(this);
            this.registerCommand("accept", new com.ansh.atlaspractice.commands.AcceptCommand(this));
            registerCommand("deny", new DenyCommand(this));
            registerCommand("setlobbyspawn", spawnCommandExecutor);
            registerCommand("spawn", spawnCommandExecutor);
            registerCommand(
                    "practice",
                    new PracticeCommandExecutor(profileManager)
            );
            registerCommand(
                    "practiceadmin",
                    new PracticeAdminCommand(this)
            );
            registerCommand("atlasreplayview", new ReplayViewCommand(replayManager));
            ReplayCommand replayCommand = new ReplayCommand(this, replayManager);
            registerCommand("replays", replayCommand);
            registerCommand("replay", replayCommand);
            getServer().getPluginManager().registerEvents(replayCommand, this);
            registerCommand("allowedrankedkits", new com.ansh.atlaspractice.commands.AllowedRankedKitsCommand(this));
            KitAdminCommand kitCommand =
                    new KitAdminCommand(this);
            PluginCommand kit =
                    getCommand("kit");
            if (kit != null) {
                kit.setExecutor(kitCommand);
                kit.setTabCompleter(
                        new KitTabCompleter(this)
                );
            }
            Bukkit.getPluginManager().registerEvents(new com.ansh.atlaspractice.menus.SettingsMenu(this), this);
            Bukkit.getPluginManager().registerEvents(new com.ansh.atlaspractice.listeners.SettingsListener(this), this);

            this.postMatchManager = new com.ansh.atlaspractice.settings.PostMatchManager(this);
            this.arenaRepository
                    .loadArenas(arenaManager);
            new QueueMatchmakingTask(this)
                    .runTaskTimer(this, 20L, 20L);
            if (getServer()
                    .getPluginManager()
                    .getPlugin("PlaceholderAPI") != null) {

                new PlaceholderAPIHook(
                        this,
                        profileManager
                ).register();
            }

            AtlasPracticeAPI.setInstance(
                    new AtlasPracticeAPI(this)
            );
            printLogo();

            fullyLoaded = true;

            getLogger().info(
                    "AtlasPractice enabled in "
                            + (System.currentTimeMillis() - start)
                            + "ms."
            );
        } catch (Exception exception) {

            getLogger().log(
                    Level.SEVERE,
                    "Failed to enable AtlasPractice.",
                    exception
            );
            getServer()
                    .getPluginManager()
                    .disablePlugin(this);
        }
        versionChecker = new VersionChecker(this);
        versionChecker.check();
    }

    @Override
    public void onDisable() {
        cleanupRuntimeArenaWorlds();
        if (this.replayManager != null) {
            this.replayManager.shutdown();
        }
        if (this.leaderboardManager != null) {
            this.leaderboardManager.shutdown();
        }
        printDisableLogo();
        if (this.database != null) {
            this.database.shutdown();
        }
        if (this.botManager != null) {
            this.botManager.despawnAll();
        }
        instance = null;
    }

    private void cleanupRuntimeArenaWorlds() {
        if (this.arenaManager == null) return;
        for (Arena arena : this.arenaManager.getArenas()) {
            if (arena.getWorldName() == null) continue;
            World world = Bukkit.getWorld(arena.getWorldName());
            if (world == null) continue;
            boolean unloaded = Bukkit.unloadWorld(world, false);
            if (unloaded) {
                getLogger().info("[AtlasPractice] Unloaded runtime arena world " + arena.getWorldName() + " during plugin disable.");
                if (arena instanceof RuntimeArena && this.worldService != null) {
                    this.worldService.deleteTemporaryWorld(arena.getWorldName()).join();
                }
            } else {
                getLogger().warning("[AtlasPractice] Bukkit refused to unload runtime arena world " + arena.getWorldName() + " during plugin disable.");
            }
        }
    }

    private void printDisableLogo() {
        getLogger().info(" ");
        getLogger().info("╔════════════════════════════════════════════════════════════╗");
        getLogger().info("║                                                            ║");
        getLogger().info("║     █████╗ ████████╗██╗      █████╗ ███████╗               ║");
        getLogger().info("║    ██╔══██╗╚══██╔══╝██║     ██╔══██╗██╔════╝               ║");
        getLogger().info("║    ███████║   ██║   ██║     ███████║███████╗               ║");
        getLogger().info("║    ██╔══██║   ██║   ██║     ██╔══██║╚════██║               ║");
        getLogger().info("║    ██║  ██║   ██║   ███████╗██║  ██║███████║               ║");
        getLogger().info("║    ╚═╝  ╚═╝   ╚═╝   ╚══════╝╚═╝  ╚═╝╚══════╝               ║");
        getLogger().info("║                                                            ║");
        getLogger().info("║                     AtlasPractice                          ║");
        getLogger().info("╠════════════════════════════════════════════════════════════╣");
        getLogger().info("║ Status           : SHUTDOWN                                ║");
        getLogger().info("║ Save Data        : Complete                                ║");
        getLogger().info("║ Resources        : Released                                ║");
        getLogger().info("║ Thank you for using AtlasPractice!                         ║");
        getLogger().info("╚════════════════════════════════════════════════════════════╝");
        getLogger().info(" ");
    }

    private void printLogo() {
        getLogger().info(" ");
        getLogger().info("╔════════════════════════════════════════════════════════════╗");
        getLogger().info("║                                                            ║");
        getLogger().info("║     █████╗ ████████╗██╗      █████╗ ███████╗               ║");
        getLogger().info("║    ██╔══██╗╚══██╔══╝██║     ██╔══██╗██╔════╝               ║");
        getLogger().info("║    ███████║   ██║   ██║     ███████║███████╗               ║");
        getLogger().info("║    ██╔══██║   ██║   ██║     ██╔══██║╚════██║               ║");
        getLogger().info("║    ██║  ██║   ██║   ███████╗██║  ██║███████║               ║");
        getLogger().info("║    ╚═╝  ╚═╝   ╚═╝   ╚══════╝╚═╝  ╚═╝╚══════╝               ║");
        getLogger().info("║                                                            ║");
        getLogger().info("║                     AtlasPractice                          ║");
        getLogger().info("║                      Version 1.0                           ║");
        getLogger().info("║                Developed by ModularBoyAnsh                 ║");
        getLogger().info("║                  🇮🇳 Proudly Made in India 🇮🇳                ║");
        getLogger().info("╠════════════════════════════════════════════════════════════╣");
        getLogger().info("║ Status           : READY                                   ║");
        getLogger().info("║ Kits             : Loaded                                  ║");
        getLogger().info("║ Arenas           : Loaded                                  ║");
        getLogger().info("║ PlaceholderAPI   : Hooked                                  ║");
        getLogger().info("║ Replay System    : Enabled                                 ║");
        getLogger().info("╚════════════════════════════════════════════════════════════╝");
        getLogger().info(" ");
    }

    private String pad(String text, int spaces) {
        return text + " ".repeat(Math.max(0, spaces - text.length()));
    }

    private void registerCommand(String commandName, CommandExecutor executor) {
        PluginCommand command = getCommand(commandName);
        if (command == null) {
            getLogger().warning("Missing command in plugin.yml: " + commandName);
            return;
        }
        command.setExecutor(executor);
        if (executor instanceof TabCompleter) {
            command.setTabCompleter((TabCompleter) executor);
        }
    }

    public static AtlasPracticePlugin getInstance() { return instance; }
    public boolean isFullyLoaded() { return fullyLoaded; }
    public BotManager getBotManager() {return botManager;}
    public LevelManager getLevelManager() { return levelManager; }
    public ProfileManager getProfileManager() { return profileManager; }
    public MatchManager getMatchManager() { return matchManager; }
    public PartyManager getPartyManager() { return partyManager; }
    public KitManager getKitManager() { return kitManager; }
    public MenuManager getMenuManager() { return menuManager; }
    public CosmeticManager getCosmeticManager() {return cosmeticManager;}
    public QueueManager getQueueManager() { return queueManager; }
    public SettingsMenu getSettingsMenu() { return settingsMenu; }
    public ArenaManager getArenaManager() { return arenaManager; }
    public InventoryUtil getInventoryUtil() { return inventoryUtil; }
    public LeaderboardManager getLeaderboardManager() { return leaderboardManager; }
    public KitEditorListener getKitEditorListener() { return kitEditorListener; }
    public ArenaRepository getArenaRepository() { return arenaRepository; }
    public ArenaAdminCommand getArenaAdminCommand() { return arenaAdminCommand; }
    public SharedArenaService getSharedArenaService() { return sharedArenaService; }
    public DuelManager getDuelManager() { return this.duelManager; }public PostMatchManager getPostMatchManager() {return postMatchManager;}
    public PlayerKitLayoutListener getPlayerKitLayoutListener() { return playerKitLayoutListener; }
    public SQLiteDatabase getDatabaseService() { return this.database; }
    public ScoreboardUpdateTask getScoreboardUpdateTask() { return this.scoreboardUpdateTask; }
    public ExplosionConfig getExplosionConfig() { return explosionConfig; }
    public FileConfiguration getScoreboardConfig() {return scoreboardConfig;}
    public CooldownManager getCooldownManager() { return cooldownManager; }
    public ExplosionManager getExplosionManager() { return explosionManager; }
    public ReplayManager getReplayManager() { return replayManager; }
    public VersionChecker getVersionChecker() {return versionChecker;}
    public WorldService getWorldService() { return worldService; }
    public com.ansh.atlaspractice.config.RankedConfig getRankedConfig() { return rankedConfig; }
}