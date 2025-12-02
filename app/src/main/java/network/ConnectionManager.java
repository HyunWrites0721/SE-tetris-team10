package network;

import java.io.*;
import java.net.*;
import java.util.concurrent.atomic.AtomicBoolean;  //Thread-safe한 boolean 값 제공

/**
 * 네트워크 연결 관리자
 * Socket 연결을 생성하고 관리하며, 입출력 스트림을 제공합니다.
 */
public class ConnectionManager {
    private NetworkRole role;                    // 현재 역할 (SERVER 또는 CLIENT)
    private ServerSocket serverSocket;           // 서버 소켓 (서버 모드에서만 사용)
    private Socket socket;                       // 연결된 소켓 (양방향 통신용)
    private ObjectOutputStream out;              // 객체 송신 스트림
    private ObjectInputStream in;                // 객체 수신 스트림
    
    private final AtomicBoolean isConnected = new AtomicBoolean(false);  // 연결 상태 (멀티스레드 안전)
    private ConnectionState state = ConnectionState.DISCONNECTED;        // 현재 연결 상태
    private volatile boolean normalShutdown = false;                     // 정상 종료 플래그
    
    private String localAddress;                 // 내 IP 주소
    private int localPort;                       // 내 포트 번호
    private String remoteAddress;                // 상대방 IP 주소
    private int remotePort;                      // 상대방 포트 번호
    
    /**
     * 서버로 시작
     * @param port 리스닝할 포트 번호
     * @throws ConnectionException 서버 시작 실패 시
     */
    public void startServer(int port) throws ConnectionException {
        try {
            normalShutdown = false;  // 정상 종료 플래그 초기화
            
            // 서버 소켓 생성
            serverSocket = new ServerSocket();
            serverSocket.setReuseAddress(true); // 포트 재사용 허용 (좀비 프로세스 방지)
            serverSocket.bind(new InetSocketAddress(port));
            serverSocket.setSoTimeout(NetworkConfig.CONNECTION_TIMEOUT);    // 10초 타임아웃 설정
            
            // 서버 역할과 정보 즉시 설정 (accept 전에!)
            this.role = NetworkRole.SERVER;  // 서버 역할 할당
            this.localPort = port;    
            this.localAddress = getLocalIPAddress();
            
            state = ConnectionState.CONNECTING;
            System.out.println("서버 시작: " + localAddress + ":" + localPort);
            System.out.println("클라이언트 연결 대기 중...");
            
            // 클라이언트 연결 대기 (블로킹)
            socket = serverSocket.accept();  // 여기서 최대 10초 대기 (10초 이내에 클라이언트 접속)
            
            // 스트림 초기화
            initializeStreams();
            
            // 연결 정보 저장
            this.remoteAddress = socket.getInetAddress().getHostAddress();   // 상대 IP주소 저장
            this.remotePort = socket.getPort();   // 상대 포트 번호 저장 (OS가 무작위로 포트 번호 할당)
            
            isConnected.set(true);    // 연결됨
            state = ConnectionState.CONNECTED;  // 연결됨 상태로 저장
            
            System.out.println("클라이언트 연결 완료: " + remoteAddress + ":" + remotePort);
            
        } catch (SocketTimeoutException e) {
            state = ConnectionState.TIMEOUT;
            throw new ConnectionException("연결 대기 시간 초과", e);
        } catch (IOException e) {
            // 정상 종료인 경우 예외를 던지지 않음
            if (normalShutdown) {
                System.out.println("서버 정상 종료");
                state = ConnectionState.DISCONNECTED;
                return;
            }
            state = ConnectionState.DISCONNECTED;
            throw new ConnectionException("서버 시작 실패: " + e.getMessage(), e);
        }
    }
    
    /**
     * 클라이언트로 서버에 연결
     * @param host (접속할) 서버 IP 주소
     * @param port (접속할) 서버 포트 번호
     * @throws ConnectionException 연결 실패 시
     */
    // 클라이언트가 서버에 접속할 때 호출 
    public void connectToServer(String host, int port) throws ConnectionException {   // 서버 IP & 포트 넘버 호출
        try {
            this.role = NetworkRole.CLIENT;
            this.remoteAddress = host;
            this.remotePort = port;
            
            state = ConnectionState.CONNECTING;
            System.out.println("서버에 연결 중: " + host + ":" + port);
            
            // 서버에 연결
            socket = new Socket();
            socket.connect(                          // 실제 TCP 연결을 수립
                new InetSocketAddress(host, port),   // 서버 주소 & 포트번호 <- 여기로 연결, 
                 NetworkConfig.CONNECTION_TIMEOUT);  // 타임아웃 시간 적용
            
            // 스트림 초기화
            initializeStreams();
            
            // 로컬 연결 정보
            this.localAddress = socket.getLocalAddress().getHostAddress();
            this.localPort = socket.getLocalPort();
            
            isConnected.set(true);
            state = ConnectionState.CONNECTED;
            
            System.out.println("서버 연결 완료!");
            
        } catch (SocketTimeoutException e) {
            state = ConnectionState.TIMEOUT;
            throw new ConnectionException("연결 시간 초과", e);
        } catch (IOException e) {
            state = ConnectionState.DISCONNECTED;
            throw new ConnectionException("서버 연결 실패: " + e.getMessage(), e);
        }
    }
    
