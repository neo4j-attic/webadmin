package org.neo4j.webadmin.domain;

import java.net.URI;

import org.neo4j.rest.domain.Representation;

public abstract class RootRepresentation implements Representation
{
    protected String baseUri;

    public RootRepresentation( URI baseUri )
    {
        this.baseUri = baseUri.toASCIIString();

        if ( this.baseUri.endsWith( "/" ) )
        {
            this.baseUri = this.baseUri.substring( 0, this.baseUri.length() - 1 );
        }
    }
}
