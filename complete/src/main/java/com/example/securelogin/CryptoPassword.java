package com.example.securelogin;

import org.springframework.security.crypto.bcrypt.BCrypt;

public class CryptoPassword {

    public static String cryptoPassword(String clearPassword){
        String salt = BCrypt.gensalt();
        return BCrypt.hashpw(clearPassword, salt);
    }

    public static String cryptoPasswordwithSalt(String clearPassword, String salt){
        return BCrypt.hashpw(clearPassword, salt);
    }

    public static boolean checkEqualsPassword(String clearPassword, String hashedPassword){
        return BCrypt.checkpw(clearPassword, hashedPassword);
    }




    public static void main(String[] args) {
        String password = "enrico";
        String salt = BCrypt.gensalt();
        System.out.println(salt);
        String hashedPassword = BCrypt.hashpw(password, salt);
        System.out.println(hashedPassword);
        System.out.println( BCrypt.checkpw(password, hashedPassword));
/*
        try (Connection conn = DriverManager.getConnection("jdbc:mysql://localhost:3306/my_database", "username", "password")) {
            String query = "INSERT INTO users(username, password_hash, salt) VALUES (?, ?, ?)";
            PreparedStatement stmt = conn.prepareStatement(query);
            stmt.setString(1, "user1");
            stmt.setString(2, hashedPassword);
            stmt.setString(3, salt);
            stmt.executeUpdate();
        } catch (Exception e) {
            e.printStackTrace();
        }*/
    }
}


