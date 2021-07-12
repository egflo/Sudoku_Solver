package cspSolver;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;

import sudoku.Converter;
import sudoku.SudokuFile;
import sudoku.SudokuOutput;
import sudoku.SudokuSolver;
/**
 * Backtracking solver. 
 *
 */
public class BTSolver implements Runnable{

	//===============================================================================
	// Properties
	//===============================================================================

	private ConstraintNetwork network;
	private static Trail trail = Trail.getTrail();
	private boolean hasSolution = false;
	private SudokuFile sudokuGrid;

	private int numAssignments;
	private int numBacktracks;
	private long startTime;
	private long endTime;
	
	public enum VariableSelectionHeuristic 	{ None, MinimumRemainingValue, Degree };
	public enum ValueSelectionHeuristic 	{ None, LeastConstrainingValue };
	public enum ConsistencyCheck	{ None, ForwardChecking, MaintainingArcConsistency, ArcConsistencyPreprocessor};
	
	private List<VariableSelectionHeuristic> varHeuristics;
	private ValueSelectionHeuristic valHeuristics;
	private List<ConsistencyCheck> cChecks;
	
	private Variable currentVariable;
	private boolean preSearchFCstatus = false;
	
	//For ArcConsistency Preprocessor
	private boolean ACPDone = false;
	private long PREPROCESSING_START = 0;
	private long PREPROCESSING_DONE = 0;
	
	//===============================================================================
	// Constructors
	//===============================================================================
	

	public BTSolver(SudokuFile sf)
	{
		this.network = Converter.SudokuFileToConstraintNetwork(sf);
		this.sudokuGrid = sf;
		numAssignments = 0;
		numBacktracks = 0;
		
		this.varHeuristics = new ArrayList<VariableSelectionHeuristic>();
		this.cChecks = new ArrayList<ConsistencyCheck>();
		
	}

	//===============================================================================
	// Modifiers
	//===============================================================================
	
	public void setVariableSelectionHeuristic(VariableSelectionHeuristic vsh)
	{
		this.varHeuristics.add(vsh);
	}
	
	public void setValueSelectionHeuristic(ValueSelectionHeuristic vsh)
	{
		this.valHeuristics = vsh;
	}
	
	public void setConsistencyChecks(ConsistencyCheck cc)
	{
		this.cChecks.add(cc);
	}
	//===============================================================================
	// Accessors
	//===============================================================================

	/** 
	 * @return true if a solution has been found, false otherwise. 
	 */
	public boolean hasSolution()
	{
		return hasSolution;
	}
	
	public long startTime()
	{
		return startTime;
	}
	
	public long endTime()
	{
		return endTime;
	}
	
	public long getPreprocessingStart()
	{
		return PREPROCESSING_START;
	}
	
	public long getPreprocessingDone()
	{
		return PREPROCESSING_DONE;
	}
	
	public long getPreprocessingTime()
	{
		return PREPROCESSING_DONE - PREPROCESSING_START;
	}

	/**
	 * @return solution if a solution has been found, otherwise returns the unsolved puzzle.
	 */
	public SudokuFile getSolution()
	{
		return sudokuGrid;
	}

	public void printSolverStats()
	{
		System.out.println("Time taken:" + (endTime-startTime) + " ms");
		System.out.println("Number of assignments: " + numAssignments);
		System.out.println("Number of backtracks: " + numBacktracks);
	}

	/**
	 * 
	 * @return time required for the solver to attain in seconds
	 */
	public long getTimeTaken()
	{
		return (PREPROCESSING_DONE - PREPROCESSING_START) + (endTime-startTime);
	}

	public int getNumAssignments()
	{
		return numAssignments;
	}

	public int getNumBacktracks()
	{
		return numBacktracks;
	}

	public ConstraintNetwork getNetwork()
	{
		return network;
	}

	public int cChecksSize()
	{
		return cChecks.size();
	}
	
	public int varHeuristicsSize()
	{
		return varHeuristics.size();
	}
	
	//===============================================================================
	// Helper Methods
	//===============================================================================

