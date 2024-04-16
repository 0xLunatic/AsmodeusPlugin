package lunatic.asmodeusplugin.cooldown;

import org.bukkit.entity.Player;

import java.util.HashMap;

public class CooldownManager {

    private HashMap<String, HashMap<String, Long>> cooldowns;

    public CooldownManager() {
        cooldowns = new HashMap<>();
    }

    public void setCooldown(String playerName, String RPGName, long cooldownInSeconds) {
        long cooldownInMillis = cooldownInSeconds * 1000;
        cooldowns.computeIfAbsent(playerName, k -> new HashMap<>()).put(RPGName, System.currentTimeMillis() + cooldownInMillis);
    }

    public void clearCooldown(String playerName, String RPGName) {
        HashMap<String, Long> playerCooldowns = cooldowns.get(playerName);
        if (playerCooldowns != null) {
            playerCooldowns.remove(RPGName);
        }
    }

    public void subtractCooldown(String playerName, String RPGName, long amountInSeconds) {
        long amountInMillis = amountInSeconds * 1000;
        HashMap<String, Long> playerCooldowns = cooldowns.get(playerName);
        if (playerCooldowns != null) {
            playerCooldowns.merge(RPGName, System.currentTimeMillis() + amountInMillis, Long::max);
        }
    }

    public void addCooldown(String playerName, String RPGName, long amountInSeconds) {
        long amountInMillis = amountInSeconds * 1000;
        HashMap<String, Long> playerCooldowns = cooldowns.get(playerName);
        if (playerCooldowns != null) {
            playerCooldowns.merge(RPGName, System.currentTimeMillis() - amountInMillis, Long::max);
        }
    }

    public boolean isOnCooldown(String playerName, String RPGName) {
        HashMap<String, Long> playerCooldowns = cooldowns.get(playerName);
        return playerCooldowns != null && playerCooldowns.getOrDefault(RPGName, 0L) > System.currentTimeMillis();
    }

    public long getRemainingCooldown(String playerName, String RPGName) {
        HashMap<String, Long> playerCooldowns = cooldowns.get(playerName);
        if (playerCooldowns != null) {
            long remainingCooldown = playerCooldowns.getOrDefault(RPGName, 0L) - System.currentTimeMillis();
            return Math.max(remainingCooldown, 0) / 1000; // Convert to seconds
        }
        return 0;
    }

    public void sendCooldownMessage(Player player, String rpgName){
        player.sendMessage("§cYou're currently on " +getRemainingCooldown(player.getName(), rpgName) + " seconds cooldown!");
    }
}
