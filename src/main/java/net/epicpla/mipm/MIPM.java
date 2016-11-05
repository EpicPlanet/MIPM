/*
 * This file is part of MIPM, licensed under the MIT License (MIT).
 *
 * Copyright (c) Epic Planet Minecraft Server <https://epicpla.net>
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */

package net.epicpla.mipm;

import net.epicpla.mipm.data.MIPluginInfo;
import net.epicpla.mipm.data.MISimplePluginInfo;
import net.epicpla.mipm.maven.MavenUtil;
import org.bukkit.plugin.*;
import org.bukkit.plugin.java.JavaPlugin;
import org.eclipse.aether.repository.RemoteRepository;
import org.eclipse.aether.resolution.ArtifactResolutionException;
import org.eclipse.aether.resolution.VersionRangeResolutionException;

import java.io.File;
import java.lang.reflect.Field;
import java.util.*;
import java.util.logging.Level;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

public class MIPM extends JavaPlugin {

    private static MIPM instance = null;

    public Map<MISimplePluginInfo, String> pluginsToBeLoadedOnStartup;

    static {
        System.out.println("static block");
    }

    @Override
    public void onLoad() {
        System.out.println("onLoad block");
        instance = this;

        MavenUtil.getInstance().repositories.addAll(getConfig().getStringList("repositories").stream().map(repo -> new RemoteRepository.Builder(null, null, repo).build()).collect(Collectors.toList()));
        pluginsToBeLoadedOnStartup = getConfig().getStringList("plugins").stream().map(MIPluginInfo::new).collect(Collectors.toMap(MIPluginInfo::getSimpleInfo, MIPluginInfo::getVersion));
        loadPlugins();
    }

    @Override
    public void onEnable() {
        System.out.println("onEnable block");
        getCommand("mipm").setExecutor(new MIPMCommandHandler());
    }

    @Override
    public void onDisable() {
        List<String> pluginsConfig = pluginsToBeLoadedOnStartup.entrySet().stream().map(e -> e.getKey().toString(e.getValue())).collect(Collectors.toList());
        getConfig().set("plugins", pluginsConfig);
        saveConfig();
    }

    private void loadPlugins() {
        getLogger().info("Starting to load plugins...");
        Plugin[] plugins = loadPlugins(pluginsToBeLoadedOnStartup);
        if (plugins == null) {
            return;
        }

        for (Plugin plugin : plugins) {
            loadPlugin(plugin);
        }
        getLogger().info("Loaded plugins.");
    }

