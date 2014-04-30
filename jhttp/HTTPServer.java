package jhttp;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Date;
import java.util.Scanner;
import jhttp.HTTPClient;

/*
 * Authors: Tony Knapp, Teagan Atwater, Jake Junda
 * Started: April 28, 2014 (Alpha)
 * Project: A simple HTTP server
 * Description: This HTTP server allows multiple clients to request documents to be sent to them
 *              simultaneously.
 */
public class HTTPServer extends Thread {
	static int PORT;
	private ArrayList<HTTPClient> activeClients;
	private ServerSocket incoming; // Socket that the server listens to
	private File log = new File("log.txt");
	File directory;
	/**
	 * version = {major, minor}
	 */
	int[] version = {1, 1};	
	private PrintWriter out;
	boolean running = true;
	private int USER_LIMIT = 100;

	/**
	 * Create new instance of HTTPServer
	 * 
	 * @author Tony Knapp
	 * @author Teagan Atwater
	 * @param newPort (optional, will default to 80)
	 * @param directoryPath (path from computer root to server root directory)
	 * @since Alpha
	 */
	public HTTPServer(int newPort, String directoryPath) throws IOException {
		this.activeClients = new ArrayList<HTTPClient>();
		HTTPServer.PORT = newPort;
		this.directory = new File(directoryPath);
		if (!this.directory.isDirectory()) {
			this.directory.mkdir();
		}
		try {
			this.out = new PrintWriter(new BufferedWriter(new FileWriter(log, true)));
			this.incoming = new ServerSocket(PORT); // Create server socket on designated port
			PORT = incoming.getLocalPort();
		}
		catch (IOException e) {
			e.printStackTrace();
		}
	}

	/**
	 * Intiate the server and handle client communications. Before exiting,
	 * close the server and the JTM.
	 * 
	 * @author Tony Knapp
	 * @author Jake Junda
	 * @since Alpha
	 */
	public static void main(String[] args) throws Exception {
		boolean portProvided = false;
		int tempPort = 80;
		String tempDir = "C://";
		Scanner in = new Scanner(System.in);
		String text;
		if (args.length > 0) {
			for (int i = 0; i < args.length; i++) {
				if (args[i].trim().equals("-p")) {
					if (args[i+1].matches("^([-+] ?)?[0-9]+(,[0-9]+)?$")) {
						if (Integer.parseInt(args[i+1]) <= 65535) {
							portProvided = true;
							tempPort = Integer.parseInt(args[i+1]);
							i = i + 2;
						}
						else {
							System.out.println("Bad port argument. Must be between 0 and 65535.");
							System.exit(2);
						}
					}
				}
				else if (args[i].trim().equals("-d")) {
					if (args[i + 1].startsWith("/") || args[i + 1].startsWith("C://")) {
						tempDir = args[i + 1];
					}
					else {
						System.out.println("Bad directory argument. Must be an absolute path.");
						System.exit(2);
					}
				}
			}
		}
		if (!portProvided) {
			System.out.println("Missing port argument. Defaulting to port 80.");
		}
		HTTPServer server = new HTTPServer(tempPort, tempDir); // Create server
		server.start();
		text = in.nextLine(); // Wait for user input
		while (text != null && !text.trim().equalsIgnoreCase("QUIT")) { // Loop until the user types QUIT
			if (text.trim().equalsIgnoreCase("PORT")) {
				System.out.println("The port number you should connect to is " + HTTPServer.PORT);
			}
			text = in.nextLine(); // Wait for user input
		}
		while (!server.stopServer()); // Wait for the server to shut down before proceeding
		in.close();
		System.exit(0); // Shut down the JVM
	}
	
	/**
	 * Wait for clients to connect to the server, then creates a socket and
	 * stores the new client session
	 * 
	 * @author Tony Knapp
	 * @author Teagan Atwater
	 * @since Alpha
	 */
	public void run() {
		System.out.println("Server started on port " + PORT + ".\rType \"QUIT\" to exit.");
		this.running = true;
		try {
			while (running) {
				Socket clientSoc = incoming.accept(); // Wait for new connection
				HTTPClient clientInst = new HTTPClient(clientSoc, this, this.directory); // Create new session on socket
				
				if (activeClients.size() > USER_LIMIT){
					clientInst.shutThingsDown(2);
				}
				else {
					activeClients.add(clientInst); // Store new client session
					clientInst.start(); // Start client thread
				}
			}
		} 
		catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	/**
	 * Closes down all user sessions and stops the server
	 * 
	 * @author Teagan Atwater
	 * @author Jake Junda
	 * @since Alpha
	 */
	private boolean stopServer() throws IOException {
		this.running = false;
		for (HTTPClient client : this.activeClients) {
			while (!client.shutThingsDown(1)); // Wait for the client to shut down before proceeding
		}
		out.close();
		System.out.println("All client sessions have been shut down.\rStopping server.");
		try {
			this.join(100); // Let the thread die
		}
		catch (InterruptedException e) {
			e.printStackTrace();
		}
		return true;
	}
}