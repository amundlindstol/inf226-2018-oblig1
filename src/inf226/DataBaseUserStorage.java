package inf226;

import inf226.Storage.KeyedStorage;
import inf226.Storage.Stored;
import inf226.Storage.TransientStorage;

import java.sql.*;
import java.util.Iterator;
import java.util.function.Function;

public class DataBaseUserStorage implements KeyedStorage<String, User> {
    private Connection connection = null;
    private static final KeyedStorage<String,User> storage
                = new TransientStorage<String,User>
                (new Function<User,String>()
                {public String apply(User u)
                {return u.getName();}});

    private final String delete = "DELETE FROM Users WHERE UserName=?";
    private final String lookup = "SELECT UserName, HashedPwd FROM users WHERE UserName = ?";
    private final String lookupLog = "SELECT log FROM logs WHERE reciever = ?";
    private final String save = "INSERT INTO users (UserName, HashedPwd) VALUES (?, ?)";
    private final String saveMessage = "INSERT INTO logs (log, reciever) VALUES (?, ?)";

    public void connect() {

        try {
            //TODO make url generic
            String url = "jdbc:sqlite:/Users/amundlindstol/git/inf226-2018-oblig1/userdata.db";
            connection = DriverManager.getConnection(url);
            System.out.println("Connection to SQL database was established");
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public void disConnect() {
        if (connection != null) {
            try {
                connection.close();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }

    public Maybe<KeyedStorage<String, User>> getStorage() {
        return Maybe.just(storage);
    }

    @Override
    public Maybe<Stored<User>> lookup(String key) {
        try {
            PreparedStatement prep = connection.prepareStatement(lookup);
            prep.setString(1, key);
            ResultSet s = prep.executeQuery();
            //int id = s.getInt("ID");
            User usr = new User(new UserName(s.getString(1)), new Password(s.getString(2)), null);
            s.close();
            return Maybe.just(storage.save(usr));
        } catch (SQLException e) {
            e.printStackTrace();
        }
//        catch (Message.Invalid invalid) {
//            invalid.printStackTrace();
//        }
        return Maybe.nothing();
    }

    @Override
    public Stored<User> save(User user) {
        try {
            PreparedStatement prep = connection.prepareStatement(save);
            prep.setString(1, user.getName());
            prep.setString(2, user.getHashedPass());
            prep.execute();
            return storage.save(new User(new UserName(user.getName()), new Password(user.getHashedPass())));
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    @Override
    public Stored<User> refresh(Stored old) throws ObjectDeletedException {
        if (old.getValue() instanceof User) {
            try {
                return lookup(((User) old.getValue()).getName()).force();
            } catch (Maybe.NothingException e) {
                e.printStackTrace();
                throw new ObjectDeletedException(old.id());
            }
        }
        return null;
    }

    @Override
    public Stored<User> update(Stored<User> old, User newValue) throws ObjectModifiedException, ObjectDeletedException, SQLException {
        PreparedStatement prep = connection.prepareStatement(lookup);
        prep.setString(1, old.getValue().getName());
        ResultSet s = prep.executeQuery();
        String username = s.getString("UserName");
        s.close();

        for (Iterator<Message> it = newValue.getMessages().iterator(); it.hasNext(); ) {
            Message message = it.next();
            if (!it.hasNext()) {
                prep = connection.prepareStatement(saveMessage);
                prep.setString(1, message.message);
                prep.setString(2, username);
                prep.execute();
            }
        }
        return storage.update(old, newValue);
    }

    @Override
    public void delete(Stored old) throws ObjectModifiedException, ObjectDeletedException, SQLException {
        PreparedStatement prep = connection.prepareStatement(delete);
        if (old.getValue() instanceof User) {
            Stored<User> s = (Stored<User>) old;
            String a = s.getValue().getName();
            prep.setString(1, a);
            prep.execute();
        }
    }
}
