package scripts;

import java.io.BufferedReader;
import java.io.FileNotFoundException;

import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;

import java.io.Reader;
import java.util.ArrayList;

import cspSolver.BTSolver;
import sudoku.SudokuBoardGenerator;
import sudoku.SudokuBoardReader;
import sudoku.SudokuFile;
import sudoku.SudokuOutput;
import sudoku.SudokuSolver;

public class ProjectStats {
	
	public static double findSTD(ArrayList<Long> numbers, double avg)
	{
		double sd = 0;
		for (int i=0; i<numbers.size();i++)
		{
		    {
		    	sd += ((numbers.get(i) - avg)*(numbers.get(i) - avg)) / (numbers.size() - 1);
		    }
		}
		
		return Math.sqrt(sd);
	}
	
	public static void runPart2()
	{
		//Try different combinations
		ArrayList<String[]> s = new ArrayList<String[]>();
		String[] arguments = {"input.txt","output.txt","3600","FC","MRV","LCV","DH"};
		s.add(arguments);
		String[] arguments1 = {"input.txt","output.txt","3600","FC"};
		s.add(arguments1);
		String[] arguments2 = {"input.txt","output.txt","3600","MRV"};
		s.add(arguments2);
		String[] arguments3 = {"input.txt","output.txt","3600","LCV"};
		s.add(arguments3);
		String[] arguments4 = {"input.txt","output.txt","3600","DH"};
		s.add(arguments4);
		String[] arguments5 = {"input.txt","output.txt","3600","BT"};
		s.add(arguments5);
		
		for(String[] arg: s)
		{
			String[] index = arg;
			part2(index);
			
		}
			
	}
 
