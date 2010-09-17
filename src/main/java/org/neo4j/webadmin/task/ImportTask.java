package org.neo4j.webadmin.task;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;

import javax.xml.stream.XMLStreamException;

import org.neo4j.graphdb.GraphDatabaseService;
import org.neo4j.webadmin.parser.GraphMLReader;
import org.neo4j.webadmin.utils.GraphDatabaseUtils;

@SuppressWarnings( "restriction" )
public class ImportTask implements Runnable
{

    private String filename;
    private InputStream stream;

    public ImportTask( String filename )
    {
        this.filename = filename;
    }

    public ImportTask( InputStream stream )
    {
        this.stream = stream;
    }

    /**
     * Do a full export in GraphML format of the underlying database.
     */
    public void run()
    {

        // Since we already have a dependency on Gremlin, we use the GraphML
        // import functionality from
        // there.

        GraphDatabaseService graph = GraphDatabaseUtils.getLocalDatabase();

        try
        {
            if ( stream == null )
            {
                try
                {
                    stream = new URL( filename ).openStream();
                }
                catch ( MalformedURLException urlEx )
                {
                    stream = new FileInputStream( filename );
                }
            }

            GraphMLReader.inputGraph( graph, stream );

            if ( filename != null )
            {
                // If we were the ones to open the stream, close it.
                stream.close();
            }
        }

        catch ( FileNotFoundException e )
        {
            e.printStackTrace();
        }
        catch ( XMLStreamException e )
        {
            e.printStackTrace();
        }
        catch ( IOException e )
        {
            e.printStackTrace();
        }
        catch ( Exception e )
        {
            e.printStackTrace();
        }

    }

}