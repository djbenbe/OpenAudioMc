package com.craftmend.openaudiomc.modules.configuration;

import com.craftmend.openaudiomc.OpenAudioMc;
import lombok.Getter;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.io.IOException;

public class ConfigurationModule {

    @Getter private String server = "http://craftmendserver.eu";
    @Getter private FileConfiguration mainConfig;
    @Getter private FileConfiguration dataConfig;

    public ConfigurationModule(OpenAudioMc openAudioMc) {
        //save default
        openAudioMc.saveDefaultConfig();
        openAudioMc.saveResource("data.yml", false);

        dataConfig = YamlConfiguration.loadConfiguration(new File("plugins/OpenAudioMc/data.yml"));

        mainConfig = openAudioMc.getConfig();

        System.out.println(OpenAudioMc.getLOG_PREFIX() + "Starting configurateion module");
    }

    /**
     * Reload the config file
     */
    public void reloadConfig() {
        OpenAudioMc.getInstance().reloadConfig();
        mainConfig = OpenAudioMc.getInstance().getConfig();
    }

    /**
     * saves the data to the file, like new regions and speakers.
     */
    public void saveAll() {
        try {
            dataConfig.save("plugins/OpenAudioMc/data.yml");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

}
