package org.neo4j.webadmin.domain;

import java.net.URI;
import java.util.HashMap;
import java.util.Map;

import org.neo4j.webadmin.rest.ConsoleService;

public class ConsoleServiceRepresentation extends RootRepresentation
{
    public ConsoleServiceRepresentation( URI baseUri )
    {
        super( baseUri );
        this.baseUri = this.baseUri + ConsoleService.ROOT_PATH;
    }

    public Object serialize()
    {
        Map<String, Object> def = new HashMap<String, Object>();
        Map<String, Object> resources = new HashMap<String, Object>();

        resources.put( "exec", baseUri + ConsoleService.EXEC_PATH );

        def.put( "resources", resources );
        return def;
    }

}
