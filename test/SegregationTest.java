import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class SegregationTest {

    //Does the generate method do the right amount of empty cells when making a map
    @Test
    void correctEmptyTilePercent() {
        int cellTotal = 30 * 30; //Change this argument a few times to make sure the total amount of empty cells is always around the right number.
        assertTrue(Math.abs(0.1 * cellTotal - Segregation.getAvailableCells(cellTotal, 0.1)[0]) < 3);
    }

    //Does the getAvailableCells() method return an array with the same total number of cells as are in the grid
    @Test
    void generatesCorrectCellAmounts() {
        int[] cells = Segregation.getAvailableCells(30 * 30, 0.1);
        int total = 0;
        for(int cell : cells) {
            total += cell;
        }
        assertEquals(30*30, total);
    }

    //Does the step function add in any extra empty cells by accident
    @Test
    void stepKeepsCorrectCellAmounts() {
        int[][] grid = new int[30][30];
        Segregation.generateGrid(grid, 0.1);
        int emptyCells = Segregation.getEmptyCellCoords(grid).size();
        Segregation.step(grid);
        assertEquals(emptyCells, Segregation.getEmptyCellCoords(grid).size());
    }

    //Does the weightedAverage method give approximately acceptable answers
    //This test could technically fail if you get really unlucky. It's basically checking
    //to see if my weighted average function is usually choosing the first option 1/4 times.
    @Test
    void weightedAvgWorks() {
        int[] weights = { 1, 3 };
        double first = 0, second = 0;
        for (int i = 0; i < 100; i++) {
            if (Segregation.getWeightedRand(weights) == 0) {
                first++;
            } else { second++; }
        }

        assertTrue(Math.abs(0.25 - (first/100.0)) < 0.05);
    }

    //Does the copy2DArray function work right
    @Test
    void copyArrayWorks() {
        int[][] a = {
                {1, 2},
                {3, 4}
        };
        int[][] b = new int[2][2];

        Segregation.copy2DArray(a, b);
        assertTrue(b[1][1] == a[1][1] && b[0][1] == a[0][1]);
    }

}
