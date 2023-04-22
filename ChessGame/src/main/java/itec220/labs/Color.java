package itec220.labs;


/**
 * An enum that is used to determine a pieces color, as well as the current move
 * @author Donovan Horne
 *
 */
public enum Color {
	/** Enum for white pieces */
	WHITE("White"),
	/** Enum for black pieces */
	BLACK("Black");
	
	/** String version of each enum, i.e Black for BLACK */
	public final String name;
	
	/**
	 * Constructor for each color
	 * @param name the String of each color
	 */
	Color(String name){
		this.name = name;
	}
}
