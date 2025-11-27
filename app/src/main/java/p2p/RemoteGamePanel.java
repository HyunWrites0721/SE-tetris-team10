package p2p;

import blocks.Block;
import game.GameView;
import game.core.GameController;
import game.panels.GameBoardPanel;

/**
 * 원격 게임 화면 관리 (단순화 버전)
 * - 받은 블록 데이터를 GameBoardPanel에 직접 전달
 */
public class RemoteGamePanel {
    
    private GameBoardPanel boardPanel;
    private Block currentBlock;
    // Remote fixed board state (matches GameState board dimensions used in GameController)
    private int[][] remoteBoard;
    private int[][] remoteColorBoard;
    // Queue for pending remote events that arrived before a spawn
    private final java.util.Queue<PendingEvent> pendingEvents = new java.util.ArrayDeque<>();

    private static class PendingEvent {
        enum Type { MOVE, ROTATE, PLACE }
        final Type type;
        final int x;
        final int y;
        PendingEvent(Type type, int x, int y) { this.type = type; this.x = x; this.y = y; }
        PendingEvent(Type type) { this(type, Integer.MIN_VALUE, Integer.MIN_VALUE); }
    }
    
    public RemoteGamePanel() {
        // Initialize remote board with GameController's default dimensions (23 x 12)
        try {
            this.remoteBoard = new int[23][12];
            this.remoteColorBoard = new int[23][12];
        } catch (Throwable t) {
            // fallback to small board if something unexpected happens
            this.remoteBoard = new int[23][12];
            this.remoteColorBoard = new int[23][12];
        }
    }

    /**
     * Synchronize RemoteGamePanel's internal remoteBoard/colorBoard from the
     * authoritative controller state and render it. Safe to call from any thread.
     */
    public void syncFromController(GameController controller) {
        if (controller == null) return;
        try {
            game.core.GameState gs = controller.getCurrentState();
            if (gs == null) return;

            int[][] board = gs.getBoardArray();
            int[][] color = gs.getColorBoard();

            // deep-copy into our internal arrays to avoid aliasing
            int ROWS = Math.max(23, board.length);
            int COLS = Math.max(12, board[0].length);
            this.remoteBoard = new int[ROWS][COLS];
            this.remoteColorBoard = new int[ROWS][COLS];
            for (int r = 0; r < board.length && r < ROWS; r++) {
                for (int c = 0; c < board[r].length && c < COLS; c++) {
                    this.remoteBoard[r][c] = board[r][c];
                    this.remoteColorBoard[r][c] = color != null && r < color.length && c < color[r].length ? color[r][c] : 0;
                }
            }

            final game.core.GameState state = new game.core.GameState.Builder(this.remoteBoard, this.remoteColorBoard, null, null, false).build();
            if (javax.swing.SwingUtilities.isEventDispatchThread()) {
                if (boardPanel != null) boardPanel.render(state);
            } else {
                javax.swing.SwingUtilities.invokeLater(() -> {
                    if (boardPanel != null) boardPanel.render(state);
                });
            }
        } catch (Throwable t) {
            System.err.println("[REMOTE] syncFromController 실패: " + t.getMessage());
        }
    }
    
    public void setRemoteComponents(GameView view, GameController controller) {
        // GameView에서 GameBoardPanel 가져오기 (재귀 탐색)
        this.boardPanel = findGameBoardPanel(view);
        // 만약 이미 currentBlock이 존재하면 boardPanel이 세팅된 후 적용
        if (this.boardPanel != null && this.currentBlock != null) {
            try {
                this.boardPanel.setRemoteBlock(this.currentBlock);
            } catch (Exception e) {
                System.err.println("[REMOTE] setRemoteBlock 예외: " + e.getMessage());
                e.printStackTrace();
            }
        }
        System.out.println("[REMOTE] ✅ Components 설정 완료: boardPanel=" + (boardPanel != null));
        // After components are set, there may be pending events to apply
        if (javax.swing.SwingUtilities.isEventDispatchThread()) {
            drainPendingEvents();
        } else {
            javax.swing.SwingUtilities.invokeLater(() -> drainPendingEvents());
        }
        // If we already have some fixed blocks, render them
        if (boardPanel != null) {
            try {
                game.core.GameState state = new game.core.GameState.Builder(
                    this.remoteBoard,
                    this.remoteColorBoard,
                    null,
                    null,
                    false
                ).build();
                if (javax.swing.SwingUtilities.isEventDispatchThread()) {
                    boardPanel.render(state);
                } else {
                    javax.swing.SwingUtilities.invokeLater(() -> boardPanel.render(state));
                }
            } catch (Throwable __) {
                // ignore render errors
            }
        }
    }

