package org.neo4j.webadmin.resources;

import static org.neo4j.webadmin.utils.FileUtils.getFileAsString;

import java.io.File;
import java.io.IOException;

public class FileJoinCompiler implements StaticResourceCompiler
{

    public void addFile( StringBuilder builder, File file )
    {
        try
        {
            builder.append( getFileAsString( file ) );
        }
        catch ( IOException e )
        {
            e.printStackTrace();
        }
    }

}
