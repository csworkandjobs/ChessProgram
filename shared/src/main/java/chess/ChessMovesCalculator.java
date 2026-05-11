package chess;

import java.util.Collection;
import java.util.ArrayList;
import chess.ChessPiece.PieceType;
import chess.ChessGame.TeamColor;

/**
 * Identifies all possible moves for a piece.
 * <p>
 * Note: This class exists to simplify the ChessPiece class.
 */
public class ChessMovesCalculator {
    /**
     * Calculates all the positions a chess piece can move to
     * Does not take into account moves that are illegal due to leaving the king in
     * danger.
     *
     * @return Collection of valid moves
     */
    public static Collection<ChessMove> pieceMoves(ChessBoard board, ChessPosition currentPosition) {
        PieceType moveType = board.getPiece(currentPosition).getPieceType();
        switch(moveType) {
            case PieceType.KING:
                return moveKing(board, currentPosition);
            case PieceType.QUEEN:
                return moveQueen(board, currentPosition);
            case PieceType.BISHOP:
                return moveBishop(board, currentPosition);
            case PieceType.KNIGHT:
                return moveKnight(board, currentPosition);
            case PieceType.ROOK:
                return moveRook(board, currentPosition);
            case PieceType.PAWN:
                return movePawn(board, currentPosition);
        }
        throw new RuntimeException("Provided piece type not recognized.");
    }

    /**
     * @return Possible moves if the given piece is a King.
     */
    public static Collection<ChessMove> moveKing(ChessBoard board, ChessPosition currentPosition) {
        int[][]relativePositions = {{-1,-1},{-1,0},{-1,1},{0,-1},{0,1},{1,-1},{1,0},{1,1}};
        return validMovesSpecificPoints(board, currentPosition, relativePositions);
    }

    /**
     * @return Possible moves if the given piece is a Queen.
     */
    public static Collection<ChessMove> moveQueen(ChessBoard board, ChessPosition currentPosition) {
        Collection<ChessMove> allPossibleMoves = new ArrayList<ChessMove>();

        //Adding Diagonal Moves
        allPossibleMoves.addAll(moveBishop(board,currentPosition));
        //Adding Straight Lines
        allPossibleMoves.addAll(moveRook(board,currentPosition));

        return allPossibleMoves;
    }

    /**
     * @return Possible moves if the given piece is a Bishop.
     */
    public static Collection<ChessMove> moveBishop(ChessBoard board, ChessPosition currentPosition) {
        Collection<ChessMove> allPossibleMoves = new ArrayList<ChessMove>();
        //Moves Up-Left
        int[] modifier = {1,-1};
        allPossibleMoves.addAll(validMovesLine(board,currentPosition,modifier));

        //Moves Up-Right
        modifier[1] = 1;
        allPossibleMoves.addAll(validMovesLine(board,currentPosition,modifier));

        //Moves Down-Right
        modifier[0] = -1;
        allPossibleMoves.addAll(validMovesLine(board,currentPosition,modifier));

        //Moves Down-Left
        modifier[1] = -1;
        allPossibleMoves.addAll(validMovesLine(board,currentPosition,modifier));

        return allPossibleMoves;
    }

    /**
     * @return Possible moves if the given piece is a Knight.
     */
    public static Collection<ChessMove> moveKnight(ChessBoard board, ChessPosition currentPosition) {
        int[][]relativePositions = {{-1,-2},{-2,-1},{-2,1},{-1,2},{1,-2},{2,-1},{2,1},{1,2}};
        return validMovesSpecificPoints(board, currentPosition, relativePositions);
    }

    /**
     * @return Possible moves if the given piece is a Rook.
     */
    public static Collection<ChessMove> moveRook(ChessBoard board, ChessPosition currentPosition) {
        Collection<ChessMove> allPossibleMoves = new ArrayList<ChessMove>();
        //Moves Up
        int[] modifier = {1,0};
        allPossibleMoves.addAll(validMovesLine(board,currentPosition,modifier));

        //Moves Down
        modifier[0] = -1;
        allPossibleMoves.addAll(validMovesLine(board,currentPosition,modifier));

        //Moves Right
        modifier[0] = 0;
        modifier[1] = 1;
        allPossibleMoves.addAll(validMovesLine(board,currentPosition,modifier));

        //Moves Left
        modifier[1] = -1;
        allPossibleMoves.addAll(validMovesLine(board,currentPosition,modifier));

        return allPossibleMoves;
    }

