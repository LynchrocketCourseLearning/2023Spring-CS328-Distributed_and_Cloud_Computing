package RMIZK;

import myrmi.Remote;

import java.lang.ref.Reference;
import java.lang.ref.WeakReference;
import java.util.concurrent.Callable;

@Deprecated
public class MonitorService<T extends Remote> implements Callable<String> {
    private final Reference<T> monitoredReference;
    private final String serverHost;

    public MonitorService(T service, String serverHost) {
        this.monitoredReference = new WeakReference<T>(service);
        this.serverHost = serverHost;
    }

    public T getMonitoredService() {
        return this.monitoredReference.get();
    }

    private boolean isMonitoredServiceGarbageCollected() {
        return this.monitoredReference.isEnqueued();
    }

    @Override
    public String call() throws Exception {
        while (!isMonitoredServiceGarbageCollected()) ;
        return this.serverHost;
    }
}
