package org.neo4j.webadmin.utils;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;

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

    public static final byte[] getFileAsBytes( final File file )
            throws IOException
    {
        final BufferedInputStream bis = new BufferedInputStream(
                new FileInputStream( file ) );
        final byte[] bytes = new byte[(int) file.length()];
        bis.read( bytes );
        bis.close();
        return bytes;
    }

    public static final String getFileAsString( final File file )
            throws IOException
    {
        return new String( getFileAsBytes( file ) );
    }

}