    // 재귀적으로 GameBoardPanel을 찾음
    private GameBoardPanel findGameBoardPanel(java.awt.Component comp) {
        if (comp instanceof GameBoardPanel) return (GameBoardPanel) comp;
        if (comp instanceof java.awt.Container) {
            java.awt.Component[] children = ((java.awt.Container) comp).getComponents();
            for (java.awt.Component c : children) {
                GameBoardPanel found = findGameBoardPanel(c);
                if (found != null) return found;
            }
        }
        return null;
    }
    
    public void spawnBlock(Block block) {
        System.out.println("[REMOTE] 🔵 spawnBlock: " + block.getClass().getSimpleName() + " at (" + block.getX() + ", " + block.getY() + ")");
        // Ensure shape is initialized for reflected/remote-created blocks
        if (block.getShape() == null) {
            try {
                block.setShape();
                System.out.println("[REMOTE] block.setShape() 호출: " + block.getClass().getSimpleName());
            } catch (Throwable t) {
                System.err.println("[REMOTE] block.setShape() 실패: " + t.getMessage());
            }
        }

        this.currentBlock = block;
        if (boardPanel != null) {
            // Ensure UI update happens on EDT
            if (javax.swing.SwingUtilities.isEventDispatchThread()) {
                boardPanel.setRemoteBlock(block);
                // Drain any events that were queued while waiting for the spawn
                drainPendingEvents();
            } else {
                javax.swing.SwingUtilities.invokeLater(() -> boardPanel.setRemoteBlock(block));
                javax.swing.SwingUtilities.invokeLater(() -> drainPendingEvents());
            }
        }
    }
    
    public void moveBlock(int x, int y) {
        if (currentBlock == null) {
            System.err.println("[REMOTE] ❌ moveBlock: currentBlock is NULL! - queuing event until spawn");
            synchronized (pendingEvents) {
                pendingEvents.add(new PendingEvent(PendingEvent.Type.MOVE, x, y));
            }
            return;
        }
        
        currentBlock.setPosition(x, y);
        if (boardPanel != null) {
            if (javax.swing.SwingUtilities.isEventDispatchThread()) {
                boardPanel.setRemoteBlock(currentBlock);
            } else {
                javax.swing.SwingUtilities.invokeLater(() -> boardPanel.setRemoteBlock(currentBlock));
            }
        }
        System.out.println("[REMOTE] 📍 moved to (" + x + ", " + y + ")");
    }
    
    public void rotateBlock() {
        if (currentBlock == null) {
            System.err.println("[REMOTE] ❌ rotateBlock: currentBlock is NULL! - queuing event until spawn");
            synchronized (pendingEvents) {
                pendingEvents.add(new PendingEvent(PendingEvent.Type.ROTATE));
            }
            return;
        }
        
        currentBlock.getRotatedShape();
        if (boardPanel != null) {
            if (javax.swing.SwingUtilities.isEventDispatchThread()) {
                boardPanel.setRemoteBlock(currentBlock);
            } else {
                javax.swing.SwingUtilities.invokeLater(() -> boardPanel.setRemoteBlock(currentBlock));
            }
        }
        System.out.println("[REMOTE] 🔄 rotated");
    }
    
