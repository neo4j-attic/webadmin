package org.neo4j.webadmin.resources;

import java.io.File;

public interface StaticResourceCompiler
{

    public void addFile( StringBuilder builder, File file );

}
