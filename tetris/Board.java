// Board.java
package tetris;

/**
 CS108 Tetris Board.
 Represents a Tetris board -- essentially a 2-d grid
 of booleans. Supports tetris pieces and row clearing.
 Has an "undo" feature that allows clients to add and remove pieces efficiently.
 Does not do any drawing or have any idea of pixels. Instead,
 just represents the abstract 2-d board.
*/
public class Board	{
	// Some ivars are stubbed out for you:
	private int width;
	private int height;
	private int[] widths, heights;
	private boolean[][] grid;
	private boolean DEBUG = true;
	boolean committed;
	
	
	// Here a few trivial methods are provided:
	
	/**
	 Creates an empty board of the given width and height
	 measured in blocks.
	*/
	public Board(int width, int height) {
		this.width = width;
		this.height = height;
		widths = new int[height];
		heights = new int[width];
		grid = new boolean[width][height + PLUS_ROW];
		committed = true;
		for (int i=0; i<width; ++i){
			heights[i] = 0;
			for (int j=0; j<height + PLUS_ROW; ++j){
				grid[i][j] = false;
			}
		}
		for (int i=0; i<height + PLUS_ROW; ++i){
			widths[i] = 0;
		}
	}
	
	
	/**
	 Returns the width of the board in blocks.
	*/
	public int getWidth() {
		return width;
	}
	
	
	/**
	 Returns the height of the board in blocks.
	*/
	public int getHeight() {
		return height;
	}
	
	
	/**
	 Returns the max column height present in the board.
	 For an empty board this is 0.
	*/
	public int getMaxHeight() {
		int answer = 0;
		for (int i=0; i<height; ++i){
			answer = Math.max(answer, heights[i]);
		}
		return answer;
	}
	
	
	/**
	 Checks the board for internal consistency -- used
	 for debugging.
	*/
	public void sanityCheck() {
		if (DEBUG) {
			// YOUR CODE HERE
		}
	}
	
	/**
	 Given a piece and an x, returns the y
	 value where the piece would come to rest
	 if it were dropped straight down at that x.
	 
	 <p>
	 Implementation: use the skirt and the col heights
	 to compute this fast -- O(skirt length).
	*/
	public int dropHeight(Piece piece, int x) {
		return 0; // YOUR CODE HERE
	}
	
	
	/**
	 Returns the height of the given column --
	 i.e. the y value of the highest block + 1.
	 The height is 0 if the column contains no blocks.
	*/
	public int getColumnHeight(int x) {
		return heights[x]; // YOUR CODE HERE
	}
	
	
	/**
	 Returns the number of filled blocks in
	 the given row.
	*/
	public int getRowWidth(int y) {
		return widths[y]; // YOUR CODE HERE
	}
	
	
	/**
	 Returns true if the given block is filled in the board.
	 Blocks outside of the valid width/height area
	 always return true.
	*/
	public boolean getGrid(int x, int y) {
		return x >= width && y >= height ?true:grid[x][y]; // YOUR CODE HERE
	}
	
	
	public static final int PLACE_OK = 0;
	public static final int PLACE_ROW_FILLED = 1;
	public static final int PLACE_OUT_BOUNDS = 2;
	public static final int PLACE_BAD = 3;
	private static final int PLUS_ROW = 4;
	
	/**
	 Attempts to add the body of a piece to the board.
	 Copies the piece blocks into the board grid.
	 Returns PLACE_OK for a regular placement, or PLACE_ROW_FILLED
	 for a regular placement that causes at least one row to be filled.
	 
	 <p>Error cases:
	 A placement may fail in two ways. First, if part of the piece may falls out
	 of bounds of the board, PLACE_OUT_BOUNDS is returned.
	 Or the placement may collide with existing blocks in the grid
	 in which case PLACE_BAD is returned.
	 In both error cases, the board may be left in an invalid
	 state. The client can use undo(), to recover the valid, pre-place state.
	*/
	public int place(Piece piece, int x, int y) {
		// flag !committed problem
		if (!committed) throw new RuntimeException("place commit problem");
			
		int result = PLACE_OK;
		TPoint[] body = piece.getBody();
		int size = body.length;
		for (int i=0; i<size; ++i){
			int pX = body[i].x;
			int pY = body[i].y;
			if (x + pX >= height || y + pY >= width){
				return PLACE_OUT_BOUNDS;
			} else if (grid[x + pX][y + pY]){
				return PLACE_BAD;
			}
		}
		for (int i=0; i<size; ++i){
			int pX = body[i].x + x;
			int pY = body[i].y + y;
			widths[pY]++;
			heights[pX] = Math.max(heights[pX], pY);
			if (widths[pY] == width){
				result = PLACE_ROW_FILLED;
			}
		}
		return result;
	}
	
	
	/**
	 Deletes rows that are filled all the way across, moving
	 things above down. Returns the number of rows cleared.
	*/
	public int clearRows() {
		int rowsCleared = 0;
		boolean[] deleted = new boolean[height];
		for (int i=0; i<height; ++i){
			boolean ind = true;
			for (int j=0; j<width; ++j){
				ind &= grid[j][i];
			}
			if (ind){
				rowsCleared++;
				deleted[i] = true;
			}
		}
		moveThingsDown(deleted, rowsCleared);
		sanityCheck();
		return rowsCleared;
	}


	/**
	 * after clear row is called this method moves pieces parts down
	 * */
	private void moveThingsDown(boolean[] deleted, int rowsCleared) {
		if (rowsCleared == 0){
			return;
		}
		int lower = 0;
		while(!deleted[lower]){
			lower++;
		}
		for (int i=0; i<width; i++){
			for (int j=lower; j<heights[i]; ++i){
				if (!deleted[i]){
					grid[i][j - 1] = grid[i][j];
				}
				grid[i][j] = false;
			}
			heights[i] -= rowsCleared;
		}
	}


	/**
	 Reverts the board to its state before up to one place
	 and one clearRows();
	 If the conditions for undo() are not met, such as
	 calling undo() twice in a row, then the second undo() does nothing.
	 See the overview docs.
	*/
	public void undo() {
		// YOUR CODE HERE
	}
	
	
	/**
	 Puts the board in the committed state.
	*/
	public void commit() {
		committed = true;
	}


	
	/*
	 Renders the board state as a big String, suitable for printing.
	 This is the sort of print-obj-state utility that can help see complex
	 state change over time.
	 (provided debugging utility) 
	 */
	public String toString() {
		StringBuilder buff = new StringBuilder();
		for (int y = height-1; y>=0; y--) {
			buff.append('|');
			for (int x=0; x<width; x++) {
				if (getGrid(x,y)) buff.append('+');
				else buff.append(' ');
			}
			buff.append("|\n");
		}
		for (int x=0; x<width+2; x++) buff.append('-');
		return(buff.toString());
	}
}


