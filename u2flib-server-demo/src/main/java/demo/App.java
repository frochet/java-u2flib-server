package demo;

import io.dropwizard.Application;
import io.dropwizard.assets.AssetsBundle;
import io.dropwizard.setup.Bootstrap;
import io.dropwizard.setup.Environment;
import io.dropwizard.views.ViewBundle;

public class App extends Application<Config> {
	
	private boolean dishonet;
	private String ip;
	private int port;
	private String APP_ID;
	
	public App(String APP_ID){
		this.dishonet = false;
		this.APP_ID = APP_ID;
	}
	
	public App(boolean dishonest, String ip, int port, String APP_ID){
		this.dishonet = dishonest;
		this.ip = ip;
		this.port = port;
		this.APP_ID = APP_ID;
	}
    @Override
    public void initialize(Bootstrap<Config> bootstrap) {
        bootstrap.addBundle(new ViewBundle());
        bootstrap.addBundle(new AssetsBundle());
    }

    @Override
    public void run(Config config, Environment environment) throws Exception {
    	if (this.dishonet)
    		environment.jersey().register(new ResourceDishonest(this.ip, this.port, this.APP_ID));
    	else
    		environment.jersey().register(new Resource(this.APP_ID));
    }

    public static void main(String... args) throws Exception {
    	String[] strTab = new String[2];
		strTab[0] = args[0];
		strTab[1] = args[1];
    	if (args.length <= 3) 
    		new App(args[2]).run(strTab);
    	else {
    		
     		new App(true, args[2], Integer.parseInt(args[3]), args[4]).run(strTab);
    	}
    		
    }
}
