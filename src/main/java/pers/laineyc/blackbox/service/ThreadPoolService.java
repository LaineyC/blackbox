package pers.laineyc.blackbox.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.concurrent.CustomizableThreadFactory;
import org.springframework.stereotype.Component;
import java.util.concurrent.*;

@Slf4j
@Component
public class ThreadPoolService {

    private final ExecutorService collectorExecutor = new ThreadPoolExecutor(8, 64, 120, TimeUnit.SECONDS, new LinkedBlockingQueue<>(64), new CustomizableThreadFactory("collector-pool-thread-"), new ThreadPoolExecutor.AbortPolicy());

    private final ExecutorService pipelineExecutor = new ThreadPoolExecutor(16, 128, 120, TimeUnit.SECONDS, new LinkedBlockingQueue<>(128), new CustomizableThreadFactory("pipeline-pool-thread-"), new ThreadPoolExecutor.AbortPolicy());

//    private final ExecutorService scheduledTaskExecutor = Executors.newScheduledThreadPool(8, new CustomizableThreadFactory("scheduled-pool-thread-"));

    public ExecutorService getCollectorExecutor() {
        return collectorExecutor;
    }

    public ExecutorService getPipelineExecutor() {
        return pipelineExecutor;
    }

/*
    public ExecutorService getScheduledTaskExecutor() {
        return scheduledTaskExecutor;
    }
*/

}
