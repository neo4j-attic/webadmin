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

public class ImportTask implements Runnable
{

    private String filename;

    public ImportTask( String filename )
    {
        this.filename = filename;
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
            InputStream stream;
            try
            {
                stream = new URL( filename ).openStream();
            }
            catch ( MalformedURLException urlEx )
            {
                stream = new FileInputStream( filename );
            }

            System.out.println( "Running import.." );
            GraphMLReader.inputGraph( graph, stream );
            System.out.println( "Done!" );
        }

        catch ( FileNotFoundException e )
        {
            e.printStackTrace();
        }
        catch ( @SuppressWarnings( "restriction" ) XMLStreamException e )
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
