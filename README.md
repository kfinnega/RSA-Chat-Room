# CSC 445 Project 3

D evelop a secure chatroom application with both server-side and client-side components, implementing RSA encryption for secure message exchange between users.
Features:
- Real-time Communication: Users can join the chatroom and send/receive messages in real time.
- RSA Encryption: Ensures that all communication between users is encrypted using RSA (Rivest-Shamir-Adleman) public-key cryptography, protecting messages from eavesdropping.
- Multi-user Support: The server manages multiple clients simultaneously, handling message broadcasts and user management.
- Key Management: Each client generates a unique pair of RSA keys (public and private). The public key is shared with the server to facilitate encrypted communication.

Server-Side:
- Socket Programming: The server listens for incoming client connections and manages multiple clients through threads.
- Key Exchange: The server receives and stores the public keys of all connected clients.
- Message Broadcasting: Once a message is received, the server decrypts the message using the client’s public key, then re-encrypts it with the recipient's public key before broadcasting it.

Client-Side:
- Socket Programming: The client establishes a connection with the server, joining the chatroom.
- RSA Key Pair Generation: Upon joining, the client generates an RSA key pair and sends the public key to the server.
- Message Encryption/Decryption: The client encrypts outgoing messages using the recipient's public key and decrypts incoming messages using its private key.



