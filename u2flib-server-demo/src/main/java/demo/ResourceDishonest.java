package demo;

import io.dropwizard.views.View;
import demo.view.AuthenticationView;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSession;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;

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
import java.net.URL;
import java.net.URLEncoder;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;

import com.yubico.u2f.exceptions.NoEligableDevicesException;

@Path("/")
@Produces(MediaType.TEXT_HTML)
public class ResourceDishonest extends Resource {

	
	private String ip;
	private int port;
	
	public ResourceDishonest(String ip, int port, String APP_ID){
		super(APP_ID);
		this.ip = ip;
		this.port = port;
		
		// Create a trust manager that does not validate certificate chains
        TrustManager[] trustAllCerts = new TrustManager[] {new X509TrustManager() {
                public java.security.cert.X509Certificate[] getAcceptedIssuers() {
                    return null;
                }
				@Override
				public void checkClientTrusted(X509Certificate[] arg0,
						String arg1) throws CertificateException {
					// TODO Auto-generated method stub
					
				}
				@Override
				public void checkServerTrusted(X509Certificate[] arg0,
						String arg1) throws CertificateException {
					// TODO Auto-generated method stub
					
				}
            }
        };
 
        // Install the all-trusting trust manager
        try {
        	SSLContext sc = SSLContext.getInstance("SSL");
			sc.init(null, trustAllCerts, new java.security.SecureRandom());
	        HttpsURLConnection.setDefaultSSLSocketFactory(sc.getSocketFactory());
	        
	        // Create all-trusting host name verifier
	        HostnameVerifier allHostsValid = new HostnameVerifier() {
				@Override
				public boolean verify(String arg0, SSLSession arg1) {
					// TODO Auto-generated method stub
					return false;
				}
	        };
	 
	        // Install the all-trusting host verifier
	        HttpsURLConnection.setDefaultHostnameVerifier(allHostsValid);
			

		} catch (KeyManagementException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (NoSuchAlgorithmException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	/*
	 * 
	 * Makes the authentication request towards a honest server
	 * 
	 */
	@Path("startAuthentication")
    @POST
    public View startAuthentication(@FormParam("username") String username) throws NoEligableDevicesException {
		URL url;
		HttpsURLConnection connection = null;
		String data = "";
		try{
		
			url = new URL("https://"+this.ip+":"+this.port+"/startAuthentication");
			connection = (HttpsURLConnection)url.openConnection();
			connection.setUseCaches(false);
			connection.setDoOutput(true);
			connection.setRequestMethod("GET");
			String charset = "UTF-8";
			connection.setRequestProperty("Accept-Charset", charset);
			connection.setRequestProperty("Content-Type", "application/x-www-form-urlencoded;charset=" + charset);
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
			System.out.println(data);
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
		HttpsURLConnection connection = null;
		try{
			url = new URL("https://"+this.ip+":"+this.port+"/finishAuthentication");
			connection = (HttpsURLConnection)url.openConnection();
			connection.setRequestMethod("POST");
			connection.setUseCaches(false);
			String charset = "UTF-8";
			connection.setRequestProperty("Accept-Charset", charset);
			connection.setRequestProperty("Content-Type", "application/x-www-form-urlencoded;charset=" + charset);
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
		return "<p>Successfully authenticated jay<p>" + NAVIGATION_MENU;
	}
}
