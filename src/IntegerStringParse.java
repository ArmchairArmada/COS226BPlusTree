/**
 * Parses an Integer from a String or creates a String from an Integer
 * 
 * @author Nathan
 *
 */
public class IntegerStringParse implements StringParseInterface {
	/**
	 * {@inheritDoc}
	 */
	@Override
	public Object parseString(String s) {
		return Integer.parseInt(s);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public String makeString(Object o) {
		return o.toString();
	}

}
