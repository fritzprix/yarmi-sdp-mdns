package net.doodream.yarmi.sdp.mdns;

import java.io.IOException;
import java.net.InetAddress;
import java.util.concurrent.ConcurrentHashMap;

import javax.jmdns.JmDNS;
import javax.jmdns.ServiceInfo;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import net.doodream.yarmi.model.RMIServiceInfo;
import net.doodream.yarmi.sdp.ServiceRegistry;
import net.doodream.yarmi.server.RMIService;

public class MDnsServiceRegistry implements ServiceRegistry {

    public static String SERVICE_TYPE = "_yarmi._tcp.local.";
    private static final Logger Log = LoggerFactory.getLogger(MDnsServiceRegistry.class);

    private final JmDNS jmDNS;
    private final ConcurrentHashMap<Integer, ServiceInfo> serviceMap;

    public static ServiceRegistry create(InetAddress network) throws IOException {
        final JmDNS jmDNS = JmDNS.create(network);
        return new MDnsServiceRegistry(jmDNS);
    }

    public static ServiceRegistry create() throws IOException {
        final JmDNS jmDNS = JmDNS.create();
        return new MDnsServiceRegistry(jmDNS);
    }

    private MDnsServiceRegistry(JmDNS dns) {
        this.jmDNS = dns;
        serviceMap = new ConcurrentHashMap<>();
    }

    @Override
    public void unregister(int id) throws IllegalArgumentException {
        if (id < 0) {
            throw new IllegalArgumentException("invalid id : " + id);
        }

        if (!serviceMap.containsKey(id)) {
            return;
        }
        final ServiceInfo svcInfo = serviceMap.remove(id);
        if (svcInfo != null) {
            Log.debug("service unregistered {}", svcInfo.getName());
        }
    }

    @Override
    public int register(RMIService service) throws IllegalArgumentException {
        final RMIServiceInfo serviceInfo = service.getServiceInfo();
        if (serviceInfo == null) {
            throw new IllegalArgumentException("null service info");
        }
        final int id = serviceInfo.hashCode();
        final ServiceInfo svcInfo = convert(serviceInfo);
        if (serviceMap.contains(id)) {

            return id;
        }
        try {
            jmDNS.registerService(svcInfo);
            serviceMap.put(id, svcInfo);
        } catch (IOException e) {
            return -1;
        }
        return id;
    }

    private ServiceInfo convert(RMIServiceInfo service) {
        return ServiceInfo.create(SERVICE_TYPE, service.getName(), String.valueOf(service.hashCode()), 0,
                service.getProxyFactoryHint());
    }

    @Override
    public void start() throws IllegalStateException, IOException {

    }

    @Override
    public void stop() throws IllegalStateException {
        serviceMap.clear();
        jmDNS.unregisterAllServices();
    }

    static boolean matches(RMIServiceInfo target, ServiceInfo info) {
        final String hashCodeStr = info.getSubtype();
        if (hashCodeStr == null || hashCodeStr.isEmpty()) {
            return false;
        }

        try {
            final int hashCode = Integer.valueOf(hashCodeStr);
            return target.hashCode() == hashCode;
        } catch (NumberFormatException e) {
            Log.warn("invalid subtype value {}", hashCodeStr);
            return false;
        }
    }

    static RMIServiceInfo buildNewServiceInfo(Class<?> service, ServiceInfo rawInfo) {
        final RMIServiceInfo serviceInfo = RMIServiceInfo.from(service);
        serviceInfo.setProxyFactoryHint(rawInfo.getTextString());
        return serviceInfo;
    }
}