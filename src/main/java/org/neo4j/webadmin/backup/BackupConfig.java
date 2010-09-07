package org.neo4j.webadmin.backup;

import static org.neo4j.rest.domain.JsonHelper.createJsonFrom;
import static org.neo4j.rest.domain.JsonHelper.jsonToMap;
import static org.neo4j.webadmin.utils.FileUtils.getFileAsString;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.neo4j.rest.domain.Representation;
import org.neo4j.webadmin.domain.BackupJobDescriptionRepresentation;

public class BackupConfig implements Representation
{
    public static final String JOB_LIST_KEY = "jobList";

    private File configFile;
    private ArrayList<BackupJobDescription> descs = new ArrayList<BackupJobDescription>();

    //
    // CONSTRUCT
    //

    public BackupConfig( File configFile ) throws IOException
    {
        this.configFile = configFile;
        load();
    }

    //
    // PUBLIC
    //

    public ArrayList<BackupJobDescription> getJobDescriptions()
    {
        return descs;
    }

    /**
     * Add or edit a job description.
     * 
     * @param desc
     * @throws IOException
     */
    public void setJobDescription( BackupJobDescription desc )
            throws IOException
    {
        removeJobDescription( desc.getName() );
        descs.add( desc );
        persist();
    }

    public BackupJobDescription getJobDescription( String name )
    {
        for ( BackupJobDescription desc : descs )
        {
            if ( desc.getName().equals( name ) )
            {
                return desc;
            }
        }
        return null;
    }

    public void removeJobDescription( String name ) throws IOException
    {
        Iterator<BackupJobDescription> it = descs.iterator();
        while ( it.hasNext() )
        {
            if ( it.next().getName().equals( name ) )
            {
                it.remove();
                persist();
                break;
            }
        }
    }

    public Object serialize()
    {
        Map<String, Object> configMap = new HashMap<String, Object>();
        ArrayList<Object> jobList = new ArrayList<Object>();

        for ( BackupJobDescription desc : descs )
        {
            jobList.add( new BackupJobDescriptionRepresentation( desc ).serialize() );
        }

        configMap.put( JOB_LIST_KEY, jobList );
        return configMap;
    }

    //
    // INTERNALS
    //

    private synchronized void persist() throws IOException
    {
        FileOutputStream configOut = new FileOutputStream( configFile );
        configOut.write( createJsonFrom( serialize() ).getBytes() );
        configOut.close();

    }

    @SuppressWarnings( "unchecked" )
    private synchronized void load() throws IOException
    {
        try
        {
            String raw = getFileAsString( configFile );

            if ( raw == null || raw.length() == 0 )
            {
                persist();
            }
            else
            {

                Map<String, Object> configMap = jsonToMap( raw );
                descs.clear();

                for ( Object item : (List<Object>) configMap.get( JOB_LIST_KEY ) )
                {
                    descs.add( BackupJobDescriptionRepresentation.deserialize( (Map<String, Object>) item ) );
                }
            }
        }
        catch ( Exception e )
        {
            e.printStackTrace();
        }
    }
}
