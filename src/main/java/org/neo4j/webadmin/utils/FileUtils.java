package org.neo4j.webadmin.utils;

import java.io.File;

/**
 * Utilities for manipulating files.
 * 
 * @author Jacob Hansson <jacob@voltvoodoo.com>
 * 
 */
public class FileUtils
{

    /**
     * Recursively destroy an entire directory.
     * 
     * @param directory
     */
    public static void delTree( File file )
    {
        for ( File childFile : file.listFiles() )
        {
            if ( childFile.isDirectory() )
            {
                delTree( childFile );
            }
            childFile.delete();
        }
    }

}
