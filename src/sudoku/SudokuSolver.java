package sudoku;

import java.util.Arrays;
import java.util.concurrent.TimeUnit;

public class SudokuSolver {
	public static long total_start;
	public static long timeout;
	
	public static boolean FC = false;
	public static boolean MRV = false;
	public static boolean DH = false;
	public static boolean LCV = false;
	public static boolean ACP = false;
	public static boolean MAC = false;
	
	public static void processTokens(String[] tokens)
	{
		//Turns on tokens based on user input
		for(String token: tokens)
		{
			token = token.toUpperCase();
						
			if(token.equals("FC"))
			{
				FC = true;
			}
			
			if(token.equals("MRV"))
			{
				MRV = true;
			}
			if(token.equals("DH"))
			{
				DH = true;
			}
			if(token.equals("LCV"))
			{
				LCV = true;
			}
			if(token.equals("ACP"))
			{
				ACP = true;
			}
			if(token.equals("MAC"))
			{
				MAC = true;
			}
		}				
	}
	
	/**
	 * 
	 * Used to finding statistics for the final report. Ignore.
	 * 
	 */
	public SudokuOutput sudokuSolverStats(SudokuFile sudokuBoard, String[] args) {
		//String inputFile = args[0];
		String outputFile = args[1];
		int secTimeOut = Integer.parseInt(args[2]);
		String[] tokens = Arrays.copyOfRange(args, 3, args.length);
		processTokens(tokens);

		total_start = System.currentTimeMillis();	
		timeout = TimeUnit.SECONDS.toMillis(secTimeOut);
				
		SudokuOutput output = new SudokuOutput(sudokuBoard,outputFile);
		output.BTstats();
		return output;	
	}
	
	public static void main(String[] args) {
		String inputFile = args[0];
		String outputFile = args[1];
		int secTimeOut = Integer.parseInt(args[2]);
		String[] tokens = Arrays.copyOfRange(args, 3, args.length);
		processTokens(tokens);

		total_start = System.currentTimeMillis();	
		timeout = TimeUnit.SECONDS.toMillis(secTimeOut);
			
		SudokuFile sudokuBoard = SudokuBoardReader.readFile(inputFile);
		
		SudokuOutput output = new SudokuOutput(sudokuBoard,outputFile);
		output.BTstats();
		
	}
	
}
