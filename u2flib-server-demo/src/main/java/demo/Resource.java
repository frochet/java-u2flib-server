package demo;

import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import com.yubico.u2f.U2F;
import com.yubico.u2f.data.DeviceRegistration;
import com.yubico.u2f.data.messages.AuthenticateRequestData;
import com.yubico.u2f.data.messages.AuthenticateResponse;
import com.yubico.u2f.data.messages.RegisterRequestData;
import com.yubico.u2f.data.messages.RegisterResponse;
import com.yubico.u2f.exceptions.DeviceCompromisedException;
import com.yubico.u2f.exceptions.NoEligableDevicesException;
import demo.view.AuthenticationView;
import demo.view.RegistrationView;
import io.dropwizard.views.View;

import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Path("/")
@Produces(MediaType.TEXT_HTML)
public class Resource {

    public static final String APP_ID = "https://perceval.elen.ucl.ac.be";
    public static final String NAVIGATION_MENU = "<h2>Navigation</h2><ul><li><a href='/assets/registerIndex.html'>Register</a></li><li><a href='/assets/loginIndex.html'>Login</a></li></ul>";
    
    protected final Map<String, String> requestStorage = new HashMap<String, String>();
    protected final LoadingCache<String, Map<String, String>> userStorage = CacheBuilder.newBuilder().build(new CacheLoader<String, Map<String, String>>() {
        @Override
        public Map<String, String> load(String key) throws Exception {
            return new HashMap<String, String>();
        }
    });
    protected final U2F u2f = new U2F();
    protected int authCounter = 0;
    @Path("startRegistration")
    @GET
    public View startRegistration(@QueryParam("username") String username) {
        RegisterRequestData registerRequestData = u2f.startRegistration(APP_ID, getRegistrations(username));
        requestStorage.put(registerRequestData.getRequestId(), registerRequestData.toJson());
        return new RegistrationView(registerRequestData.toJson(), username);
    }

    @Path("finishRegistration")
    @POST
    public String finishRegistration(@FormParam("tokenResponse") String response, @FormParam("username") String username) {
        RegisterResponse registerResponse = RegisterResponse.fromJson(response);
        RegisterRequestData registerRequestData = RegisterRequestData.fromJson(requestStorage.remove(registerResponse.getRequestId()));
        DeviceRegistration registration = u2f.finishRegistration(registerRequestData, registerResponse);
        addRegistration(username, registration);
        return "<p>Successfully registered device:</p><pre>" +
                registration +
                "</pre>" + NAVIGATION_MENU;
    }

    @Path("startAuthentication")
    @GET
    public View startAuthentication(@QueryParam("username") String username) throws NoEligableDevicesException {
        AuthenticateRequestData authenticateRequestData = u2f.startAuthentication(APP_ID, getRegistrations(username));
        requestStorage.put(authenticateRequestData.getRequestId(), authenticateRequestData.toJson());
        return new AuthenticationView(authenticateRequestData.toJson(), username);
    }

    @Path("finishAuthentication")
    @POST
    public String finishAuthentication(@FormParam("tokenResponse") String response,
                                       @FormParam("username") String username) {
        AuthenticateResponse authenticateResponse = AuthenticateResponse.fromJson(response);
        AuthenticateRequestData authenticateRequest = AuthenticateRequestData.fromJson(requestStorage.remove(authenticateResponse.getRequestId()));
        DeviceRegistration registration = null;
        try {
            registration = u2f.finishAuthentication(authenticateRequest, authenticateResponse, getRegistrations(username));
        } catch (DeviceCompromisedException e) {
            registration = e.getDeviceRegistration();
            return "<p>Device possibly compromised and therefore blocked: " + e.getMessage() + "</p>" + NAVIGATION_MENU;
        } finally {
            userStorage.getUnchecked(username).put(registration.getKeyHandle(), registration.toJson());
        }
        this.authCounter++;
        return "<p>Successfully authenticated for the "+this.authCounter+"th times!<p>" + NAVIGATION_MENU;
    }

    protected Iterable<DeviceRegistration> getRegistrations(String username) {
        List<DeviceRegistration> registrations = new ArrayList<DeviceRegistration>();
        for (String serialized : userStorage.getUnchecked(username).values()) {
            registrations.add(DeviceRegistration.fromJson(serialized));
        }
        return registrations;
    }

    protected void addRegistration(String username, DeviceRegistration registration) {
        userStorage.getUnchecked(username).put(registration.getKeyHandle(), registration.toJson());
    }
}
