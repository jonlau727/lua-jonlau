package com.example.anticheat;

import org.bukkit.BanList;
import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;

import java.time.Duration;
import java.time.Instant;
import java.util.Date;

public final class SimpleAntiCheatPlugin extends JavaPlugin {
    private static final Duration BAN_DURATION = Duration.ofDays(2);

    @Override
    public void onEnable() {
        getServer().getPluginManager().registerEvents(new AntiCheatListener(this), this);
        getLogger().info("SimpleAntiCheat enabled.");
    }

    public void banPlayer(String playerName, String reason) {
        Instant expiresAt = Instant.now().plus(BAN_DURATION);
        String fullReason = reason + " | Banned for 2 days by SimpleAntiCheat";

        Bukkit.getBanList(BanList.Type.NAME).addBan(
                playerName,
                fullReason,
                Date.from(expiresAt),
                "SimpleAntiCheat"
        );

        Bukkit.getScheduler().runTask(this, () -> {
            if (Bukkit.getPlayerExact(playerName) != null) {
                Bukkit.getPlayerExact(playerName).kickPlayer(fullReason);
            }
        });

        Bukkit.broadcastMessage("§c[AntiCheat] §f" + playerName + " was banned for 2 days. Reason: " + reason);
        getLogger().warning("Banned " + playerName + " for cheating. Reason: " + reason + ". Expires: " + expiresAt);
    }
}
