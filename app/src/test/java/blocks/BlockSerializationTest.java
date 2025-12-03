package blocks;

import java.awt.Color;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.Test;

/**
 * Block 직렬화/역직렬화 테스트
 */
public class BlockSerializationTest {

    @Test
    public void testSerializeAndDeserializeBlock_preservesShapeAndColorAndPosition() throws Exception {
        // 블록 생성
        Block.reloadSettings();
        Block b = Block.spawn();
        // 색상과 위치 설정
        b.setExactColor(Color.MAGENTA);
        b.setPosition(4, 2);

        // 직렬화
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        try (ObjectOutputStream oos = new ObjectOutputStream(baos)) {
            oos.writeObject(b);
        }

        // 역직렬화
        ByteArrayInputStream bais = new ByteArrayInputStream(baos.toByteArray());
        Block deserialized;
        try (ObjectInputStream ois = new ObjectInputStream(bais)) {
            Object obj = ois.readObject();
            assertTrue(obj instanceof Block, "Deserialized object should be a Block");
            deserialized = (Block) obj;
        }

        // 상태 검증: shape, color, position
        assertNotNull(deserialized.getShape(), "Shape should be preserved");
        assertEquals(b.getShape().length, deserialized.getShape().length, "Shape rows should match");
        assertEquals(b.getShape()[0].length, deserialized.getShape()[0].length, "Shape cols should match");

        // Color는 java.awt.Color가 Serializable이라 저장되며 equals로 비교
        assertEquals(b.getColor(), deserialized.getColor(), "Color should be preserved after serialization");

        // 위치(x,y)
        assertEquals(b.getX(), deserialized.getX(), "X position should be preserved");
        assertEquals(b.getY(), deserialized.getY(), "Y position should be preserved");

        // transient 필드는 직렬화로 사라지므로 null이어야 함
        // (역직렬화 후에는 외부에서 bind(...) 를 호출해 재연결해야 함)
        // 단순히 동작 확인용으로 null 여부 체크
        // gameBoard/gameModel 필드는 transient로 정의되어야 함
        // (접근 제한자가 public이므로 리플렉션 없이 접근 가능)
        // 여기서는 null일 수 있음을 확인한다.
        try {
            java.lang.reflect.Field fb = Block.class.getDeclaredField("gameBoard");
            fb.setAccessible(true);
            Object gb = fb.get(deserialized);
            assertNull(gb, "gameBoard should be transient and null after deserialization");
        } catch (NoSuchFieldException nsf) {
            // 필드가 없으면 패스(구버전 가능성)
        }
    }
    
    @Test
    public void testSerializeAllBlockTypes() throws Exception {
        // 모든 블록 타입이 직렬화/역직렬화 가능한지 테스트
        Block[] blocks = {
            new IBlock(), new JBlock(), new LBlock(), new OBlock(),
            new SBlock(), new TBlock(), new ZBlock()
        };
        
        for (Block original : blocks) {
            original.setShape();
            original.setPosition(5, 10);
            
            // 직렬화
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            try (ObjectOutputStream oos = new ObjectOutputStream(baos)) {
                oos.writeObject(original);
            }
            
            // 역직렬화
            ByteArrayInputStream bais = new ByteArrayInputStream(baos.toByteArray());
            Block deserialized;
            try (ObjectInputStream ois = new ObjectInputStream(bais)) {
                deserialized = (Block) ois.readObject();
            }
            
            // 검증
            assertNotNull(deserialized, original.getClass().getSimpleName() + "은 역직렬화되어야 합니다");
            assertEquals(original.getX(), deserialized.getX(), "X 위치가 보존되어야 합니다");
            assertEquals(original.getY(), deserialized.getY(), "Y 위치가 보존되어야 합니다");
            assertEquals(original.getColor(), deserialized.getColor(), "색상이 보존되어야 합니다");
        }
    }
    
