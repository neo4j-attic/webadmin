package org.neo4j.webadmin.resources;

import static org.neo4j.webadmin.utils.FileUtils.getFileAsString;

import java.io.File;
import java.io.IOException;

public class JSCompiler implements StaticResourceCompiler
{

    public void addFile( StringBuilder builder, File file )
    {
        try
        {
            builder.append( getFileAsString( file ) );
            builder.append( ";" );
        }
        catch ( IOException e )
        {
            e.printStackTrace();
        }
    }

}
