import java.io.*;
import java.net.*;
import java.util.*;
import java.util.concurrent.*;

import javax.swing.Timer;

public class CatchServer {
    private final int port;
    private final List<ClientHandler> clients = new CopyOnWriteArrayList<>();
    private List<String> userInfo =  new ArrayList<String>(); //유저 정보 저장
    private List<Integer> existing = new ArrayList<Integer>(); // 유저 인덱스 저장
    private String quizcorrect=null;
    private CatchMindTimer timer; // 타이머 추가
    private final int TIMER_DURATION = 60; // 기본 타이머 시간 (60초)
    
    public CatchServer(int port) {
    	 // 타이머 초기화
        timer = new CatchMindTimer(
            TIMER_DURATION,
            this::onTimerExpired,    // 타이머 종료 시
            this::onTimerUpdate 
        );
        
        this.port = port;
    }

    public void start() throws IOException {
        ServerSocket serverSocket = new ServerSocket(port);
        System.out.println("Server started on port " + port);

        try {
            while (true) {
                Socket clientSocket = serverSocket.accept();
                ClientHandler clientHandler = new ClientHandler(clientSocket);
                clients.add(clientHandler);
                new Thread(clientHandler).start();
            }
        } finally {
            serverSocket.close();
        }
    }
    
    private void onTimerExpired() {
        for (ClientHandler client : clients) {
            client.send("TIMER:expired");
            timer.reset(TIMER_DURATION); 
        }
    }

    private void onTimerUpdate() { // 시간 매 초 모두에게 보냄 
        int timeRemaining = timer.getTimeRemaining();
        for (ClientHandler client : clients) {
            client.send("TIMER:" + timeRemaining);
        }
    }

    private class ClientHandler implements Runnable {
        private final Socket clientSocket;
        private DataOutputStream output;

        public ClientHandler(Socket socket) throws IOException {
            this.clientSocket = socket;
            this.output = new DataOutputStream(socket.getOutputStream());
        }

        public void run() {
            try (DataInputStream input = new DataInputStream(clientSocket.getInputStream())) {
            	
                String inputLine;
                
                for (ClientHandler client : clients) {
                	client.send(Integer.toString(clients.size())); // 클라이언트 수 보냄
                }
                while ((inputLine = input.readUTF()) != null) {
                	if (inputLine.startsWith("DRAW:")){ // 그리기일때 -> 모두에게 그대로 보냄
	                    for (ClientHandler client : clients) {
	                        if (client != this) {
	                            client.send(inputLine);
	                        }
	                    }
                	}
                	else if (inputLine.startsWith("users:")) // 유저 정보일때 -> 유저 번호와 함께 보냄
                	{
                		String user = Integer.toString(clients.size()-1) + " " + inputLine; 
                		System.out.println(user);
                		
                		String info = inputLine.replace("users:", ""); // users: 빠지고 이미지 주소, 이름만 
                		
                		userInfo.add(info); // "이미지주소 이름" 추가
                		
                		int here = -1;
                		boolean found = false;
                		for(int i=0;i<4;i++) { // 0~3
                			if (existing.size()== 0)
                				here = 0;
                			else {
	                			if(existing.contains(i)) {
	                				continue;
	                			}
                				else {
                					here = i;
                					found = true;
                					break;
                				}
	                		}
                			if (found) break; 
                		}
                		existing.add(here);// 유저 들어와있는 인덱스 (0~3)
                		System.out.println(existing);
                		
                		send("Your client number:" + existing.get(clients.size()-1) );
                		
                		
                		
                		String ext = "";
                		String users = "";
                		for(int i=0;i<existing.size();i++) {
                			ext = ext + existing.get(i) + " ";
                			users = users + userInfo.get(i) + ",";
                		}
                		for (ClientHandler client : clients) {
                			client.send("all userinfos:" + ext + "*" + users);
	                    }
                		
                		
                		
                	}
                	else if (inputLine.startsWith("CHAT:")){ // 채팅일때
	                    for (ClientHandler client : clients) {
	                        if (client != this) {
	                            client.send(inputLine);
	                        }
	                    }
                	}
                	else if (inputLine.startsWith("CORRECT:")){ // 정답세팅
                		timer.reset(TIMER_DURATION); 
                		quizcorrect = inputLine.replace("CORRECT:", "");
                		for (ClientHandler client : clients) {
	                        if (client != this) {
	                            client.send(inputLine);
	                        }
	                    }
                	}
                	else if (inputLine.startsWith("start")) {//타이머
                		/// 타이머 시작 + 타이머 시간 모두에게 보냄
                		timer.reset(TIMER_DURATION); // 타이머 초기화 후 시작
                	}
                	else if (inputLine.startsWith("SCORE:")) {
                	    String[] scoreInfo = inputLine.split(":");
                	    String playerName = scoreInfo[1];
                	    int score = Integer.parseInt(scoreInfo[2]);
                	    //scores.put(playerName, score); // 서버의 점수 저장소 업데이트
                	    for (ClientHandler client : clients) {
                	    	client.send("SCORE_UPDATE:" + playerName + ":" + score);
                	    }// 모든 클라이언트에 점수 브로드캐스트
                	}
                	
                }
            } catch (IOException e) {
                System.out.println("Client disconnected: " );
            } finally {
                try { /// 유저 나갔을 때 
                	int index = clients.indexOf(this); 
                	// 이 인덱스에 있는 userinfo도 삭제
                	userInfo.remove(index);
                	existing.remove(index); 
                	
                	
                	// 다른 유저들 한테도 이 인덱스의 유저가 나갔음을 알려야 함 -> 프로필 삭제
                	String ext = "";
            		String users = "";
            		for(int i=0;i<existing.size();i++) {
            			ext = ext + existing.get(i) + " ";
            			users = users + userInfo.get(i) + ",";
            		}
            		for (ClientHandler client : clients) {
                        if (client != this) {
                        	client.send("all userinfos:" + ext + "*" + users);
                        }
                    }
                	
            		clients.remove(this);
                    clientSocket.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }

        public void send(String message) {
            try {
                output.writeUTF(message);
            } catch (IOException e) {
                System.out.println("Error sending message to client: " + e.getMessage());
                clients.remove(this);
                try {
                    clientSocket.close();
                } catch (IOException ex) {
                    ex.printStackTrace();
                }
            }
        }
    }

    public static void main(String[] args) {
        int port = 30000; // Use your desired port
        try {
            new CatchServer(port).start();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
