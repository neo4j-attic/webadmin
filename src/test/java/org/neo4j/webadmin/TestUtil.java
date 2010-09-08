package org.neo4j.webadmin;

import java.io.File;

import org.neo4j.rest.domain.DatabaseLocator;

/**
 * This is here temporarily, it is simply a copy-paste of a class in the REST
 * test suite.
 * 
 */
public abstract class TestUtil
{
    /**
     * The "admin" part is here temporarily, while we wait to move management
     * services into the REST project instead.
     */
    public static String SERVER_BASE = AdminServer.INSTANCE.getBaseUri()
                                       + "admin";

    public static void deleteTestDb()
    {
        deleteFileOrDirectory( new File( DatabaseLocator.DB_PATH ) );
    }

    public static void deleteFileOrDirectory( File file )
    {
        if ( !file.exists() )
        {
            return;
        }

        if ( file.isDirectory() )
        {
            for ( File child : file.listFiles() )
            {
                deleteFileOrDirectory( child );
            }
        }
        else
        {
            file.delete();
        }
    }

}
