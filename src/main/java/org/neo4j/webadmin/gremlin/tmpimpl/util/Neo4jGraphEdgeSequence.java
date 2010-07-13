package org.neo4j.webadmin.gremlin.tmpimpl.util;

import java.util.Iterator;

import org.neo4j.graphdb.Direction;
import org.neo4j.graphdb.Node;
import org.neo4j.graphdb.Relationship;
import org.neo4j.webadmin.gremlin.tmpimpl.Neo4jEdge;
import org.neo4j.webadmin.gremlin.tmpimpl.Neo4jGraphTemp;

import com.tinkerpop.blueprints.pgm.Edge;

/**
 * @author Marko A. Rodriguez (http://markorodriguez.com)
 */
public class Neo4jGraphEdgeSequence implements Iterator<Edge>, Iterable<Edge>
{

    private Neo4jGraphTemp graph;
    private Iterator<Node> nodes;
    private Iterator<Relationship> currentRelationships;
    private boolean complete = false;

    public Neo4jGraphEdgeSequence( final Iterable<Node> nodes,
            final Neo4jGraphTemp graph )
    {
        this.graph = graph;
        this.nodes = nodes.iterator();
        this.complete = this.goToNextEdge();

    }

    public Edge next()
    {
        Edge edge = new Neo4jEdge( currentRelationships.next(), this.graph );
        this.complete = this.goToNextEdge();
        return edge;
    }

    public boolean hasNext()
    {
        return !complete;
    }

    public void remove()
    {
        throw new UnsupportedOperationException();
    }

    private boolean goToNextEdge()
    {
        if ( this.currentRelationships == null
             || !this.currentRelationships.hasNext() )
        {
            if ( nodes.hasNext() )
            {
                this.currentRelationships = nodes.next().getRelationships(
                        Direction.OUTGOING ).iterator();
            }
            else
            {
                return true;
            }
        }

        if ( this.currentRelationships.hasNext() )
        {
            return false;
        }
        else
        {
            return this.goToNextEdge();
        }
    }

    public Iterator<Edge> iterator()
    {
        return this;
    }
}
