package com.example.securelogin;

import java.sql.*;
import java.util.LinkedList;
import java.util.List;

public class DbUtility {

    public static Connection createConnection() throws SQLException, ClassNotFoundException {
        Connection c = null;
            Class.forName("org.postgresql.Driver");
             c = DriverManager
                    .getConnection("jdbc:postgresql://localhost:5432/rubrica",
                            "postgres", "jollyj");
        return c;
    }

    public static void closeConnection(Connection connection) throws SQLException {
        connection.close();
    }

    public static boolean checkIfUserIsPresent(Connection connection, String username) throws SQLException {
        String sql = "SELECT * FROM contatto.contatti WHERE username = ?";
        PreparedStatement stmt = connection.prepareStatement(sql);
        stmt.setString(1, username);
        ResultSet rs = stmt.executeQuery();
        boolean isPresent = rs.next();
        rs.close();
        stmt.close();
        return isPresent;
    }

    public static User getUser(Connection connection, String usernameToSearch) throws SQLException {
        String sql = "SELECT * FROM contatto.contatti WHERE username = ? LIMIT 1";
        PreparedStatement stmt = connection.prepareStatement(sql);
        stmt.setString(1, usernameToSearch);
        ResultSet rs = stmt.executeQuery();
        rs.next();
        String username = rs.getString("username");
        String password = rs.getString("password");
        String salt = rs.getString("salt");
        String name = rs.getString("name");
        String surname = rs.getString("surname");
        String telephone = rs.getString("telephone");
        User result = new User(username, password, salt, name, surname, telephone);
        rs.close();
        stmt.close();
        return result;
    }

    public static List<User> getAllUsers(Connection connection) throws SQLException {
        Statement stmt = connection.createStatement();
        String sql = "SELECT * FROM contatto.contatti LIMIT 100";
        List<User> listUser = new LinkedList<>();
        ResultSet rs = stmt.executeQuery(sql);
        while ( rs.next() ) {
            String  username = rs.getString("username");
            String  password = rs.getString("password");
            String  salt = rs.getString("salt");
            String  name = rs.getString("name");
            String  surname = rs.getString("surname");
            String telephone = rs.getString("telephone");
            User user = new User(username, password, salt, name, surname, telephone);
            listUser.add(user);
        }
        rs.close();
        stmt.close();
        return listUser;
    }

    public static void insertUserPreparedStatement(Connection connection, String username, String password, String salt, String name, String surname) throws SQLException {
        String sql = "INSERT INTO contatto.contatti(password,surname,name,salt,username) VALUES( ?, ?, ?, ?, ?);";
        PreparedStatement stmt = connection.prepareStatement(sql);
        stmt.setString(1, password);
        stmt.setString(2, surname);
        stmt.setString(3, name);
        stmt.setString(4, salt);
        stmt.setString(5, username);

        stmt.executeUpdate();
        stmt.close();
    }

    private static boolean nameToUpdate(User user, User dbUser) {
        return !user.getName().equals((dbUser.getName()));
    }

    private static boolean surnameToUpdate(User user, User dbUser) {
        return !user.getSurname().equals((dbUser.getSurname()));
    }

    private static boolean telephoneToUpdate(User user, User dbUser) {
        return (dbUser.getTelephone() == null && !user.getTelephone().isBlank()) ||
                (dbUser.getTelephone() != null && !user.getTelephone().equals((dbUser.getTelephone())));
    }

    private static boolean passwordToUpdate(User user, User dbUser) {
        return !user.getPassword().isBlank() && !CryptoPassword.cryptoPasswordwithSalt(user.getPassword(), dbUser.getSalt()).equals(dbUser.getPassword());
    }

    public static void updateUserPreparedStatement(Connection connection, User user, User dbUser) throws SQLException {
        String sql = "UPDATE contatto.contatti SET ";
        boolean first = true;

        if (nameToUpdate(user, dbUser)) {
            //System.out.println("Cambio name");
            if (!first) {
                sql += ", ";
            }
            sql += " name = ?";
            first = false;
        }
        if (surnameToUpdate(user, dbUser)) {
            //System.out.println("Cambio surname");
            if (!first) {
                sql += ", ";
            }
            sql += " surname = ?";
            first = false;
        }

        if (telephoneToUpdate(user, dbUser)) {
            //System.out.println("Cambio telephone");
            if (!first) {
                sql += ", ";
            }
            sql += " telephone = ?";
            first = false;
        }
        if (passwordToUpdate(user, dbUser)) {
            //System.out.println("Cambio password");
            if (!first) {
                sql += ", ";
            }
            sql += " password = ?";
            first = false;
        }

        if (!first) {
            sql += " WHERE username = ?";
            PreparedStatement stmt = connection.prepareStatement(sql);
            int index = 1;
            if (nameToUpdate(user, dbUser)) {
                stmt.setString(index++, user.getName());
            }
            if (surnameToUpdate(user, dbUser)) {
                stmt.setString(index++, user.getSurname());
            }

            if (telephoneToUpdate(user, dbUser)) {
                stmt.setString(index++, user.getTelephone());
            }
            if (passwordToUpdate(user, dbUser)) {
                stmt.setString(index++, CryptoPassword.cryptoPasswordwithSalt(user.getPassword(), dbUser.getSalt()));
            }
            stmt.setString(index, dbUser.getUsername());
            stmt.executeUpdate();
            stmt.close();
        }
    }


}
