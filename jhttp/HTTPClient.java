package jhttp;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.Inet4Address;
import java.net.Socket;
import java.util.Date;

/*
 * Authors: Tony Knapp, Teagan Atwater, Jake Junda
 * Started: April 28, 2014 (Alpha)
 * Project: A simple HTTP client
 * Description: This HTTP client handles all commands and interaction between server and client.
 */

public class HTTPClient extends Thread {
	Socket controlSoc;
	BufferedReader controlIn;
	DataOutputStream controlOut;
	Date loginTime;
	HTTPServer server;
	String dirName = "/";
	File parentDir;
	boolean running = true;
	boolean dataConnection = false;
	boolean isSending = false;	
	private int[] curVersion;
	private Inet4Address clAddr;
	
	/**
   * This constructs a Thread that handles the client's connections.
	 * 
	 * @author Tony Knapp
	 * @param cSoc
   * @param server
   * @param parentFolder
   * @since Alpha
	 */
	public HTTPClient(Socket cSoc, HTTPServer server, File parentFolder) throws IOException {
		this.controlSoc = cSoc; // Attach to client socket
		this.controlIn = new BufferedReader(new InputStreamReader(controlSoc.getInputStream()));
		this.controlOut = new DataOutputStream(controlSoc.getOutputStream());
		this.server = server;
		this.parentDir = parentFolder;
	}
	
  /**
   * 
   * 
   * @author Tony Knapp
   * @since Alpha
   */
	public void run() {
    int method = 0; // 1->GET, 2->HEAD, 0->NOT_SUPPORTED
    String http = new String(); // A bunch of strings to hold
    String path = new String(); // The various things, what http v, what path,
    String file = new String(); // What file
    String user_agent = new String(); // What user_agent
    try {
      //This is the two types of request we can handle
      //GET /index.html HTTP/1.1
      //HEAD /index.html HTTP/1.1
      String tmp = controlIn.readLine(); //read from the stream
      tmp.toUpperCase(); //convert it to uppercase
      if (tmp.startsWith("GET")) { 
      	//TODO create GET method
        method = 1;
      }
      if (tmp.startsWith("HEAD")) { 
      	//TODO create HEAD method
        method = 2;
      } 

      if (method == 0) { // not supported
        try {
        	controlOut.writeBytes(construct_http_header(501, 0));
        	controlOut.close();
          return;
        }
        catch (Exception e3) { //if some error happened catch it
          s("error:" + e3.getMessage());
        } //and display error
      }
      //}

      //tmp contains "GET /index.html HTTP/1.0 ......."
      //find first space
      //find next space
      //copy whats between minus slash, then you get "index.html"
      //it's a bit of dirty code, but bear with me...
      int start = 0;
      int end = 0;
      for (int a = 0; a < tmp2.length(); a++) {
        if (tmp2.charAt(a) == ' ' && start != 0) {
          end = a;
          break;
        }
        if (tmp2.charAt(a) == ' ' && start == 0) {
          start = a;
        }
      }
      path = tmp2.substring(start + 2, end); //fill in the path
    }
    catch (Exception e) {
      s("errorr" + e.getMessage());
    } //catch any exception

    //path do now have the filename to what to the file it wants to open
    s("\nClient requested:" + new File(path).getAbsolutePath() + "\n");
    FileInputStream requestedfile = null;

    try {
      //NOTE that there are several security consideration when passing
      //the untrusted string "path" to FileInputStream.
      //You can access all files the current user has read access to!!!
      //current user is the user running the javaprogram.
      //you can do this by passing "../" in the url or specify absoulute path
      //or change drive (win)

      //try to open the file,
      requestedfile = new FileInputStream(path);
    }
    catch (Exception e) {
      try {
        //if you could not open the file send a 404
        output.writeBytes(construct_http_header(404, 0));
        //close the stream
        output.close();
      }
      catch (Exception e2) {}
      ;
      s("error" + e.getMessage());
    } //print error to gui

    //happy day scenario
    try {
      int type_is = 0;
      //find out what the filename ends with,
      //so you can construct a the right content type
      if (path.endsWith(".zip"
             ) {
        type_is = 3;
      }
      if (path.endsWith(".jpg") || path.endsWith(".jpeg")) {
        type_is = 1;
      }
      if (path.endsWith(".gif")) {
        type_is = 2;
        //write out the header, 200 ->everything is ok we are all happy.
      }
      output.writeBytes(construct_http_header(200, 5));

      //if it was a HEAD request, we don't print any BODY
      if (method == 1) { //1 is GET 2 is head and skips the body
        while (true) {
          //read the file from filestream, and print out through the
          //client-outputstream on a byte per byte base.
          int b = requestedfile.read();
          if (b == -1) {
            break; //end of file
          }
          output.write(b);
        }
        
      }
//clean up the files, close open handles
      output.close();
      requestedfile.close();
    }

    catch (Exception e) {}

  }
	
	/**
	 * Create the HTTP header for the response. The header tells the browser the
   * result of the request
   * 
	 * @author Tony Knapp
   * @author Teagan Atwater
   * @author Jake Junda
	 * @since Alpha
	 */	
  private String construct_http_header(int return_code, int file_type) {
    String s = "HTTP/1.0 ";
    switch (return_code) {
      case 200:
        s = s + "200 OK";
        break;
      case 400:
        s = s + "400 Bad Request";
        break;
      case 403:
        s = s + "403 Forbidden";
        break;
      case 404:
        s = s + "404 Not Found";
        break;
      case 500:
        s = s + "500 Internal Server Error";
        break;
      case 501:
        s = s + "501 Not Implemented";
        break;
    }

    s = s + "\r\n"; //other header fields,
    s = s + "Connection: close\r\n"; //we can't handle persistent connections
    s = s + "Server: SimpleHTTPtutorial v0\r\n"; //server name

    //Construct the right Content-Type for the header.
    //This is so the browser knows what to do with the
    //file, you may know the browser dosen't look on the file
    //extension, it is the servers job to let the browser know
    //what kind of file is being transmitted. You may have experienced
    //if the server is miss configured it may result in
    //pictures displayed as text!
    
	/**
	 * this switch statement lets the browser know
	 * what kind of file is being sent
	 */	
    switch (file_type) {
      //plenty of types for you to fill in
      case 0:
        break;
      case 1:
        s = s + "Content-Type: image/jpeg\r\n";
        break;
      case 2:
        s = s + "Content-Type: image/gif\r\n";
      case 3:
        s = s + "Content-Type: application/x-zip-compressed\r\n";
      default:
        s = s + "Content-Type: text/html\r\n";
        break;
    }

    s = s + "\r\n"; //this marks the end of the httpheader
    //and the start of the body
    //ok return our newly created header!
    return s;
  }
}