	/**
	 * Checks whether the changes from the last time this method was called are consistent. 
	 * @return true if consistent, false otherwise
	 */
	private boolean checkConsistency()
	{
		boolean isConsistent = false;
		
		if(cChecks.size() > 1)
		{
			if(cChecks.contains(ConsistencyCheck.MaintainingArcConsistency) &&
					cChecks.contains(ConsistencyCheck.ForwardChecking))
			{
				isConsistent = forwardChecking();
				if(!isConsistent){return false;}
				isConsistent = arcConsistency();
				return isConsistent;
				
			}
			
			if(cChecks.contains(ConsistencyCheck.ArcConsistencyPreprocessor) &&
					cChecks.contains(ConsistencyCheck.ForwardChecking))
			{
				//ACP will be done before search
				isConsistent = forwardChecking();
				return isConsistent;			
			}		
			
		}
			
		ConsistencyCheck cCheck = cChecks.get(0);
		switch(cCheck)
		{
		case None: 				isConsistent = assignmentsCheck();
		break;
		case ForwardChecking: 	isConsistent = forwardChecking();
		break;
		case ArcConsistencyPreprocessor: isConsistent = assignmentsCheck();
		break;
		case MaintainingArcConsistency: isConsistent = arcConsistency();
		default: 				isConsistent = assignmentsCheck();
		break;
		}
		return isConsistent;
	}
	
	/**
	 * default consistency check. Ensures no two variables are assigned to the same value.
	 * @return true if consistent, false otherwise. 
	 */
	private boolean assignmentsCheck()
	{
		for(Variable v : network.getVariables())
		{
			if(v.isAssigned())
			{
				for(Variable vOther : network.getNeighborsOfVariable(v))
				{					
					if (v.getAssignment() == vOther.getAssignment())
					{
						return false;
					}
				}
			}
		}
		return true;
	}
	
	/**
	 * 
	 * Used for Forward Checking pre-Assigned Cells 
	 * 
	 */
	public List<Variable> getAssignedVariables()
	{
		Set<Variable> variables = new HashSet<Variable>();
		for(Variable v: network.getVariables())
		{
			if(v.isAssigned())
			{
				variables.add(v);
			}		
		}
		return new ArrayList<Variable>(variables);
	}
	
	private boolean preSearchFC()
	{	
		for(Variable v: getAssignedVariables())
		{		
			for(Variable vOther: network.getNeighborsOfVariable(v))
			{
				if(!vOther.isAssigned())
				{
					vOther.removeValueFromDomain(v.getAssignment());
					if(vOther.size() == 0)
					{
						preSearchFCstatus = true;
						return false;
					}
				}
						
			}			
		}
			
		preSearchFCstatus = true;	
		return true;
	}
	
	/**
	 * TODO: Implement forward checking. 
	 */
	private boolean forwardChecking()
	{							
  	    for(Variable vOther: network.getNeighborsOfVariable(currentVariable))
		{				
  	    	if(!vOther.isAssigned())
  	    	{
  	    		vOther.removeValueFromDomain(currentVariable.getAssignment());
  	    		
  	    		if(vOther.size() == 0)
  	    		{
  	    			return false;
  	    		}
  	    	}  	
  	    	else 
			{
  	    		if (currentVariable.getAssignment() == vOther.getAssignment())
  	    		{
  	    			return false;
  	    		}
			}
		}
  	    	    
		return true;	
	}
	
	/**
	 * Only called when ACP is the only consistency check
	 * once called it reverts to default consistency check
	 */
	private boolean arcConsistencyPreprocessor()
	{
		boolean isConsistent = false;
				
		PREPROCESSING_START = System.currentTimeMillis();
		isConsistent = arcConsistency();
		PREPROCESSING_DONE = System.currentTimeMillis();	
		ACPDone = true; //Set to true means perform only once (in pre-search)
					
		return isConsistent;
	}
	
