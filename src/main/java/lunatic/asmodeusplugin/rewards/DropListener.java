package lunatic.asmodeusplugin.rewards;

import lunatic.asmodeusplugin.Main;
import lunatic.asmodeusplugin.cooldown.CooldownManager;
import org.bukkit.Bukkit;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.potion.PotionEffectType;

import java.sql.SQLException;
import java.util.Random;

public class DropListener implements Listener {
    private Main plugin;
    private CooldownManager cooldownManager;

    public DropListener(Main plugin){
        this.plugin = plugin;
        this.cooldownManager = new CooldownManager();

    }


    @EventHandler
    public void onBelialDeath(EntityDeathEvent event) {
        if (event.getEntity().getName().equals("§c§lBelial")) {
            if (plugin.asmodeusPlayer != null) {
                if (plugin.asmodeusPlayer.equals(event.getEntity().getKiller().getName())) {
                    Player player = Bukkit.getPlayer(plugin.asmodeusPlayer);

                    for (Player onlinePlayer : Bukkit.getOnlinePlayers()) {
                        onlinePlayer.playSound(onlinePlayer, Sound.ENTITY_IRON_GOLEM_DEATH, 1f, 0f);
                    }

                    try {
                        plugin.database.addAsmodeusKilled(player.getUniqueId(), player.getName());
                    } catch (SQLException e) {
                        throw new RuntimeException(e);
                    }

                    int treasurePower = 0;
                    try {
                        treasurePower = plugin.database.getTreasurePower(player.getUniqueId(), player.getName());
                    } catch (SQLException e) {
                        throw new RuntimeException(e);
                    }

                    // Base chances
                    double commonChance = 0.20;
                    double rareChance = 0.15;
                    double epicChance = 0.10;
                    double legendChance = 0.05;

                    double epicBoostFactor = 0.05; // Adjusted boost factor
                    double legendBoostFactor = 0.03; // Adjusted boost factor

                    epicChance += treasurePower * epicBoostFactor;
                    legendChance += treasurePower * legendBoostFactor;

                    double totalChance = commonChance + rareChance + epicChance + legendChance;
                    if (totalChance > 1.0) {
                        double excess = totalChance - 1.0;
                        epicChance -= excess / 2; // Adjusting excess to epic
                        legendChance -= excess / 2; // Adjusting excess to legend
                    }

                    Random rand = new Random();

                    int randomNumber = rand.nextInt(100); // Generates a number between 0 (inclusive) and 101 (exclusive)
                    double random = randomNumber / 100.0;

                    double cumulativeRareChance = commonChance + rareChance;
                    double cumulativeEpicChance = cumulativeRareChance + epicChance;
                    double cumulativeLegendChance = cumulativeEpicChance + legendChance;

                    Random randDem = new Random();
                    int randomDemonEssence = randDem.nextInt(25);
                    try {
                        plugin.database.registerDataPlayer(player.getUniqueId(), player.getName());
                        plugin.database.addDemonEssence(player.getUniqueId(), player.getName(), randomDemonEssence);
                    } catch (SQLException e) {
                        throw new RuntimeException(e);
                    }
                    double petBroadcastChance = 0.05;
                    if (rand.nextDouble() < petBroadcastChance) {
                        Bukkit.dispatchCommand(Bukkit.getConsoleSender(), "lp user " + player.getName() + " permission set advancedpets.pet.demon");
                        Bukkit.broadcastMessage("");
                        Bukkit.broadcastMessage("§e" + player.getName() + " §fberhasil mengalahkan §c§lBelial§f dan mendapatkan §c§lDemon Pet§f!");
                        Bukkit.broadcastMessage("");
                        for (Player onlinePlayer : Bukkit.getOnlinePlayers()) {
                            onlinePlayer.playSound(onlinePlayer, Sound.ENTITY_ENDER_DRAGON_GROWL, 1f, 0f);
                        }
                    }

                    if (random < commonChance) {
                        Bukkit.dispatchCommand(Bukkit.getConsoleSender(), "mi give material pure_common_box " + player.getName() + " 3");
                        Bukkit.broadcastMessage("");
                        Bukkit.broadcastMessage("§e" + player.getName() + " §fberhasil mengalahkan §c§lBelial§f dan mendapatkan §a§lCommon Box§f dan §c" + randomDemonEssence + " Demon Essence§f! §7(§6" + treasurePower + "% Treasure Power§7)");
                        Bukkit.broadcastMessage("");
                    } else if (random < cumulativeRareChance) {
                        Bukkit.dispatchCommand(Bukkit.getConsoleSender(), "mi give material pure_rare_box " + player.getName() + " 2");
                        Bukkit.broadcastMessage("");
                        Bukkit.broadcastMessage("§e" + player.getName() + " §fberhasil mengalahkan §c§lBelial§f dan mendapatkan §9§lRare Box§f dan §c" + randomDemonEssence + " Demon Essence§f! §7(§6" + treasurePower + "% Treasure Power§7)");
                        Bukkit.broadcastMessage("");
                    } else if (random < cumulativeEpicChance) {
                        Bukkit.dispatchCommand(Bukkit.getConsoleSender(), "mi give material pure_epic_box " + player.getName() + " 1");
                        Bukkit.broadcastMessage("");
                        Bukkit.broadcastMessage("§e" + player.getName() + " §fberhasil mengalahkan §c§lBelial§f dan mendapatkan §5§lEpic Box§f dan §c" + randomDemonEssence + " Demon Essence§f! §7(§6" + treasurePower + "% Treasure Power§7)");
                        Bukkit.broadcastMessage("");
                    } else if (random < cumulativeLegendChance) {
                        Bukkit.dispatchCommand(Bukkit.getConsoleSender(), "mi give material pure_legendary_box " + player.getName() + " 1");
                        Bukkit.broadcastMessage("");
                        Bukkit.broadcastMessage("§e" + player.getName() + " §fberhasil mengalahkan §c§lBelial§f dan mendapatkan §e§lLegendary Box§f dan §c" + randomDemonEssence + " Demon Essence§f! §7(§6" + treasurePower + "% Treasure Power§7)");
                        Bukkit.broadcastMessage("");
                    }
                    player.removePotionEffect(PotionEffectType.NIGHT_VISION);

                    Bukkit.getScheduler().runTaskLater(plugin, () -> {
                        plugin.clearVariables();
                        Bukkit.dispatchCommand(Bukkit.getConsoleSender(), "spawn " + player.getName());
                        Bukkit.dispatchCommand(Bukkit.getConsoleSender(), "spawn " + player.getName());
                        plugin.processQueue();
                    }, 2 * 20L);
                }
            }
        }
    }

}
