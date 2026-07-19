package me.funnyairdrop.addon;

import me.funnyairdrop.FunnyAirdrop;
import me.funnyairdrop.api.FunnyAirdropAPI;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.*;
import java.net.URL;
import java.net.URLConnection;
import java.util.logging.Logger;

@SuppressWarnings("unused")
public abstract class AbstractAddon {

    private Logger logger;
    private AddonDescription description;
    private AddonClassLoader classLoader;
    private File dataFolder;
    private boolean enabled = false;
    private FileConfiguration config = null;
    private File configFile = null;

    public AbstractAddon() {
        ClassLoader cl = this.getClass().getClassLoader();
        if (cl instanceof AddonClassLoader loader) {
            loader.initialize(this);
        } else {
            throw new IllegalStateException("AbstractAddon требует AddonClassLoader");
        }
    }

    final void init(AddonDescription description, AddonClassLoader classLoader, AddonLoader addonLoader) {
        this.description = description;
        this.classLoader = classLoader;
        this.logger = Logger.getLogger("FunnyAirdrop#" + description.name());
        this.dataFolder = new File(new File(FunnyAirdrop.getInstance().getDataFolder(), "addons"), description.name());
        if (!dataFolder.exists()) {
            dataFolder.mkdirs();
        }
        this.configFile = new File(dataFolder, "config.yml");
    }

    protected abstract void onEnable();

    protected abstract void onDisable();

    public final void setEnabled(boolean enabled) {
        if (this.enabled == enabled) return;
        this.enabled = enabled;
        if (enabled) {
            logger.info("Включение " + description.name() + "...");
            onEnable();
        } else {
            logger.info("Выключение " + description.name() + "...");
            onDisable();
        }
    }

    public FileConfiguration getConfig() {
        if (config == null) {
            reloadConfig();
        }
        return config;
    }

    public void reloadConfig() {
        if (configFile == null) return;
        config = YamlConfiguration.loadConfiguration(configFile);
        InputStream defaultStream = getResource("config.yml");
        if (defaultStream != null) {
            YamlConfiguration defaultConfig = YamlConfiguration.loadConfiguration(new InputStreamReader(defaultStream));
            config.setDefaults(defaultConfig);
        }
    }

    public void saveConfig() {
        if (config == null || configFile == null) return;
        try {
            getConfig().save(configFile);
        } catch (IOException e) {
            logger.severe("Ошибка сохранения config.yml: " + e.getMessage());
        }
    }

    public void saveDefaultConfig() {
        if (configFile != null && !configFile.exists()) {
            saveResource("config.yml", false);
        }
        reloadConfig();
    }

    public InputStream getResource(String filename) {
        try {
            URL url = classLoader.getResource(filename);
            if (url == null) return null;
            URLConnection conn = url.openConnection();
            conn.setUseCaches(false);
            return conn.getInputStream();
        } catch (IOException e) {
            return null;
        }
    }

    public void saveResource(String resourcePath, boolean replace) {
        File outFile = new File(dataFolder, resourcePath);
        if (outFile.exists() && !replace) return;
        if (!outFile.getParentFile().exists()) {
            outFile.getParentFile().mkdirs();
        }
        try (InputStream in = getResource(resourcePath)) {
            if (in == null) {
                logger.warning("Ресурс не найден: " + resourcePath);
                return;
            }
            try (FileOutputStream out = new FileOutputStream(outFile)) {
                byte[] buf = new byte[1024];
                int len;
                while ((len = in.read(buf)) > 0) out.write(buf, 0, len);
            }
        } catch (IOException e) {
            logger.severe("Ошибка сохранения ресурса " + resourcePath + ": " + e.getMessage());
        }
    }

    public FunnyAirdropAPI getAirdropAPI() {
        return FunnyAirdrop.getInstance().getApi();
    }

    public Logger getLogger() {
        return logger;
    }

    public AddonDescription getDescription() {
        return description;
    }

    public File getDataFolder() {
        return dataFolder;
    }

    public boolean isEnabled() {
        return enabled;
    }
}