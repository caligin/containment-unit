package org.anima.ptsd;

import java.io.Closeable;
import java.io.IOException;
import org.junit.Assert;
import org.junit.Test;

public class SofteningTest {

    @Test
    public void closeDoesNotThrowWhenWrappedDoesNotThrow() {
        Softening.close(new NoopClose());
    }

    @Test(expected = RuntimeException.class)
    public void closeAdaptsIOExToRuntimeWhenWrappedThrows() {
        Softening.close(new BombClose());
    }

    @Test
    public void closeThrowsRuntimeThatContainsOriginalException() {
        try {
            Softening.close(new BombClose());
        } catch (RuntimeException ex) {
            Assert.assertEquals(IOException.class, ex.getCause().getClass());
            return;
        }
        Assert.fail("no exception got");
    }

    public static class NoopClose implements Closeable {

        @Override
        public void close() throws IOException {
        }
    }

    public static class BombClose implements Closeable {

        @Override
        public void close() throws IOException {
            throw new IOException();
        }
    }

}
