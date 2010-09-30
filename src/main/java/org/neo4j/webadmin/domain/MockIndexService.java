package org.neo4j.webadmin.domain;

import org.neo4j.graphdb.Node;
import org.neo4j.index.IndexHits;
import org.neo4j.index.IndexService;

/**
 * Used to make it possible to instantiate gremlin-wrapped neo4j databases when
 * using a remote database. This is because indexing is not yet implemented in
 * webadmin for remote databases.
 * 
 * @author Jacob Hansson <jacob@voltvoodoo.com>
 * 
 */
public class MockIndexService implements IndexService
{

    public IndexHits<Node> getNodes( String arg0, Object arg1 )
    {
        // TODO Auto-generated method stub
        return null;
    }

    public Node getSingleNode( String arg0, Object arg1 )
    {
        // TODO Auto-generated method stub
        return null;
    }

    public void index( Node arg0, String arg1, Object arg2 )
    {
        // TODO Auto-generated method stub

    }

    public void removeIndex( String arg0 )
    {
        // TODO Auto-generated method stub

    }

    public void removeIndex( Node arg0, String arg1 )
    {
        // TODO Auto-generated method stub

    }

    public void removeIndex( Node arg0, String arg1, Object arg2 )
    {
        // TODO Auto-generated method stub

    }

    public void shutdown()
    {
        // TODO Auto-generated method stub

    }

}