    /**
     * 입출력 스트림 초기화
     */
    private void initializeStreams() throws IOException {
        // OutputStream을 먼저 생성해야 deadlock 방지 (데이터 보내기용)
        out = new ObjectOutputStream(socket.getOutputStream());
        out.flush();   // 버퍼에 쌓여있는 데이터 즉시 전송
        
        in = new ObjectInputStream(socket.getInputStream());  // InputStream 생성 (데이터 받기용)
        
        // 타임아웃 설정 (5초)
        socket.setSoTimeout(NetworkConfig.READ_TIMEOUT);
    }
    
    /**
     * 연결 종료
     */
    public void disconnect() {
        normalShutdown = true;  // 정상 종료 플래그 설정
        isConnected.set(false);
        state = ConnectionState.DISCONNECTED;
        
        System.out.println("연결 종료 중...");
        
        // 스트림 닫기
        closeQuietly(out);
        closeQuietly(in);
        
        // 소켓 닫기
        closeQuietly(socket);
        closeQuietly(serverSocket);
        
        System.out.println("연결 종료 완료");
    }
    
    /**
     * 조용히 닫기 (예외 무시)
     */
    private void closeQuietly(Closeable closeable) {
        if (closeable != null) {
            try {
                closeable.close();
            } catch (IOException e) {
                // 무시
            }
        }
    }
    
    /**
     * 로컬 IP 주소 가져오기
     */
    private String getLocalIPAddress() {
        try {
            // 네트워크 인터페이스 순회
            var interfaces = NetworkInterface.getNetworkInterfaces();
            String fallbackIP = null;
            
            while (interfaces.hasMoreElements()) {
                NetworkInterface iface = interfaces.nextElement();
                
                // 루프백과 비활성 인터페이스 제외
                if (iface.isLoopback() || !iface.isUp()) {
                    continue;
                }
                
                // IP 주소 순회
                var addresses = iface.getInetAddresses();
                while (addresses.hasMoreElements()) {
                    InetAddress addr = addresses.nextElement();
                    
                    // IPv4만 사용
                    if (addr instanceof Inet4Address && !addr.isLoopbackAddress()) {
                        String ip = addr.getHostAddress();
                        
                        // 사설 네트워크 대역이면 바로 반환
                        if (ip.startsWith("192.168.") || ip.startsWith("10.") || ip.startsWith("172.")) {
                            return ip;
                        }
                        
                        // 공인 IP는 fallback으로 저장
                        if (fallbackIP == null) {
                            fallbackIP = ip;
                        }
                    }
                }
            }
            
            // 사설 IP 못 찾으면 fallback 또는 localhost
            if (fallbackIP != null) {
                return fallbackIP;
            }
            return InetAddress.getLocalHost().getHostAddress();
            
        } catch (Exception e) {
            return "127.0.0.1";
        }
    }
    
    // ===== Getter 메서드 =====
    
    public boolean isConnected() {
        return isConnected.get();
    }
    
    public boolean isServer() {
        return role == NetworkRole.SERVER;
    }
    
    public boolean isClient() {
        return role == NetworkRole.CLIENT;
    }
    
    public ConnectionState getState() {
        return state;
    }
    
    public void setState(ConnectionState state) {
        this.state = state;
    }
    
    public NetworkRole getRole() {
        return role;
    }
    
    public String getLocalAddress() {
        return localAddress;
    }
    
    public int getLocalPort() {
        return localPort;
    }
    
    public String getRemoteAddress() {
        return remoteAddress;
    }
    
    public int getRemotePort() {
        return remotePort;
    }
    
    public ObjectOutputStream getOutputStream() {
        return out;
    }
    
    public ObjectInputStream getInputStream() {
        return in;
    }
    
    public Socket getSocket() {
        return socket;
    }
    
    /**
     * 서버 소켓이 실행 중인지 확인
     */
    public boolean isServerRunning() {
        return serverSocket != null && !serverSocket.isClosed();
    }
    
    /**
     * 연결 정보 문자열
     */
    @Override
    public String toString() {
        return String.format("ConnectionManager[role=%s, state=%s, local=%s:%d, remote=%s:%d]",
            role, state, localAddress, localPort, remoteAddress, remotePort);
    }
}
