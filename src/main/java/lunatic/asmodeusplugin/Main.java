package lunatic.asmodeusplugin;

import lunatic.asmodeusplugin.cooldown.CooldownManager;
import lunatic.asmodeusplugin.db.AsmodeusDatabase;
import lunatic.asmodeusplugin.listener.BelialListener;
import lunatic.asmodeusplugin.listener.JoinListener;
import lunatic.asmodeusplugin.listener.PlayerDeathListener;
import lunatic.asmodeusplugin.rewards.BoxOpenListener;
import lunatic.asmodeusplugin.rewards.DropListener;
import lunatic.asmodeusplugin.shop.AsmodeusShop;
import lunatic.asmodeusplugin.shop.DerickNPC;
import lunatic.asmodeusplugin.skills.EvasiveManeuver;
import lunatic.asmodeusplugin.skills.MonsterGenerator;
import lunatic.asmodeusplugin.treasurepower.TreasurePowerUpgrader;
import net.milkbowl.vault.economy.Economy;
import org.bukkit.Bukkit;
import org.bukkit.block.Block;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.Player;
import org.bukkit.plugin.RegisteredServiceProvider;
import org.bukkit.plugin.java.JavaPlugin;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;

public final class Main extends JavaPlugin {
    private FileConfiguration config;
    public AsmodeusDatabase database;
    public static Economy econ = null;

    public boolean asmodeusStarted;
    public String asmodeusPlayer;
    public boolean isInAction;

    public Block generatorBlockAbove;
    public Block generatorRespawnAnchorBlock;

    public boolean isAbaddonDeath;
    public boolean isCerberusDeath;
    public boolean isAzraelDeath;
    public boolean isAstarothDeath;


    public int manueverCount = 0;

    public List<ArmorStand> armorStandList;


    public Map<Player, Action> actions;


    public HashMap<String, Integer> playerResistance;
    public HashMap<String, Integer> playerDamage;

    public Player[] joinQueue;
    public int queueIndex = 0;

    CooldownManager cooldownManager;

    @Override
    public void onEnable() {
        // Plugin startup logic

        this.playerResistance = new HashMap<>();
        this.playerDamage = new HashMap<>();
        this.joinQueue = new Player[5];
        this.cooldownManager = new CooldownManager();

        this.actions = new HashMap<>();

        System.out.println("Asmodeus Plugin Enabled");

        getServer().getPluginManager().registerEvents(new JoinListener(this), this);
        getServer().getPluginManager().registerEvents(new EvasiveManeuver(this), this);
        getServer().getPluginManager().registerEvents(new MonsterGenerator(this), this);
        getServer().getPluginManager().registerEvents(new BelialListener(this), this);
        getServer().getPluginManager().registerEvents(new PlayerDeathListener(this), this);
        getServer().getPluginManager().registerEvents(new BoxOpenListener(this), this);
        getServer().getPluginManager().registerEvents(new TreasurePowerUpgrader(this), this);
        getServer().getPluginManager().registerEvents(new DropListener(this), this);
        getServer().getPluginManager().registerEvents(new AsmodeusShop(this), this);
        getServer().getPluginManager().registerEvents(new DerickNPC(this), this);

        getCommand("derickgui").setExecutor(new DerickNPC(this));
        getCommand("belialshop").setExecutor(new AsmodeusShop(this));
        getCommand("treasureupgrader").setExecutor(new TreasurePowerUpgrader(this));

        if (!setupEconomy() ) {
            getLogger().severe(String.format("[%s] - Disabled due to no Vault dependency found!", getDescription().getName()));
            getServer().getPluginManager().disablePlugin(this);
            return;
        }

        saveDefaultConfig();
        this.config = getConfig();

        armorStandList = new ArrayList<>();

        clearVariables();

        this.database = new AsmodeusDatabase(getConfig());
        try {
            database.connect();
            database.createTableIfNotExists();
            getLogger().log(Level.INFO, "\u001B[32m[Asmodeus Plugin] Connected to database!");
        } catch (SQLException e) {
            getLogger().severe("Failed to connect to the database: " + e.getMessage());
            return;
        }

    }


    @Override
    public void onDisable() {
        // Plugin shutdown logic
        for (ArmorStand armorStand : armorStandList) {
            armorStand.remove();
        }
        armorStandList.clear();
    }

    public void removeFromQueue(String playerName) {
        // Find the index of the player in the queue
        int index = -1;
        for (int i = 0; i < queueIndex; i++) {
            if (joinQueue[i] != null && joinQueue[i].getName().equals(playerName)) {
                index = i;
                break;
            }
        }

        // If the player is found in the queue, remove them
        if (index != -1) {
            for (int i = index; i < queueIndex - 1; i++) {
                joinQueue[i] = joinQueue[i + 1];
            }
            joinQueue[queueIndex - 1] = null;
            queueIndex--;
        }
    }


    public void processQueue() {
        if (asmodeusStarted || queueIndex == 0 || joinQueue[0] == null) {
            return;
        }

        Bukkit.getScheduler().runTaskLater(this, () -> {
            // Start the Asmodeus event for the first player in the queue
            JoinListener joinListener = new JoinListener(this);
            joinListener.startAsmodeusEvent(joinQueue[0]);

            // Shift the elements of the queue forward
            for (int i = 0; i < queueIndex - 1; i++) {
                joinQueue[i] = joinQueue[i + 1];
            }

            // Set the last element to null and decrement the queue index
            joinQueue[queueIndex - 1] = null;
            queueIndex--;

        }, 2 * 20L);
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

    public enum Action {
        JUMP,
        SNEAK,
        CLICK
    }
    public void clearVariables() {
        // Monster Variable
        isAbaddonDeath = false;
        isCerberusDeath = false;
        isAzraelDeath = false;
        isAstarothDeath = false;

        // Clearing boolean variables
        asmodeusStarted = false;
        isInAction = false;

        // Clearing String variables
        asmodeusPlayer = null;

        // Clearing Block variables
        generatorBlockAbove = null;
        generatorRespawnAnchorBlock = null;

        // Clearing ArmorStand list
        for (ArmorStand armorStand : armorStandList) {
            armorStand.remove();
        }
        armorStandList.clear();

        // Clearing the actions map
        actions.clear();

        manueverCount = 0;


        // Execute the command
        Bukkit.dispatchCommand(Bukkit.getConsoleSender(), "mm m kill Belial");
        Bukkit.dispatchCommand(Bukkit.getConsoleSender(), "mm m kill Abaddon");
        Bukkit.dispatchCommand(Bukkit.getConsoleSender(), "mm m kill GrimReaper");
        Bukkit.dispatchCommand(Bukkit.getConsoleSender(), "mm m kill Cerberus");
        Bukkit.dispatchCommand(Bukkit.getConsoleSender(), "mm m kill Astaroth");

        EvasiveManeuver evasiveManeuver = new EvasiveManeuver(this);
        evasiveManeuver.restoreBlocks();
    }
    public AsmodeusDatabase getDatabase() {
        return database;
    }
    public static Economy getEconomy() {
        return econ;
    }

}
