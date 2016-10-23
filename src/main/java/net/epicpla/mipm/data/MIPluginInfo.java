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

package net.epicpla.mipm.data;

import org.bukkit.plugin.InvalidDescriptionException;
import org.bukkit.plugin.InvalidPluginException;
import org.bukkit.plugin.Plugin;
import org.eclipse.aether.artifact.Artifact;
import org.eclipse.aether.resolution.ArtifactResolutionException;
import org.eclipse.aether.resolution.VersionRangeResolutionException;

import java.io.File;

public class MIPluginInfo {

    private MISimplePluginInfo simpleInfo;
    private String version;

    public MIPluginInfo(MISimplePluginInfo simpleInfo, String version) {
        this.simpleInfo = simpleInfo;
        this.version = version;
    }

    public MIPluginInfo(String groupId, String artifactId, String version) {
        simpleInfo = new MISimplePluginInfo(groupId, artifactId);
        this.version = version;
    }

    public MIPluginInfo(String s) {
        String[] ss = s.split(":");
        simpleInfo = new MISimplePluginInfo(ss[0], ss[1]);
        version = ss[2];
    }

    public File getFile() throws VersionRangeResolutionException, ArtifactResolutionException {
        return simpleInfo.getFile(version);
    }

    public Artifact getArtifact() throws VersionRangeResolutionException, ArtifactResolutionException {
        return simpleInfo.getArtifact(version);
    }

    public Plugin load() throws InvalidDescriptionException, InvalidPluginException, ArtifactResolutionException, VersionRangeResolutionException {
        return simpleInfo.load(version);
    }

    @Override
    public String toString() {
        return simpleInfo.toString(version);
    }

    public MISimplePluginInfo getSimpleInfo() {
        return simpleInfo;
    }

    public String getVersion() {
        return version;
    }

}
