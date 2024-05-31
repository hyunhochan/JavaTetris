package tetrisProject;

import java.io.*;
import java.util.*;
import java.net.*;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Scanner;


public class TetrisServer {

	static ArrayList<ServerThread> list = new ArrayList<>();
	private int readyCount = 0; // 준비된 클라이언트 수를 추적하는 카운터, "게임을 플레이하는" 인원 수
	static int clientCount = 1;
	static int haveLeft = 0; //게임오버된 사람 
	private final int maxPlayers = 6; // 최대 플레이어 수
	

	public static void main(String[] args) throws IOException {
		ServerSocket ssocket = new ServerSocket(5123);
		System.out.println("Server loading...");
		TetrisServer server = new TetrisServer(); // Instantiate the server
		Socket s;
		boolean said = false;

		while (true) {
				if (clientCount < server.maxPlayers+1) { // Check if the maximum players have been reached
					s = ssocket.accept();
		
					DataInputStream is = new DataInputStream(s.getInputStream());
					DataOutputStream os = new DataOutputStream(s.getOutputStream());
		
					ServerThread thread = new ServerThread(server, s, "client " + clientCount, clientCount, is, os);
					list.add(thread);
					thread.start();
					System.out.println("Server Accepted... " + clientCount);
					clientCount++;
				} else {
					if(!said) {
		                System.out.println("Maximum players reached. No more connections are being accepted.");
		                // Optionally, you can close the ServerSocket to completely stop accepting new requests
		                // ssocket.close();
		                // break; // Use this if you want the server to stop accepting ANY connections
						}
		            said = true;
	            }
		}
	}
	
	public synchronized void incrementReadyCount() {
		readyCount++;
		
	}
	
	
	
	// readyCount에 접근하는 동기화된 메서드
    public synchronized int getReadyCount() {
        return readyCount;
    }
    
 // clientcount에 접근하는 동기화된 메서드
    public synchronized int getclientCount() {
        return clientCount;
    }
    
    // haveLeft에 접근하는 동기화된 메서드
    public synchronized int gethaveLeft() {
        return haveLeft;
    }
    
 // haveLeft에 접근하는 동기화된 메서드
    public synchronized void incrementhaveLeft() {
    	haveLeft++;
    }
    
 // haveLeft에 접근하는 동기화된 메서드
    public synchronized void resetRdyCount() {
    	readyCount = 0;
    }
    
 // haveLeft에 접근하는 동기화된 메서드
    public synchronized void resethaveLeft() {
    	haveLeft = 0;
    }

 // 모든 플레이어가 준비되었을 때 게임을 시작하는 메소드입니다.
    public void startGame() {
        // 모든 클라이언트에게 게임 시작 신호를 보냅니다.
    	for (ServerThread t : TetrisServer.list) {
            try {
                t.os.writeUTF("START_GAME");
                t.os.flush(); // 데이터가 실제로 전송되도록 flush 호출이 중요합니다.
                
            } catch (IOException e) {
                e.printStackTrace(); // 오류가 발생하면 콘솔에 출력합니다.
            }
        }
        // 게임 로직 시작
    }
}

class ServerThread extends Thread {
	Scanner scn = new Scanner(System.in);
	private String name;
	private int myNum;
	private int clientCount;
	final DataInputStream is;
	final DataOutputStream os;
	Socket s;
	boolean active;
	boolean hasSentNumber = false;
	TetrisServer server;
	private final Object serverLock = new Object();

	public ServerThread(TetrisServer server, Socket s, String name, int myNum, DataInputStream is, DataOutputStream os) {
		this.is = is;
		this.os = os;
		this.name = name;
		this.myNum = myNum;
		this.s = s;
		this.active = true;
		this.server = server;
	}

