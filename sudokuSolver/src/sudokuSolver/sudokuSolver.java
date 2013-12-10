package sudokuSolver;

import java.util.*;
import java.io.*;

public class sudokuSolver {
	
	private static int[][] board;
	private static int[][] domainSize;
	static final int empty = 0;
	static final int success = -100;
	static final int fail = -1;
	static long calls = 0;
	
	static void displaySudoku() {
		for (int i = 0; i < 9; i++) {
			System.out.println(" -------------------------------------");
			for (int j = 0; j < 9; j++) { 
				if (board[i][j]>0) System.out.print(" | " + board[i][j]);
				else System.out.print(" |  ");
			}
			System.out.println(" |");
    	}      
		System.out.println(" -------------------------------------");
	}
	
	static int next(int pos) {
		// pos: the last four bits are column number and the next four bits are row number.
		// look for next open position 

	    // fix for some java compilers which handle -1 as bit vector wrong.
	    if (pos == -1) {
		if (board[0][0] == empty) return 0;
		else pos = 0;
	    }

	    int col = pos&15; 
		int row = (pos >> 4)&15; 
		
		while (true) {
			++col; 
			if (col >= 9) { col = 0; row++; }
			if (row >= 9) return success; // no more open positions
			if (board[row][col] <= empty) return (row << 4)+col;
		}
	}
	
	static int betterNext() {
		// A next function that returns the position with the least feasible values.
		// We don't care about the current position, so we take no parameters.
		int bestN = 10;
		int bestPos = 0;
		int num;
		boolean done = true;
		for (int i=0; i<9; i++) {
			for (int j=0; j<9; j++) {
				if (board[i][j] == 0) {
					done = false;
					num = numberFeasible(i, j);
					if (num <= 1) {
						bestN = num;
						bestPos = (i << 4)+j;
						break;
					}
					else if (num < bestN) {
						bestN = num;
						bestPos = (i << 4)+j;
					}
				}
			}
		}
		if (done) return success;  // No more open positions
		//System.out.println();
		return bestPos;
	}
	
	static boolean feasible (int row, int col, int k) {
		// check if k is feasible at position <row, col>	
		for (int i = 0; i < 9; i++) {
			if (board[i][col] == k) return false;  // used in the same column
			if (board[row][i] == k) return false;  // used in the same row
		}
		
		int row0 = (row/3)*3;
		int col0 = (col/3)*3;
		for (int i = 0; i < 3; i++) for (int j = 0; j < 3; j++) 
			if (board[i+row0][j+col0] == k) return false; // used in the same region
		return true;	
	}
	
	static int numberFeasible(int row, int col) {
		int count = 0;
		for (int i=1; i<=9; i++) {
			if (feasible(row, col, i)) count++;
		}
		return count;
	}
	
	static int backtrack (int pos) {
		// backtrack procedure
		calls++;
		if (pos == success) return success;

		// pos: the last four bits are column number and the next four bits are row number.
		int col = pos&15;
		int row = (pos >> 4)&15;
		
		// Tries all of the feasible values for the position.
		for (int k = 1; k <= 9; k++) if (feasible(row, col, k)) {
			board[row][col] = k;
			// System.out.println("["+row+","+col+"]="+k);
			if (backtrack(betterNext()) == success) return success;
		}
		
		board[row][col] = empty;
		return fail;
	}
	

    public static void main(String[] args) {
    	
    	board = new int[9][9];
    	domainSize = new int[9][9];
    	

	// read in a puzzle
        try {
        	BufferedReader read = new BufferedReader(new InputStreamReader(System.in));
        	
            System.out.print("Please enter sudoku puzzle file name: ");
            String filename = read.readLine();
            
            Scanner scanner = new Scanner(new File(filename));
            for (int i = 0; i < 9; i++)
            	for (int j = 0; j < 9; j++) {  // while(scanner.hasNextInt())
            		board[i][j] = scanner.nextInt();
            		domainSize[i][j] = (board[i][j]>0)? 1 : 9;
            	}      
            System.out.println("Read in:");
            displaySudoku();
        }
        catch(Exception ex){
            ex.printStackTrace();
        }
        
	// Solve the puzzle by backtrack search and display the solution if there is one.
	if (backtrack(betterNext()) == success) {
	    System.out.println("\nSolution:");
	    displaySudoku();
	} else {
	    System.out.println("no solution");
	}
      	System.out.println("recursive calls = "+calls);
    }
}
