package org.neo4j.webadmin.gremlin.tmpimpl.util;

import java.util.Iterator;

import org.neo4j.graphdb.Relationship;
import org.neo4j.webadmin.gremlin.tmpimpl.Neo4jEdge;
import org.neo4j.webadmin.gremlin.tmpimpl.Neo4jGraphTemp;

import com.tinkerpop.blueprints.pgm.Edge;

/**
 * @author Marko A. Rodriguez (http://markorodriguez.com)
 */
public class Neo4jEdgeSequence implements Iterator<Edge>, Iterable<Edge>
{

    Iterator<Relationship> relationships;
    Neo4jGraphTemp graph;

    public Neo4jEdgeSequence( final Iterable<Relationship> relationships,
            final Neo4jGraphTemp graph )
    {
        this.graph = graph;
        this.relationships = relationships.iterator();
    }

    public void remove() throws UnsupportedOperationException
    {
        throw new UnsupportedOperationException();
    }

    public Edge next()
    {
        return new Neo4jEdge( this.relationships.next(), this.graph );
    }

    public boolean hasNext()
    {
        return this.relationships.hasNext();
    }

    public Iterator<Edge> iterator()
    {
        return this;
    }
}
