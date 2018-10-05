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
	private static final KeyedStorage<String,User> storage
	  = new TransientStorage<String,User>
	         (new Function<User,String>()
	        		 {public String apply(User u)
	        		 {return u.getName();}});
	
	public static Maybe<Stored<User>> authenticate(String username, String password) {
		if (storage.lookup(username).isNothing())
			return Maybe.nothing();
		try {
			if (storage.lookup(username).force().getValue().verifyPassword(password))
				return Maybe.just(storage.lookup(username).force());
		} catch (Maybe.NothingException e) {
			e.printStackTrace();
			System.err.println("failed to authenticate user");
		}
		return Maybe.nothing();
	}

	public static Maybe<Stored<User>> register(UserName username, Password password) {
		try {
			return Maybe.just(storage.save(new User(username, password)));
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return Maybe.nothing();
	}
	
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

	public static Maybe<Stored<User>> authenticate(String username, Token token) {
		try {
			if (storage.lookup(username).force().getValue().getToken().equals(token.stringRepresentation()));
				return Maybe.just(storage.lookup(username).force());
		} catch (Maybe.NothingException e) {
			e.printStackTrace();
		}
		return Maybe.nothing();
	}

	public static boolean sendMessage(Stored<User> sender, String recipient, String content) {
		try {
			if (storage.lookup(recipient).isNothing())
				return false;
			Message message = new Message(sender.getValue().getName(), recipient, content);
			User updatedReciever = storage.lookup(recipient).force().getValue().addMessage(message);
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
		SSLServerSocketFactory serverFactory = (SSLServerSocketFactory) SSLServerSocketFactory.getDefault();
		//try (final SSLServerSocket socket = (SSLServerSocket) serverFactory.createServerSocket(portNumber)) {
		//try (final ServerSocket socket = new ServerSocket(portNumber)) {
		try (final SSLServerSocket socket = new SecureSSLSocket(portNumber, "").createServerSocket()) {
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
		}
	}


}
