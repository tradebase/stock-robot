package model.robot;

import java.io.BufferedReader;

import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.net.Socket;

/**
 * Class for receiving notifications that there's new stock data.
 * <p>
 * Receives a message from Connector class on port 45000,
 * containing the time in ms on the latest parsing.
 * <p>
 * @author Erik
 *
 */
public class AstroReciever {
	
	private final int PORT_NR = 45000;
	private final int PING_PORT_NR = 45001;
	private final String SERVER_ADRESS = "localhost";
	private final int RECIEVE_DELAY = 300;
	private final int PING_DELAY = 450;
	
	private boolean newData = false;
	private Socket serverSocket;
	private Socket serverPingSocket;
	boolean isConnected = false;
	
	public AstroReciever() {
		AstroClient client = new AstroClient();
		Pinger ping = new Pinger();
		
		Thread clientThread = new Thread(client);
		Thread pingerThread = new Thread(ping);
		
		clientThread.start();
		pingerThread.start();
	}

	/**
	 * Asks Connector to get the time of the latest parsing in ms.
	 * <p>
	 * 
	 * @return true if it was sent successfully.
	 */
	public boolean newData() {

		if (newData) {
			newData = false;
			return true;
		}
		return false;

	}
	
	private class AstroClient implements Runnable {
		
		InputStreamReader inFromServer;
		BufferedReader fromServer;
		@Override
		public void run() {
			while (true) {
				if(!isConnected){
					connect();
				}
				while (isConnected) {
					try {
						if (!fromServer.ready()) {
							try {
								Thread.sleep(RECIEVE_DELAY / 2);
							} catch (InterruptedException e) {
								e.printStackTrace();
							}
							if (!fromServer.ready()) {
								try {
									Thread.sleep(RECIEVE_DELAY / 2);
								} catch (InterruptedException e) {
									e.printStackTrace();
								}
							}
						}
						if (!fromServer.ready()) {
							isConnected = false;
						}
							
					} catch (IOException e) {
						e.printStackTrace();
					}
					if (isConnected) {
						String latestStocks = null;
						try {
							latestStocks = fromServer.readLine();
						} catch (IOException e) {
							isConnected = false;
							e.printStackTrace();
						}
						if(!latestStocks.equals("")){
							System.out.println("New data is available from database.");
							newData = true;
						} 
						else {
							
						}
					}
				}
				
			}

		}
		
		public boolean connect(){
			try {
				serverSocket = new Socket(SERVER_ADRESS, PORT_NR);
				serverPingSocket = new Socket(SERVER_ADRESS, PING_PORT_NR);
				serverPingSocket.setKeepAlive(true);
				serverSocket.setKeepAlive(true);
				inFromServer = new InputStreamReader(serverSocket.getInputStream());
				fromServer = new BufferedReader(inFromServer);
				System.out.println("Connnected");
				isConnected = true;
			} catch (IOException e) {
				try {
					Thread.sleep(2000);
				} catch (InterruptedException e1) {
					e1.printStackTrace();
				}
				System.err.println("Error: Cant connect to host, reconnecting...");
				isConnected = false;
				connect();
			}

			return true;
		}
		
	}
	
	private class Pinger implements Runnable {
		OutputStream outToServer;
		@Override
		public void run() {
			while(true){
				while(isConnected && serverSocket.isConnected()){
					try {
						outToServer = serverPingSocket.getOutputStream();
						PrintWriter pw = new PrintWriter(outToServer, true);
						pw.println("");
						pw.flush();
						try {
							Thread.sleep(PING_DELAY);
						} catch (InterruptedException e) {
							e.printStackTrace();
						}
					} catch (IOException e) {
						e.printStackTrace();
						System.err.println("Disconnected from server.");
						isConnected = false;
					}
				}
				while(!isConnected){
					try {
						Thread.sleep(500);
					} catch (InterruptedException e) {
						e.printStackTrace();
					}
				}
			}
		}
		
	}

	/**
	 * Method only for testing!
	 * Change PORT_NR and address to the correct value.
	 * <p>
	 * Not to be included in final version.
	 * <p>
	 */
	public static void main(String[] args) {
		
		//Added this suppress because I'm not sure it the GC will remove this 
		//variable if it's not assigned to a local variable
		@SuppressWarnings("unused")
		AstroReciever rec = new AstroReciever();
		
		try {
			Thread.sleep(2000);
		} catch (InterruptedException e1) {
			e1.printStackTrace();
		}
		while(true){
			try {
				Thread.sleep(1000);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		} 			
	 }
}