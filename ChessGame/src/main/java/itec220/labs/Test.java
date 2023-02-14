package itec220.labs;

import java.util.Arrays;
import java.util.Scanner;

public class Test {
	public static final String[] columnLetters = {"A", "B", "C", "D", "E", "F", "G", "H"};

	public static void main(String[] args) {
		Scanner kb = new Scanner(System.in);
		Game game = new Game();
		do {
			System.out.println(game.getCopyOfCurrBoard());
			System.out.printf("It is %s's turn.\n", game.getCurrMove().name);
			System.out.println("Enter the space you want to move from, i.e C4: ");
			int[] moveFrom = formatInput(kb.nextLine());
			System.out.println("Enter the space you want to move to, i.e C5: ");
			int[] moveTo = formatInput(kb.nextLine());
			game.move(moveFrom[1]-1, moveFrom[0], moveTo[1]-1, moveTo[0]);
		}while(game.getCurrState() == GameState.IN_PROGRESS || game.getCurrState() == GameState.CHECK);
	}
	
	public static int[] formatInput(String input) {
		int[] temp = new int[2];
		String[] tempString = input.split("");
		if(tempString.length == 2) {
			temp[0] = Arrays.asList(columnLetters).indexOf(tempString[0].toUpperCase());
			temp[1] = Integer.parseInt(tempString[1]);
		} else {
			System.out.println("Invalid Input");
		}
		
		return temp;
	}
}
