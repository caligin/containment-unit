package org.anima.ptsd;

import com.spotify.docker.client.DockerClient;
import com.spotify.docker.client.DockerException;
import com.spotify.docker.client.messages.ContainerConfig;
import com.spotify.docker.client.messages.ContainerCreation;
import com.spotify.docker.client.messages.HostConfig;
import com.spotify.docker.client.messages.PortBinding;
import java.io.Closeable;
import java.net.Inet4Address;
import java.net.UnknownHostException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class PostgresContainer implements Closeable {

    public final String id;
    public final String host;
    public final int port;
    private final DockerClient client;

    public PostgresContainer(String id, String host, int port, DockerClient client) {
        this.id = id;
        this.host = host;
        this.port = port;
        this.client = client;
    }

    @Override
    public void close() {
        try {
            client.stopContainer(id, 5);
            client.removeContainer(id);
        } catch (DockerException | InterruptedException ex) {
            throw new RuntimeException(ex);
        }
    }

    public static PostgresContainer fromImage(DockerClient docker, DockerImage image, String bindAddress, int bindPort) {
        //this assumes the image derives from the original postgres one or opens 5432 anyway
        try {
            final ContainerConfig config = ContainerConfig.builder().image(image.tag).env("POSTGRES_PASSWORD=").portSpecs(String.format("%s:%s:5432", bindAddress, bindPort)).build();
            final ContainerCreation creation = docker.createContainer(config);
            final String id = creation.id();
            //TODO can split creation and start
            final Map<String, List<PortBinding>> portBindings = new HashMap<>();
            portBindings.put("5432/tcp", Arrays.asList(PortBinding.of(bindAddress, bindPort)));
            final HostConfig hostConfig = HostConfig.builder().portBindings(portBindings).build();
            docker.startContainer(id, hostConfig);
            Connections.awaitAvailable(Inet4Address.getByName(bindAddress), bindPort, 10, 1000);
            return new PostgresContainer(id, bindAddress, bindPort, docker);
        } catch (UnknownHostException | DockerException | InterruptedException ex) {
            throw new RuntimeException(ex);
        }
    }

}