	public static void part2(String[] params)
	{
		// TODO Auto-generated method stub
		String filePath = "PHnames.txt";
		String filePath2 = "ExampleSudokuFiles/";
				
		try (Reader reader = new FileReader(filePath)) 
		{
			try(BufferedReader br = new BufferedReader(reader)){
				String line;
				FileWriter fout = new FileWriter("part2.txt", true);;
				long timeTaken = 0;
				int assignments = 0;
				
				while((line = br.readLine()) != null)
				{	
					
					System.out.println(line);
						
					SudokuFile sf = SudokuBoardReader.readFile(filePath2+line);
				
					SudokuSolver sv = new SudokuSolver();
					SudokuOutput out = sv.sudokuSolverStats(sf, params);
						
					BTSolver solver = out.getSolver();
						
			
					fout.append(line + System.getProperty("line.separator"));
					fout.append("-------------------------------"+ System.getProperty("line.separator"));	
					
					String tokens = "";
					for(String param: params)
					{
						tokens += param + " ";
					}
					
										
					fout.append("Input :" + tokens + System.getProperty("line.separator"));
					fout.append("Assignments: "+ solver.getNumAssignments() + System.getProperty("line.separator"));
					fout.append("Time Taken: " + solver.getTimeTaken() + System.getProperty("line.separator"));
					fout.append("Solution: "+ solver.hasSolution() + System.getProperty("line.separator"));
					//fout.append("Status: " + out.status);
					fout.append(System.getProperty("line.separator"));
					
					timeTaken += solver.getTimeTaken();
					assignments += solver.getNumAssignments();
					
					fout.flush();
								
					
				}
				
				
				fout.append(System.getProperty("line.separator"));
				fout.append("Overall Stats" + System.getProperty("line.separator"));
				fout.append("------------------------------------" + System.getProperty("line.separator"));
				fout.append("Overall Assignments: " + assignments + System.getProperty("line.separator"));
				fout.append("Overall Time Taken: " + timeTaken + System.getProperty("line.separator"));
				fout.append(System.getProperty("line.separator"));
				
				fout.append("Averages Stats" + System.getProperty("line.separator"));
				fout.append("------------------------------------" + System.getProperty("line.separator"));
				fout.append("Average Assignments: " + (double)assignments/5 + System.getProperty("line.separator"));
				fout.append("Average Time Taken: " + (double)timeTaken/5 + System.getProperty("line.separator"));
				fout.append(System.getProperty("line.separator"));
				fout.close();	
														
			}
			
			
			
				
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			
	}
		
		
	
	@SuppressWarnings("static-access")
	public static void part3() {
		// TODO Auto-generated method stub
		String filePath = "GenerateBoards.txt";
		
		try (Reader reader = new FileReader(filePath)) {
			try(BufferedReader br = new BufferedReader(reader)){
				String line;
				while((line = br.readLine()) != null)
				{	
					String[] lineParts = line.split("\\s+");
					int M = Integer.parseInt(lineParts[0]);
					int N = Integer.parseInt(lineParts[1]);
					int P = Integer.parseInt(lineParts[2]);
					int Q = Integer.parseInt(lineParts[3]);
					
				
					System.out.println(M + " " + N + " " + P +" " + Q);
			
					FileWriter fout = new FileWriter("part3.txt", true);
					fout.append(M + " " + N + " " + P +" " + Q + System.getProperty("line.separator"));
					fout.append("-------------------------------"+ System.getProperty("line.separator"));			
					int count = 0;
					int solved = 0;
					long timeTaken = 0;
					int assignments = 0;
					ArrayList<Long> times = new ArrayList<Long>();
					
					while(count != 10)
					{
						count++;
						fout.append("# "+ count + System.getProperty("line.separator"));
						
						String[] arguments = {"input.txt","output.txt","100","FC","MRV","MAC"};
						SudokuFile sf = SudokuBoardGenerator.generateBoard(N, P, Q, M);
						SudokuSolver sv = new SudokuSolver();
						SudokuOutput out = sv.sudokuSolverStats(sf, arguments);
						
						BTSolver solver = out.getSolver();
						
						
						fout.append("Assignments: "+ solver.getNumAssignments() + System.getProperty("line.separator"));
						fout.append("Time Taken: " + solver.getTimeTaken() + System.getProperty("line.separator"));
						fout.append("Solution: "+ solver.hasSolution() + System.getProperty("line.separator"));
						fout.append(System.getProperty("line.separator"));
						
						if(!out.status.contains("timeout"))
						{
							solved++;
						}
						timeTaken += solver.getTimeTaken();
						assignments += solver.getNumAssignments();
						times.add(solver.getTimeTaken());
													
					}
					
					fout.append(System.getProperty("line.separator"));
					fout.append("Overall Stats" + System.getProperty("line.separator"));
					fout.append("------------------------------------" + System.getProperty("line.separator"));
					fout.append("Overall Assignments: " + assignments + System.getProperty("line.separator"));
					fout.append("Overall Time Taken: " + timeTaken + System.getProperty("line.separator"));
					fout.append("Overall Solvable: " + solved + System.getProperty("line.separator"));
					fout.append(System.getProperty("line.separator"));
					
					fout.append("Averages Stats" + System.getProperty("line.separator"));
					fout.append("------------------------------------" + System.getProperty("line.separator"));
					fout.append("Average Assignments: " + (double)assignments/10 + System.getProperty("line.separator"));
					fout.append("Average Time Taken: " + (double)timeTaken/10 + System.getProperty("line.separator"));
					
					if(solved == 10)
					{
						fout.append("Std Dev Time: " + findSTD(times,(double)solved/10) + System.getProperty("line.separator"));
					}
					else
					{
						fout.append("Std Dev Time: 0" + System.getProperty("line.separator"));
					}
				
					fout.append("Average Solvable: " + (double)solved/10 + System.getProperty("line.separator"));
					fout.append(System.getProperty("line.separator"));
					fout.close();
				}
				
				
				br.close();
				
			}
			
			reader.close();

	} catch (FileNotFoundException e) {
		// TODO Auto-generated catch block
		e.printStackTrace();
	} catch (IOException e) {
		// TODO Auto-generated catch block
		e.printStackTrace();
	}
		
	}
	
	public static void part4()
	{
		String filePath = "GenerateBoardsPart5.txt";
		
		try (Reader reader = new FileReader(filePath)) {
			try(BufferedReader br = new BufferedReader(reader)){
				String line;
				while((line = br.readLine()) != null)
				{	
					String[] lineParts = line.split("\\s+");
					int N = Integer.parseInt(lineParts[0]);
					int P = Integer.parseInt(lineParts[1]);
					int Q = Integer.parseInt(lineParts[2]);
					double R = Double.parseDouble(lineParts[3]);
					
				    //int M = (int) Math.round((N*N) * 0.247);
					int M = (int) Math.round((N*N) * R);
				
					System.out.println(M + " " + N + " " + P +" " + Q);
			
					FileWriter fout = new FileWriter("part5.txt", true);
					fout.append(M + " " + N + " " + P +" " + Q + System.getProperty("line.separator"));
					fout.append("-------------------------------"+ System.getProperty("line.separator"));			
					int count = 0;
					int solved = 0;
					long timeTaken = 0;
					int assignments = 0;
					ArrayList<Long> times = new ArrayList<Long>();
					
					while(count != 10)
					{
						count++;
						fout.append("# "+ count + System.getProperty("line.separator"));
						
						String[] arguments = {"input.txt","output.txt","300","FC","MRV","MAC"};
						SudokuFile sf = SudokuBoardGenerator.generateBoard(N, P, Q, M);
						SudokuSolver sv = new SudokuSolver();
						SudokuOutput out = sv.sudokuSolverStats(sf, arguments);
						
						BTSolver solver = out.getSolver();
						
						
						fout.append("Assignments: "+ solver.getNumAssignments() + System.getProperty("line.separator"));
						fout.append("Time Taken: " + solver.getTimeTaken() + System.getProperty("line.separator"));
						fout.append("Solution: "+ solver.hasSolution() + System.getProperty("line.separator"));
						fout.append(System.getProperty("line.separator"));
						
						if(solver.hasSolution())
						{
							solved += 1;
						}
						
						timeTaken += solver.getTimeTaken();
						assignments += solver.getNumAssignments();
						times.add(solver.getTimeTaken());
						
						
						fout.flush();					
					}
					
					fout.append(System.getProperty("line.separator"));
					fout.append("Overall Stats" + System.getProperty("line.separator"));
					fout.append("------------------------------------" + System.getProperty("line.separator"));
					fout.append("Overall Assignments: " + assignments + System.getProperty("line.separator"));
					fout.append("Overall Time Taken: " + timeTaken + System.getProperty("line.separator"));
					fout.append("Overall Solvable: " + solved + System.getProperty("line.separator"));
					fout.append(System.getProperty("line.separator"));
					
					fout.append("Averages Stats" + System.getProperty("line.separator"));
					fout.append("------------------------------------" + System.getProperty("line.separator"));
					fout.append("Average Assignments: " + (double)assignments/10 + System.getProperty("line.separator"));
					fout.append("Average Time Taken: " + (double)timeTaken/10 + System.getProperty("line.separator"));
					fout.append("Std Dev Time: " + findSTD(times,(double)solved) + System.getProperty("line.separator"));
					fout.append("Average Solvable: " + (double)solved/10 + System.getProperty("line.separator"));
					fout.append(System.getProperty("line.separator"));
					fout.close();
				}
				
				
				br.close();
				
			}
			
			reader.close();

	} catch (FileNotFoundException e) {
		// TODO Auto-generated catch block
		e.printStackTrace();
	} catch (IOException e) {
		// TODO Auto-generated catch block
		e.printStackTrace();
	}
		
	}
	public static void main(String[] args){
		//runPart2();
		//part3();
		
		part4();
		/**
		 * ArrayList<String[]> s = new ArrayList<String[]>();
		
		String[] arguments = {"input.txt","output.txt","3600","FC","MAC","MRV"};
		s.add(arguments);
		String[] arguments1 = {"input.txt","output.txt","3600","FC","ACP","MAC"};
		s.add(arguments1);
		
		for(String[] d: s)
		{
			part2(d);
		}
		*/
		//String[] best = {"input.txt","output.txt","3600","FC","MRV","LCV"};
		//part2(best);
	}
	
}
