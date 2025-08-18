package net.nextfur.fws.misc;

import net.nextfur.fws.FwMain;

import org.bukkit.Bukkit;
import org.bukkit.Location;

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

        String world = compact.split(":")[0];

        double x = Double.parseDouble(compact.split(":")[1].split(";")[0]);
        double y = Double.parseDouble(compact.split(":")[1].split(";")[1]);
        double z = Double.parseDouble(compact.split(":")[1].split(";")[2]);
        float pitch = Float.parseFloat(compact.split(":")[1].split(";")[3]);
        float yaw = Float.parseFloat(compact.split(":")[1].split(";")[4]);

        Location location = new Location(Bukkit.getWorld(world), x, y, z, yaw, pitch);
        return location;
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
