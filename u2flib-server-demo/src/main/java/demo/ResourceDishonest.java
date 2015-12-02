package demo;

import io.dropwizard.views.View;
import demo.view.AuthenticationView;
 
import javax.ws.rs.FormParam;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;

import com.yubico.u2f.exceptions.NoEligableDevicesException;

@Path("/")
@Produces(MediaType.TEXT_HTML)
public class ResourceDishonest extends Resource {

	
	private String ip;
	private int port;
	
	public ResourceDishonest(String ip, int port){
		this.ip = ip;
		this.port = port;
	}
	
	/*
	 * 
	 * Makes the authentication request towards a honest server
	 * 
	 */
	@Path("startAuthentication")
    @GET
    public View startAuthentication(@QueryParam("username") String username) throws NoEligableDevicesException {
		URL url;
		HttpURLConnection connection = null;
		String data = "";
		try{
			url = new URL("https://"+this.ip+":"+this.port+"/startAuthentication");
			connection = (HttpURLConnection)url.openConnection();
			connection.setRequestMethod("GET");
			connection.setUseCaches(false);
			connection.setDoOutput(true);
			
			DataOutputStream wr = new DataOutputStream(connection.getOutputStream());
			wr.writeBytes("username="+URLEncoder.encode(username, "UTF-8"));
			wr.flush();
			wr.close();
			
			//get response
			InputStream is = connection.getInputStream();
			BufferedReader rd = new BufferedReader(new InputStreamReader(is));
			//Extract authenticate request from honnest server
			String line;
			while((line = rd.readLine()) != null){
				if (line.contains("var request =")){
					String[] authrequest = line.split("=");
					data = authrequest[1];
					break;
				}
			}
			rd.close();
			return new AuthenticationView(data, username);
			
		}catch(Exception e){
			e.printStackTrace();
			return null;
		}finally{
			if (connection != null)
				connection.disconnect();
		}
		
	}
	@Path("finishAuthentication")
	@POST
	public String finishAuthentication(@FormParam("tokenResponse") String response,
			@FormParam("username") String username) {
		URL url;
		HttpURLConnection connection = null;
		try{
			url = new URL("https://"+this.ip+":"+this.port+"/finishAuthentication");
			connection = (HttpURLConnection)url.openConnection();
			connection.setRequestMethod("POST");
			connection.setUseCaches(false);

			DataOutputStream wr = new DataOutputStream(connection.getOutputStream());
			wr.writeBytes("tokenResponse="+URLEncoder.encode(response, "UTF-8")
					+"&username="+URLEncoder.encode(username, "UTF-8"));
			wr.flush();
			wr.close();
				
		}catch(Exception e){
			e.printStackTrace();
			return null;
		}finally{
			if (connection != null)
				connection.disconnect();
		}		 
		this.authCounter++;
		return "<p>Successfully authenticated for the "+this.authCounter+"th times!<p>" + NAVIGATION_MENU;
	}
}
