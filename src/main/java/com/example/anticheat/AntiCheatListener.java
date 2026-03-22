package com.example.anticheat;

import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.player.PlayerToggleFlightEvent;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public final class AntiCheatListener implements Listener {
    private static final double MAX_HORIZONTAL_BLOCKS_PER_TICK = 0.95D;
    private static final int MAX_AIR_TICKS = 12;

    private final SimpleAntiCheatPlugin plugin;
    private final Map<UUID, Integer> airTicks = new HashMap<>();

    public AntiCheatListener(SimpleAntiCheatPlugin plugin) {
        this.plugin = plugin;
    }

    @EventHandler(ignoreCancelled = true)
    public void onPlayerMove(PlayerMoveEvent event) {
        Player player = event.getPlayer();
        if (shouldIgnore(player) || event.getTo() == null || event.getFrom().getWorld() != event.getTo().getWorld()) {
            return;
        }

        checkSpeed(player, event.getFrom(), event.getTo());
        checkFlight(player);
    }

    @EventHandler(ignoreCancelled = true)
    public void onToggleFlight(PlayerToggleFlightEvent event) {
        Player player = event.getPlayer();
        if (shouldIgnore(player)) {
            return;
        }

        plugin.banPlayer(player.getName(), "Illegal flight toggle detected");
        event.setCancelled(true);
    }

    private void checkSpeed(Player player, Location from, Location to) {
        double dx = to.getX() - from.getX();
        double dz = to.getZ() - from.getZ();
        double horizontalDistance = Math.sqrt((dx * dx) + (dz * dz));

        if (horizontalDistance > MAX_HORIZONTAL_BLOCKS_PER_TICK && player.isOnGround() && !player.isSprinting()) {
            plugin.banPlayer(player.getName(), "Speed hacks detected");
        }
    }

    private void checkFlight(Player player) {
        UUID uuid = player.getUniqueId();
        if (player.isOnGround() || isNearClimbable(player) || player.getVelocity().getY() < 0 || player.isInsideVehicle()) {
            airTicks.put(uuid, 0);
            return;
        }

        int updatedAirTicks = airTicks.getOrDefault(uuid, 0) + 1;
        airTicks.put(uuid, updatedAirTicks);
        if (updatedAirTicks > MAX_AIR_TICKS) {
            plugin.banPlayer(player.getName(), "Fly hacks detected");
            airTicks.put(uuid, 0);
        }
    }

    private boolean isNearClimbable(Player player) {
        Material type = player.getLocation().getBlock().getType();
        return type == Material.LADDER || type == Material.VINE || type == Material.SCAFFOLDING;
    }

    private boolean shouldIgnore(Player player) {
        return player.getGameMode() == GameMode.CREATIVE
                || player.getGameMode() == GameMode.SPECTATOR
                || player.isFlying()
                || player.isGliding()
                || player.getAllowFlight();
    }
}
