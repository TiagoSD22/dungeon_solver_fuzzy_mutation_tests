package com.dungeon.validation;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

/**
 * Validator implementation for the @ValidDungeon annotation.
 * Validates dungeon grids according to business rules and constraints.
 */
public class DungeonValidator implements ConstraintValidator<ValidDungeon, int[][]> {
    
    private static final int MIN_DIMENSION = 1;
    private static final int MAX_DIMENSION = 200;
    private static final int MIN_CELL_VALUE = -1000;
    private static final int MAX_CELL_VALUE = 100;
    
    @Override
    public void initialize(ValidDungeon constraintAnnotation) {
        // No initialization needed
    }
    
    @Override
    public boolean isValid(int[][] dungeon, ConstraintValidatorContext context) {
        if (dungeon == null) {
            addConstraintViolation(context, "Dungeon cannot be null");
            return false;
        }
        
        // Check if dungeon is empty
        if (dungeon.length == 0) {
            addConstraintViolation(context, "Dungeon cannot be empty");
            return false;
        }
        
        // Check row count constraints
        if (dungeon.length > MAX_DIMENSION) {
            addConstraintViolation(context, 
                String.format("Dungeon height cannot exceed %d rows", MAX_DIMENSION));
            return false;
        }
        
        // Check if first row is empty
        if (dungeon[0].length == 0) {
            addConstraintViolation(context, "Dungeon rows cannot be empty");
            return false;
        }
        
        int cols = dungeon[0].length;
        
        // Check column count constraints
        if (cols > MAX_DIMENSION) {
            addConstraintViolation(context, 
                String.format("Dungeon width cannot exceed %d columns", MAX_DIMENSION));
            return false;
        }
        
        // Validate each row
        for (int i = 0; i < dungeon.length; i++) {
            if (dungeon[i] == null) {
                addConstraintViolation(context, 
                    String.format("Row %d cannot be null", i));
                return false;
            }
            
            // Check consistent row length
            if (dungeon[i].length != cols) {
                addConstraintViolation(context, 
                    String.format("All rows must have the same length. Expected %d, but row %d has %d", 
                        cols, i, dungeon[i].length));
                return false;
            }
            
            // Validate cell values
            for (int j = 0; j < dungeon[i].length; j++) {
                int cellValue = dungeon[i][j];
                if (cellValue < MIN_CELL_VALUE || cellValue > MAX_CELL_VALUE) {
                    addConstraintViolation(context, 
                        String.format("Cell value at position [%d,%d] must be between %d and %d, but was %d", 
                            i, j, MIN_CELL_VALUE, MAX_CELL_VALUE, cellValue));
                    return false;
                }
            }
        }
        
        // Additional validation: check if dungeon is theoretically solvable
        if (!isTheoreticallySolvable(dungeon)) {
            addConstraintViolation(context, "Dungeon appears to be unsolvable");
            return false;
        }
        
        return true;
    }
    
    /**
     * Performs a basic check to see if the dungeon might be solvable.
     * This is a heuristic check - the actual solver will do the definitive calculation.
     * 
     * @param dungeon the dungeon grid
     * @return true if the dungeon might be solvable
     */
    private boolean isTheoreticallySolvable(int[][] dungeon) {
        int rows = dungeon.length;
        int cols = dungeon[0].length;
        
        // Simple heuristic: if there's a path of non-extreme negative values, it might be solvable
        // This prevents obviously impossible dungeons from being processed
        
        // Check if the starting position would require more than reasonable HP
        if (dungeon[0][0] <= -1000) {
            return false;
        }
        
        // Check if the ending position is accessible
        if (dungeon[rows - 1][cols - 1] <= -1000) {
            return false;
        }
        
        // For very small dungeons, they're likely solvable
        if (rows <= 2 && cols <= 2) {
            return true;
        }
        
        // Count extremely negative values - if too many, might be unsolvable
        int extremeNegativeCount = 0;
        int totalCells = rows * cols;
        
        for (int[] row : dungeon) {
            for (int cell : row) {
                if (cell <= -500) {
                    extremeNegativeCount++;
                }
            }
        }
        
        // If more than 80% of cells are extremely negative, likely unsolvable
        return (double) extremeNegativeCount / totalCells < 0.8;
    }
    
    /**
     * Adds a custom constraint violation message.
     * 
     * @param context the validation context
     * @param message the custom message
     */
    private void addConstraintViolation(ConstraintValidatorContext context, String message) {
        context.disableDefaultConstraintViolation();
        context.buildConstraintViolationWithTemplate(message)
               .addConstraintViolation();
    }
}