    @Test
    public void testSerializeItemBlocks() throws Exception {
        // 아이템 블록도 직렬화/역직렬화 가능한지 테스트
        blocks.item.AllClearBlock allClear = new blocks.item.AllClearBlock();
        allClear.setShape();
        allClear.setPosition(5, 2);
        
        // 직렬화
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        try (ObjectOutputStream oos = new ObjectOutputStream(baos)) {
            oos.writeObject(allClear);
        }
        
        // 역직렬화
        ByteArrayInputStream bais = new ByteArrayInputStream(baos.toByteArray());
        Block deserialized;
        try (ObjectInputStream ois = new ObjectInputStream(bais)) {
            deserialized = (Block) ois.readObject();
        }
        
        // 검증
        assertNotNull(deserialized, "아이템 블록도 역직렬화되어야 합니다");
        assertTrue(deserialized instanceof blocks.item.AllClearBlock, 
            "역직렬화된 객체는 AllClearBlock 타입이어야 합니다");
        assertEquals(5, deserialized.getX());
        assertEquals(2, deserialized.getY());
    }
    
    @Test
    public void testSerializeRotatedBlock() throws Exception {
        // 회전된 블록도 직렬화/역직렬화 가능한지 테스트
        IBlock block = new IBlock();
        block.setShape();
        block.getRotatedShape(); // 회전
        block.setPosition(3, 7);
        
        int[][] originalShape = deepCopy(block.getShape());
        
        // 직렬화
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        try (ObjectOutputStream oos = new ObjectOutputStream(baos)) {
            oos.writeObject(block);
        }
        
        // 역직렬화
        ByteArrayInputStream bais = new ByteArrayInputStream(baos.toByteArray());
        Block deserialized;
        try (ObjectInputStream ois = new ObjectInputStream(bais)) {
            deserialized = (Block) ois.readObject();
        }
        
        // 검증: 회전된 shape이 그대로 보존되어야 함
        int[][] deserializedShape = deserialized.getShape();
        assertEquals(originalShape.length, deserializedShape.length, "회전된 shape의 행 크기가 보존되어야 합니다");
        assertEquals(originalShape[0].length, deserializedShape[0].length, "회전된 shape의 열 크기가 보존되어야 합니다");
    }
    
    @Test
    public void testBindMethod_FieldExists() {
        // bind 메서드와 gameBoard 필드 존재 확인
        Block block = new TBlock();
        block.setShape();
        
        // gameBoard 필드가 존재하는지 확인
        try {
            java.lang.reflect.Field fb = Block.class.getDeclaredField("gameBoard");
            fb.setAccessible(true);
            assertNotNull(block, "블록 자체는 null이 아니어야 합니다");
        } catch (Exception e) {
            throw new AssertionError("gameBoard 필드 접근 실패: " + e.getMessage());
        }
    }
    
    @Test
    public void testDeserializedBlock_GameBoardIsNull() throws Exception {
        // 직렬화 후 역직렬화하면 transient 필드(gameBoard)가 null이 되는지 테스트
        Block original = new TBlock();
        original.setShape();
        original.setPosition(4, 8);
        
        // 직렬화
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        try (ObjectOutputStream oos = new ObjectOutputStream(baos)) {
            oos.writeObject(original);
        }
        
        // 역직렬화
        ByteArrayInputStream bais = new ByteArrayInputStream(baos.toByteArray());
        Block deserialized;
        try (ObjectInputStream ois = new ObjectInputStream(bais)) {
            deserialized = (Block) ois.readObject();
        }
        
        // 검증: transient 필드는 역직렬화 후 null이어야 함
        try {
            java.lang.reflect.Field fb = Block.class.getDeclaredField("gameBoard");
            fb.setAccessible(true);
            Object gb = fb.get(deserialized);
            assertNull(gb, "역직렬화 후 gameBoard(transient 필드)는 null이어야 합니다");
        } catch (Exception e) {
            throw new AssertionError("gameBoard 필드 확인 실패: " + e.getMessage());
        }
        
        // 다른 필드들은 정상적으로 복원되어야 함
        assertEquals(4, deserialized.getX());
        assertEquals(8, deserialized.getY());
    }
    
    private int[][] deepCopy(int[][] src) {
        int[][] dst = new int[src.length][];
        for (int i = 0; i < src.length; i++) {
            dst[i] = java.util.Arrays.copyOf(src[i], src[i].length);
        }
        return dst;
    }
}
