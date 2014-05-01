package jhttp;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.Socket;
import java.util.ArrayList;
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
	int requestNum;
	ArrayList<String> input = new ArrayList<String>();

	
	/**This constructs a Thread that
	 * handles the client's connections. 
	 * 
	 * @author Tony Knapp
	 * @since Alpha (04/29/2014)
	 * @param Socket, Server
	 */
	public HTTPClient(Socket cSoc, HTTPServer server, File parentFolder, int count) throws IOException {
		this.controlSoc = cSoc; // attach to client socket
		this.controlIn = new BufferedReader(new InputStreamReader(controlSoc.getInputStream()));
		this.controlOut = new DataOutputStream(controlSoc.getOutputStream());
		this.server = server;
		this.parentDir = parentFolder;
		this.requestNum = count;
		
		System.out.println(this.requestNum + "New Request");
	}
	
	public void run() { 
		try {
    	  	String tmp = controlIn.readLine();
    	  	while (!tmp.isEmpty()){
    	  		System.out.println(this.requestNum + "IN: " + tmp);
    	  		this.input.add(tmp);	  		
    				tmp = controlIn.readLine();
      		}
    	    if (this.input.get(0).startsWith("GET")) { //if tmp equals GET, set to method 1
    	      get();
    //      	controlOut.writeBytes(construct_http_header(501, 0));
    	    }
    	    else if (this.input.get(0).startsWith("HEAD")) { //if tmp equals HEAD, set to method 2
    	      head();
    //      	controlOut.writeBytes(construct_http_header(501, 0));
    	    } 
    	    else { // not supported
          	controlOut.writeBytes(construct_http_header(501, 0));
          	controlOut.close();
            return;
    	    }
    	    shutThingsDown(0);
		} 
		catch (Exception e3) { //notify user of an error
			e3.printStackTrace();
    }
	}
	
	private void get() throws IOException {
    String path = this.input.get(0).substring(3, input.get(0).length()-8); //fill in the path
    String version = this.input.get(0).substring(2 + path.length()).trim();
    path = path.trim();
    System.out.println(this.requestNum + "URI: " + path);
    System.out.println(this.requestNum + "VER: " + version.substring(5));
//    if (version.startsWith("HTTP")) {
//    	version = "1.1";
//    	String[] versionInt = version.substring(5).split(".");
//    	System.out.println("versionInt.length: " + versionInt.length);
//    	if (server.version[0] < Integer.parseInt(versionInt[0])) {
//    		//HTTP version is not supported
//    	}
//    	else if (server.version[0] == Integer.parseInt(versionInt[0]) && server.version[1] < Integer.parseInt(versionInt[1])) {
//    		//HTTP version is not supported
//    	}
//    }
    System.out.println(server.directory.getAbsolutePath());
    if (path.equalsIgnoreCase("/")) {
    	path = server.directory.getAbsolutePath() + "index.html";
    }
    else {
    	path = server.directory.getAbsolutePath() + path;
    }
    final File sendFile = new File(path);
    if (!sendFile.isFile()) {
    	controlOut.writeBytes(construct_http_header(404, 0));
    }	
    else {
    	int type_is;
    	if (path.endsWith(".zip")) {
    		type_is = 3;
	    }
    	else if (path.endsWith(".jpg") || path.endsWith(".jpeg")) {
	      type_is = 1;
	    }
    	else if (path.endsWith(".gif")) {
	      type_is = 2;
	    }
    	else {
    		type_is = 0;
    	}
	  	controlOut.writeBytes(construct_http_header(200, type_is));
	  	FileInputStream requestedFile = new FileInputStream(sendFile);
	    int b = requestedFile.read();
	    while (b != -1) {
	      b = requestedFile.read();
	      controlOut.write(b);
      }
	    requestedFile.close();
    }
	}

	private void head() throws IOException {
    String path = this.input.get(0).substring(2, input.get(0).length()-8).trim(); //fill in the path
    String version = this.input.get(0).substring(2 + path.length()).trim();
    if (version.startsWith("HTTP")) {
    	String[] versionInt = version.substring(4).split(".");
    	if (server.version[0] < Integer.parseInt(versionInt[0])) {
    		//HTTP version is not supported
    	}
    	else if (server.version[0] == Integer.parseInt(versionInt[0]) && server.version[1] < Integer.parseInt(versionInt[1])) {
    		//HTTP version is not supported
    	}
    }
    if (path.equalsIgnoreCase("/")) {
    	path = server.directory.getAbsolutePath() + "index.html";
    }
    else {
    	path = server.directory.getAbsolutePath() + path;
    }
    final File sendFile = new File(path);
    if (!sendFile.isFile()) {
    	controlOut.writeBytes(construct_http_header(404, 0));
    }	
    else {
    	int type_is;
    	if (path.endsWith(".zip")) {
    		type_is = 3;
	    }
    	else if (path.endsWith(".jpg") || path.endsWith(".jpeg")) {
	      type_is = 1;
	    }
    	else if (path.endsWith(".gif")) {
	      type_is = 2;
	    }
    	else {
    		type_is = 0;
    	}
	  	controlOut.writeBytes(construct_http_header(200, type_is));
    }
	}

  /**
   * Handle form submission using method="POST"
   * 
   * @author Teagan Atwater
   * @since Alpha
   */
  private void post() throws IOException {
    //Resource created on server:
    //201 Created
    //Result in no resource that can be identified:
    //200 OK - includes entity that describes result
    //204 No Content - doesn't include entity that describes result

    //Need Cache-Control and Expires header files
    //303 See Other - direct to retrieve cacheable resource
  }
	
	
	//
