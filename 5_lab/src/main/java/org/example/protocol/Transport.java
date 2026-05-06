package org.example.protocol;

import java.io.IOException;


public interface Transport {

    void send(Message msg) throws IOException;
    Message receive() throws IOException;

    void close() throws IOException;
}