	/**
	 * TODO: Implement Maintaining Arc Consistency.
	 */
	private boolean arcConsistency()
	{		
		for(Variable var : network.getVariables())
		{
			if(var.isAssigned())
			{
				Integer assignment = var.getAssignment();
				for (Variable otherVar : network.getNeighborsOfVariable(var))
				{
				
					if (otherVar.size() == 1 && otherVar.getAssignment() ==  assignment)
					{
						return false;
					}
					
					if(otherVar.isAssigned())
						continue;
					
					otherVar.removeValueFromDomain(assignment);
					
					if(otherVar.size() == 0)
					{
						return false;
					}
			
				}
			}
		}
		
		return true;
	}
	
	/**
	 * Selects the next variable to check.
	 * @return next variable to check. null if there are no more variables to check. 
	 */
	private Variable selectNextVariable()
	{
		Variable next = null;
		
		if(varHeuristics.size() > 1)
		{
			if(varHeuristics.contains(VariableSelectionHeuristic.MinimumRemainingValue) &&
					varHeuristics.contains(VariableSelectionHeuristic.Degree))
			{
	
				next = getMRVwithDH();
				return next;
				
			}
		
		}
		
		VariableSelectionHeuristic varHeuristic = varHeuristics.get(0);
		switch(varHeuristic)
		{
		case None: 					next = getfirstUnassignedVariable();
		break;
		case MinimumRemainingValue: next = getMRV();
		break;
		case Degree:				next = getDegree();
		break;
		default:					next = getfirstUnassignedVariable();
		break;
		}
		return next;
	}
	
	/**
	 * default next variable selection heuristic. Selects the first unassigned variable. 
	 * @return first unassigned variable. null if no variables are unassigned. 
	 */
	private Variable getfirstUnassignedVariable()
	{
		for(Variable v : network.getVariables())
		{
			if(!v.isAssigned())
			{
				return v;
			}
		}
		return null;
	}
	
	private Variable getMRVwithDH()
	{
		
		Variable variableMRV = null;
		int remainingValues = Integer.MAX_VALUE;
		
		Set<Variable> canidates = new HashSet<Variable>();
			
		for(Variable v: network.getVariables())
		{
			if(!v.isAssigned())
			{
				if(v.size() <= remainingValues)
				{
					variableMRV = v;
					remainingValues = v.size();
			
					canidates.add(variableMRV);
					
				}					
			}
		}
		
		//Remove any Variables that greater than remaining Values
		Set<Variable> revisedCanidates = new HashSet<Variable>();
		for(Variable c: canidates)
		{
			if(c.size() <= remainingValues)
			{
				revisedCanidates.add(c);
			}
		}
	
			
		//Perform DH
		Variable DH = null;
		int largestUnassigned = Integer.MIN_VALUE;
		
		for(Variable v: revisedCanidates)
		{
			int unassigned = getNumberOfUassignedNeigbors(v);
		
			if(unassigned > largestUnassigned)
			{
				DH = v;
				largestUnassigned = unassigned;
			}
				
			else if(unassigned == largestUnassigned)
			{
				//Tie-breakers
				//Can Activate if needed otherwise keep commented
				//DH = defaultTieBreaker(v,DH);
				//largestUnassigned =  getNumberOfUassignedNeigbors(DH);
				//DH = lexicographicallyTieBreaker(v,DH);
				//largestUnassigned = DH.size();
				
			}
			
		}
		
		return DH;		
	}
	
	/**
	 * 
	 * Returns a variable based on row and column location
	 * if both are equal return random
	 */
	public Variable defaultTieBreaker(Variable a, Variable b)
	{
		if(a.col() < b.col())
		{
			return a;
		}
		
		else if(b.col() < a.col())
		{
			return b;
		}
		
		else if(a.row() < b.row())
		{
		
			return a;

		}
		else if(b.row() < a.row())
		{
			return b;
			
		}
		
		//Randomly Chose a Variable
		else
		{
			Random rand = new Random();
			int index = rand.nextInt(1); 
			
			Variable[] variables = {a,b};
			Variable choice = variables[index];
			return choice;
		}
		
	}
	
	public Variable lexicographicallyTieBreaker(Variable a, Variable b)
	{
		int result = a.getName().compareTo(b.toString());
		
		if(result > 0)
		{
			return a;
		}
		
		else if(result < 0)
		{
			return b;
		}
		
		else
		{
			Random rand = new Random();
			int index = rand.nextInt(1); 
			
			Variable[] variables = {a,b};
			Variable choice = variables[index];
			return choice;
			
		}		
	}
	
