package org.anima.ptsd;

import com.spotify.docker.client.DefaultDockerClient;
import com.spotify.docker.client.DockerCertificateException;
import com.spotify.docker.client.DockerClient;
import com.spotify.docker.client.DockerException;
import com.spotify.docker.client.messages.ContainerConfig;
import com.spotify.docker.client.messages.ContainerCreation;
import com.spotify.docker.client.messages.ContainerInfo;
import com.spotify.docker.client.messages.HostConfig;
import com.spotify.docker.client.messages.Image;
import com.spotify.docker.client.messages.PortBinding;
import java.io.Closeable;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.logging.Logger;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.sql.DataSource;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.postgresql.Driver;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.jdbc.datasource.SimpleDriverDataSource;
import org.springframework.orm.hibernate4.HibernateTransactionManager;
import org.springframework.orm.hibernate4.LocalSessionFactoryBean;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.transaction.support.TransactionOperations;
import org.springframework.transaction.support.TransactionTemplate;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration
public class CanDoSpringTestsWithSingleContainer {

    @Autowired
    private SessionFactory hibernate;
    @Autowired
    private TransactionOperations txOps;

    @Test
    public void canStore() {
        txOps.execute(state -> {
            final Session session = hibernate.getCurrentSession();
            final AnEntity e = new AnEntity();
            e.setId(0);
            e.setValue("asd");
            session.save(e);
            return null;
        });
        final AnEntity got = txOps.execute(state -> {
            final Session session = hibernate.getCurrentSession();
            return (AnEntity) session.get(AnEntity.class, 0);
        });
        Assert.assertEquals("asd", got.getValue());
    }

    public static class PostgresContainer implements Closeable {

        public final String id;
        public final String host;
        public final String port;
        private final DockerClient client;

        public PostgresContainer(String id, String host, String port, DockerClient client) {
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
    }

    @Configuration
    public static class Config {

        @Bean
        public PostgresContainer postgresContainer() throws DockerCertificateException, DockerException, InterruptedException {
            final String host = "127.0.0.1";
            final String port = "15432";
            final DockerClient docker = DefaultDockerClient.fromEnv().build();
            final List<Image> images = docker.listImages();
            boolean alreadyPulled = images.stream().anyMatch(image -> image.repoTags().contains("postgres:9.4"));
            if (!alreadyPulled) {
                Logger.getAnonymousLogger().info("pulling");
                docker.pull("postgres:9.4");
                Logger.getAnonymousLogger().info("done pulling");
            }
            final ContainerConfig config = ContainerConfig.builder()
                    .image("postgres:9.4")
                    .env("POSTGRES_PASSWORD=")
                    .portSpecs(String.format("%s:%s:5432", host, port))
                    .build();
            final ContainerCreation creation = docker.createContainer(config);
            final String id = creation.id();

            final Map<String, List<PortBinding>> portBindings = new HashMap<>();
            portBindings.put("5432/tcp", Arrays.asList(PortBinding.of(host, port)));
            final HostConfig hostConfig = HostConfig.builder()
                    .portBindings(portBindings)
                    .build();
            docker.startContainer(id, hostConfig);
            Thread.sleep(15000); // temporary hack. container initializes postgres before accepting connections on pgport, must wait until ready or fail because can't connect
            return new PostgresContainer(id, host, port, docker);
        }

        @Bean
        public DataSource dataSource(PostgresContainer container) {
            return new SimpleDriverDataSource(new Driver(), String.format("jdbc:postgresql://%s:%s/", container.host, container.port), "postgres", ""); // TODO: use a test db, not the default one and ensure that it is created
        }

        @Bean
        public LocalSessionFactoryBean sessionFactory(DataSource dataSource) {
            final Properties hibernateProperties = new Properties();
            hibernateProperties.setProperty("hibernate.dialect", "org.hibernate.dialect.PostgreSQLDialect");
            hibernateProperties.setProperty("hibernate.hbm2ddl.auto", "create");
            final LocalSessionFactoryBean fb = new LocalSessionFactoryBean();
            fb.setDataSource(dataSource);
            fb.setPackagesToScan("org.anima.ptsd");
            fb.setHibernateProperties(hibernateProperties);
            return fb;
        }

        @Bean
        public TransactionTemplate transactionTemplate(SessionFactory sessionFactory) {
            return new TransactionTemplate(new HibernateTransactionManager(sessionFactory));
        }

    }

    @Entity
    public static class AnEntity {

        @Id
        private int id;
        private String value;

        public int getId() {
            return id;
        }

        public void setId(int id) {
            this.id = id;
        }

        public String getValue() {
            return value;
        }

        public void setValue(String value) {
            this.value = value;
        }

    }

}
