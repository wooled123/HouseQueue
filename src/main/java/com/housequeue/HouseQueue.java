package com.housequeue;

import org.bukkit.*;
import org.bukkit.command.*;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.*;

public class HouseQueue extends JavaPlugin {

    // Players waiting for a match
    final List<UUID> queue = new ArrayList<>();

    // Where each player came from before being teleported
    final Map<UUID, Location> origins = new HashMap<>();

    // All registered house spawn points
    final List<Location> houses = new ArrayList<>();

    @Override
    public void onEnable() {
        getCommand("queue").setExecutor(this::onQueue);
        getCommand("leavequeue").setExecutor(this::onLeave);
        getCommand("addhouse").setExecutor(this::onAddHouse);
    }

    boolean onQueue(CommandSender sender, Command cmd, String label, String[] args) {
        if (!(sender instanceof Player p)) return true;

        if (origins.containsKey(p.getUniqueId())) {
            p.sendMessage(color("&cYou're already visiting! Type /leavequeue to leave."));
            return true;
        }
        if (queue.contains(p.getUniqueId())) {
            p.sendMessage(color("&cYou're already in the queue!"));
            return true;
        }
        if (houses.isEmpty()) {
            p.sendMessage(color("&cNo houses registered yet. Ask an admin to use /addhouse."));
            return true;
        }

        queue.add(p.getUniqueId());
        p.sendMessage(color("&aJoined the queue! Waiting for someone..."));

        // Try to match if 2+ people are waiting
        if (queue.size() >= 2) {
            UUID id1 = queue.remove(0);
            UUID id2 = queue.remove(0);
            Player p1 = Bukkit.getPlayer(id1);
            Player p2 = Bukkit.getPlayer(id2);

            if (p1 == null || p2 == null) return true; // someone went offline

            Location house = houses.get(new Random().nextInt(houses.size()));

            origins.put(id1, p1.getLocation());
            origins.put(id2, p2.getLocation());

            p1.teleport(house);
            p2.teleport(house);

            p1.sendMessage(color("&6Match found! You're visiting a house. Type /leavequeue when done."));
            p2.sendMessage(color("&6Match found! You're visiting a house. Type /leavequeue when done."));
        }

        return true;
    }

    boolean onLeave(CommandSender sender, Command cmd, String label, String[] args) {
        if (!(sender instanceof Player p)) return true;
        UUID id = p.getUniqueId();

        // Remove from queue if waiting
        if (queue.remove(id)) {
            p.sendMessage(color("&cLeft the queue."));
            return true;
        }

        // Send home if visiting
        Location origin = origins.remove(id);
        if (origin != null) {
            p.teleport(origin);
            p.sendMessage(color("&aSent you back home!"));
        } else {
            p.sendMessage(color("&cYou're not in the queue or visiting anyone."));
        }

        return true;
    }

    boolean onAddHouse(CommandSender sender, Command cmd, String label, String[] args) {
        if (!(sender instanceof Player p)) return true;
        houses.add(p.getLocation());
        p.sendMessage(color("&aHouse #" + houses.size() + " registered here!"));
        return true;
    }

    String color(String s) {
        return s.replace("&", "\u00a7");
    }
}
