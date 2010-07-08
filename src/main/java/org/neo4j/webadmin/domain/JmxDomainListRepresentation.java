package org.neo4j.webadmin.domain;

import java.util.ArrayList;

import org.neo4j.rest.domain.Representation;

public class JmxDomainListRepresentation implements Representation
{

    protected ArrayList<String> domains = new ArrayList<String>();

    public JmxDomainListRepresentation( String[] domains )
    {
        for ( String domain : domains )
        {
            this.domains.add( domain );
        }
    }

    public Object serialize()
    {
        return this.domains;
    }

}
