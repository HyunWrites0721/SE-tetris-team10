package network;

import network.messages.GameControlMessage;
import network.messages.GameControlMessage.ControlType;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import versus.VersusMode;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

/**
 * 간단한 통합 테스트: MODE_SELECT -> READY -> START_GAME 핸드셰이크 흐름 검증
 */
public class P2PHandshakeIntegrationTest {

    @Test
    public void testHandshakeFlow() throws Exception {
        int port = 23456; // 테스트용 포트 (충돌 가능성 낮음)

        NetworkManager server = new NetworkManager();
        NetworkManager client = new NetworkManager();

        CountDownLatch connectedLatch = new CountDownLatch(2);
        CountDownLatch modeReceivedLatch = new CountDownLatch(1);
        CountDownLatch startReceivedLatch = new CountDownLatch(1);
        CountDownLatch readyReceivedLatch = new CountDownLatch(1);

        // 서버: 수신된 READY를 받으면 START_GAME 전송
        server.addGameControlListener(msg -> {
            if (msg.getControlType() == ControlType.READY) {
                readyReceivedLatch.countDown();
                // 서버가 START_GAME 전송
                server.sendMessage(new GameControlMessage(ControlType.START_GAME, VersusMode.NORMAL));
            }
        });

        // 클라이언트: MODE_SELECT 받으면 READY 전송, START_GAME 받으면 완료
        client.addGameControlListener(msg -> {
            if (msg.getControlType() == ControlType.MODE_SELECT) {
                modeReceivedLatch.countDown();
                client.sendMessage(new GameControlMessage(ControlType.READY));
            } else if (msg.getControlType() == ControlType.START_GAME) {
                startReceivedLatch.countDown();
            }
        });

        // 서버 시작 (비동기)
        Thread serverThread = new Thread(() -> {
            try {
                server.startAsServer(port);
                connectedLatch.countDown();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }, "test-server");
        serverThread.start();

        // 클라이언트 연결 (비동기)
        Thread clientThread = new Thread(() -> {
            try {
                // 잠시 대기 후 연결 시도
                Thread.sleep(200);
                client.connectAsClient("127.0.0.1", port);
                connectedLatch.countDown();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }, "test-client");
        clientThread.start();

        // 연결 대기
        boolean bothConnected = connectedLatch.await(5, TimeUnit.SECONDS);
        Assertions.assertTrue(bothConnected, "서버와 클라이언트가 연결되어야 합니다");

        // 서버가 MODE_SELECT 전송
        boolean sent = server.sendMessage(new GameControlMessage(ControlType.MODE_SELECT, VersusMode.NORMAL));
        Assertions.assertTrue(sent, "서버가 MODE_SELECT 메시지를 전송해야 합니다");

        // MODE_SELECT 수신 및 READY/START_GAME 흐름 대기
        boolean modeReceived = modeReceivedLatch.await(3, TimeUnit.SECONDS);
        Assertions.assertTrue(modeReceived, "클라이언트는 MODE_SELECT를 수신해야 합니다");

        boolean readyReceived = readyReceivedLatch.await(3, TimeUnit.SECONDS);
        Assertions.assertTrue(readyReceived, "서버는 READY를 수신해야 합니다");

        boolean startReceived = startReceivedLatch.await(3, TimeUnit.SECONDS);
        Assertions.assertTrue(startReceived, "클라이언트는 START_GAME을 수신해야 합니다");

        // 정리
        server.disconnect();
        client.disconnect();
    }
}
