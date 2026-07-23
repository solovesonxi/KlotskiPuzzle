package view.lab;

import model.BoardRules;
import model.MovementRule;
import model.PuzzlePreset;
import model.PuzzleState;
import view.GameTheme;

import javax.swing.JComponent;
import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;

import static util.Messages.text;

/** Shared board renderer for puzzle selection, state inspection, and solution replay. */
final class LabBoardView extends JComponent {
    private int[][] board = PuzzlePreset.TUTORIAL
            .definition(MovementRule.CELL_STEP).initialBoard();

    LabBoardView() {
        setPreferredSize(new Dimension(430, 520));
        setMinimumSize(new Dimension(320, 380));
    }

    void setBoard(int[][] board) {
        this.board = BoardRules.copy(board);
        repaint();
    }

    void setState(PuzzleState state) {
        setBoard(state.board());
    }

    @Override
    protected void paintComponent(Graphics graphics) {
        Graphics2D graphics2D = (Graphics2D) graphics.create();
        graphics2D.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
                RenderingHints.VALUE_ANTIALIAS_ON);
        int cell = Math.max(36, Math.min((getWidth() - 34) / BoardRules.GAME_COLUMNS,
                (getHeight() - 34) / BoardRules.GAME_ROWS));
        int boardWidth = cell * BoardRules.GAME_COLUMNS;
        int boardHeight = cell * BoardRules.GAME_ROWS;
        int originX = (getWidth() - boardWidth) / 2;
        int originY = (getHeight() - boardHeight) / 2;
        graphics2D.setColor(new Color(20, 18, 20));
        graphics2D.fillRoundRect(originX - 7, originY - 7,
                boardWidth + 14, boardHeight + 14, 18, 18);
        graphics2D.setColor(new Color(214, 177, 105, 35));
        for (int column = 1; column < BoardRules.GAME_COLUMNS; column++) {
            graphics2D.drawLine(originX + column * cell, originY,
                    originX + column * cell, originY + boardHeight);
        }
        for (int row = 1; row < BoardRules.GAME_ROWS; row++) {
            graphics2D.drawLine(originX, originY + row * cell,
                    originX + boardWidth, originY + row * cell);
        }

        for (BoardRules.Piece piece : BoardRules.pieces(board)) {
            int pieceWidth = switch (piece.type()) {
                case BoardRules.HORIZONTAL, BoardRules.CAO_CAO -> 2;
                default -> 1;
            };
            int pieceHeight = switch (piece.type()) {
                case BoardRules.VERTICAL, BoardRules.CAO_CAO -> 2;
                default -> 1;
            };
            int x = originX + piece.col() * cell + 4;
            int y = originY + piece.row() * cell + 4;
            int width = pieceWidth * cell - 8;
            int height = pieceHeight * cell - 8;
            graphics2D.setColor(pieceColor(piece.type()));
            graphics2D.fillRoundRect(x, y, width, height, 15, 15);
            graphics2D.setColor(GameTheme.GOLD_SOFT);
            graphics2D.setStroke(new BasicStroke(1.4f));
            graphics2D.drawRoundRect(x, y, width, height, 15, 15);
            drawPieceLabel(graphics2D, pieceLabel(piece.type()), x, y, width, height);
        }

        graphics2D.setColor(GameTheme.GOLD);
        graphics2D.setStroke(new BasicStroke(4f));
        graphics2D.drawLine(originX + cell, originY + boardHeight + 5,
                originX + cell, originY + boardHeight - 8);
        graphics2D.drawLine(originX + cell * 3, originY + boardHeight + 5,
                originX + cell * 3, originY + boardHeight - 8);
        graphics2D.dispose();
    }

    private static Color pieceColor(int type) {
        return switch (type) {
            case BoardRules.CAO_CAO -> GameTheme.LACQUER_HOVER;
            case BoardRules.HORIZONTAL -> new Color(132, 91, 47);
            case BoardRules.VERTICAL -> new Color(72, 61, 56);
            case BoardRules.SOLDIER -> new Color(73, 83, 76);
            default -> GameTheme.SURFACE_RAISED;
        };
    }

    private static String pieceLabel(int type) {
        return switch (type) {
            case BoardRules.CAO_CAO -> text("lab.piece.target");
            case BoardRules.HORIZONTAL -> text("lab.piece.horizontal");
            case BoardRules.VERTICAL -> text("lab.piece.vertical");
            case BoardRules.SOLDIER -> text("lab.piece.soldier");
            default -> "";
        };
    }

    private static void drawPieceLabel(Graphics2D graphics, String label,
                                       int x, int y, int width, int height) {
        graphics.setFont(GameTheme.strongFont(Math.max(12, Math.min(20, width / 6))));
        FontMetrics metrics = graphics.getFontMetrics();
        int textX = x + (width - metrics.stringWidth(label)) / 2;
        int textY = y + (height - metrics.getHeight()) / 2 + metrics.getAscent();
        graphics.setColor(GameTheme.TEXT);
        graphics.drawString(label, textX, textY);
    }
}
