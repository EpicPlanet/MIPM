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

package net.epicpla.mipm.maven;

import org.apache.maven.repository.internal.MavenRepositorySystemUtils;
import org.eclipse.aether.DefaultRepositorySystemSession;
import org.eclipse.aether.RepositorySystem;
import org.eclipse.aether.RepositorySystemSession;
import org.eclipse.aether.artifact.Artifact;
import org.eclipse.aether.artifact.DefaultArtifact;
import org.eclipse.aether.connector.basic.BasicRepositoryConnectorFactory;
import org.eclipse.aether.impl.DefaultServiceLocator;
import org.eclipse.aether.repository.LocalRepository;
import org.eclipse.aether.repository.RemoteRepository;
import org.eclipse.aether.resolution.*;
import org.eclipse.aether.spi.connector.RepositoryConnectorFactory;
import org.eclipse.aether.spi.connector.transport.TransporterFactory;
import org.eclipse.aether.transport.file.FileTransporterFactory;
import org.eclipse.aether.transport.http.HttpTransporterFactory;
import org.eclipse.aether.version.Version;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class MavenUtil {

    private static MavenUtil instance;

    private RepositorySystem system;
    private RepositorySystemSession session;
    public List<RemoteRepository> repositories = newRepositories();

    public static MavenUtil getInstance() {
        if (instance == null) {
            instance = new MavenUtil();
        }
        return instance;
    }

    private MavenUtil() {
        system = newRepositorySystem();
        session = newSession(system);
    }

    public Artifact resolveNewestArtifact(String groupId, String artifactId) throws VersionRangeResolutionException, ArtifactResolutionException {
        Artifact artifact = new DefaultArtifact(groupId + ":" + artifactId + ":[0,)");

        VersionRangeRequest rangeRequest = new VersionRangeRequest();
        rangeRequest.setArtifact(artifact);
        rangeRequest.setRepositories(repositories);
        VersionRangeResult rangeResult = system.resolveVersionRange(session, rangeRequest);
        Version newestVersion = rangeResult.getHighestVersion();
        artifact = artifact.setVersion(newestVersion.toString());

        ArtifactRequest artifactRequest = new ArtifactRequest();
        artifactRequest.setArtifact(artifact);
        artifactRequest.setRepositories(repositories);

        ArtifactResult artifactResult = system.resolveArtifact(session, artifactRequest);
        artifact = artifactResult.getArtifact();

        return artifact;
    }

    public Artifact resolveArtifact(String groupId, String artifactId, String version) throws VersionRangeResolutionException, ArtifactResolutionException {
        Artifact artifact = new DefaultArtifact(groupId + ":" + artifactId + ":" + version);

        ArtifactRequest artifactRequest = new ArtifactRequest();
        artifactRequest.setArtifact(artifact);
        artifactRequest.setRepositories(repositories);

        ArtifactResult artifactResult = system.resolveArtifact(session, artifactRequest);
        artifact = artifactResult.getArtifact();

        return artifact;
    }

    private static RepositorySystem newRepositorySystem()
    {
        DefaultServiceLocator locator = MavenRepositorySystemUtils.newServiceLocator();
        locator.addService( RepositoryConnectorFactory.class, BasicRepositoryConnectorFactory.class );
        locator.addService( TransporterFactory.class, FileTransporterFactory.class );
        locator.addService( TransporterFactory.class, HttpTransporterFactory.class );

        return locator.getService( RepositorySystem.class );
    }

    private static RepositorySystemSession newSession(RepositorySystem system)
    {
        DefaultRepositorySystemSession session = MavenRepositorySystemUtils.newSession();

        LocalRepository localRepo = new LocalRepository( System.getProperty("user.home") + File.separator + ".m2" + File.separator + "repository" );
        session.setLocalRepositoryManager( system.newLocalRepositoryManager( session, localRepo ) );

        return session;
    }

    private static List<RemoteRepository> newRepositories()
    {
        return new ArrayList<>(Collections.singletonList(newCentralRepository()));
    }

    private static RemoteRepository newCentralRepository()
    {
        return new RemoteRepository.Builder("central", "default", "http://central.maven.org/maven2/").build();
    }
}
