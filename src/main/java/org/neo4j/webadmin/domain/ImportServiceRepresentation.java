package org.neo4j.webadmin.domain;

import java.net.URI;
import java.util.HashMap;
import java.util.Map;

import org.neo4j.webadmin.rest.ImportService;

public class ImportServiceRepresentation extends RootRepresentation
{
    public ImportServiceRepresentation( URI baseUri )
    {
        super( baseUri );
        this.baseUri = this.baseUri + ImportService.ROOT_PATH;
    }

    public Object serialize()
    {
        Map<String, Object> def = new HashMap<String, Object>();
        Map<String, Object> resources = new HashMap<String, Object>();

        resources.put( "import_from_url", baseUri
                                          + ImportService.IMPORT_URL_PATH );
        resources.put( "import_from_file", baseUri
                                           + ImportService.IMPORT_UPLOAD_PATH );

        def.put( "resources", resources );
        return def;
    }
}
