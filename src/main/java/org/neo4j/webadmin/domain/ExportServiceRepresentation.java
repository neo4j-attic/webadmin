package org.neo4j.webadmin.domain;

import java.net.URI;
import java.util.HashMap;
import java.util.Map;

import org.neo4j.webadmin.rest.ExportService;

public class ExportServiceRepresentation extends RootRepresentation
{
    public ExportServiceRepresentation( URI baseUri )
    {
        super( baseUri );
        this.baseUri = this.baseUri + ExportService.ROOT_PATH;
    }

    public Object serialize()
    {
        Map<String, Object> def = new HashMap<String, Object>();
        Map<String, Object> resources = new HashMap<String, Object>();

        resources.put( "export_all", baseUri + ExportService.TRIGGER_PATH );

        def.put( "resources", resources );
        return def;
    }

}
