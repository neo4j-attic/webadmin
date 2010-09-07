package org.neo4j.webadmin.backup;

public class BackupJobDescription
{

    private String name;
    private String path;
    private String cronExpression;

    private Boolean autoFoundation = false;

    public String getName()
    {
        return name;
    }

    public void setName( String name )
    {
        this.name = name;
    }

    public String getPath()
    {
        return path;
    }

    public void setPath( String path )
    {
        this.path = path;
    }

    public String getCronExpression()
    {
        return cronExpression;
    }

    public void setCronExpression( String cronExpression )
    {
        this.cronExpression = cronExpression;
    }

    public Boolean isAutoFoundation()
    {
        return autoFoundation;
    }

    public void setAutoFoundation( Boolean autoFoundation )
    {
        this.autoFoundation = autoFoundation;
    }
}
