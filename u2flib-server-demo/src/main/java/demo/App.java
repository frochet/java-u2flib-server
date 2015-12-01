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
	
	public App(){
		this.dishonet = false;
	}
	
	public App(boolean dishonest, String ip, int port){
		this.dishonet = dishonest;
		this.ip = ip;
		this.port = port;
	}
    @Override
    public void initialize(Bootstrap<Config> bootstrap) {
        bootstrap.addBundle(new ViewBundle());
        bootstrap.addBundle(new AssetsBundle());
    }

    @Override
    public void run(Config config, Environment environment) throws Exception {
    	if (this.dishonet)
    		environment.jersey().register(new ResourceDishonest(this.ip, this.port));
    	else
    		environment.jersey().register(new Resource());
    }

    public static void main(String... args) throws Exception {
    	if (args.length <= 2) 
    		new App().run(args);
    	else if (args[2].equals("dishonest")){
    		String[] strTab = new String[2];
    		strTab[0] = args[0];
    		strTab[1] = args[1];
    		
     		new App(true, strTab[2], Integer.parseInt(strTab[3])).run(strTab);
    	}
    		
    }
}
