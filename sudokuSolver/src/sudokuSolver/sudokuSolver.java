package sudokuSolver;

import java.util.*;
import java.io.*;

public class sudokuSolver {
	
	private static int[][] board;
	private static int[][] domainSize;
	private static boolean[][][] domain;
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
		// This function is made more efficient by storing each cell's domain
		// in the domain array.  This results in less calls to feasible().
		int count = 0;
		for (int k=0; k<9; k++) {
			if (domain[row][col][k]) count++;
		}
		return count;
	}
	
	static void removeFromDomain(int row, int col, int k) {
		// Removes k from the domain of all cells incident with the cell at row/col.
		for(int i=0; i<9; i++) {
			domain[i][col][k-1] = false;
			domain[row][i][k-1] = false;
		}
		
		int row0 = (row/3)*3;
		int col0 = (col/3)*3;
		for (int i=0; i<3; i++) for (int j=0; j<3; j++) {
			domain[i+row0][j+col0][k-1] = false;
		}
	}
	
	static void updateDomains(int row, int col, int k) {
		// Updates the domains of all cells incident with the given row and col for value k.
		for(int i=0; i<9; i++) {
			domain[i][col][k-1] = feasible(i, col, k);
			domain[row][i][k-1] = feasible(row, i, k);
		}
		
		int row0 = (row/3)*3;
		int col0 = (col/3)*3;
		for (int i=0; i<3; i++) for (int j=0; j<3; j++) {
			domain[i+row0][j+col0][k-1] = feasible(i+row0, j+col0, k);
		}
	}
	
	static int backtrack (int pos) {
		// backtrack procedure
		calls++;
		if (pos == success) return success;

		// pos: the last four bits are column number and the next four bits are row number.
		int col = pos&15;
		int row = (pos >> 4)&15;
		
		// Tries all of the values in the domain for the position.
		for (int k = 1; k <= 9; k++) if (domain[row][col][k-1]) {
			// Tries the value, and removes it from the domains of all incident cells.
			board[row][col] = k;
			removeFromDomain(row, col, k);
			// System.out.println("["+row+","+col+"]="+k);
			if (backtrack(betterNext()) == success) return success;
			else {
				// If this path fails, then reset the domains.
				board[row][col] = 0;
				updateDomains(row, col, k);
			}
		}
		
		board[row][col] = empty;
		return fail;
	}
	

    public static void main(String[] args) {
    	
    	board = new int[9][9];
    	domainSize = new int[9][9];
    	domain = new boolean[9][9][9];

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
        
        // Initialize the domain for each position
        for(int i=0; i<9; i++) for (int j=0; j<9; j++) for (int k=0; k<9; k++) {
        	if (feasible(i, j, k+1)) {
        		domain[i][j][k] = true;
        	} else domain[i][j][k] = false;
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
