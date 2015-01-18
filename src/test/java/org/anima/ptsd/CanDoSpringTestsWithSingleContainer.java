package org.anima.ptsd;

import com.spotify.docker.client.DefaultDockerClient;
import com.spotify.docker.client.DockerCertificateException;
import com.spotify.docker.client.DockerClient;
import com.spotify.docker.client.DockerException;
import java.net.UnknownHostException;
import java.util.Properties;
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
            final AnEntity e = AnEntity.of(0, "asd");
            session.save(e);
            return null;
        });
        final AnEntity got = txOps.execute(state -> {
            final Session session = hibernate.getCurrentSession();
            return (AnEntity) session.get(AnEntity.class, 0);
        });
        Assert.assertEquals("asd", got.getValue());
    }

    @Configuration
    public static class Config {

        @Bean
        public PostgresContainer postgresContainer() throws DockerCertificateException, DockerException, InterruptedException, UnknownHostException {
            final String host = "127.0.0.1";
            final int port = 15432;
            final DockerClient docker = DefaultDockerClient.fromEnv().build();
            final DockerImage postgresImage = DockerImage.fromIndex(docker, "postgres:9.4");
            return PostgresContainer.fromImage(docker, postgresImage, host, port);
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

        public static AnEntity of(int id, String value) {
            final AnEntity ae = new AnEntity();
            ae.id = id;
            ae.value = value;
            return ae;
        }

    }

}
