package org.neo4j.webadmin.task;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.OutputStream;

import javax.xml.stream.XMLStreamException;

import org.neo4j.webadmin.AdminServer;
import org.neo4j.webadmin.console.GremlinFactory;

import com.tinkerpop.blueprints.pgm.TransactionalGraph;
import com.tinkerpop.blueprints.pgm.parser.GraphMLWriter;

/**
 * Performs a full export of the underlying database and puts the resulting
 * GraphML file in a subfolder available to the web.
 * 
 * @author Jacob Hansson <jacob@voltvoodoo.com>
 * 
 */
public class ExportTask implements Runnable
{

    public static final String EXPORT_FOLDER_PATH = "export";
    public static final String EXPORT_FILE_PATH = "database.gml";

    public static final File EXPORT_FOLDER = new File( new File(
            AdminServer.INSTANCE.getStaticPath() ), EXPORT_FOLDER_PATH );

    public static final File EXPORT_FILE = new File( EXPORT_FOLDER,
            EXPORT_FILE_PATH );

    /**
     * Do a full export in GraphML format of the underlying database.
     */
    public void run()
    {

        // Since we already have a dependency on Gremlin, we use the GraphML
        // export functionality from
        // there.

        TransactionalGraph graph = GremlinFactory.getGremlinWrappedGraph();

        try
        {

            EXPORT_FOLDER.mkdirs();

            if ( EXPORT_FILE.exists() )
            {
                // Delete old export
                EXPORT_FILE.delete();
            }

            OutputStream stream = new FileOutputStream( EXPORT_FILE );

            GraphMLWriter.outputGraph( graph, stream );
        }
        catch ( FileNotFoundException e )
        {
            e.printStackTrace();
        }
        catch ( @SuppressWarnings( "restriction" ) XMLStreamException e )
        {
            e.printStackTrace();
        }
    }

}
