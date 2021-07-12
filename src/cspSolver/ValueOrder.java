package cspSolver;
import java.util.*;

//Used in LCV to keep the order of least constrained value
//Uses comparison to sort the values by most constraining to least constraining
//
public class ValueOrder implements Comparator<ValueOrder>, Comparable<ValueOrder> {
	private int value;
	private int count = 0;
	
	public ValueOrder(int value,int count)
	{
		this.value = value;
		this.count = count;
	}
	
	public int getValue()
	{
		//Get values 1,2,3,4,5,6...
		return this.value;
	}
	
	public int getCount()
	{
		//Get number of constraints
		return this.count;
	}
	
	public void incrementCount()
	{
		//Increment constraint count
		this.count = this.count + 1;
	}
	
	public void setCount(int num)
	{
		//Set constraint count
		this.count = this.count + num;
	}
	
	public boolean equals(ValueOrder o)
	{
		return this.value == o.getValue();
	}
	
	@Override
	public String toString()
	{
		return "Value: " + this.value + " Count: " + this.count;
		
	}
	
	/***
	 * Below is what used to compare values in LCV used in sorting
	 */
	@Override
	public int compare(ValueOrder o1, ValueOrder o2) {
		// Compares another ValueOrder
		return o1.compareTo(o2);
	}

	@Override
	public int compareTo(ValueOrder other) {
		// Compares another ValueOrder
		if (this.count < other.count) 
		{
			return -1;
		}
		else if(this.count > other.count)
		{
			return 1;
		}
		else
		{
			 return Integer.compare(this.value, other.getValue());
		}
	}
}
