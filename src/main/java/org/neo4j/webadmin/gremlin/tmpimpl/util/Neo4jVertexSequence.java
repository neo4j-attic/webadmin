package org.neo4j.webadmin.gremlin.tmpimpl.util;

import java.util.Iterator;

import org.neo4j.graphdb.Node;
import org.neo4j.webadmin.gremlin.tmpimpl.Neo4jGraphTemp;
import org.neo4j.webadmin.gremlin.tmpimpl.Neo4jVertex;

import com.tinkerpop.blueprints.pgm.Vertex;

/**
 * @author Marko A. Rodriguez (http://markorodriguez.com)
 */
public class Neo4jVertexSequence implements Iterator<Vertex>, Iterable<Vertex>
{

    Iterator<Node> nodes;
    Neo4jGraphTemp graph;

    public Neo4jVertexSequence( final Iterable<Node> nodes,
            final Neo4jGraphTemp graph )
    {
        this.graph = graph;
        this.nodes = nodes.iterator();
    }

    public void remove() throws UnsupportedOperationException
    {
        throw new UnsupportedOperationException();
    }

    public Vertex next()
    {
        return new Neo4jVertex( this.nodes.next(), this.graph );
    }

    public boolean hasNext()
    {
        return this.nodes.hasNext();
    }

    public Iterator<Vertex> iterator()
    {
        return this;
    }
}