	@Override
	public void run() {
		String message;
		while (true) {
			try {
				if(hasSentNumber == false) {
					for (ServerThread t : TetrisServer.list) {
						t.os.writeUTF("YourNumber:" + myNum);
				        t.os.flush();
				        hasSentNumber = true;
					}
				}
				
				message = is.readUTF();
				System.out.println(message);
				if (message.equals("logout")) {
					this.active = false;
					this.s.close();
					break;
				}// 메시지가 "READY"면 호출합니다.
                else if ("READY".equals(message)) {
                	synchronized(server) {
                		server.incrementReadyCount(); // Synchronize access to readyCount
	                	int readyCount = server.getReadyCount();
	                	int clientCount = server.getclientCount();
	                	for (ServerThread t : TetrisServer.list) {
	    					t.os.writeUTF(this.name + " " + message + ", Total Ready Count : " + readyCount);
	                		t.os.writeUTF(this.name + " : " + message);
	    				}
	                	if (readyCount == clientCount-1) {
	                		//예를들어 1명이면 클카 2(아까 위에서 ++해줘서), 레카 1 // 6명이면 클카 7, 레카 6
	            			server.startGame();
	            		}
                	}
                }// 메시지가 "OVER를 포함하면 호출합니다.
	            else if (message.contains("OVER")) {
	            	if(!message.contains("ALL")) { //단일 플레이어의 종료일 경우
	            
		            	int clientNum = Integer.parseInt(message.substring(5));
		            	
		            	synchronized(server) {
		                	int haveLeft = server.gethaveLeft(); //1명일 경우 0 반환
		                	int readyCount = server.getReadyCount(); //1명일 경우 1 반환
		                	int yourRank = readyCount - haveLeft; //1명일 경우 1이 됨.(전체 레디 인원수 - 죽은사람 인수)
		                	//System.out.println("server.gethaveLeft() " + server.gethaveLeft() + " " +  "server.getReadyCount() " + server.getReadyCount());
		                	server.incrementhaveLeft(); // left, count는 Rank를 위해 갖고 온 것이라 decrement는 마지막에.
		                	//System.out.println(haveLeft + readyCount + yourRank);
		                	for (ServerThread t : TetrisServer.list) {
		    					t.os.writeUTF(this.name + " " + "OVER" + "," + clientNum + "," + yourRank);
		    				}
		                	if (readyCount  == haveLeft) { //다뒤지면
		                		for (ServerThread t : TetrisServer.list) {
			    					t.os.writeUTF("ALL OVER");
			    				}
		            		}
		            	}
	            	}else { //전체 플레이어가 모두 종료되었을 경우
	            		for (ServerThread t : TetrisServer.list) {
	            			server.resethaveLeft();
	            			server.resetRdyCount();
	    					t.os.writeUTF("FINISH");
	    				}
	            	}
	            }
				// 메시지가 "removed를 포함하면 호출합니다.
	            else if (message.contains("removed")) {
	            	synchronized (serverLock) {
	            	String[] parts = message.substring(10).split(",");
                    int clientNum = Integer.parseInt(parts[0].trim());
                    int removedlines = Integer.parseInt(parts[1].trim());
                    int combo = Integer.parseInt(parts[2].trim());
                    int randomNum = Integer.parseInt(parts[3].trim());
                    //System.out.println("clientNum" + clientNum + "removedlines" + removedlines);
                 // 모든 클라이언트에게 특정 클라이언트의 위치를 알립니다.
                    for (ServerThread t : TetrisServer.list) {
                        t.os.writeUTF(this.name + " " + "removed" + "," + clientNum + "," + removedlines + "," + combo + "," + randomNum);
                        t.os.flush();
                    }
	            	}
	            }
				// 메시지가 "gotItem"을 포함하면 호출합니다.
	            else if (message.contains("gotItem")) {
	            	synchronized (serverLock) {
	            	String[] parts = message.substring(10).split(",");
                    int clientNum = Integer.parseInt(parts[0].trim());
                    int itemType = Integer.parseInt(parts[1].trim());
                    
                 
                    for (ServerThread t : TetrisServer.list) {
                        t.os.writeUTF(this.name + " " + "gotItem" + "," + clientNum + "," + itemType);
                        t.os.flush();
                    }
	            	}
	            }
				// 메시지가 "usedItem"을 포함하면 호출합니다.
	            else if (message.contains("usedItem")) {
	            	synchronized (serverLock) {
	            	String[] parts = message.substring(10).split(",");
                    int clientNum = Integer.parseInt(parts[0].trim());
                    int itemType = Integer.parseInt(parts[1].trim());
                    int calledNumber = Integer.parseInt(parts[2].trim());
                    int randomNum = Integer.parseInt(parts[3].trim());
                 
                    for (ServerThread t : TetrisServer.list) {
                        t.os.writeUTF(this.name + " " + "usedItem" + "," + clientNum + "," + itemType + "," + calledNumber + "," + randomNum);
                        t.os.flush();
                    }
	            	}
	            }
				// 메시지가 "rotate"를 포함하면 호출합니다.
	            else if (message.contains("rotate")) {
	            	synchronized (serverLock) {
	            	String[] parts = message.substring(8).split(",");
                    int clientNum = Integer.parseInt(parts[0].trim());
                    
                    for (ServerThread t : TetrisServer.list) {
                        t.os.writeUTF(this.name + " " + "rotate" + "," + clientNum);
                        t.os.flush();
                    }
	            	}
	            }
				// 위치 메시지 처리
                else if (message.startsWith("Position: ")) {
                	synchronized (serverLock) {
                    String[] parts = message.substring(10).split(",");
                    int clientNum = Integer.parseInt(parts[0].trim());
                    int x = Integer.parseInt(parts[1].trim());
                    int y = Integer.parseInt(parts[2].trim());
                    int type = Integer.parseInt(parts[3].trim());
                    int orientation = Integer.parseInt(parts[4].trim());
                    boolean hasItemBlock = Boolean.parseBoolean(parts[5].trim());
                    int itemIndex = Integer.parseInt(parts[6].trim());
                    int itemType = Integer.parseInt(parts[7].trim());
                    
                    // 모든 클라이언트에게 특정 클라이언트의 위치를 알립니다.
                    for (ServerThread t : TetrisServer.list) {
                        t.os.writeUTF(this.name + " landed : " + clientNum + "," + x + "," + y + "," + type + "," + orientation + "," + hasItemBlock + "," + itemIndex + "," + itemType);
                        t.os.flush();
                    }
                	}
                }else {
                	for (ServerThread t : TetrisServer.list) {
    					t.os.writeUTF(this.name + " : " + message);
    				}
                }
                
				
			} catch (IOException e) {
				e.printStackTrace();
				break;
			}
		}
		try {
			this.is.close();
			this.os.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}

