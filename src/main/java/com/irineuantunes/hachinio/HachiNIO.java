package com.irineuantunes.hachinio;

import java.io.IOException;

public interface HachiNIO {

    boolean isActive();

    void stop() throws IOException;
}