//  //retrieve the path to the filename of file to be downloaded
//  FileInputStream requestedfile = null;
//
//  try {
//    //NOTE that there are several security consideration when passing
//    //the untrusted string "path" to FileInputStream.
//    //You can access all files the current user has read access to!!!
//    //current user is the user running the javaprogram.
//    //you can do this by passing "../" in the url or specify absoulute path
//    //or change drive (win)
//
//    //open the file
//    requestedfile = new FileInputStream(path);
//  }
//  catch (Exception e) {
//    try {
//      //if file open fails send the infamous 404 Not Found
//      controlOut.writeBytes(construct_http_header(404, 0));
//      //close the stream
//      controlOut.close();
//    }
//    catch (Exception e2) {}
//    ;
//    System.out.println(this.requestNum + "error" + e.getMessage());
//  }
//
//  //happy day scenario
//  try {
//    int type_is = 0;
//    //find out what the filename ends with,
//    //so you can construct a the right content type
//    if (path.endsWith(".zip")) {
//      type_is = 3;
//    }
//    if (path.endsWith(".jpg") || path.endsWith(".jpeg")) {
//      type_is = 1;
//    }
//    if (path.endsWith(".gif")) {
//      type_is = 2;
//    }
//    controlOut.writeBytes(construct_http_header(200, 5)); //it checks out, send 200 OK
//
//    //if it was a HEAD request, we don't print any BODY
//    if (method == 1) { //1 is GET 2 is head and skips the body
//      while (true) {
//        //read the file from filestream, and print out through the
//        //client-outputstream on a byte per byte basis.
//        int b = requestedfile.read();
//        if (b == -1) {
//          break; //end of file
//        }
//        controlOut.write(b);
//      }
//      
//	    }
//	    //clean up the files, close open handles
//	    requestedfile.close();
//	    shutThingsDown(0);
//  	}
//	}
  
  
	boolean shutThingsDown(int errorCode) throws IOException{
		if (errorCode==0) {
			
		}
		this.running = false;
		controlOut.close();
		return true;
	}
	
	/**
	 * Description: this method creates the HTTP header for the response
	 * the header tells the browser what the result of the request
	 * @author Tony Knapp, Teagan Atwater, Jake Junda
	 * @since Alpha April 29, 2014 
	 */	
  private String construct_http_header(int return_code, int file_type) {
    String s = "HTTP/1.1 ";
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
    System.out.println(this.requestNum + "OUT: "+ s);
    return s;//return the header
  }

}