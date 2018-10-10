package inf226;

import de.mkammerer.argon2.Argon2;
import de.mkammerer.argon2.Argon2Factory;

/**
 * Immutable class for users.
 * @author INF226
 *
 */
public final class User {
	private final UserName name;
	private final ImmutableLinkedList<Message> log;
	private final Password hashed;
	private final Token token;
	private final Argon2 argon2 = Argon2Factory.create();

	public User(final UserName name, final Password pw) {
		this.name = name;
		this.token = new Token();
		this.log = new ImmutableLinkedList<Message>();
		this.hashed = hashPass(pw);
	}
	public User(final UserName name, final Password pw, final Token token) {
		if (token == null) {
			this.name=name;
			this.token = new Token();
			this.log = new ImmutableLinkedList<Message>();
			this.hashed = pw;
			return;
		}
		this.name=name;
		this.token = token;
		this.log = new ImmutableLinkedList<Message>();
		this.hashed = hashPass(pw);
	}

	private User(final UserName name, Password hashedPw, final ImmutableLinkedList<Message> log, Token token) {
		this.name=name;
		this.token = token;
		this.log = log;
		this.hashed = hashedPw;
	}

	/**
	 * hash password with Argon2
	 * @param pw password to hash
	 */
	private Password hashPass(Password pw) {
		char[] password = pw.getPassword().toCharArray();
		//int iterations = Argon2Helper.findIterations(argon2, 1000, 65536, 1); TODO UNCOMMENT
		int iterations = 1;
		Password hashedPw = new Password(argon2.hash(iterations, 65536, 1, password));
		argon2.wipeArray(password);
		return hashedPw;
	}

	/**
	 *
	 * @param password
	 * @return true if password is correct
	 */
	public boolean verifyPassword(String password) {
		if (argon2.verify(hashed.getPassword(), password)) {
			return true;
		} else {
			return false;
		}
	}
	
	/**
	 * 
	 * @return User name
	 */
	public String getName() {
		return name.getUserName();
	}

	public String getHashedPass() {return hashed.getPassword();}

	public String getToken() {return token.stringRepresentation();}
	
	/**
	 * @return Messages sent to this user.
	 */
	public Iterable<Message> getMessages() {
		return log;
	}

	public User newToken(Token token) {
		return new User(name, hashed, log, token);
	}

	/**
	 * Add a message to this user’s log.
	 * @param m Message
	 * @return Updated user object.
	 */
	public User addMessage(Message m) {
		return new User(name, hashed, new ImmutableLinkedList<Message>(m,log), token);
	}

}
