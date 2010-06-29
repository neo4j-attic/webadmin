package org.neo4j.webadmin;

import java.io.File;

import org.neo4j.rest.WebServer;

import com.sun.grizzly.http.embed.GrizzlyWebServer;
import com.sun.grizzly.http.servlet.ServletAdapter;
import com.sun.grizzly.tcp.http11.GrizzlyAdapter;
import com.sun.grizzly.tcp.http11.GrizzlyRequest;
import com.sun.grizzly.tcp.http11.GrizzlyResponse;
import com.sun.jersey.spi.container.servlet.ServletContainer;

/**
 * Launcher for the Grizzly server that handles the admin interface.
 * This code based on {@link WebServer} in the neo4j REST interface.
 * 
 * @author Jacob Hansson <jacob@voltvoodoo.com>
 *
 */
public enum AdminServer {
	INSTANCE;
	
	public static final int DEFAULT_PORT = 9988;
	public static final String DEFAULT_WEBROOT = "public";
	
    private GrizzlyWebServer server;
    private int port = DEFAULT_PORT;

    public void startServer() {
        startServer( DEFAULT_PORT, DEFAULT_WEBROOT );
    }
    
    public void startServer( int port, String webRoot ) {
    	try {
	        this.port = port;
	        String absWebRoot = (new File(webRoot)).getAbsolutePath();
	        
	        // Instantiate the server
            server = new GrizzlyWebServer(port, absWebRoot);
            
            // Create REST-adapter
            ServletAdapter jerseyAdapter = new ServletAdapter();
            jerseyAdapter.addInitParameter("com.sun.jersey.config.property.packages", "org.neo4j.webadmin.rest");
            jerseyAdapter.setContextPath("/api");
            jerseyAdapter.setServletInstance(new ServletContainer());
            
            // Add adapters
            server.addGrizzlyAdapter(jerseyAdapter);
            
            /*
             * This is a bit of a hack. Grizzly comes with a built-in file-serving-adapter, serving files from the
             * directory defined when instantiating GrizzlyWebServer above. However, this adapter is for some
             * reason discarded when adding other adapters, like out REST adapter above. 
             * 
             * Through devil magic, the problem is resolved by adding an empty adapter.
             */
            server.addGrizzlyAdapter(new GrizzlyAdapter(){public void service(GrizzlyRequest arg0, GrizzlyResponse arg1) {}});
            
            // Start server
            server.start();
            
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
    }
    
    public String getBaseUri()
    {
        return getLocalhostBaseUri( port );
    }
    
    public void stopServer() {
        server.stop();
    }

    public static String getLocalhostBaseUri()
    {
        return getLocalhostBaseUri( DEFAULT_PORT );
    }
    
    public static String getLocalhostBaseUri( int port )
    {
        return "http://localhost:" + port + "/";
    }
	
}
