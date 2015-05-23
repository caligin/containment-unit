package org.anima.ptsd;

import java.io.IOException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.SocketAddress;
import net.emaze.dysfunctional.contracts.dbc;

public class Connections {

    public static boolean awaitAvailable(InetAddress address, int port, int maxRetries, long retryIntervalMillis) {
        dbc.precondition(address != null, "address must be not null");
        final SocketAddress a = new InetSocketAddress(address, port);
        int i = 0;
        for (; !canConnect(a) && i != maxRetries; i++) {
            softenedSleep(retryIntervalMillis);
        }
        return i < maxRetries;
    }

    private static void softenedSleep(long timeoutMillis) {
        try {
            Thread.sleep(timeoutMillis);
        } catch (InterruptedException ex) {
            throw new RuntimeException(ex);
        }
    }

    private static boolean canConnect(SocketAddress address) {
        try {
            final Socket socket = new Socket();
            socket.connect(address);
            Softening.close(socket);
            return true;
        } catch (IOException ex) {
            return false;
        }
    }
}
