package org.neo4j.webadmin.properties;

import java.io.File;
import java.io.IOException;

import org.neo4j.rest.domain.DatabaseLocator;

public class ConfigFileFactory
{

    public static final String APP_ARGS_DEV_PATH = "target/appargs";
    public static final String JVM_ARGS_DEV_PATH = "target/jvmargs";

    public static final String DB_CONFIG_PATH = "neo4j.properties";
    public static final String GENERAL_CONFIG_PATH = "startup.properties";
    public static final String BACKUP_CONFIG_PATH = "backup.json";
    public static final String BACKUP_LOG_PATH = "backup.log.json";

    public static final String SERVICE_CONFIG_PATH = "./conf/wrapper.conf";

    /**
     * Get database config file, creating one if it does not exist.
     * 
     * @return
     * @throws IOException
     */
    public static File getDbConfigFile() throws IOException
    {
        File configFile = new File( new File( DatabaseLocator.DB_PATH ),
                DB_CONFIG_PATH );

        return getFile( configFile );
    }

    /**
     * Get startup config file, creating one if it does not exist.
     * 
     * @return
     * @throws IOException
     */
    public static File getGeneralConfigFile() throws IOException
    {
        File configFile = new File( new File( DatabaseLocator.DB_PATH ),
                GENERAL_CONFIG_PATH );

        return getFile( configFile );
    }

    /**
     * Get file that stores JVM startup arguments during development.
     * 
     * @return
     * @throws IOException
     */
    public static File getDevelopmentJvmArgsFile() throws IOException
    {
        return getFile( new File( JVM_ARGS_DEV_PATH ) );
    }

    /**
     * Get file that stores app startup arguments during development.
     * 
     * @return
     * @throws IOException
     */
    public static File getDevelopmentAppArgsFile() throws IOException
    {
        return getFile( new File( APP_ARGS_DEV_PATH ) );
    }

    /**
     * Get the service configuration file, this is where JVM args are changed
     * when running in production. This method is slightly different from the
     * above, it does not try to create a file if one does not exist. It assumes
     * that if there is no service config file, the environment we're running in
     * is not the production service env.
     * 
     * @return the service file or null if no file exists.
     * @throws IOException
     */
    public static File getServiceConfigFile() throws IOException
    {
        File configFile = new File( SERVICE_CONFIG_PATH );

        if ( !configFile.exists() )
        {
            return null;
        }

        return configFile;
    }

    public static File getBackupConfigFile() throws IOException
    {
        return getFile( new File( new File( DatabaseLocator.DB_PATH ),
                BACKUP_CONFIG_PATH ) );
    }

    public static File getBackupLogFile() throws IOException
    {
        return getFile( new File( new File( DatabaseLocator.DB_PATH ),
                BACKUP_LOG_PATH ) );
    }

    //
    // INTERNALS
    //

    private static File getFile( File file ) throws IOException
    {
        if ( !file.exists() && !file.createNewFile() )
        {
            throw new IllegalStateException( file.getAbsolutePath() );
        }

        return file;
    }

}