    private Plugin[] loadPlugins(Map<MISimplePluginInfo, String> pluginsToBeLoadedOnStartup) {
        SimplePluginManager manager = (SimplePluginManager) getServer().getPluginManager();
        Field f;

        try {
            f = SimplePluginManager.class.getDeclaredField("fileAssociations");
        } catch (NoSuchFieldException e) {
            getLogger().severe("Field 'fileAssociations' doesn't exist in the PluginManager. MIPM could not load plugins.");
            return null;
        }
        f.setAccessible(true);
        Map<Pattern, PluginLoader> fileAssociations;
        try {
            fileAssociations = (Map<Pattern, PluginLoader>) f.get(manager);
        } catch (IllegalAccessException e) {
            getLogger().severe("Unable to access field 'fileAssociations' in the PluginManager. MIPM could not load plugins.");
            return null;
        }

        try {
            f = SimplePluginManager.class.getDeclaredField("lookupNames");
        } catch (NoSuchFieldException e) {
            getLogger().severe("Field 'lookupNames' doesn't exist in the PluginManager. MIPM could not load plugins.");
            return null;
        }
        f.setAccessible(true);
        Map<String, Plugin> lookupNames;
        try {
            lookupNames = (Map<String, Plugin>) f.get(manager);
        } catch (IllegalAccessException e) {
            getLogger().severe("Unable to access field 'lookupNames' in the PluginManager. MIPM could not load plugins.");
            return null;
        }

        List<Plugin> result = new ArrayList<>();
        Set<Pattern> filters = fileAssociations.keySet();

        Map<String, File> plugins = new HashMap<>();
        Set<String> loadedPlugins = new HashSet<>(lookupNames.keySet());
        Map<String, Collection<String>> dependencies = new HashMap<>();
        Map<String, Collection<String>> softDependencies = new HashMap<>();

        for (Map.Entry<MISimplePluginInfo, String> entry : pluginsToBeLoadedOnStartup.entrySet()) {
            MIPluginInfo info = new MIPluginInfo(entry.getKey(), entry.getValue());
            File file;
            try {
                file = info.getFile();
            } catch (VersionRangeResolutionException e) {
                e.printStackTrace();
                continue;
            } catch (ArtifactResolutionException e) {
                e.printStackTrace();
                continue;
            }

            PluginLoader loader = null;
            for (Pattern filter : filters) {
                Matcher match = filter.matcher(file.getName());
                if (match.find()) {
                    loader = fileAssociations.get(filter);
                }
            }

            if (loader == null) continue;

            PluginDescriptionFile description;
            try {
                description = loader.getPluginDescription(file);
                String name = description.getName();
                if (name.equalsIgnoreCase("bukkit") || name.equalsIgnoreCase("minecraft") || name.equalsIgnoreCase("mojang")) {
                    getLogger().log(Level.SEVERE,"Could not load '" + file.getPath() + "' in " + getClass().getSimpleName() + ": Restricted Name");
                    continue;
                } else if (description.getRawName().indexOf(' ') != -1) {
                    getLogger().warning(String.format(
                            "Plugin `%s' uses the space-character (0x20) in its name `%s' - this is discouraged",
                            description.getFullName(),
                            description.getRawName()
                    ));
                }
            } catch (InvalidDescriptionException ex) {
                getLogger().log(Level.SEVERE, "Could not load '" + file.getPath() + "' in " + getClass().getSimpleName(), ex);
                continue;
            }

            File replacedFile = plugins.put(description.getName(), file);
            if (replacedFile != null) {
                getLogger().severe(String.format(
                        "Ambiguous plugin name `%s' for files `%s' and `%s' in `%s'",
                        description.getName(),
                        file.getPath(),
                        replacedFile.getPath(),
                        getClass().getSimpleName()
                ));
            }

            Collection<String> softDependencySet = description.getSoftDepend();
            if (softDependencySet != null && !softDependencySet.isEmpty()) {
                if (softDependencies.containsKey(description.getName())) {
                    softDependencies.get(description.getName()).addAll(softDependencySet);
                } else {
                    softDependencies.put(description.getName(), new LinkedList<>(softDependencySet));
                }
            }

            Collection<String> dependencySet = description.getDepend();
            if (dependencySet != null && !dependencySet.isEmpty()) {
                dependencies.put(description.getName(), new LinkedList<>(dependencySet));
            }

            Collection<String> loadBeforeSet = description.getLoadBefore();
            if (loadBeforeSet != null && !loadBeforeSet.isEmpty()) {
                for (String loadBeforeTarget : loadBeforeSet) {
                    if (softDependencies.containsKey(loadBeforeTarget)) {
                        softDependencies.get(loadBeforeTarget).add(description.getName());
                    } else {
                        Collection<String> shortSoftDependency = new LinkedList<>();
                        shortSoftDependency.add(description.getName());
                        softDependencies.put(loadBeforeTarget, shortSoftDependency);
                    }
                }
            }
        }

        while (!plugins.isEmpty()) {
            boolean missingDependency = true;
            Iterator<String> pluginIterator = plugins.keySet().iterator();

            while (pluginIterator.hasNext()) {
                String plugin = pluginIterator.next();

                if (dependencies.containsKey(plugin)) {
                    Iterator<String> dependencyIterator = dependencies.get(plugin).iterator();

                    while (dependencyIterator.hasNext()) {
                        String dependency = dependencyIterator.next();

                        if (loadedPlugins.contains(dependency)) {
                            dependencyIterator.remove();

                        } else if (!plugins.containsKey(dependency)) {
                            missingDependency = false;
                            File file = plugins.get(plugin);
                            pluginIterator.remove();
                            softDependencies.remove(plugin);
                            dependencies.remove(plugin);

                            getLogger().log(
                                    Level.SEVERE,
                                    "Could not load '" + file.getPath() + "' in " + getClass().getSimpleName(),
                                    new UnknownDependencyException(dependency));
                            break;
                        }
                    }

                    if (dependencies.containsKey(plugin) && dependencies.get(plugin).isEmpty()) {
                        dependencies.remove(plugin);
                    }
                }
                if (softDependencies.containsKey(plugin)) {
                    Iterator<String> softDependencyIterator = softDependencies.get(plugin).iterator();

                    while (softDependencyIterator.hasNext()) {
                        String softDependency = softDependencyIterator.next();

                        if (!plugins.containsKey(softDependency)) {
                            softDependencyIterator.remove();
                        }
                    }

                    if (softDependencies.get(plugin).isEmpty()) {
                        softDependencies.remove(plugin);
                    }
                }
                if (!(dependencies.containsKey(plugin) || softDependencies.containsKey(plugin)) && plugins.containsKey(plugin)) {
                    File file = plugins.get(plugin);
                    pluginIterator.remove();
                    missingDependency = false;

                    try {
                        result.add(manager.loadPlugin(file));
                        loadedPlugins.add(plugin);
                    } catch (InvalidPluginException ex) {
                        getLogger().log(Level.SEVERE, "Could not load '" + file.getPath() + "' in " + getClass().getSimpleName(), ex);
                    }
                }
            }

            if (missingDependency) {
                pluginIterator = plugins.keySet().iterator();

                while (pluginIterator.hasNext()) {
                    String plugin = pluginIterator.next();

                    if (!dependencies.containsKey(plugin)) {
                        softDependencies.remove(plugin);
                        missingDependency = false;
                        File file = plugins.get(plugin);
                        pluginIterator.remove();

                        try {
                            result.add(manager.loadPlugin(file));
                            loadedPlugins.add(plugin);
                            break;
                        } catch (InvalidPluginException ex) {
                            getLogger().log(Level.SEVERE, "Could not load '" + file.getPath() + "' in " + getClass().getSimpleName(), ex);
                        }
                    }
                }
                if (missingDependency) {
                    softDependencies.clear();
                    dependencies.clear();
                    Iterator<File> failedPluginIterator = plugins.values().iterator();

                    while (failedPluginIterator.hasNext()) {
                        File file = failedPluginIterator.next();
                        failedPluginIterator.remove();
                        getLogger().log(Level.SEVERE, "Could not load '" + file.getPath() + "' in " + getClass().getSimpleName() + ": circular dependency detected");
                    }
                }
            }
        }

        return result.toArray(new Plugin[result.size()]);
    }

    private void loadPlugin(Plugin plugin) {
        try {
            String message = String.format("Loading %s", plugin.getDescription().getFullName());
            plugin.getLogger().info(message);
            plugin.onLoad();
        } catch (Throwable ex) {
            getLogger().log(Level.SEVERE, ex.getMessage() + " initializing " + plugin.getDescription().getFullName() + " (Is it up to date?)", ex);
        }
    }

    public static MIPM getInstance() {
        return instance;
    }

}
