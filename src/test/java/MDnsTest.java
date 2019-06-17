import java.util.concurrent.TimeUnit;

import javax.jmdns.JmDNS;
import javax.jmdns.ServiceEvent;
import javax.jmdns.ServiceInfo;
import javax.jmdns.ServiceListener;

import org.junit.Test;

import io.reactivex.Observable;

public class MDnsTest {

    @Test
    public void testMDnsSameHost() throws Exception {
        JmDNS jmDNS1 = JmDNS.create();
        JmDNS jmDNS2 = JmDNS.create();
        final ServiceInfo info = ServiceInfo.create("_http._tcp.local.", "test", "example", 2, "hello");
        jmDNS2.registerService(info);
        Observable.<ServiceInfo>create(emitter -> {
            jmDNS2.addServiceListener("_http._tcp.local.", new ServiceListener() {

                @Override
                public void serviceResolved(ServiceEvent event) {
                    emitter.onNext(event.getInfo());
                }

                @Override
                public void serviceRemoved(ServiceEvent event) {

                }

                @Override
                public void serviceAdded(ServiceEvent event) {

                }
            });
        }).timeout(5L, TimeUnit.SECONDS).blockingFirst();

    }
}