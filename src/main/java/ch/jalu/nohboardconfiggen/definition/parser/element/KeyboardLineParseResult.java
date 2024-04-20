package ch.jalu.nohboardconfiggen.definition.parser.element;

/**
 * Interface for a parsed result of a keyboard line in the definition file.
 * A keyboard line is a line in the "Keys" section. It is one of the following:
 * <ul>
 *   <li>{@link KeyLine} defining an actual keyboard key</li>
 *   <li>{@link AttributeList} for configuring the current keyboard row</li>
 *   <li>{@link KeyboardRowEnd} to create a new keyboard row</li>
 * </ul>
 */
public interface KeyboardLineParseResult {

}
