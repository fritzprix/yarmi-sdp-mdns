package net.doodream.yarmi.sdp.mdns;

import java.io.IOException;
import java.net.InetAddress;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.atomic.AtomicBoolean;

import javax.jmdns.JmDNS;
import javax.jmdns.ServiceEvent;
import javax.jmdns.ServiceInfo;
import javax.jmdns.ServiceListener;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import net.doodream.yarmi.model.RMIServiceInfo;
import net.doodream.yarmi.sdp.ServiceDiscovery;
import net.doodream.yarmi.sdp.ServiceDiscoveryListener;

public class MDnsServiceDiscovery implements ServiceDiscovery {

    private static final Logger Log = LoggerFactory.getLogger(MDnsServiceDiscovery.class);
    private final ExecutorService executorService = Executors.newCachedThreadPool();
    private final ArrayBlockingQueue<ServiceInfo> discoveredServiceQueue = new ArrayBlockingQueue<>(10);
    private Future<?> discoveryTask;
    private final ServiceListener mListener = new ServiceListener() {

        @Override
        public void serviceResolved(ServiceEvent event) {
            final ServiceInfo serviceInfo = event.getInfo();
            try {
                discoveredServiceQueue.add(serviceInfo);
            } catch (IllegalStateException e) {
                Log.warn("service discovery event dropped : {}", e.getMessage());
            }
        }

        @Override
        public void serviceRemoved(ServiceEvent event) {

        }

        @Override
        public void serviceAdded(ServiceEvent event) {

        }
    };
    private final AtomicBoolean isStarted = new AtomicBoolean(false);
    private final JmDNS jmDns;

    @Override
    protected void finalize() throws Throwable {
        super.finalize();
        stop();
        jmDns.close();
        executorService.shutdown();
    }

    public static final ServiceDiscovery create() throws IOException {
        final JmDNS jmDNS = JmDNS.create();
        return new MDnsServiceDiscovery(jmDNS);
    }

    public static final ServiceDiscovery create(InetAddress network) throws IOException {
        final JmDNS jmDNS = JmDNS.create(network);
        return new MDnsServiceDiscovery(jmDNS);
    }

    private MDnsServiceDiscovery(JmDNS dns) {
        this.jmDns = dns;
    }

    @Override
    public void start(Class<?> service, ServiceDiscoveryListener listner) throws IOException, IllegalArgumentException {
        if (listner == null) {
            throw new IllegalArgumentException("null listener");
        }
        if (service == null) {
            throw new IllegalArgumentException("null service");
        }
        if (isStarted.compareAndSet(false, true)) {
            final RMIServiceInfo targetServiceInfo = RMIServiceInfo.from(service);
            listner.onDiscoveryStarted();
            jmDns.addServiceListener(MDnsServiceRegistry.SERVICE_TYPE, mListener);
            synchronized (this) {
                discoveryTask = executorService.submit(() -> {
                    try {
                        while (isStarted.get()) {
                            final ServiceInfo discoveredRawService = discoveredServiceQueue.take();
                            if (MDnsServiceRegistry.matches(targetServiceInfo, discoveredRawService)) {
                                final RMIServiceInfo discoveredService = MDnsServiceRegistry
                                        .buildNewServiceInfo(service, discoveredRawService);

                                executorService.submit(() -> listner.onServiceDiscovered(discoveredService));
                            }
                        }
                    } catch (InterruptedException e) {
                        Log.debug("discovery stopped");
                    } catch (Exception e) {
                        listner.onDiscoveryFinished(ServiceDiscoveryListener.FINISH_ERROR, e);
                        return;
                    }

                    listner.onDiscoveryFinished(ServiceDiscoveryListener.FINISH_NORMAL, null);
                });
            }
        }
    }

    @Override
    public void stop() {
        if (isStarted.compareAndSet(true, false)) {
            jmDns.removeServiceListener(MDnsServiceRegistry.SERVICE_TYPE, mListener);
            cancelDiscoveryTask();
        }
    }

    private synchronized void cancelDiscoveryTask() {
        if (discoveryTask == null) {
            return;
        }
        if (discoveryTask.isDone() || discoveryTask.isCancelled()) {
            return;
        }
        discoveryTask.cancel(true);
    }
}