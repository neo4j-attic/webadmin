package org.neo4j.webadmin.rest;

import static org.neo4j.webadmin.rest.WebUtils.addHeaders;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Properties;

import javax.ws.rs.Consumes;
import javax.ws.rs.FormParam;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.neo4j.rest.domain.JsonRenderers;

/**
 * A simple key/value store for handling preferences in the admin interface.
 * This is a fairly naive implementation that is based on the assumption that at
 * most two or three users would ever be modifying admin properties at the same
 * time.
 * 
 * @author Jacob Hansson <jacob@voltvoodoo.com>
 * 
 */
@Path( AdminPropertiesService.ROOT_PATH )
public class AdminPropertiesService
{

    protected static final String ROOT_PATH = "/properties";

    /**
     * Get settings file, creating one if it does not exist.
     * 
     * @return
     * @throws IOException
     */
    public static File getPropertiesFile() throws IOException
    {
        String userHome = System.getProperty( "user.home" );
        if ( userHome == null )
        {
            throw new IllegalStateException( "user.home==null" );
        }

        // Make sure settings directory exists
        File home = new File( userHome );
        File settingsDirectory = new File( home, ".neo4jwebadmin" );
        if ( !settingsDirectory.exists() )
        {
            if ( !settingsDirectory.mkdir() )
            {
                throw new IllegalStateException( settingsDirectory.toString() );
            }
        }

        // Make sure settings file exists
        File settingsFile = new File( settingsDirectory, "settings" );

        if ( !settingsFile.exists() && !settingsFile.createNewFile() )
        {
            throw new IllegalStateException( settingsFile.toString() );
        }

        return settingsFile;
    }

    public static volatile Properties properties;

    //
    // CONSTRUCT
    //

    public AdminPropertiesService() throws IOException
    {
        if ( properties == null )
        {
            properties = new Properties();
            FileInputStream in = new FileInputStream( getPropertiesFile() );
            properties.load( in );
            in.close();
        }
    }

    //
    // PUBLIC
    //

    @GET
    @Produces( MediaType.APPLICATION_JSON )
    @Path( "/{key}" )
    public Response getValue( @PathParam( "key" ) String key )
    {
        Object value = properties.get( key );

        if ( value == null )
        {
            return addHeaders(
                    Response.ok( "undefined",
                            JsonRenderers.DEFAULT.getMediaType() ) ).build();
        }

        return addHeaders(
                Response.ok( (String) value,
                        JsonRenderers.DEFAULT.getMediaType() ) ).build();
    }

    @POST
    @Produces( MediaType.APPLICATION_JSON )
    @Consumes( MediaType.APPLICATION_JSON )
    @Path( "/{key}" )
    public Response jsonSetValue( @PathParam( "key" ) String key, String value )
    {
        return setValue( key, value );
    }

    @POST
    @Produces( MediaType.APPLICATION_JSON )
    @Consumes( MediaType.APPLICATION_FORM_URLENCODED )
    @Path( "/{key}" )
    public Response formSetValueJSON( @PathParam( "key" ) String key,
            @FormParam( "value" ) String value )
    {
        return setValue( key, value );
    }

    //
    // INTERNALS
    //

    protected Response setValue( String key, String value )
    {
        properties.put( key, value );

        FileOutputStream out;
        try
        {
            out = new FileOutputStream( getPropertiesFile() );
            properties.store( out, "--Changed via admin gui--" );
            out.close();
        }
        catch ( FileNotFoundException e )
        {
            return Response.serverError().build();
        }
        catch ( IOException e )
        {
            return Response.serverError().build();
        }

        return Response.ok().build();
    }
}
