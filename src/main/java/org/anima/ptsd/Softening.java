package org.anima.ptsd;

import java.io.Closeable;
import java.io.IOException;

public class Softening {

    public static void close(Closeable c) {
        try {
            c.close();
        } catch (IOException ex) {
            throw new RuntimeException(ex);
        }
    }

}
