package org.anima.ptsd;

import com.spotify.docker.client.DefaultDockerClient;
import com.spotify.docker.client.DockerCertificateException;
import com.spotify.docker.client.DockerClient;
import com.spotify.docker.client.DockerException;
import com.spotify.docker.client.messages.ContainerConfig;
import com.spotify.docker.client.messages.ContainerCreation;
import com.spotify.docker.client.messages.HostConfig;
import org.junit.rules.ExternalResource;

public class ContainerRule extends ExternalResource {

    //TODO: full-delegating proxy + safe/simpler opts. e.g. ports instead of portspecs and only specifies port, iface will be localhost always
    private final ContainerConfig.Builder configBuilder;
    private final DockerClient docker;
    private final Fulfillable<String> containerId;

    public ContainerRule(String imageName) {
        try {
            this.configBuilder = ContainerConfig.builder().image(imageName);
            this.docker = DefaultDockerClient.fromEnv().build();
            this.containerId = new Fulfillable<>();
        } catch (DockerCertificateException ex) {
            throw new RuntimeException(ex);
        }
    }

    @Override
    protected void before() throws Throwable {
        try {
            final ContainerCreation creation = docker.createContainer(configBuilder.build());
            containerId.fulfill(creation.id());
            docker.startContainer(containerId.value(), HostConfig.builder().build());
        } catch (DockerException | InterruptedException ex) {
            throw new RuntimeException(ex);
        }
    }

    @Override
    protected void after() {
        try {
            docker.stopContainer(containerId.value(), 3);
        } catch (DockerException | InterruptedException ex) {
            throw new RuntimeException(ex);
        }
    }

    public String id() {
        return containerId.value();
    }

}
