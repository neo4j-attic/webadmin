package org.neo4j.webadmin.rest;

import com.sun.grizzly.arp.AsyncExecutor;
import com.sun.grizzly.arp.AsyncFilter;
import com.sun.grizzly.http.ProcessorTask;
import com.sun.grizzly.tcp.Response;

/**
 * Hack to make grizzly add proper content-disposition headers to GraphML files.
 * 
 * Babies died while writing this. It is done this way because it is meant to be
 * a temporary solution, replaced by something more elegant when the export
 * functionality is moved into neo4j-rest (which uses Jetty6).
 * 
 * @author Jacob Hansson <jacob@voltvoodoo.com>
 * 
 */
public class ContentDispositionFilter implements AsyncFilter
{
    private static final String CONTENT_DISPOSITION = "Content-disposition";
    private static final String ATTACHMENT_DISPOSITION = "attachment;";
    private static final String GRAPHML_SUFFIX = ".gml";

    public boolean doFilter( AsyncExecutor asyncExecutor )
    {
        ProcessorTask task = (ProcessorTask) asyncExecutor.getProcessorTask();

        Response response = task.getRequest().getResponse();

        if ( task.getRequest().requestURI().toString().endsWith( GRAPHML_SUFFIX ) )
        {
            response.addHeader( CONTENT_DISPOSITION, ATTACHMENT_DISPOSITION );
        }

        // Pass execution onwards
        task.invokeAdapter();

        return true;
    }
}
