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
    
    public RemoteGamePanel() {
    }
    
    public void setRemoteComponents(GameView view, GameController controller) {
        // GameView에서 GameBoardPanel 가져오기
        for (java.awt.Component comp : view.getComponents()) {
            if (comp instanceof GameBoardPanel) {
                this.boardPanel = (GameBoardPanel) comp;
                break;
            }
        }
        System.out.println("[REMOTE] ✅ Components 설정 완료: boardPanel=" + (boardPanel != null));
    }
    
    public void spawnBlock(Block block) {
        System.out.println("[REMOTE] 🔵 spawnBlock: " + block.getClass().getSimpleName() + " at (" + block.getX() + ", " + block.getY() + ")");
        this.currentBlock = block;
        if (boardPanel != null) {
            boardPanel.setRemoteBlock(block);
        }
    }
    
    public void moveBlock(int x, int y) {
        if (currentBlock == null) {
            System.err.println("[REMOTE] ❌ moveBlock: currentBlock is NULL!");
            return;
        }
        
        currentBlock.setPosition(x, y);
        if (boardPanel != null) {
            boardPanel.setRemoteBlock(currentBlock);
        }
        System.out.println("[REMOTE] 📍 moved to (" + x + ", " + y + ")");
    }
    
    public void rotateBlock() {
        if (currentBlock == null) {
            System.err.println("[REMOTE] ❌ rotateBlock: currentBlock is NULL!");
            return;
        }
        
        currentBlock.getRotatedShape();
        if (boardPanel != null) {
            boardPanel.setRemoteBlock(currentBlock);
        }
        System.out.println("[REMOTE] 🔄 rotated");
    }
    
    public void placeBlock() {
        if (currentBlock == null) {
            System.err.println("[REMOTE] ❌ placeBlock: currentBlock is NULL!");
            return;
        }
        
        System.out.println("[REMOTE] 🔻 placeBlock");
        
        // TODO: 블록을 보드에 고정 (나중에 구현)
        
        this.currentBlock = null;
        if (boardPanel != null) {
            boardPanel.setRemoteBlock(null);
        }
    }
    
    public void updateScore(int score) {
        // Score is handled by label in P2PVersusFrameBoard
    }
}

