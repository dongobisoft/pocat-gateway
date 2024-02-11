package io.pocat.utils.stage;

import java.util.concurrent.ExecutorService;

public interface Stage extends ExecutorService {
    void start();
    void stop();
}