    public void placeBlock() {
        if (currentBlock == null) {
            System.err.println("[REMOTE] ❌ placeBlock: currentBlock is NULL! - queuing PLACE until spawn");
            synchronized (pendingEvents) {
                pendingEvents.add(new PendingEvent(PendingEvent.Type.PLACE));
            }
            return;
        }
        
        System.out.println("[REMOTE] 🔻 placeBlock");
        // Apply the block's filled cells into the remote fixed board
        try {
            int[][] shape = currentBlock.getShape();
            System.out.println("[REMOTE] placeBlock: block=" + currentBlock.getClass().getSimpleName() + " pos=(" + currentBlock.getX() + ", " + currentBlock.getY() + ")");
            if (shape != null) {
                System.out.print("[REMOTE] placeBlock: shape:\n");
                for (int rr = 0; rr < shape.length; rr++) {
                    StringBuilder sb = new StringBuilder();
                    for (int cc = 0; cc < shape[rr].length; cc++) {
                        sb.append(shape[rr][cc]);
                    }
                    System.out.println(sb.toString());
                }
            }
            if (shape != null) {
                for (int r = 0; r < shape.length; r++) {
                    for (int c = 0; c < shape[r].length; c++) {
                        if (shape[r][c] != 0) {
                            int boardRow = currentBlock.getY() + r;
                            int boardCol = currentBlock.getX() + c;
                            if (boardRow >= 0 && boardRow < remoteBoard.length && boardCol >= 0 && boardCol < remoteBoard[0].length) {
                                // mark occupied cell using the original shape value so types match
                                remoteBoard[boardRow][boardCol] = shape[r][c];
                                try {
                                    java.awt.Color col = currentBlock.getColor();
                                    remoteColorBoard[boardRow][boardCol] = col != null ? col.getRGB() : 0;
                                } catch (Throwable __) {
                                    remoteColorBoard[boardRow][boardCol] = 0;
                                }
                                // detailed debug of written cell
                                System.out.println("[REMOTE] placeBlock -> wrote cell at board[" + boardRow + "][" + boardCol + "] = 1, color=" + remoteColorBoard[boardRow][boardCol]);
                            } else {
                                System.err.println("[REMOTE] placeBlock -> SKIPPED out-of-bounds write at (" + boardRow + "," + boardCol + ")");
                            }
                        }
                    }
                }
            }

            // Build a lightweight GameState to render the fixed board
            final game.core.GameState gs = new game.core.GameState.Builder(
                this.remoteBoard,
                this.remoteColorBoard,
                null,
                null,
                false
            ).build();

            // Clear transient falling block and update UI on EDT
            this.currentBlock = null;
            if (boardPanel != null) {
                if (javax.swing.SwingUtilities.isEventDispatchThread()) {
                    boardPanel.render(gs);
                    boardPanel.setRemoteBlock(null);
                } else {
                    javax.swing.SwingUtilities.invokeLater(() -> {
                        boardPanel.render(gs);
                        boardPanel.setRemoteBlock(null);
                    });
                }
            }
        } catch (Throwable t) {
            System.err.println("[REMOTE] placeBlock 처리 중 예외: " + t.getMessage());
            t.printStackTrace();
        }
    }

