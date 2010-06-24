package org.neo4j.webadmin;

import org.neo4j.rest.WebServer;

import com.sun.grizzly.http.embed.GrizzlyWebServer;

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
        this.port = port;

        try {
            server = new GrizzlyWebServer(port, webRoot);
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
