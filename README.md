# MIPM
MavenIzed Plugin Manager

## Installation
MIPM requires a [Bukkit](https://hub.spigotmc.org/stash/projects/SPIGOT/repos/bukkit/browse) Version 1.10.2-R0.1-SNAPSHOT implementation to work. It is tested on [Paper](https://paper.readthedocs.io/en/paper-1.10/).

1. [Download](https://github.com/EpicPlanet/MIPM/releases) and put `MIPM-${version}.jar` into the server's `plugins` directory.
2. Restart the server.

## Usage
Default `config.yml`:

    plugins: []
    repositories:
    - http://central.maven.org/maven2/
    - http://peulia.iptime.org:8081/repository/maven-public/
    - https://jitpack.io

Edit the `config.yml` to add plugins or repositories. e.g.

    plugins:
    - net.epicpla:epic-item-api:1.0.0
    repositories:
    - http://central.maven.org/maven2/
    - http://peulia.iptime.org:8081/repository/maven-public/
    - https://jitpack.io

The `plugin` should match the format `groupId:artifactId:version`.
The `version` can be `LATEST`. MIPM will find the latest version of the plugin.

When MIPM is loaded, it will try to load the plugins listed in the `config.yml`.

## Credits
MiPpeum - Project Name Motivator
