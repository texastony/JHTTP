package jhttp;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.Socket;
import java.util.Date;

/*
 * Authors: Tony Knapp, Teagan Atwater, Jake Junda
 * Started: April 28, 2014
 * Project: A simple HTTP server Client
 * Description: This HTTP server Client handles an instance of the each client
 * that connects to the server.  It handles the return codes that let the user
 * know the result of thier commands.  
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
	
	/**This constructs a Thread that
	 * handles the client's connections. 
	 * 
	 * @author Tony Knapp
	 * @since Alpha (04/29/2014)
	 * @param Socket, Server
	 */
	public HTTPClient(Socket cSoc, HTTPServer server, File parentFolder ) throws IOException {
		this.controlSoc = cSoc; // attach to client socket
		this.controlIn = new BufferedReader(new InputStreamReader(controlSoc.getInputStream()));
		this.controlOut = new DataOutputStream(controlSoc.getOutputStream());
		this.server = server;
		this.parentDir = parentFolder;
		System.out.println("A new guest has Conncected\rAwaiting Username and Password");
	}
	
	public void run() {
    int method = 0; //1 get, 2 head, 0 not supported
    String http = new String(); //a bunch of strings to hold
    String path = new String(); //holds the http version, and path,
    String file = new String(); //hold the filename
    String user_agent = new String(); //user_agent
    try {
      //This is the two types of request we can handle
      //GET /index.html HTTP/1.0
      //HEAD /index.html HTTP/1.0
      String tmp = controlIn.readLine(); //read from the stream
      String tmp2 = new String(tmp);
      tmp.toUpperCase(); //convert it to uppercase
      if (tmp.startsWith("GET")) { //if tmp equals GET, set to method 1
        method = 1;
      }
      if (tmp.startsWith("HEAD")) { //if tmp equals HEAD, set to method 2
        method = 2;
      } 

      if (method == 0) { // not supported
        try {
        	controlOut.writeBytes(construct_http_header(501, 0));
        	controlOut.close();
          return;
        }
        catch (Exception e3) { //notify user of an error
        	System.out.println("error:" + e3.getMessage());
        }
      }
      //}

      //tmp contains "GET /index.html HTTP/1.0 ......."
      //find first space
      //find next space
      //copy whats between minus slash, then you get "index.html"???
      //it's a bit of dirty code, but bear with me...???
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
    	System.out.println("error" + e.getMessage());
    }

    //retrieve the path to the filename of file to be downloaded
    s("\nClient requested:" + new File(path).getAbsolutePath() + "\n");
    FileInputStream requestedfile = null;

    try {
      //NOTE that there are several security consideration when passing
      //the untrusted string "path" to FileInputStream.
      //You can access all files the current user has read access to!!!
      //current user is the user running the javaprogram.
      //you can do this by passing "../" in the url or specify absoulute path
      //or change drive (win)

      //open the file
      requestedfile = new FileInputStream(path);
    }
    catch (Exception e) {
      try {
        //if file open fails send the infamous 404 Not Found
        output.writeBytes(construct_http_header(404, 0));
        //close the stream
        output.close();
      }
      catch (Exception e2) {}
      ;
      System.out.println("error" + e.getMessage());
    }

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
      }
      output.writeBytes(construct_http_header(200, 5)); //it checks out, send 200 OK

      //if it was a HEAD request, we don't print any BODY
      if (method == 1) { //1 is GET 2 is head and skips the body
        while (true) {
          //read the file from filestream, and print out through the
          //client-outputstream on a byte per byte basis.
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
	 * Description: this method creates the HTTP header for the response
	 * the header tells the browser what the result of the request
	 * @author Tony Knapp, Teagan Atwater, Jake Junda
	 * @since Alpha April 29, 2014 
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
      //WE CAN EITHER FILL IN A BUNCH MORE FILE TYPES OR DO IT PETER'S "LET THE BROWSER HANDLE IT" WAY
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
    return s;//return the header
  }

}