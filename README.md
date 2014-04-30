#JHTTP#

###Project Brief###

An HTTP server in Java. For a group project in a networks class.

###Notes###

Client-server communication:

1. Client TCP SYN `PORT_X` -> `80`

2. Server TCP SYN/ACK `80` -> `PORT_X`

3. Client ACK `PORT_Y` -> `80`

4. Client GET file `PORT_Y` -> `80`

5. Server reply UNKNOWN

6. Client reply UNKNOWN

7. Server 
