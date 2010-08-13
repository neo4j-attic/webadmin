package org.neo4j.webadmin.rest;

import static org.neo4j.webadmin.rest.WebUtils.addHeaders;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Response;

import org.neo4j.webadmin.Main;
import org.neo4j.webadmin.resources.StaticResourceHandler;

/**
 * A web service that handles minifying and serving css, javascript and
 * templates.
 * 
 * @author Jacob Hansson <jacob@voltvoodoo.com>
 * 
 */
@Path( StaticResourcesService.ROOT_PATH )
public class StaticResourcesService
{

    protected static final String ROOT_PATH = "/static";

    protected static final String CSS_MEDIA_TYPE = "text/css";
    protected static final String JS_MEDIA_TYPE = "application/javascript";
    protected static final String TEMPLATE_MEDIA_TYPE = "text/html";

    protected final static StaticResourceHandler jsCompiler = new StaticResourceHandler(
            Main.getWebRoot(), "js/resources.lst" );

    protected final static StaticResourceHandler templateCompiler = new StaticResourceHandler(
            Main.getWebRoot(), "templates/resources.lst" );

    //
    // PUBLIC
    //

    /**
     * Get the application js code as a single file.
     */
    @GET
    @Path( "/app.js" )
    @Produces( JS_MEDIA_TYPE )
    public synchronized Response getJavaScript()
    {

        return addHeaders(
                Response.ok( jsCompiler.getCompiled(), JS_MEDIA_TYPE ) ).build();

    }

    /**
     * Get the application templates as a single file.
     */
    @GET
    @Path( "/templates.html" )
    @Produces( TEMPLATE_MEDIA_TYPE )
    public synchronized Response getTemplates()
    {

        return addHeaders(
                Response.ok( templateCompiler.getCompiled(),
                        TEMPLATE_MEDIA_TYPE ) ).build();

    }
}
