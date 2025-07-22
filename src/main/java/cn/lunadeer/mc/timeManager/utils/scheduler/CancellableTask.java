package cn.lunadeer.mc.timeManager.utils.scheduler;

public interface CancellableTask {
    void cancel();

    boolean isCancelled();
}
