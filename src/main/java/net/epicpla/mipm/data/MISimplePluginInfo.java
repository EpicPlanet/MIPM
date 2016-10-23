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

import net.epicpla.mipm.MIPM;
import net.epicpla.mipm.maven.MavenUtil;
import org.bukkit.plugin.InvalidDescriptionException;
import org.bukkit.plugin.InvalidPluginException;
import org.bukkit.plugin.Plugin;
import org.eclipse.aether.artifact.Artifact;
import org.eclipse.aether.resolution.ArtifactResolutionException;
import org.eclipse.aether.resolution.VersionRangeResolutionException;

import java.io.File;

public class MISimplePluginInfo {

    private String groupId;
    private String artifactId;

    public MISimplePluginInfo(String groupId, String artifactId) {
        this.groupId = groupId;
        this.artifactId = artifactId;
    }

    public MISimplePluginInfo(String s) {
        String[] ss = s.split(":");
        groupId = ss[0];
        artifactId = ss[1];
    }

    public File getFile(String version) throws VersionRangeResolutionException, ArtifactResolutionException {
        return getArtifact(version).getFile();
    }

    public Artifact getArtifact(String version) throws VersionRangeResolutionException, ArtifactResolutionException {
        if (version.equalsIgnoreCase("LATEST")) {
            return MavenUtil.getInstance().resolveNewestArtifact(groupId, artifactId);
        }
        return MavenUtil.getInstance().resolveArtifact(groupId, artifactId, version);
    }

    public Plugin load(String version) throws VersionRangeResolutionException, ArtifactResolutionException, InvalidDescriptionException, InvalidPluginException {
        return MIPM.getInstance().getServer().getPluginManager().loadPlugin(getFile(version));
    }

    @Override
    public String toString() {
        return groupId + ":" + artifactId;
    }

    public String toString(String version) {
        return groupId + ":" + artifactId + ":" + version;
    }

    @Override
    public int hashCode() {
        return toString().hashCode();
    }

    @Override
    public boolean equals(Object o) {
        if (o.getClass().equals(MISimplePluginInfo.class)) {
            MISimplePluginInfo o1 = (MISimplePluginInfo) o;
            return o1.groupId.equals(groupId) && o1.artifactId.equals(artifactId);
        } else {
            return false;
        }
    }
}
