package io.pocat.gateway;

import io.pocat.utils.stage.Stage;

import java.util.Map;

public class StageManager {
    private final Map<String, Stage> stages;

    public StageManager(Map<String, Stage> stages) {
        this.stages = stages;
    }

    public Stage getExecutor(String stageName) {
        return stages.get(stageName);
    }

    public void start() {
        for(Stage stage:stages.values()) {
            stage.start();
        }
    }

    public void stop() {
        for(Stage stage:stages.values()) {
            stage.stop();
        }
    }
}
