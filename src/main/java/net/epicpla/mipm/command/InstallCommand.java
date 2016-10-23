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

package net.epicpla.mipm.command;

import net.epicpla.mipm.MIPM;
import net.epicpla.mipm.data.MIPluginInfo;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.eclipse.aether.resolution.ArtifactResolutionException;
import org.eclipse.aether.resolution.VersionRangeResolutionException;

public class InstallCommand extends AbstractCommand {

    public static final InstallCommand CMD = new InstallCommand();

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (args.length != 4) {
            return false;
        }

        MIPluginInfo info = new MIPluginInfo(args[1], args[2], args[3]);
        try {
            info.getArtifact();
        } catch (VersionRangeResolutionException e) {
            e.printStackTrace();
            sender.sendMessage("Fail. VersionRangeResolutionException.");
            return true;
        } catch (ArtifactResolutionException e) {
            e.printStackTrace();
            sender.sendMessage("Fail. ArtifactResolutionException.");
            return true;
        }
        MIPM.getInstance().pluginsToBeLoadedOnStartup.put(info.getSimpleInfo(), info.getVersion());

        return true;
    }

}
