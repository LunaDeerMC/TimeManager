package cn.lunadeer.mc.timeManager.utils.scheduler;

import io.papermc.paper.threadedregions.scheduler.ScheduledTask;

public class PaperTask implements CancellableTask {
    private final ScheduledTask task;

    public PaperTask(ScheduledTask task) {
        this.task = task;
    }

    @Override
    public void cancel() {
        task.cancel();
    }

    @Override
    public boolean isCancelled() {
        return task.isCancelled();
    }
}
