package sudoku;

import java.util.concurrent.TimeUnit;

public class SudokuSolver {
	public static long total_start;
	public static long timeout;

	public static void main(String[] args) {
		String inputFile = args[0];
		String outputFile = args[1];
		int secTimeOut = Integer.parseInt(args[2]);

		total_start = System.currentTimeMillis();	
		timeout = TimeUnit.SECONDS.toMillis(secTimeOut);
			
		SudokuFile sudokuBoard = SudokuBoardReader.readFile(inputFile);
		
		SudokuOutput output = new SudokuOutput(sudokuBoard,outputFile);
		output.BTstats();
		
	}

}
