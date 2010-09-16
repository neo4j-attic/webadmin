package org.neo4j.webadmin.domain;

import java.net.URI;

import org.neo4j.rest.domain.Representation;

/**
 * Contains a string URL, pointing to a completed export that can be downloaded.
 * 
 * @author Jacob Hansson <jacob@voltvoodoo.com>
 * 
 */
public class ExportRepresentation implements Representation
{

    private URI exportUri;

    public ExportRepresentation( URI exportURI )
    {
        this.exportUri = exportURI;
    }

    public Object serialize()
    {
        // TODO Auto-generated method stub
        return null;
    }

}
