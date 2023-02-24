package pers.laineyc.blackbox.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.TaskScheduler;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.config.CronTask;
import org.springframework.stereotype.Component;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ScheduledFuture;

@Slf4j
@Component
@EnableScheduling
public class ScheduleService {

    @Autowired
    private TaskScheduler taskScheduler;

    private final Map<Runnable, ScheduledFuture<?>> scheduledTasks = new ConcurrentHashMap<>(16);

    public void addCronTask(CronTask cronTask) {
        if (cronTask != null) {
            Runnable task = cronTask.getRunnable();
            if (scheduledTasks.containsKey(task)) {
                removeCronTask(task);
            }
            scheduledTasks.put(task, scheduleCronTask(cronTask));
        }
    }

    public void removeCronTask(Runnable task) {
        ScheduledFuture<?> scheduledTask = scheduledTasks.remove(task);
        if (scheduledTask != null) {
            scheduledTask.cancel(true);
        }
    }

    public ScheduledFuture<?> scheduleCronTask(CronTask cronTask) {
        return taskScheduler.schedule(cronTask.getRunnable(), cronTask.getTrigger());
    }

    public void clear() {
        for (ScheduledFuture<?> scheduledFuture : scheduledTasks.values()) {
            scheduledFuture.cancel(true);
        }
        scheduledTasks.clear();
    }

}
