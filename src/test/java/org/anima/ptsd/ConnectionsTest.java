package org.anima.ptsd;

import java.net.Inet4Address;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import org.anima.ptsd.Softening.ThrowingConsumer;
import org.junit.Assert;
import org.junit.Test;

public class ConnectionsTest {

    @Test
    public void blocksUntilAvailable() throws Exception {
        final InetAddress localhost = Inet4Address.getLocalHost();
        final int port = availablePort(localhost);
        new Thread(() -> {
            Softening.wrap((ThrowingConsumer<Long>) Thread::sleep).accept(1000l);
            ServerSocket serverSocket = null;
            Socket connection = null;
            try {
                serverSocket = new ServerSocket(port, 1, localhost);
                connection = serverSocket.accept();
            } catch (Exception e) {
                throw new RuntimeException(e);
            } finally {
                Softening.close(connection);
                Softening.close(serverSocket);
            }
        }).start();
        final boolean available = Connections.awaitAvailable(localhost, port, 3, 500);
        Assert.assertTrue(available);
    }

    @Test
    public void throwsIfNotConnectedAfterMaxRetries() throws Exception {
        InetAddress localhost = Inet4Address.getLocalHost();
        final int port = availablePort(localhost);
        final boolean available = Connections.awaitAvailable(localhost, port, 1, 500);
        Assert.assertFalse(available);
    }

    public static int availablePort(InetAddress onInterface) throws Exception {
        try (ServerSocket serverSocket = new ServerSocket(0, 1, onInterface)) {
            return serverSocket.getLocalPort();
        }
    }

}