    /**
     * @return Possible moves if the given piece is a Pawn.
     */
    public static Collection<ChessMove> movePawn(ChessBoard board, ChessPosition currentPosition) {
        ArrayList<ChessMove> allPossibleMoves = new ArrayList<ChessMove>();
        ChessPiece piece = board.getPiece(currentPosition);
        int currentRow = currentPosition.getRow();
        int currentCol = currentPosition.getColumn();

        //Defining direction of movement based on color.
        int direction = 1;
        if(piece.getTeamColor() == TeamColor.BLACK){
            direction = -1;
        }

        //Mark possible promotions.
        PieceType[] promotionTypeList = {null};
        if((currentRow + direction == 1) || (currentRow + direction == 8)) {
            promotionTypeList = new PieceType[]{PieceType.QUEEN, PieceType.BISHOP, PieceType.KNIGHT, PieceType.ROOK};
        }

        //Move Forward 1 space
        ChessPosition possibleMove = new ChessPosition(currentRow + direction, currentCol);
        if(isSquareOpen(board, possibleMove)) {
            //allPossibleMoves.add(new ChessMove(currentPosition, possibleMove, promotion));
            addPossiblePawnMoves(currentPosition, possibleMove, promotionTypeList, allPossibleMoves);

            //Move Forward 2 spaces
            if((currentRow == 2) || (currentRow == 7)) {
                possibleMove = new ChessPosition(currentRow + (2 * direction), currentCol);
                if (isSquareInBounds(possibleMove) && isSquareOpen(board, possibleMove)) {
                    //allPossibleMoves.add(new ChessMove(currentPosition, possibleMove, promotion));
                    addPossiblePawnMoves(currentPosition, possibleMove, promotionTypeList, allPossibleMoves);
                }
            }
        }


        //Move diagonals
        possibleMove = new ChessPosition(currentRow + direction, currentCol+1);
        if(canPawnCapture(board, possibleMove, piece.getTeamColor())) {
            //allPossibleMoves.add(new ChessMove(currentPosition, possibleMove, promotion));
            addPossiblePawnMoves(currentPosition, possibleMove, promotionTypeList, allPossibleMoves);
        }
        possibleMove = new ChessPosition(currentRow + direction, currentCol-1);
        if(canPawnCapture(board, possibleMove, piece.getTeamColor())) {
            //allPossibleMoves.add(new ChessMove(currentPosition, possibleMove, promotion));
            addPossiblePawnMoves(currentPosition, possibleMove, promotionTypeList, allPossibleMoves);
        }

        return allPossibleMoves;
    }

    /**
     * Helper method containing the promotion for loop needed by the movePawn
     * method since it can have a promotion type other than null.
     */
    public static void addPossiblePawnMoves(ChessPosition currentPosition, ChessPosition possibleMove,
                                            PieceType[] promotionTypeList, ArrayList<ChessMove> moveList) {
        for (PieceType promotion : promotionTypeList) {
            moveList.add(new ChessMove(currentPosition, possibleMove, promotion));
        }
    }


    /**
     * @return Is the provided square valid as a position to move to.
     * Checks if the position is in bounds and unoccupied,
     * or in bounds and occupied by an enemy piece.
     * Is NEVER used for PAWNs.
     * <p>
     * Return meanings: null = square is empty
     *                  own team color = out of bounds OR space has a friendly piece
     *                  opponent team color = occupied by an opponent's piece
     */
    public static TeamColor isSquareValid(ChessBoard board, ChessPosition position, TeamColor attackerColor) {
        //Check if in bounds
        if(!isSquareInBounds(position)) {
            return attackerColor;
        }
        //Check if space is unoccupied.
        if(isSquareOpen(board, position)){
            return null;
        }

        //Check if occupying piece is enemy piece
        if(isEnemyPosition(board, position, attackerColor)) {
            TeamColor enemyColor = board.getPiece(position).getTeamColor();
            return enemyColor;
        }
        //Position is occupied by a friendly piece.
        return attackerColor;
    }

