// SPDX-License-Identifier: MIT
package com.liquidpackage.config;

import java.io.FileReader;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.NoSuchFileException;
import java.nio.file.Path;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.liquidpackage.LiquidPackage;

import net.fabricmc.loader.api.FabricLoader;

public final class ConfigReader {
    private static final Gson GSON = new Gson();

    private ConfigReader() {
        throw new UnsupportedOperationException("This is a utility class and cannot be instantiated");
    }

    private static int getIntOrDefault(JsonObject obj, String key, int defaultValue) {
        if (obj.has(key) && !obj.get(key).isJsonNull()) {
            return obj.get(key).getAsInt();
        }
        return defaultValue;
    }

    /**
     * reads config, returns default on Error
     * 
     * @param fileName имя или путь к файлу конфигурации
     * @return считанный Config или Config.DEFAULT в случае ошибки
     */
    public static Config request(String fileName) {
        Path configDir = FabricLoader.getInstance().getConfigDir();
        Path configPath = configDir.resolve(fileName + ".json");
        try (FileReader reader = new FileReader(configPath.toFile())) {

            JsonObject rootObject = GSON.fromJson(reader, JsonObject.class);
            JsonArray shellsArray = rootObject.getAsJsonArray("shells");

            Config config = new Config();

            config.setEmptyShellFuelTime(
                    getIntOrDefault(rootObject, "emptyShellFuelTime", Config.DEFAULT.emptyShellFuelTime()));

            if (shellsArray != null) {
                for (JsonElement el : shellsArray) {
                    config.push(GSON.fromJson(el, Config.Item.class));
                }
            }

            return config;

        } catch (NoSuchFileException e) {
            LiquidPackage.LOGGER.info("There is no configuration file \"{}\". Default values will apply", configPath);
            return Config.DEFAULT;

        } catch (IOException e) {
            LiquidPackage.LOGGER.warn("There is no access to configuration file \"{}\". Default values will apply",
                    configPath);
            return Config.DEFAULT;
        }
    }

    /**
     * Сохраняет (дампит) текущий конфиг в файл.
     * 
     * @param cfg объект конфигурации для сохранения
     * @return true, если сохранение прошло успешно, иначе false
     */
    public static boolean save(Config cfg, String fileName) {
        if (cfg == null) {
            return false;
        }

        Path configDir = FabricLoader.getInstance().getConfigDir();
        Path configPath = configDir.resolve(fileName + ".json");

        try {
            JsonObject rootObject = new JsonObject();
            JsonArray shellJsonArray = new JsonArray();

            rootObject.addProperty("emptyShellFuelTime", cfg.emptyShellFuelTime());

            for (Config.Item item : cfg) {
                shellJsonArray.add(GSON.toJsonTree(item));
            }

            rootObject.add("shells", shellJsonArray);

            String prettyJson = GSON.newBuilder().setPrettyPrinting().create().toJson(rootObject);

            Files.writeString(configPath, prettyJson);

            return true;
        } catch (Exception e) {
            LiquidPackage.LOGGER.warn("Unable to save configuration to \"{}\"", e.getMessage());
            return false;
        }
    }
}