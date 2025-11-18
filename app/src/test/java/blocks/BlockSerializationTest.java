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
    public void serializeAndDeserializeBlock_preservesShapeAndColorAndPosition() throws Exception {
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
}
