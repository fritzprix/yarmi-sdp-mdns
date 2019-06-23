import net.doodream.yarmi.annotation.method.RMIExpose;
import net.doodream.yarmi.annotation.parameter.Body;
import net.doodream.yarmi.model.Response;

public interface TestController {

    @RMIExpose
    Response<String> hello(@Body final String message);
}