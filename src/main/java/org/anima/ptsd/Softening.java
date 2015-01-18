package org.anima.ptsd;

import java.io.Closeable;
import java.io.IOException;
import java.util.function.Consumer;

public class Softening {

    public static void close(Closeable c) {
        try {
            c.close();
        } catch (IOException ex) {
            throw new RuntimeException(ex);
        }
    }

    public static <T> Consumer<T> wrap(ThrowingConsumer<T> wrapped) {
        return (arg) -> {
            try {
                wrapped.accept(arg);
            } catch (Exception ex) {
                throw new RuntimeException(ex);
            }
        };
    }

    @FunctionalInterface
    public static interface ThrowingConsumer<T> {

        void accept(T t) throws Exception;
    }

}
