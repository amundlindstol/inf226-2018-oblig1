package inf226;

import java.io.*;
import java.net.*;
import java.sql.SQLException;
import java.util.function.Function;

import inf226.Storage.KeyedStorage;
import inf226.Storage.Storage;
import inf226.Storage.Storage.ObjectDeletedException;
import inf226.Storage.Stored;
import inf226.Storage.TransientStorage;

import javax.net.ssl.SSLServerSocket;
import javax.net.ssl.SSLServerSocketFactory;

/**
 * 
 * The Server main class. This implements all critical server functions.
 * 
 * @author INF226
 *
 */
public class Server {
	private static final int portNumber = 1337;
	private static KeyedStorage<String,User> storage;
    private static DataBaseUserStorage db;

    public static Maybe<Stored<User>> authenticate(String username, String password) {
		try {
			if (storage.lookup(username).force().getValue().verifyPassword(password))
				return Maybe.just(storage.lookup(username).force());
			Maybe<Stored<User>> checkUser = db.lookup(username);
			if (!checkUser.isNothing() && checkUser.force().getValue().verifyPassword(password)) {
			    return checkUser;
            }
		} catch (Maybe.NothingException e) {
			e.printStackTrace();
			System.err.println("failed to authenticate user");
		}
		return Maybe.nothing();
	}

    /**
     * register a new account
     * @param username
     * @param password
     * @return Stored user
     */
	public static Maybe<Stored<User>> register(UserName username, Password password) {
        User usr = new User(username, password);
        return Maybe.just(db.save(usr));
	}

    /**
     * Create fresh token for user
     * @param user
     * @return Token if user exists
     */
	public static Maybe<Token> createToken(Stored<User> user) {
		Token token = new Token();
		User updatedUsr = user.getValue().newToken(token);
		try {
			storage.update(user, updatedUsr);
		} catch (Storage.ObjectModifiedException | ObjectDeletedException | SQLException e) {
			e.printStackTrace();
		}
		return Maybe.just(token);
	}

    /**
     * Authenticate user with token
     * @param username
     * @param token
     * @return Stored user if token is valid
     */
	public static Maybe<Stored<User>> authenticate(String username, Token token) {
		try {
			if (storage.lookup(username).force().getValue().getToken().equals(token.stringRepresentation()));
				return Maybe.just(storage.lookup(username).force());
		} catch (Maybe.NothingException e) {
			e.printStackTrace();
		}
		return Maybe.nothing();
	}

    /**
     * Send message to recipient. Update user object and update database
     * @param sender
     * @param recipient
     * @param content
     * @return true if message was sent
     */
	public static boolean sendMessage(Stored<User> sender, String recipient, String content) {
		try {
			if (storage.lookup(recipient).isNothing())
				return false;
			Message message = new Message(sender.getValue().getName(), recipient, content);
			User updatedReciever = storage.lookup(recipient).force().getValue().addMessage(message);
			//db.update(storage.lookup(recipient).force(), updatedReciever);
            storage.update(storage.lookup(recipient).force(), updatedReciever);
			return true;
		} catch (Message.Invalid | Maybe.NothingException | SQLException | Storage.ObjectModifiedException | ObjectDeletedException invalid) {
			invalid.printStackTrace();
		}
		return false;
	}
	
	/**
	 * Refresh the stored user object from the storage.
	 * @param user
	 * @return Refreshed value. Nothing if the object was deleted.
	 */
	public static Maybe<Stored<User>> refresh(Stored<User> user) {
		try {
			return Maybe.just(storage.refresh(user));
		} catch (ObjectDeletedException | SQLException e) {
			e.printStackTrace();
		}
		return Maybe.nothing();
	}

	/**
	 * @param args TODO: Parse args to get port number
	 */
	public static void main(String[] args) {
		System.setProperty("javax.net.ssl.keyStore","keystore.pfx");
		final RequestProcessor processor = new RequestProcessor();
		System.out.println("Staring authentication server");
		processor.start();
		try (final SSLServerSocket socket = new SecureSSLSocket(portNumber, "").createServerSocket()) {
		    db = new DataBaseUserStorage();
		    db.connect();
		    storage = db.getStorage().force();
            while(!socket.isClosed()) {
				//Arrays.stream(socket.getEnabledProtocols()).forEach(e -> System.out.println(e));
            	System.err.println("Waiting for client to connect…");
        		Socket client = socket.accept();
            	System.err.println("Client connected.");
        		processor.addRequest(new RequestProcessor.Request(client));
			}
		} catch (IOException e) {
			System.out.println("Could not listen on port " + portNumber);
			e.printStackTrace();
		} catch (Maybe.NothingException e) {
            e.printStackTrace();
        }
    }


}