    /**
     * @return Is the provided square valid as a position to move to via capture.
     * <p>
     * Checks if the position is in bounds and unoccupied, or in bounds and
     * occupied by an enemy piece.  Is ONLY used for PAWN attacks.
     */
    public static boolean canPawnCapture(ChessBoard board, ChessPosition position, TeamColor attackerColor) {
        //Check if in bounds
        if(!isSquareInBounds(position)) {
            return false;
        }
        //Check if space is unoccupied.
        if(isSquareOpen(board, position)){
            return false;
        }
        //Check if occupying piece is enemy piece
        if(isEnemyPosition(board, position, attackerColor)) {
            return true;
        }
        //Position is occupied by a friendly piece.
        return false;
    }


    /**
     * @return Is the provided chess square in the bounds of the board.
     */
    public static boolean isSquareInBounds(ChessPosition position) {
        if(position.getRow() < 1) {
            return false;
        } else if(position.getRow() > 8) {
            return false;
        } else if(position.getColumn() < 1) {
            return false;
        } else if(position.getColumn() > 8) {
            return false;
        }
        return true;
    }

    /**
     * @return Is the provided chess square empty.
     */
    public static boolean isSquareOpen(ChessBoard board, ChessPosition position) {
        if(board.getPiece(position) == null) {
            return true;
        }
        return false;
    }

    /**
     * @return Is the provided chess square occupied by an enemy piece.
     */
    public static boolean isEnemyPosition(ChessBoard board, ChessPosition position, TeamColor attackerColor) {
        if(board.getPiece(position).getTeamColor() == attackerColor) {
            return false;
        }
        return true;
    }

    /**
     * @return Valid chess positions along a line based on a given modifier (both straight and diagonal).
     * <p>
     * Used by moveBishop() and moveKnight().
     */
    public static ArrayList<ChessMove> validMovesLine(ChessBoard board, ChessPosition currentPosition, int[]modifier) {
        TeamColor pieceColor = board.getPiece(currentPosition).getTeamColor();
        ArrayList<ChessMove> possibleMoves = new ArrayList<ChessMove>();

        TeamColor positionValid;
        int nextRow = currentPosition.getRow();
        int nextCol = currentPosition.getColumn();
        do {
            nextRow += modifier[0];
            nextCol += modifier[1];
            ChessPosition nextPosition = new ChessPosition(nextRow, nextCol);

            positionValid = isSquareValid(board, nextPosition, pieceColor);
            if(positionValid != pieceColor) {
                possibleMoves.add(new ChessMove(currentPosition, nextPosition, null));
            }
        }while(positionValid == null);

        return possibleMoves;
    }

    /**
     * @return Valid chess positions after given a list of locations to check.
     * <p>
     * Used by moveKing() and moveKinght()
     */
    public static ArrayList<ChessMove> validMovesSpecificPoints(ChessBoard board, ChessPosition currentPosition, int[][] relativePositions) {
        ArrayList<ChessMove> allPossibleMoves = new ArrayList<ChessMove>();
        TeamColor pieceColor = board.getPiece(currentPosition).getTeamColor();
        int currentRow = currentPosition.getRow();
        int currentColumn = currentPosition.getColumn();

        for(int i = 0; i < relativePositions.length; i++) {
            int nextRow = currentRow + relativePositions[i][0];
            int nextCol = currentColumn + relativePositions[i][1];
            ChessPosition nextPosition = new ChessPosition(nextRow,nextCol);
            TeamColor nextStatus = isSquareValid(board,nextPosition,pieceColor);
            if(nextStatus != pieceColor) {
                allPossibleMoves.add(new ChessMove(currentPosition, nextPosition, null));
            }
        }
        return allPossibleMoves;
    }
}