    // Apply any pending events that arrived before a spawn. Must be called on EDT.
    private void drainPendingEvents() {
        if (!javax.swing.SwingUtilities.isEventDispatchThread()) {
            javax.swing.SwingUtilities.invokeLater(() -> drainPendingEvents());
            return;
        }
        synchronized (pendingEvents) {
            while (!pendingEvents.isEmpty()) {
                PendingEvent ev = pendingEvents.poll();
                try {
                    switch (ev.type) {
                        case MOVE:
                            if (currentBlock != null) {
                                currentBlock.setPosition(ev.x, ev.y);
                                if (boardPanel != null) boardPanel.setRemoteBlock(currentBlock);
                                System.out.println("[REMOTE] ▶ applied queued MOVE to (" + ev.x + "," + ev.y + ")");
                            }
                            break;
                        case ROTATE:
                            if (currentBlock != null) {
                                currentBlock.getRotatedShape();
                                if (boardPanel != null) boardPanel.setRemoteBlock(currentBlock);
                                System.out.println("[REMOTE] ▶ applied queued ROTATE");
                            }
                            break;
                        case PLACE:
                            // Call placeBlock() to apply and render
                            placeBlock();
                            System.out.println("[REMOTE] ▶ applied queued PLACE");
                            break;
                    }
                } catch (Throwable t) {
                    System.err.println("[REMOTE] pending event 처리 중 예외: " + t.getMessage());
                    t.printStackTrace();
                }
            }
        }
    }
    
    public void updateScore(int score) {
        // Score is handled by label in P2PVersusFrameBoard
    }

    /**
     * Clear the specified rows from the remote board and apply gravity.
     * The rows are absolute indices matching the board array (same indexing as GameEngine).
     */
    public void clearLines(int[] clearedLines) {
        if (clearedLines == null || clearedLines.length == 0) return;
        try {
            java.util.Arrays.sort(clearedLines); // ascending
            int ROWS = remoteBoard.length;
            int COLS = remoteBoard[0].length;
            boolean[] remove = new boolean[ROWS];
            for (int r : clearedLines) {
                if (r >= 0 && r < ROWS) remove[r] = true;
            }

            int[][] newBoard = new int[ROWS][COLS];
            int[][] newColor = new int[ROWS][COLS];

            int writeR = ROWS - 1;
            for (int r = ROWS - 1; r >= 0; r--) {
                if (remove[r]) continue; // skip cleared rows
                // copy this row to writeR
                for (int c = 0; c < COLS; c++) {
                    newBoard[writeR][c] = remoteBoard[r][c];
                    newColor[writeR][c] = remoteColorBoard[r][c];
                }
                writeR--;
            }

            // fill remaining top rows with 0
            for (int r = writeR; r >= 0; r--) {
                for (int c = 0; c < COLS; c++) {
                    newBoard[r][c] = 0;
                    newColor[r][c] = 0;
                }
            }

            // replace boards
            this.remoteBoard = newBoard;
            this.remoteColorBoard = newColor;

            final game.core.GameState gs = new game.core.GameState.Builder(this.remoteBoard, this.remoteColorBoard, null, null, false).build();
            if (javax.swing.SwingUtilities.isEventDispatchThread()) {
                boardPanel.render(gs);
            } else {
                javax.swing.SwingUtilities.invokeLater(() -> boardPanel.render(gs));
            }
        } catch (Throwable t) {
            System.err.println("[REMOTE] clearLines 실패: " + t.getMessage());
            t.printStackTrace();
        }
    }

