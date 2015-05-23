package org.anima.ptsd;

import com.spotify.docker.client.DefaultDockerClient;
import com.spotify.docker.client.DockerCertificateException;
import com.spotify.docker.client.DockerClient;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.rules.ExternalResource;

public class CanDoSpringTestsWithSingleContainer {

    private Connection connection;

    @Before
    public void setUp() throws DockerCertificateException, SQLException {
        final String host = "127.0.0.1";
        final int port = 15432;
        final DockerClient docker = DefaultDockerClient.fromEnv().build();
        final DockerImage postgresImage = DockerImage.fromIndex(docker, "postgres:9.4");
        final PostgresContainer container = PostgresContainer.fromImage(docker, postgresImage, host, port);
        connection = DriverManager.getConnection(String.format("jdbc:postgresql://%s:%s/", container.host, container.port), "postgres", "");
    }

    @After
    public void tearDown() throws SQLException {
        connection.close();
    }

    @Test
    public void canStore() throws SQLException {
        connection.prepareStatement("create table an_entity ( id int, t text)").execute();
        connection.prepareStatement("insert into an_entity values (0, 'asd')").executeUpdate();
        final ResultSet rs = connection.prepareStatement("select * from an_entity limit 0 1").executeQuery();
        rs.next();
        final String got = rs.getString(2);
        Assert.assertEquals("asd", got);
    }

    public static class NewContainerAndConnectionRule extends ExternalResource {

        @Override
        protected void after() {

        }

        @Override
        protected void before() throws Throwable {

        }

    }

}
