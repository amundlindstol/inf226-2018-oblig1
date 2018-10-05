package inf226;

import java.security.SecureRandom;
import java.util.Base64;

//immutable class representing a token.
public final class Token {
	private final byte bytes[];

	/**
	 * The constructor should generate a random 128 bit token
	 */
	public Token(){
		bytes = new byte[16];
		SecureRandom random = new SecureRandom();
		random.nextBytes(bytes);
	}

	public Token(String base64){
		bytes = Base64.getDecoder().decode(base64);
	}

	/**
	 * This method should return the Base64 encoding of the token
	 * @return A Base64 encoding of the token
	 */
	public String stringRepresentation() {
		return Base64.getEncoder().encodeToString(bytes);
	}
}
