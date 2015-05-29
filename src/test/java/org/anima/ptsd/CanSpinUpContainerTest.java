package org.anima.ptsd;

import com.spotify.docker.client.DefaultDockerClient;
import com.spotify.docker.client.DockerCertificateException;
import com.spotify.docker.client.DockerClient;
import com.spotify.docker.client.DockerException;
import com.spotify.docker.client.messages.Container;
import java.sql.SQLException;
import java.util.List;
import net.emaze.dysfunctional.Consumers;
import net.emaze.dysfunctional.Filtering;
import net.emaze.dysfunctional.dispatching.logic.Predicate;
import org.junit.Assert;
import org.junit.Rule;
import org.junit.Test;

public class CanSpinUpContainerTest {

    @Rule
    public ContainerRule container = new ContainerRule("centos");

    @Test
    public void isRunning() throws SQLException, DockerException, InterruptedException, DockerCertificateException {
        final List<Container> runningContainers = DefaultDockerClient.fromEnv().build().listContainers(DockerClient.ListContainersParam.allContainers());
        final List<Container> withExpectedId = Consumers.all(Filtering.filter(runningContainers, new Predicate<Container>() {

            @Override
            public boolean accept(Container element) {
                return element.id().equals(container.id());
            }
        }));
        Assert.assertEquals(1, withExpectedId.size());
    }

}
