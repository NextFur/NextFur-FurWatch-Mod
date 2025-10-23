/*
 * Copyright (c) 2025 NiixLabs
 * Todos os direitos reservados.
 *
 * Autor: Niix-Dan
 */
package net.nextfur.fws.bukkit.utils;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;

public final class PositionParser {
    private PositionParser() {
        throw new UnsupportedOperationException("Esta é uma classe utilitária e não pode ser instanciada.");
    }

    /**
     * Serializa uma {@link Location} do Bukkit para uma String.
     *
     * @param location A localização a ser serializada.
     * @return A string formatada (ex: "100.5,64.0,200.0,90.0,0.0:world")
     * @throws IllegalArgumentException Se a localização for nula ou o mundo for nulo.
     */
    public static String serialize(Location location) {
        if (location == null) {
            throw new IllegalArgumentException("A localização não pode ser nula.");
        }
        if (location.getWorld() == null) {
            throw new IllegalArgumentException("O mundo na localização não pode ser nulo.");
        }

        return location.getX() + "," +
                location.getY() + "," +
                location.getZ() + "," +
                location.getYaw() + "," +
                location.getPitch() + ":" +
                location.getWorld().getName();
    }

    /**
     * Desserializa uma string de volta para uma {@link Location} do Bukkit.
     *
     * @param input A string formatada (ex: "100.5,64.0,200.0,90.0,0.0:world")
     * @return O objeto {@link Location} correspondente.
     * @throws IllegalArgumentException Se o formato da string for inválido,
     * os números forem inválidos, ou o mundo não for encontrado.
     */
    public static Location deserialize(String input) {
        if (input == null || input.isEmpty()) {
            throw new IllegalArgumentException("A string de entrada não pode ser nula ou vazia.");
        }

        String[] parts = input.split(":");
        if (parts.length != 2) {
            throw new IllegalArgumentException("Formato inválido. Esperado 'coords:world'. String recebida: " + input);
        }

        String[] coords = parts[0].split(",");
        if (coords.length != 5) {
            throw new IllegalArgumentException("Formato de coordenadas inválido. Esperado 'x,y,z,yaw,pitch'. String recebida: " + parts[0]);
        }

        try {
            double x = Double.parseDouble(coords[0]);
            double y = Double.parseDouble(coords[1]);
            double z = Double.parseDouble(coords[2]);
            float yaw = Float.parseFloat(coords[3]);
            float pitch = Float.parseFloat(coords[4]);

            String worldName = parts[1];
            World world = Bukkit.getWorld(worldName);

            if (world == null) {
                throw new IllegalArgumentException("Mundo não encontrado ou não carregado: '" + worldName + "'");
            }

            return new Location(world, x, y, z, yaw, pitch);

        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("Número inválido na string de localização: " + e.getMessage());
        }
    }
}
