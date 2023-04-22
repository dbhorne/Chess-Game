package itec220.labs;


/**
 * Purpose:	An enum that is used to determine a pieces color, as well as the current move
 * Date:	4/20/2023
 * @author Donovan Horne
 *
 */
public enum Color {
	WHITE("White"), BLACK("Black");
	
	public final String name;
	
	/**
	 * Constructor for each color
	 * @param name the String of each color
	 */
	Color(String name){
		this.name = name;
	}
}
