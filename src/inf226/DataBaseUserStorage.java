package inf226;

import inf226.Storage.KeyedStorage;
import inf226.Storage.Stored;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.SQLException;

public class DataBaseUserStorage implements KeyedStorage<String, User> {
    private Connection connection = null;
    private final String delete = "DELETE FROM Users WHERE UserName=?";
    private final String lookup = "SELECT UserName FROM users WHERE UserName = ?";
    private final String save = "INSERT INTO users (UserName, HashedPwd) VALUES (?, ?)";

    public void connect() {
        try {
            //String url = "jdbc:sqlite:${basedir}/userdata.db";
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

    @Override
    public Maybe<Stored<User>> lookup(String key) {
        try {
            PreparedStatement prep = connection.prepareStatement(lookup);
            prep.setString(1, (String) key);
            prep.execute();
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    //TODO: use SSL for database traffic encryption during user authentication.
    @Override
    public Stored<User> save(User user) {
        try {
            PreparedStatement prep = connection.prepareStatement(save);
            prep.setString(1, user.getName());
            prep.setString(2, user.getHashedPass());
            prep.execute();

        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    @Override
    public Stored refresh(Stored old) throws ObjectDeletedException {
        return null;
    }

    @Override
    public Stored<User> update(Stored<User> old, User newValue) throws ObjectModifiedException, ObjectDeletedException, SQLException {
        //PreparedStatement prep = connection.prepareStatement("INSERT INTO users (UserName, HashedPwd) VALUES (?, ?)");
        return null;
    }

    @Override
    public void delete(Stored old) throws ObjectModifiedException, ObjectDeletedException, SQLException {
        PreparedStatement prep = connection.prepareStatement(delete);
    }
}
