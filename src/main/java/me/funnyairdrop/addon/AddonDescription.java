package me.funnyairdrop.addon;

import org.bukkit.configuration.file.YamlConfiguration;

import java.util.*;

public class AddonDescription {

    private final String name;
    private final String mainClass;
    private final String version;
    public final String description;
    public final String apiVersion;
    private final Set<String> authors;
    private final List<String> depend;
    public final List<String> softDepend;

    public AddonDescription(YamlConfiguration yml) {
        name = Objects.requireNonNull(yml.getString("name"), "addon.yml missing 'name'!");
        mainClass = Objects.requireNonNull(yml.getString("main"), "addon.yml missing 'main'!");
        version = yml.getString("version", "1.0");
        description = yml.getString("description", "");
        apiVersion = yml.getString("api-version", "1.0");
        depend = yml.getStringList("depend");
        softDepend = yml.getStringList("soft-depend");

        authors = new HashSet<>();
        authors.addAll(yml.getStringList("authors"));
        String single = yml.getString("author");
        if (single != null) authors.add(single);
    }

    public String name() { return name; }
    public String mainClass() { return mainClass; }
    public String version() { return version; }
    public Set<String> authors() { return authors; }
    public List<String> depend() { return depend; }
}