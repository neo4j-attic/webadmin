package org.neo4j.webadmin.backup;

import java.io.File;

public class BackupLog
{

    private File logFile;

    public BackupLog( File logFile )
    {
        this.logFile = logFile;
    }

}
