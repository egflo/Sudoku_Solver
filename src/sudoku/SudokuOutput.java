package sudoku;


import java.io.PrintWriter;
import cspSolver.BTSolver;
import cspSolver.BTSolver.ConsistencyCheck;
import cspSolver.BTSolver.ValueSelectionHeuristic;
import cspSolver.BTSolver.VariableSelectionHeuristic;

public class SudokuOutput {
	
	private SudokuFile sf;
	private PrintWriter writer;
	public static String status = "Error \r\n";
	private BTSolver solverBT;
	
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
	
	private String printBoard(int[][] board, Boolean solution)
	{
		//Print Solution in tuple format
		String tuple = "SOLUTION=(";
				
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
	
	private int convertToSeconds(long millis)
	{
		//Convert milliseconds to seconds
		return (int)((millis/1000));
	}
	
	/**
	 *Sets the solver based on activated tokens 
	 * @param solver
	 */
	private void setTokens(BTSolver solver)
	{
				
		solver.setValueSelectionHeuristic(ValueSelectionHeuristic.None);
		
		//Consistency Checks
		if(SudokuSolver.FC)
		{
			solver.setConsistencyChecks(ConsistencyCheck.ForwardChecking);	
		}
		
		if(SudokuSolver.ACP)
		{
			solver.setConsistencyChecks(ConsistencyCheck.ArcConsistencyPreprocessor);
		}
		
		if(SudokuSolver.MAC)
		{
			solver.setConsistencyChecks(ConsistencyCheck.MaintainingArcConsistency);
		}
		
		//If still zero choose default
		if(solver.cChecksSize() == 0)
		{
			solver.setConsistencyChecks(ConsistencyCheck.None);
		}
		
		//Variable Selection Heuristic
		if(SudokuSolver.MRV)
		{
			solver.setVariableSelectionHeuristic(VariableSelectionHeuristic.MinimumRemainingValue);	
			
		}
		
		if(SudokuSolver.DH)
		{
			solver.setVariableSelectionHeuristic(VariableSelectionHeuristic.Degree);
		}
		
		if(solver.varHeuristicsSize() == 0)
		{
			solver.setVariableSelectionHeuristic(VariableSelectionHeuristic.None);
		}
		
		//Value Selection Heuristic
		if(SudokuSolver.LCV)
		{
			solver.setValueSelectionHeuristic(ValueSelectionHeuristic.LeastConstrainingValue);
		}
		
	}
	
	public BTSolver getSolver()
	{
		return solverBT;
	}
	
	public void BTstats()
	{
		/** Run stats on BT board and outputs to file */
		
		BTSolver solver = new BTSolver(sf);
		setTokens(solver);
	
		
		Thread t1 = new Thread(solver);
		try
		{
			t1.start();
			t1.join(SudokuSolver.timeout);
			if(t1.isAlive())
			{
				t1.interrupt();
			}
		}catch(InterruptedException e)
		{
			status = "Error \r\n";
			System.out.println("Error: " + e);
		}
		
		solverBT = solver;
		
		//Note: Either Works but covertToSeconds seems more correct
		//in sense of project guidelines
		
		//long totalStart = TimeUnit.MILLISECONDS.toSeconds(SudokuSolver.total_start);
		int totalStart = convertToSeconds(SudokuSolver.total_start);
		//long startSeconds = TimeUnit.MILLISECONDS.toSeconds(solver.startTime());
		int startSeconds = convertToSeconds(solver.startTime());
		//long doneSeconds = TimeUnit.MILLISECONDS.toSeconds(solver.endTime());
		int doneSeconds = convertToSeconds(solver.endTime());
		//long solutionDone = solver.getTimeTaken();
		int timeTaken = convertToSeconds(solver.getTimeTaken());
		int PREPROCESSING_START = convertToSeconds(solver.getPreprocessingStart());
		int PREPROCESSING_DONE = convertToSeconds(solver.getPreprocessingDone());
		
		writer.print("TOTAL_START=" + totalStart + "\r\n");
		writer.print("PREPROCESSING_START=" + PREPROCESSING_START +  "\r\n");
		writer.print("PREPROCESSING_DONE=" + PREPROCESSING_DONE +  "\r\n");
		writer.print("SEARCH_START=" + startSeconds +  "\r\n");
		writer.print("SEARCH_DONE=" + doneSeconds +  "\r\n");
		writer.print("SOLUTION_TIME=" + timeTaken +  "\r\n");
		writer.print("STATUS=" + status);
	
		
		if(solver.hasSolution())
		{
			SudokuFile solution = solver.getSolution();	
			int[][] board = solution.getBoard();
			writer.print(printBoard(board,true));
	
		}
		
		else 
		{
			SudokuFile solution = solver.getSolution();	
			int[][] board = solution.getBoard();
			writer.print(printBoard(board,false));
			
		}
		
		writer.print("COUNT_NODES=" + solver.getNumAssignments()+  "\r\n");
		writer.print("COUNT_DEADENDS=" + solver.getNumBacktracks()+  "\r\n");
		writer.close();
		
		//solver.printSolverStats();
	
	}

}