	/**
	 * TODO: Implement MRV heuristic
	 * @return variable with minimum remaining values that isn't assigned, null if all variables are assigned. 
	 */
	private Variable getMRV()
	{
		Variable MRV = null;
		int remainingValues = Integer.MAX_VALUE;
			
		for(Variable v: network.getVariables())
		{
			if(!v.isAssigned())
			{
				if(v.size() < remainingValues)
				{
					MRV = v;
					remainingValues = v.getDomain().size();
				}
				
				else if(v.size() == remainingValues)
				{
					//Can Activate if needed otherwise keep commented
					//MRV = defaultTieBreaker(v,MRV);
					//remainingValues = MRV.size();
					MRV = lexicographicallyTieBreaker(v,MRV);
					remainingValues = MRV.size();
				}			
			}		
		}
	
		return MRV;
	}
	
	
	/**
	 * 
	 * Used for finding degree heuristic 
	 * Returns the number of unassigned neighbors for
	 * parameter Variable v
	 * 
	 */
	int getNumberOfUassignedNeigbors(Variable v)
	{
		int uassignedCount = 0;
		for(Variable vOther: network.getNeighborsOfVariable(v))
		{
			if(!vOther.isAssigned())
			{
				uassignedCount++;
			}
		}
		
		return uassignedCount;
	}
	
	/**
	 * TODO: Implement Degree heuristic
	 * @return variable constrained by the most unassigned variables, null if all variables are assigned.
	 */
	private Variable getDegree()
	{
		Variable DH = null;
		int largestUnassigned = Integer.MIN_VALUE;
			
		for(Variable v : network.getVariables())
		{					
			if(!v.isAssigned())
			{
				int unassigned =  getNumberOfUassignedNeigbors(v);
				
				if(unassigned > largestUnassigned)
				{
					DH = v;
					largestUnassigned = unassigned;
				}
				
				else if(unassigned == largestUnassigned)
				{
					//Can Activate if needed otherwise keep commented
					DH = defaultTieBreaker(v,DH);
					//largestUnassigned =  getNumberOfUassignedNeigbors(DH);
					//DH = lexicographicallyTieBreaker(v,DH);
					
				}		
			}
		}
		
		return DH;		
	}
	
	/**
	 * Value Selection Heuristics. Orders the values in the domain of the variable 
	 * passed as a parameter and returns them as a list.
	 * @return List of values in the domain of a variable in a specified order. 
	 */
	public List<Integer> getNextValues(Variable v)
	{
		List<Integer> orderedValues;
		switch(valHeuristics)
		{
		case None: 						orderedValues = getValuesInOrder(v);
		break;
		case LeastConstrainingValue: 	orderedValues = getValuesLCVOrder(v);
		break;
		default:						orderedValues = getValuesInOrder(v);
		break;
		}
		return orderedValues;
	}
	
