import com.berzellius.integrations.basic.exception.APIAuthException;
import com.berzellius.integrations.calltrackingru.dto.api.calltracking.Call;
import com.berzellius.integrations.calltrackingru.dto.api.errorhandlers.CalltrackingAPIRequestErrorHandler;
import com.berzellius.integrations.service.CallTrackingAPIService;
import com.berzellius.integrations.service.CallTrackingAPIServiceImpl;
import org.junit.Before;
import org.junit.Test;
import org.springframework.http.HttpMethod;

import java.util.Calendar;
import java.util.Date;
import java.util.List;

/**
 * Created by berz on 08.01.2017.
 */
public class MainTest {

    private CallTrackingAPIService callTrackingAPIService;

    private Integer defaultProjectId;

    private Date earlyDate(){
        Calendar calendar = Calendar.getInstance();
        calendar.set(Calendar.YEAR, 2016);
        calendar.set(Calendar.MONTH, Calendar.NOVEMBER);
        calendar.set(Calendar.DAY_OF_MONTH, 1);
        calendar.set(Calendar.HOUR, 0);
        calendar.set(Calendar.MINUTE, 0);
        calendar.set(Calendar.SECOND, 0);
        calendar.set(Calendar.MILLISECOND, 0);

        return calendar.getTime();
    }

    private Date lateDate(){
        Calendar calendar = Calendar.getInstance();
        calendar.set(Calendar.YEAR, 2016);
        calendar.set(Calendar.MONTH, Calendar.DECEMBER);
        calendar.set(Calendar.DAY_OF_MONTH, 31);
        calendar.set(Calendar.HOUR, 23);
        calendar.set(Calendar.MINUTE, 59);
        calendar.set(Calendar.SECOND, 59);
        calendar.set(Calendar.MILLISECOND, 999);

        return calendar.getTime();
    }

    @Before
    public void setup(){
        this.setCallTrackingAPIService(new CallTrackingAPIServiceImpl());
        this.getCallTrackingAPIService().setLogin(TestAPI.CallTrackingLogin);
        this.getCallTrackingAPIService().setLoginURL(TestAPI.CallTrackingAPILoginUrl);
        this.getCallTrackingAPIService().setPassword(TestAPI.CallTrackingPassword);
        this.getCallTrackingAPIService().setLoginMethod(HttpMethod.POST);
        this.getCallTrackingAPIService().setApiURL(TestAPI.CallTrackingAPIUrl);
        this.getCallTrackingAPIService().setApiMethod(HttpMethod.POST);
        this.getCallTrackingAPIService().setWebSiteLoginUrl(TestAPI.CallTrackingLoginUrl);
        this.getCallTrackingAPIService().setWebSiteLogin(TestAPI.CallTrackingWebLogin);
        this.getCallTrackingAPIService().setWebSitePassword(TestAPI.CallTrackingWebPassword);
        this.getCallTrackingAPIService().setProjects(TestAPI.CallTrackingProjects);

        CalltrackingAPIRequestErrorHandler calltrackingAPIRequestErrorHandler = new CalltrackingAPIRequestErrorHandler();
        this.getCallTrackingAPIService().setErrorHandler(calltrackingAPIRequestErrorHandler);

        this.setDefaultProjectId(TestAPI.CallTrackingProjects[0]);
    }

    @Test
    public void testReadCalls() throws APIAuthException {
        List<Call> callList = this.getCallTrackingAPIService().getCalls(this.earlyDate(), this.lateDate(), 0l, 100, getDefaultProjectId());
        System.out.println(callList);
    }

    public CallTrackingAPIService getCallTrackingAPIService() {
        return callTrackingAPIService;
    }

    public void setCallTrackingAPIService(CallTrackingAPIService callTrackingAPIService) {
        this.callTrackingAPIService = callTrackingAPIService;
    }

    public Integer getDefaultProjectId() {
        return defaultProjectId;
    }

    public void setDefaultProjectId(Integer defaultProjectId) {
        this.defaultProjectId = defaultProjectId;
    }
}
