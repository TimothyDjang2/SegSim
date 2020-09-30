/**
 * Segregation Simulator - Made by Timothy Djang for CS-172, Sep 30, 2020
 * 
 * Emeline was technically my partner but we both made separate programs again, since I don't like using stdLib.
 * 
 * Features:
 * - The ability to change the amount of cell types with minimal effort.
 * - No stdLib again, contrary to popular belief it's less work for me to not use it.
 * - Very little error-checking for edge cases in certain methods.
 * - Overthinking the randomization systems way too much.
 */

import java.util.ArrayList;
import javax.swing.JFrame;
import java.awt.Canvas;
import java.awt.image.BufferStrategy;
import java.awt.Graphics;
import java.awt.Color;

public class Segregation {

    //Simulation settings
    private static final int CELL_TYPES = 4; //How many different types of cells are there. (Doesn't count empty cells.) Make sure the color pallette has enough colors to handle the amount of cells.
    private static final int SIZE_X = 30, SIZE_Y = 30; //Size of grid. Making this too large may affect visibility of window, adjust CELL_SIZE if this is an issue.
    private static final double PERCENT_EMPTY = 0.1; //Percentage of empty cells in the grid.
    private static final double PERCENT_SATISFIED = 0.3; //Percentage of like adjacent cells required for a cell to be satisfied
    private static final long STEP_TIME = 200; //Time between simulation steps in ms

    //Window settings
    private static final int WINDOW_MARGINS = 100; //Pixels around the grid.
    private static final int CELL_SIZE = 20; //Size of a cell in px.

    private static JFrame window = new JFrame("SegSim");
    private static Canvas canvas = new Canvas();

    private static Color[] pallette = {
        new Color(255, 255, 255),
        new Color(255, 0, 0),
        new Color(0, 0, 255),
        new Color(0, 255, 0),
        new Color(255, 255, 0)
    };

    public static void main(String[] args) {

        if(CELL_TYPES > pallette.length) {
            System.out.println("ERR - You don't have enough colors in the color pallette to handle " + CELL_TYPES + " cell types.");
            return;
        }

        long oldTime = System.currentTimeMillis();

        int[][] grid = new int[SIZE_X][SIZE_Y];
        generateGrid(grid, PERCENT_EMPTY);
        setupWindow();

        while(true) {
            drawGrid(grid);
            if (System.currentTimeMillis() - oldTime > STEP_TIME) {
                step(grid);
                oldTime = System.currentTimeMillis();
            }
        }

    }

    /**
     * Runs a step of the simulation on the grid it is given.
     * @param grid - Grid to run the simulation rules on.
     */
    public static void step(int[][] grid) {

        int[][] temp = new int[grid.length][grid[0].length];
        copy2DArray(grid, temp);

        ArrayList<int[]> emptyCells = getEmptyCellCoords(grid);
        ArrayList<int[]> dissatisfiedCells = getDissatisfiedCellCoords(grid);

        for (int i = 0; i < dissatisfiedCells.size(); i++) {
            if (emptyCells.isEmpty()) break;

            int[] cellLoc = dissatisfiedCells.get(i);
            int randIndex = (int)(Math.random() * (emptyCells.size() - 1));
            int[] emptyLoc = emptyCells.get(randIndex);

            temp[emptyLoc[0]][emptyLoc[1]] = grid[cellLoc[0]][cellLoc[1]];
            emptyCells.remove(randIndex);
            emptyCells.add(randIndex, cellLoc);

            temp[cellLoc[0]][cellLoc[1]] = 0;
        }

        copy2DArray(temp, grid);
    }

    /**
     * Returns an ArrayList containing coordinates for every empty cell in the grid.
     * @param grid - Grid to search
     * @return - ArrayList of coordinates for empty cells.
     */
    public static ArrayList<int[]> getEmptyCellCoords(int[][] grid) {

        ArrayList<int[]> emptyCells = new ArrayList<int[]>();
        for (int i = 0; i < grid.length; i++) {
            for (int j = 0; j < grid[0].length; j++) {
                if (grid[i][j] == 0) {
                    int[] arr = {i, j};
                    emptyCells.add(0, arr);
                }
            }
        }

        return emptyCells;
    }

