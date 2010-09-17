package org.neo4j.webadmin.console;

import java.util.List;

/**
 * Data structure keeping a script to be evaluated and its result together.
 * 
 * @author Jacob Hansson <jacob@voltvoodoo.com>
 * 
 */
class ConsoleEvaluationJob
{

    protected String script;
    protected List<String> result;
    protected volatile boolean complete = false;

    public ConsoleEvaluationJob( String script )
    {
        this.script = script;
    }

    public boolean isComplete()
    {
        return complete;
    }

    public void setResult( List<String> result )
    {
        this.result = result;
        this.complete = true;
    }

    public List<String> getResult()
    {
        return this.result;
    }

    public String getScript()
    {
        return this.script;
    }

}
