package org.neo4j.webadmin.domain;

import java.net.URI;
import java.util.HashMap;
import java.util.Map;

import org.neo4j.webadmin.rest.ConfigService;

public class ConfigServiceRepresentation extends RootRepresentation
{
    public ConfigServiceRepresentation( URI baseUri )
    {
        super( baseUri );
        this.baseUri = this.baseUri + ConfigService.ROOT_PATH;
    }

    public Object serialize()
    {
        Map<String, Object> def = new HashMap<String, Object>();
        Map<String, Object> resources = new HashMap<String, Object>();

        resources.put( "properties", baseUri + ConfigService.ALL_SETTINGS_PATH );

        def.put( "resources", resources );
        return def;
    }

}
