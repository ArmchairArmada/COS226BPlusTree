/**
 * Interface for converting a data object into a string or extracting data from
 * a string.
 * 
 * @author Nathan
 *
 */
public interface StringParseInterface {
	/**
	 * For making an Object from a String.
	 * 
	 * @param s  String to get object data from.
	 * @return   Object created from that string.
	 */
	public Object parseString(String s);
	
	/**
	 * For making a String from an Object.
	 * 
	 * @param o  Object that will be created from string.
	 * @return   A String representing the object's data.
	 */
	public String makeString(Object o);
}