    /**
     * Apply attack rows visually to the remote panel (used by local sender to show
     * that an attack was sent). This mutates the internal remoteBoard similarly to
     * GameController.addAttackLines but only affects the visual remote board.
     */
    public void applyAttackVisual(int lines, int[][] blockPattern, int blockX) {
        if (lines <= 0) return;
        try {
            int ROWS = remoteBoard.length;
            int COLS = remoteBoard[0].length;
            int INNER_LEFT = 1;
            int INNER_RIGHT = COLS - 2;
            int INNER_TOP = 2;
            int INNER_BOTTOM = ROWS - 2;

            // shift up
            for (int r = INNER_TOP; r <= INNER_BOTTOM - lines; r++) {
                for (int c = INNER_LEFT; c <= INNER_RIGHT; c++) {
                    remoteBoard[r][c] = remoteBoard[r + lines][c];
                    remoteColorBoard[r][c] = remoteColorBoard[r + lines][c];
                }
            }

            // fill bottom with attack color
            for (int r = INNER_BOTTOM - lines + 1; r <= INNER_BOTTOM; r++) {
                for (int c = INNER_LEFT; c <= INNER_RIGHT; c++) {
                    remoteBoard[r][c] = 1;
                    remoteColorBoard[r][c] = 8;
                }
            }

            // carve holes based on blockPattern
            // If there are more attack lines than pattern rows, repeat the pattern
            if (blockPattern != null && blockPattern.length > 0) {
                int patternH = blockPattern.length;
                int patternW = blockPattern[0].length;
                for (int rOff = 0; rOff < lines; rOff++) {
                    int boardRow = INNER_BOTTOM - rOff;
                    int patternRow = rOff % patternH;
                    for (int j = 0; j < patternW; j++) {
                        int boardCol = blockX + j;
                        if (boardCol >= INNER_LEFT && boardCol <= INNER_RIGHT && patternRow >= 0
                                && patternRow < blockPattern.length && blockPattern[patternRow][j] == 1) {
                            remoteBoard[boardRow][boardCol] = 0;
                            remoteColorBoard[boardRow][boardCol] = 0;
                        }
                    }
                }
            }

            final game.core.GameState st = new game.core.GameState.Builder(this.remoteBoard, this.remoteColorBoard, null, null, false).build();
            if (javax.swing.SwingUtilities.isEventDispatchThread()) {
                if (boardPanel != null) boardPanel.render(st);
            } else {
                javax.swing.SwingUtilities.invokeLater(() -> { if (boardPanel != null) boardPanel.render(st); });
            }
        } catch (Throwable t) {
            System.err.println("[REMOTE] applyAttackVisual 실패: " + t.getMessage());
            t.printStackTrace();
        }
    }

