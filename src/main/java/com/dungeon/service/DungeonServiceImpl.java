package com.dungeon.service;

import com.dungeon.dto.DungeonRequest;
import com.dungeon.dto.DungeonResponse;
import com.dungeon.model.DungeonSolveResult;
import com.dungeon.solver.DungeonSolver;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * Implementation of DungeonService following hexagonal architecture principles.
 * 
 * This service acts as the application layer, orchestrating the interaction
 * between the domain logic (solver) and the infrastructure layer (controllers).
 * 
 * Follows SOLID principles:
 * - Single Responsibility: Handles dungeon solving business logic
 * - Open/Closed: Open for extension through dependency injection
 * - Liskov Substitution: Can be substituted for any DungeonService implementation
 * - Interface Segregation: Implements only necessary service methods
 * - Dependency Inversion: Depends on DungeonSolver abstraction, not concrete implementation
 */
@Service
public class DungeonServiceImpl implements DungeonService {
    
    private static final Logger logger = LoggerFactory.getLogger(DungeonServiceImpl.class);
    
    private final DungeonSolver dungeonSolver;
    
    /**
     * Constructor injection for dependency inversion.
     * 
     * @param dungeonSolver the solver implementation to use
     */
    @Autowired
    public DungeonServiceImpl(DungeonSolver dungeonSolver) {
        this.dungeonSolver = dungeonSolver;
    }
    
    @Override
    public DungeonResponse solveDungeon(DungeonRequest request) throws DungeonSolvingException {
        logger.info("Solving dungeon with dimensions: {}x{}", 
                   request.getRows(), request.getCols());
        
        try {
            DungeonSolveResult result = processDungeonSolving(request.input());
            
            return switch (result) {
                case DungeonSolveResult.Success success -> {
                    logger.info("Dungeon solved successfully. Minimum HP: {}, Path length: {}", 
                               success.minHp(), success.path().size());
                    yield DungeonResponse.fromPositions(success.input(), success.path(), success.minHp());
                }
                case DungeonSolveResult.Failure failure -> {
                    logger.warn("Dungeon solving failed: {} ({})", failure.reason(), failure.errorCode());
                    throw new DungeonSolvingException(failure.reason(), failure.errorCode().name(), failure.input());
                }
            };
            
        } catch (DungeonSolvingException e) {
            // Re-throw known exceptions
            throw e;
        } catch (Exception e) {
            logger.error("Unexpected error during dungeon solving", e);
            throw new DungeonSolvingException(
                "An unexpected error occurred while solving the dungeon: " + e.getMessage(),
                "PROCESSING_ERROR",
                e
            );
        }
    }
    
    @Override
    public DungeonSolveResult processDungeonSolving(int[][] dungeonGrid) {
        if (dungeonGrid == null) {
            return new DungeonSolveResult.Failure(
                null,
                "Dungeon grid cannot be null",
                DungeonSolveResult.ErrorCode.INVALID_INPUT
            );
        }
        
        logger.debug("Processing dungeon grid with {} rows and {} columns", 
                    dungeonGrid.length, 
                    dungeonGrid.length > 0 ? dungeonGrid[0].length : 0);
        
        try {
            // Delegate to the solver
            DungeonSolveResult result = dungeonSolver.solve(dungeonGrid);
            
            // Log the result for monitoring
            switch (result) {
                case DungeonSolveResult.Success success -> 
                    logger.debug("Solver found solution with minimum HP: {}", success.minHp());
                case DungeonSolveResult.Failure failure -> 
                    logger.debug("Solver failed: {} ({})", failure.reason(), failure.errorCode());
            }
            
            return result;
            
        } catch (Exception e) {
            logger.error("Error in solver processing", e);
            return new DungeonSolveResult.Failure(
                dungeonGrid,
                "Solver processing error: " + e.getMessage(),
                DungeonSolveResult.ErrorCode.PROCESSING_ERROR
            );
        }
    }
}
