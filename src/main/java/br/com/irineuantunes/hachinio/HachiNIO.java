package br.com.irineuantunes.hachinio;

import java.io.IOException;

public interface HachiNIO {

    boolean isActive();

    void close() throws IOException;
}
