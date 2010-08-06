package org.neo4j.webadmin.domain;

public class JvmArgumentRepresentation extends ServerPropertyRepresentation
{

    protected String prefix = "";

    //
    // CONSTRUCT
    //

    public JvmArgumentRepresentation( String key, String displayName,
            String value, String prefix )
    {
        super( key, displayName, value,
                ServerPropertyRepresentation.PropertyType.JVM_ARGUMENT );
        this.prefix = prefix;
    }

    //
    // PUBLIC
    //

    @Override
    public String getValue()
    {
        return prefix + super.getValue();
    }

    public String getValueWithoutPrefix()
    {
        return super.getValue();
    }

}
