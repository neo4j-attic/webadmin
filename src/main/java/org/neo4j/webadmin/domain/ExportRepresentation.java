package org.neo4j.webadmin.domain;

import java.net.URI;
import java.util.HashMap;
import java.util.Map;

import org.neo4j.rest.domain.Representation;

/**
 * Contains a string URL, pointing to a completed export that can be downloaded.
 * 
 * @author Jacob Hansson <jacob@voltvoodoo.com>
 * 
 */
public class ExportRepresentation implements Representation
{

    public static final String EXPORT_URL_KEY = "url";

    private URI exportUri;

    public ExportRepresentation( URI exportURI )
    {
        this.exportUri = exportURI;
    }

    public Object serialize()
    {
        Map<String, Object> serial = new HashMap<String, Object>();

        serial.put( EXPORT_URL_KEY, exportUri.toString() );

        return serial;
    }
}