	/**
	 * Default value ordering. 
	 * @param v Variable whose values need to be ordered
	 * @return values ordered by lowest to highest. 
	 */
	public List<Integer> getValuesInOrder(Variable v)
	{
		List<Integer> values = v.getDomain().getValues();
		
		Comparator<Integer> valueComparator = new Comparator<Integer>(){

			@Override
			public int compare(Integer i1, Integer i2) {
				return i1.compareTo(i2);
			}
		};
		Collections.sort(values, valueComparator);		
		return values;
	}
	
	
	/**
	 * TODO: LCV heuristic
	 */
	public List<Integer> getValuesLCVOrder(Variable v)
	{			
		Map<Integer,Integer> LCV = new HashMap<Integer,Integer>();
		
		//trail.placeBreadCrumb();
		List<Integer> values = v.getDomain().getValues();
			
		for(Integer value: values)
		{
			LCV.put(value, 0);
								
			for(Variable vOther : network.getNeighborsOfVariable(v))
			{				
				if(!vOther.isAssigned())
			    {						
					trail.placeBreadCrumb();
				
					vOther.removeValueFromDomain(value);	
							
					if(vOther.size() != 0)
					{
						LCV.put(value, LCV.get(value) + vOther.size());
					
					}					
					trail.undo();
				}			
			}					
		}
			
		//trail.undo();
		
		//Turn HashMap into List<VariableOrder> for sorting
		List<ValueOrder> ListLCV = new ArrayList<ValueOrder>();
		for (Integer key : LCV.keySet()) 
		{
			Integer count = LCV.get(key);
			ListLCV.add(new ValueOrder(key,count));    
		}
				
		//Sort List to least constraining to most constraining values
		Collections.sort(ListLCV);
				
		//Get values from sorted list
		List<Integer> LCVOrdered = new ArrayList<Integer>();
		for(ValueOrder order: ListLCV)
		{
			LCVOrdered.add(order.getValue());
		}
		
		return LCVOrdered;
	}
	/**
	 * Called when solver finds a solution
	 */
	private void success()
	{
		hasSolution = true;
		SudokuOutput.status = "success \r\n";
		sudokuGrid = Converter.ConstraintNetworkToSudokuFile(network, sudokuGrid.getN(), sudokuGrid.getP(), sudokuGrid.getQ());
	}

	//===============================================================================
	// Solver
	//===============================================================================

	/**
	 * Method to start the solver
	 */
	public void solve()
	{
		startTime = System.currentTimeMillis();
		try {
			solve(0);
		}catch (VariableSelectionException e)
		{
			SudokuOutput.status = "error \r\n";
			System.out.println("error with variable selection heuristic.");
	
		}
		endTime = System.currentTimeMillis();
		Trail.clearTrail();
	}

	/**
	 * Solver
	 * @param level How deep the solver is in its recursion. 
	 * @throws VariableSelectionException 
	 */

	private void solve(int level) throws VariableSelectionException
	{
		long TOTAL_START = SudokuSolver.total_start;
		long CURRENT_TIME = System.currentTimeMillis();
		long TIMEOUT = SudokuSolver.timeout;
		long PREPROCESSING_TIME = getPreprocessingTime();
		
		
		//if((currentTime-totalStart) > timeout)
		if(PREPROCESSING_TIME + (CURRENT_TIME - TOTAL_START) > TIMEOUT)
		{//Check if time has not exceeded timeout.
			SudokuOutput.status = "timeout \r\n";
			return;
		}
		
		if(!Thread.currentThread().isInterrupted())
		
		{//Check if assignment is completed
			if(hasSolution)
			{
				SudokuOutput.status = "success \r\n";
				return;
			}

			//Select unassigned variable
			Variable v = selectNextVariable();	
			
		

			//check if the assignment is complete
			if(v == null)
			{
				for(Variable var : network.getVariables())
				{
					if(!var.isAssigned())
					{
						SudokuOutput.status = "error \r\n";
						throw new VariableSelectionException("Something happened with the variable selection heuristic");
					}
				}
				success();
				return;
			}
			
			//Perform Only if ACP token is activated otherwise ignore
			if(cChecks.contains(ConsistencyCheck.ArcConsistencyPreprocessor) &&  !ACPDone)
			{
				arcConsistencyPreprocessor();
				
			}
			
			//Perform FC on existing filled cells then turn off
			if(cChecks.contains(ConsistencyCheck.ForwardChecking)&& !preSearchFCstatus )
			{
				preSearchFC();
				
			}

			for(Integer i : getNextValues(v))
			{
			
				trail.placeBreadCrumb();

				//check a value
				v.updateDomain(new Domain(i));
				currentVariable = v;
				
				numAssignments++;
				boolean isConsistent = checkConsistency();
				
				//move to the next assignment
				if(isConsistent)
				{		
					solve(level + 1);
				}

				//if this assignment failed at any stage, backtrack
				if(!hasSolution)
				{
					trail.undo();
					numBacktracks++;
				}
				
				else
				{
					//Note: Might change but since either no solution
					//or solution result in success status.
					SudokuOutput.status = "success \r\n";
					return;
				}
			}	
		}	
	}

	@Override
	public void run() {
		solve();
	}
}
