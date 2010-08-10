package org.neo4j.webadmin.properties;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import org.neo4j.rest.domain.Representation;

/**
 * Defines a property value, including stuff like default value,
 * prepending/appending stuff, widget to show the value with etc.
 * 
 * @author Jacob Hansson <jacob@voltvoodoo.com>
 * 
 */
public class ValueDefinition implements Representation
{

    public enum Widget
    {

        /**
         * Plain text input, this is the default.
         */
        TEXT,

        /**
         * A widget that switches between the default value and no value at all,
         * or between two default values.
         */
        TOGGLE,

        /**
         * A widget that shows a list of pre-defined values.
         */
        DROPDOWN
    }

    /**
     * Convinience method for converting a map into an arraylist of Map{value,
     * name}
     * 
     * @param values
     * @return
     */
    public static ArrayList<HashMap<String, String>> mapToValues(
            Map<String, String> values )
    {
        ArrayList<HashMap<String, String>> listValues = new ArrayList<HashMap<String, String>>();
        for ( String key : values.keySet() )
        {
            System.out.println( key );
            HashMap<String, String> value = new HashMap<String, String>();
            value.put( "name", key );
            value.put( "value", values.get( key ) );
            listValues.add( value );
        }
        return listValues;
    }

    /**
     * Convinience method for converting a list of values into an arraylist of
     * Map{value, name}. Name will be the same as values.
     * 
     * @param values
     * @return
     */
    public static ArrayList<HashMap<String, String>> itemsToValues(
            String... values )
    {
        ArrayList<HashMap<String, String>> listValues = new ArrayList<HashMap<String, String>>();
        for ( String value : values )
        {
            System.out.println( value );
            HashMap<String, String> mapValue = new HashMap<String, String>();
            mapValue.put( "name", value );
            mapValue.put( "value", value );
            listValues.add( mapValue );
        }
        return listValues;
    }

    private ArrayList<HashMap<String, String>> values;
    private String prepend;
    private String append;
    private Widget widget;

    //
    // CONSTRUCTORS
    //

    public ValueDefinition()
    {
        this( "", "" );
    }

    public ValueDefinition( String prepend, String append )
    {
        this( prepend, append, new HashMap<String, String>(), Widget.TEXT );
    }

    /**
     * Creates a toggle-widget that toggles between the value you define and no
     * value at all.
     * 
     * @param prepend
     * @param append
     * @param value
     */
    public ValueDefinition( String prepend, String append, String value )
    {
        this.prepend = prepend;
        this.values = itemsToValues( value );
        this.append = append;
        this.widget = Widget.TOGGLE;
    }

    /**
     * Creates a toggle-widget that toggles between two values.
     * 
     * @param prepend
     * @param append
     * @param value
     */
    public ValueDefinition( String prepend, String append, String firstValue,
            String secondValue )
    {
        this.prepend = prepend;
        this.values = itemsToValues( firstValue, secondValue );
        this.append = append;
        this.widget = Widget.TOGGLE;
    }

    /**
     * Creates a drop-down list.
     * 
     * @param prepend
     * @param append
     * @param values
     */
    public ValueDefinition( String prepend, String append,
            Map<String, String> values )
    {
        this( prepend, append, values, Widget.DROPDOWN );
    }

    public ValueDefinition( String prepend, String append,
            Map<String, String> values, Widget widget )
    {
        this.prepend = prepend;
        this.values = mapToValues( values );
        this.append = append;
        this.widget = widget;
    }

    //
    // PUBLIC
    //

    public Object serialize()
    {
        Map<String, Object> map = new HashMap<String, Object>();
        map.put( "widget", this.widget );

        if ( this.widget != Widget.TEXT )
        {
            map.put( "values", this.values );
        }

        return map;
    }

    public String getPrepend()
    {
        return this.prepend;
    }

    public String getAppend()
    {
        return this.append;
    }

    /**
     * Take a user-entered value, and convert it to whatever should be appended
     * to the config files.
     * 
     * @param value
     * @return
     */
    public String toFullValue( String value )
    {
        if ( this.widget == Widget.TOGGLE && value.length() == 0 )
        {
            return "";
        }

        return this.prepend + value + this.append;
    }

    /**
     * Take a full configuration value, and remove any append/prepend stuff that
     * we don't want the user to see.
     * 
     * @param value
     * @return
     */
    public String fromFullValue( String value )
    {
        return value.replaceFirst( "^" + this.prepend, "" ).replaceFirst(
                this.append + "$", "" );
    }
}
