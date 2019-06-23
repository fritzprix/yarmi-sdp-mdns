import net.doodream.yarmi.annotation.server.Controller;
import net.doodream.yarmi.annotation.server.Service;
import net.doodream.yarmi.model.Response;
import net.doodream.yarmi.net.tcp.TcpServiceAdapter;
import net.doodream.yarmi.serde.bson.BsonConverter;

@Service(name = "test-service", provider = "com.doodream", converter = BsonConverter.class, adapter = TcpServiceAdapter.class)
public class TestService {

    public static class DefaultController implements TestController {

        @Override
        public Response<String> hello(String message) {
            return null;
        }
    }

    @Controller(path = "/contoller", version = 1, module = DefaultController.class)
    TestController controller;
}