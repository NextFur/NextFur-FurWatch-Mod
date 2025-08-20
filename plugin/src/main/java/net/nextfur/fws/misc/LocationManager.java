package net.nextfur.fws.misc;

import net.nextfur.fws.FwMain;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;

import java.io.IOException;

public class LocationManager {
    public static void saveLocation(Location location, String name) {
        String world = location.getWorld().getName();
        double x = location.getX();
        double y = location.getY();
        double z = location.getZ();
        float yaw = location.getYaw();
        float pitch = location.getPitch();

        String compact = world + ":" + x + ";" + y + ";" + z + ";" + pitch + ";" + yaw;

        FwMain.config.set(name, compact);
        try {
            FwMain.config.save(FwMain.file);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static Location getLocation(String name) {
        String compact = FwMain.config.getString(name);

        if (compact == null || compact.isEmpty()) {
            Bukkit.getLogger().severe("[FURSMP] Location '" + name + "' not found in config.yml!");
            return null;
        }

        String[] parts = compact.split(":");
        if (parts.length != 2) {
            Bukkit.getLogger().severe("[FURSMP] Invalid location format for '" + name + "'. Expected 'world:x;y;z;pitch;yaw'.");
            return null;
        }

        String worldName = parts[0];
        String[] coords = parts[1].split(";");

        if (coords.length < 5) {
            Bukkit.getLogger().severe("[FURSMP] Invalid coordinates for '" + name + "'. Expected 5 values for x,y,z,pitch,yaw.");
            return null;
        }

        try {
            World world = Bukkit.getWorld(worldName);
            if (world == null) {
                Bukkit.getLogger().severe("[FURSMP] World '" + worldName + "' for location '" + name + "' is not loaded!");
                return null;
            }
            
            double x = Double.parseDouble(coords[0]);
            double y = Double.parseDouble(coords[1]);
            double z = Double.parseDouble(coords[2]);
            float pitch = Float.parseFloat(coords[3]);
            float yaw = Float.parseFloat(coords[4]);

            return new Location(world, x, y, z, yaw, pitch);

        } catch (NumberFormatException e) {
            Bukkit.getLogger().severe("[FURSMP] Could not parse numbers for location '" + name + "'. Please check the values.");
            return null;
        }
    }

    public static void deleteLocation(String name) {
        FwMain.config.set(name, null);
        try {
            FwMain.config.save(FwMain.file);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