    /**
     * Returns an ArrayList containing coordinates for every dissatisfied cell in the grid.
     * @param grid - Grid to search.
     * @return - ArrayList of coordinates of dissatisfied cells.
     */
    public static ArrayList<int[]> getDissatisfiedCellCoords(int[][] grid) {
        ArrayList<int[]> dissatisfiedCells = new ArrayList<int[]>();
        for (int i = 0; i < grid.length; i++) {
            for (int j = 0; j < grid[0].length; j++) {

                if (grid[i][j] != 0) {

                    int totalCells = 0, totalSatisfactory = 0;

                    for (int k = i - 1; k <= i + 1; k++) {
                        for (int l = j - 1; l <= j + 1; l++) {
                            if (k > -1 && l > -1 && k < grid.length && l < grid.length) {
                                if (k != i || l != j) {
                                    totalCells++;
                                    if (grid[k][l] == grid[i][j]) {
                                        totalSatisfactory++;
                                    }
                                }
                            }
                        }
                    }

                    double percentSatisfied = (double)totalSatisfactory / (double)totalCells;

                    if (percentSatisfied < PERCENT_SATISFIED) {
                        int[] arr = {i, j};
                        dissatisfiedCells.add((int)(Math.random() * dissatisfiedCells.size()), arr);
                    }
                }

            }
            
        }

        return dissatisfiedCells;
    }

    /**
     * Copies the contents of a 2D array into another 2D array.
     * @param copyFrom - Array to copy from, will not be modified.
     * @param copyTo - Array to copy to, will be modified.
     */
    public static void copy2DArray(int[][] copyFrom, int[][] copyTo) {
        for (int i = 0; i < copyFrom.length; i++) {
            for (int j = 0; j < copyFrom[0].length; j++) {
                copyTo[i][j] = copyFrom[i][j];
            }
        }
    }

    /**
     * Generates a random grid.
     * @param percentEmpty - What percent of the grid should be empty cells, from 0.0 to 1.0.
     * @param grid - Grid to generate cells in.
     */
    public static void generateGrid(int[][] grid, double percentEmpty) {
        int totalCells = grid.length * grid[0].length;
        int[] availableCells = getAvailableCells(totalCells, percentEmpty);

        for(int i = 0; i < grid.length; i++) {
            for(int j = 0; j < grid[0].length; j++) {

                    int cellType = getWeightedRand(availableCells);

                    grid[i][j] = cellType;
                    availableCells[cellType]--;
            }
        }
    }

    /**
     * Returns the type of tile for the generator to place based on a weighted average of the remaining tile amounts
     * @param weights - Weights for each tile type, where index corresponds to tile type.
     * @return - Cell type to use.
     */
    public static int getWeightedRand(int[] weights) {
        double total = 0;
        for(int weight : weights) {
            total += (double)weight;
        }

        double rand = Math.random() * total;
        for(int i = 0; i < weights.length; i++) {
            if (rand < (double)weights[i]) return i;
            rand -= (double)weights[i];
        }

        return 0; //Should never be called.
    }

    /**
     * Returns an array with the allowable number of each type of cell in a given number of total cells. For example, returning {5, 10, 10}
     * means that there can be 5 empty cells, and 10 for each other type.
     * @param totalCells - Total amount of cells in the grid
     * @param percentEmpty - Percentage of empty cells, from 0.0 to 1.0
     * @return - Array with allowable amounts for each type of cell.
     */
    public static int[] getAvailableCells(int totalCells, double percentEmpty) {
        int emptyCells = (int)(totalCells * percentEmpty);
        totalCells -= emptyCells;
        int avgCells = totalCells / CELL_TYPES;
        totalCells -= (avgCells * CELL_TYPES);
        emptyCells += totalCells;

        int[] availableCells = new int[1 + CELL_TYPES];
        availableCells[0] = emptyCells;
        for (int i = 1; i < availableCells.length; i++) {
            availableCells[i] = avgCells;
        }

        return availableCells;
    }

    /**
     * Here's where all the garbage required to set up a JFrame goes.
     * I know I could use stdLib, but I already know how to use swing and awt, and I'm much more likely
     * to still use them once i leave this class.
     */
    private static void setupWindow() {
        window.setSize((2 * WINDOW_MARGINS) + (CELL_SIZE * SIZE_X), (2 * WINDOW_MARGINS) + (CELL_SIZE * SIZE_Y));
        window.add(canvas);
        window.setVisible(true);
        window.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        canvas.createBufferStrategy(2);
    }

    /**
     * Draws the grid to the screen.
     * @param grid - The grid to draw.
     */
    private static void drawGrid(int[][] grid) {
        BufferStrategy buf = canvas.getBufferStrategy();
        Graphics g = buf.getDrawGraphics();

        g.clearRect(0, 0, (2 * WINDOW_MARGINS) + (CELL_SIZE * SIZE_X), (2 * WINDOW_MARGINS) + (CELL_SIZE * SIZE_Y));

        for (int i = 0; i < grid.length; i++) {
            for (int j = 0; j < grid[0].length; j++) {
                g.setColor(pallette[grid[i][j]]);
                g.fillRect(WINDOW_MARGINS + (CELL_SIZE * i), WINDOW_MARGINS + (CELL_SIZE * j), CELL_SIZE, CELL_SIZE);
            }
        }

        if (!buf.contentsLost()) buf.show();
        g.dispose();
    }

}