package chat.server;

import java.io.*;
import java.net.*;

public class PeekedSocket extends Socket {

    private final Socket   delegate;
    private final InputStream wrappedIn;

    public PeekedSocket(Socket delegate, byte peekedByte) throws IOException {
        this.delegate  = delegate;
        InputStream original = delegate.getInputStream();
        this.wrappedIn = new SequenceInputStream(
                new ByteArrayInputStream(new byte[]{peekedByte}),
                original
        );
    }

    @Override public InputStream  getInputStream()  throws IOException { return wrappedIn; }
    @Override public OutputStream getOutputStream() throws IOException { return delegate.getOutputStream(); }
    @Override public void close() throws IOException { delegate.close(); }
    @Override public void setSoTimeout(int timeout) throws SocketException { delegate.setSoTimeout(timeout); }
    @Override public boolean isClosed() { return delegate.isClosed(); }
}