    /**
     * Apply item effect to remote board view.
     * Supported types: "ALL_CLEAR", "BOX_CLEAR", "ONE_LINE_CLEAR", "CLEAR_LINE"
     */
    public void applyItemEffect(String itemType) {
        System.out.println("[REMOTE] ⚙️ applyItemEffect: " + itemType);
        try {
            int ROWS = remoteBoard.length;
            int COLS = remoteBoard[0].length;
            int INNER_TOP = 2;
            int INNER_BOTTOM = ROWS - 2;
            int INNER_LEFT = 1;
            int INNER_RIGHT = COLS - 2;

            switch (itemType) {
                case "ALL_CLEAR":
                    for (int r = INNER_TOP; r <= INNER_BOTTOM; r++) {
                        for (int c = INNER_LEFT; c <= INNER_RIGHT; c++) {
                            remoteBoard[r][c] = 0;
                            remoteColorBoard[r][c] = 0;
                        }
                    }
                    break;
                case "ONE_LINE_CLEAR":
                case "CLEAR_LINE":
                    // Find lowest non-empty row and clear it
                    for (int r = INNER_BOTTOM; r >= INNER_TOP; r--) {
                        boolean nonEmpty = false;
                        for (int c = INNER_LEFT; c <= INNER_RIGHT; c++) {
                            if (remoteBoard[r][c] != 0) { nonEmpty = true; break; }
                        }
                        if (nonEmpty) {
                            // clear row r and apply gravity
                            for (int cc = INNER_LEFT; cc <= INNER_RIGHT; cc++) {
                                remoteBoard[r][cc] = 0;
                                remoteColorBoard[r][cc] = 0;
                            }
                            // gravity
                            for (int row = r; row > INNER_TOP; row--) {
                                for (int cc = INNER_LEFT; cc <= INNER_RIGHT; cc++) {
                                    remoteBoard[row][cc] = remoteBoard[row-1][cc];
                                    remoteColorBoard[row][cc] = remoteColorBoard[row-1][cc];
                                }
                            }
                            // clear top
                            for (int cc = INNER_LEFT; cc <= INNER_RIGHT; cc++) {
                                remoteBoard[INNER_TOP][cc] = 0;
                                remoteColorBoard[INNER_TOP][cc] = 0;
                            }
                            break;
                        }
                    }
                    break;
                case "BOX_CLEAR":
                    // approximate: clear a 5x5 area centered near bottom-middle
                    int centerR = Math.max(INNER_TOP + 2, INNER_BOTTOM - 3);
                    int centerC = (INNER_LEFT + INNER_RIGHT) / 2;
                    for (int r = centerR - 2; r <= centerR + 2; r++) {
                        if (r < INNER_TOP || r > INNER_BOTTOM) continue;
                        for (int c = centerC - 2; c <= centerC + 2; c++) {
                            if (c < INNER_LEFT || c > INNER_RIGHT) continue;
                            remoteBoard[r][c] = 0;
                            remoteColorBoard[r][c] = 0;
                        }
                    }
                    // apply gravity column-wise
                    for (int cc = INNER_LEFT; cc <= INNER_RIGHT; cc++) {
                        int writeR = INNER_BOTTOM;
                        for (int r = INNER_BOTTOM; r >= INNER_TOP; r--) {
                            if (remoteBoard[r][cc] != 0) {
                                remoteBoard[writeR][cc] = remoteBoard[r][cc];
                                remoteColorBoard[writeR][cc] = remoteColorBoard[r][cc];
                                if (writeR != r) { remoteBoard[r][cc] = 0; remoteColorBoard[r][cc] = 0; }
                                writeR--;
                            }
                        }
                        for (int r = writeR; r >= INNER_TOP; r--) { remoteBoard[r][cc] = 0; remoteColorBoard[r][cc] = 0; }
                    }
                    break;
                case "WEIGHT_BLOCK":
                    // Approximate drill effect: clear a vertical segment in center columns
                    int weightCenter = (INNER_LEFT + INNER_RIGHT) / 2;
                    int[] colsToDrill = new int[] { weightCenter, weightCenter - 1, weightCenter + 1 };
                    int drillDepth = 4;
                    for (int cc : colsToDrill) {
                        if (cc < INNER_LEFT || cc > INNER_RIGHT) continue;
                        // find first occupied cell from top
                        for (int r = INNER_TOP; r <= INNER_BOTTOM; r++) {
                            if (remoteBoard[r][cc] != 0) {
                                // clear downwards up to drillDepth
                                for (int d = 0; d < drillDepth && (r + d) <= INNER_BOTTOM; d++) {
                                    remoteBoard[r + d][cc] = 0;
                                    remoteColorBoard[r + d][cc] = 0;
                                }
                                break; // move to next column
                            }
                        }
                    }
                    // apply gravity after drilling (column-wise)
                    for (int cc = INNER_LEFT; cc <= INNER_RIGHT; cc++) {
                        int writeR = INNER_BOTTOM;
                        for (int r = INNER_BOTTOM; r >= INNER_TOP; r--) {
                            if (remoteBoard[r][cc] != 0) {
                                remoteBoard[writeR][cc] = remoteBoard[r][cc];
                                remoteColorBoard[writeR][cc] = remoteColorBoard[r][cc];
                                if (writeR != r) { remoteBoard[r][cc] = 0; remoteColorBoard[r][cc] = 0; }
                                writeR--;
                            }
                        }
                        for (int r = writeR; r >= INNER_TOP; r--) { remoteBoard[r][cc] = 0; remoteColorBoard[r][cc] = 0; }
                    }
                    break;
                default:
                    System.out.println("[REMOTE] 알수없는 아이템: " + itemType + " - 적용 없이 반환");
                    break;
            }

            // Render new board state
            final game.core.GameState state = new game.core.GameState.Builder(this.remoteBoard, this.remoteColorBoard, null, null, false).build();
            if (javax.swing.SwingUtilities.isEventDispatchThread()) {
                boardPanel.render(state);
            } else {
                javax.swing.SwingUtilities.invokeLater(() -> boardPanel.render(state));
            }

        } catch (Throwable t) {
            System.err.println("[REMOTE] applyItemEffect 실패: " + t.getMessage());
            t.printStackTrace();
        }
    }
}

