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
	private int height, maxHeight, backUpMaxHeight;
	private int[] widths, heights, backupWidths, backupHeighths;
	private boolean[][] grid, backUp;
	private boolean DEBUG = true;
	private boolean committed;
	
	
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
		backupWidths = new int[height];
		backupHeighths = new int[width];

		grid = new boolean[width][height];
		backUp = new boolean[width][height];
		maxHeight = backUpMaxHeight = 0;

		committed = true;

		for (int i=0; i<width; ++i){
			heights[i] = backupHeighths[i] = 0;
			for (int j=0; j<height; ++j){
				grid[i][j] = backUp[i][j] = false;
			}
		}

		for (int i=0; i<height; ++i){
			widths[i] = backupWidths[i] = 0;
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
		return maxHeight;
	}
	

	private static final int WIDTHS_NOT_CORRECT = 1;
	private static final int HEIGHTS_NOT_CORRECT = 2;
	private static final int MAX_HEIGHT_NOT_CORRECT = 3;

	/**
	 Checks the board for internal consistency -- used
	 for debugging.
	*/
	public void sanityCheck() {
		if (DEBUG) {
			int errorLog = checkCorrectness();
			if (errorLog == WIDTHS_NOT_CORRECT){
				throw new RuntimeException("WIDTHS arraay is counted wrongly!");
			}
			if (errorLog == HEIGHTS_NOT_CORRECT){
				throw new RuntimeException("HEIGHTS arraay is counted wrongly!");
			}
			if (errorLog == MAX_HEIGHT_NOT_CORRECT){
				throw new RuntimeException("maxHeight is counted wrongly!");
			}
		}
	}


	/**
	 * checks corrects of relations between data
	 * and returns error codes in in manner described above
	 * */
	private int checkCorrectness(){
		int[] checkArray = new int [width];
		for (int i=0; i<width; ++i){
			checkArray[i] = 0;
		}
		for (int j=0; j<height; ++j){
			int num = 0;
			for (int i=0; i<width; ++i){
				if (grid[i][j]){
					num++;
					checkArray[i] = Math.max(j + 1, checkArray[i]);
				}
			}
			if (num != widths[j]){
				return WIDTHS_NOT_CORRECT;
			}
		}
		int newMax = 0;
		for (int i=0; i<width; ++i){
			if (checkArray[i] != heights[i]){
				return HEIGHTS_NOT_CORRECT;
			}
			newMax = Math.max(newMax, checkArray[i]);
		}
		if (maxHeight != newMax){
			return MAX_HEIGHT_NOT_CORRECT;
		}
		return 0;
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
		int[] skirt = piece.getSkirt();
		int size = skirt.length;
		int answer = heights[x] - skirt[0];
		for (int i=1; i<size; ++i){
			answer = Math.max(answer, heights[x + i] - skirt[i]);
		}
		return answer;
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
		return x >= width && y >= height ?true:grid[x][y];
	}
	
	
	public static final int PLACE_OK = 0;
	public static final int PLACE_ROW_FILLED = 1;
	public static final int PLACE_OUT_BOUNDS = 2;
	public static final int PLACE_BAD = 3;

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

		committed = false;
		if (x < 0 || x >= width || y < 0 || y >= height){
			return PLACE_OUT_BOUNDS;
		}

		backUpData();

		int result = PLACE_OK;
		TPoint[] body = piece.getBody();
		int size = body.length;
		boolean ind = false;
		for (int i=0; i<size; ++i){
			int pX = body[i].x;
			int pY = body[i].y;
			if (x + pX >= width || y + pY >= height){
				return PLACE_OUT_BOUNDS;
			}
			ind |= grid[x + pX][y + pY];
		}
		if (ind){
			return PLACE_BAD;
		}
		for (int i=0; i<size; ++i){
			int pX = body[i].x + x;
			int pY = body[i].y + y;
			grid[pX][pY] = true;
			widths[pY]++;
			heights[pX] = Math.max(heights[pX], pY + 1);
			maxHeight = Math.max(maxHeight, pY + 1);
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
		committed = false;
		int rowsCleared = 0;
		boolean[] deleted = new boolean[height];
		for (int i=0; i<height; ++i){
			if (widths[i] == width){
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
		int num = 0;
		int max = getMaxHeight();
		for (int j=lower; j<max; ++j){
			if (deleted[j]){
				num++;
			}
			if (!deleted[j]){
				widths[j - num] = widths[j];
			}
			widths[j] = 0;
			for (int i=0; i<width; i++){
				if (!deleted[j]){
					grid[i][j - num] = grid[i][j];
				}
				grid[i][j] = false;
			}
		}
		maxHeight = 0;
		for (int i=0; i<width; ++i) {
			heights[i] = 0;
			for (int j=height - 1; j > -1; --j){
				if (grid[i][j]){
					heights[i] = j + 1;
					maxHeight = Math.max(maxHeight, j + 1);
					break;
				}
			}
		}
	}


	private void backUpData(){
		for (int i=0; i<width; ++i){
			System.arraycopy(grid[i], 0, backUp[i], 0, height);
		}
		System.arraycopy(widths, 0, backupWidths, 0, height);
		System.arraycopy(heights, 0, backupHeighths, 0, width);
		backUpMaxHeight = maxHeight;
	}

	/**
	 Reverts the board to its state before up to one place
	 and one clearRows();
	 If the conditions for undo() are not met, such as
	 calling undo() twice in a row, then the second undo() does nothing.
	 See the overview docs.
	*/
	public void undo() {
		if (committed){
			return;
		}
		for (int i=0; i<width; ++i){
			System.arraycopy(backUp[i], 0, grid[i], 0, height);
		}
		System.arraycopy(backupWidths, 0, widths, 0, height);
		System.arraycopy(backupHeighths, 0, heights, 0, width);
		maxHeight = backUpMaxHeight;
		committed = true;
	}
	
	
	/**
	 Puts the board in the committed state.
	*/
	public void commit() {
		backUpData();
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


