package sudoku;


import java.io.PrintWriter;
import cspSolver.BTSolver;
import cspSolver.BTSolver.ConsistencyCheck;
import cspSolver.BTSolver.ValueSelectionHeuristic;
import cspSolver.BTSolver.VariableSelectionHeuristic;

public class SudokuOutput {
	
	private SudokuFile sf;
	private PrintWriter writer;
	public static String status;
	
	public SudokuOutput(SudokuFile sudokuBoard, String outputName)
	{
		this.sf = sudokuBoard;
		
		try 
		{
			writer = new PrintWriter(outputName, "UTF-8");
		} 
		catch (Exception e) 
		{
			System.err.println("Error: Output File not found");
			
		}
	
	}
	
	public String printBoard(int[][] board, Boolean solution)
	{
		String tuple = "(";
		int c = 0;
		for (int j = 0; j< board[0].length; j++)
		{
			for (int i = 0; i< board.length; i++)
		     {
				if(solution)
				{
					 if(c==0)
			    	 {
			    		 tuple += board[j][i];
			    		 c++;
			    	 }	    	 
			    	 else
			    	 {
			    		 tuple += ","+ board[j][i];
			    	 }	
				}
				
				else
				{
					if(c==0)
					{
						tuple += "0";
						c++;
					}
					else
					{
						tuple += ",0";
						
					}
				} 	
		     }
		}	
		tuple += ") \r\n";
		
		return tuple;
		
	}
	
	public int convertToSeconds(long millis)
	{
		//Convert milliseconds to seconds
		return (int)((millis/1000)%60);
	}
	
	public void BTstats()
	{
		/** Run stats on BT board */
		
		BTSolver solver = new BTSolver(sf);
		
		solver.setConsistencyChecks(ConsistencyCheck.None);
		solver.setValueSelectionHeuristic(ValueSelectionHeuristic.None);
		solver.setVariableSelectionHeuristic(VariableSelectionHeuristic.None);
		
		Thread t1 = new Thread(solver);
		try
		{
			t1.start();
			t1.join(60000);
			if(t1.isAlive())
			{
				t1.interrupt();
			}
		}catch(InterruptedException e)
		{
			System.out.println("Error: " + e);
			status = "Error \r\n";
		}
		
		//Note: Either Works but covertToSeconds seems more correct
		//in sense of project guidelines
		
		//long totalStart = TimeUnit.MILLISECONDS.toSeconds(SudokuSolver.total_start);
		int totalStart = convertToSeconds(SudokuSolver.total_start);
		//long startSeconds = TimeUnit.MILLISECONDS.toSeconds(solver.startTime());
		int startSeconds = convertToSeconds(solver.startTime());
		//long doneSeconds = TimeUnit.MILLISECONDS.toSeconds(solver.endTime());
		int doneSeconds = convertToSeconds(solver.endTime());
		//long solutionDone = solver.getTimeTaken();
		int solutionDone = convertToSeconds(solver.getTimeTaken());
		
		writer.print("TOTAL_START=" + totalStart + "\r\n");
		writer.print("PREPROCESSING_START=" + 0 +  "\r\n");
		writer.print("PREPROCESSING_DONE=" + 0 +  "\r\n");
		writer.print("SEARCH_START=" + startSeconds +  "\r\n");
		writer.print("SEARCH_DONE=" + doneSeconds +  "\r\n");
		writer.print("SOLUTION_TIME=" + solutionDone +  "\r\n");
		writer.print("STATUS=" + status);
		
		
		if(solver.hasSolution())
		{
			SudokuFile solution = solver.getSolution();	
			int[][] board = solution.getBoard();
			writer.println(printBoard(board,true));
	
		}
		
		else 
		{
			SudokuFile solution = solver.getSolution();	
			int[][] board = solution.getBoard();
			writer.println(printBoard(board,false));
			
		}
		
		writer.print("COUNT_NODES=" + solver.getNumAssignments()+  "\r\n");
		writer.print("COUNT_DEADENDS=" + solver.getNumBacktracks()+  "\r\n");
		writer.close();
	
	}

}
