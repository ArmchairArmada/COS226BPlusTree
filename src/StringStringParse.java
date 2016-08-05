/**
 * Parses a String from a String or a String into a String...
 * 
 * I know this sounds silly, but it is so the BPTree can easily use Strings.
 * 
 * @author Nathan
 *
 */
public class StringStringParse implements StringParseInterface {

	/**
	 * {@inheritDoc}
	 */
	@Override
	public Object parseString(String s) {
		return s;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public String makeString(Object o) {
		return (String)o;
	}

}
