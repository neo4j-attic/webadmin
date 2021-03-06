package org.neo4j.webadmin.utils;

import java.io.File;

/**
 * Get info on the current platform we are running.
 * 
 * @author Jacob Hansson <jacob@voltvoodoo.com>
 * 
 */
public class PlatformUtils
{

    public static final String SERVER_JVM_FOLDER = "bin/server";

    /**
     * This is.. yeah. It's used to determine whether to use *.bat scripts or
     * not.
     * 
     * Based on Suns platform-checker, that lists the following values for
     * os.name for windows:
     * <ul>
     * <li>Windows 2000</li>
     * <li>Windows 7</li>
     * <li>Windows 95</li>
     * <li>Windows 98</li>
     * <li>Windows NT</li>
     * <li>Windows Vista</li>
     * <li>Windows XP</li>
     * </ul>
     * 
     * @return
     */
    public static boolean isWindows()
    {
        return System.getProperty( "os.name" ).toLowerCase().startsWith(
                "windows" );
    }

    /**
     * Determine if we are running in development mode, or as a production
     * service. This will affect where certain config files are found and how
     * actions like restarting the jvm are performed.
     * 
     * @return
     */
    public static boolean isProductionMode()
    {
        return !System.getProperty( "org.neo4j.webadmin.developmentmode" ).toLowerCase().equals(
                "true" );
    }

    /**
     * Check if the current environment has jvm server mode available.
     * 
     * @return
     */
    public static boolean jvmServerModeIsAvailable()
    {
        return new File( new File( System.getProperty( "java.home" ) ),
                SERVER_JVM_FOLDER ).exists();
    }
}
