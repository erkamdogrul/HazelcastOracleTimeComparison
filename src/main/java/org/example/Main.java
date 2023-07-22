package org.example;

import com.hazelcast.collection.IList;
import com.hazelcast.core.Hazelcast;
import com.hazelcast.core.HazelcastInstance;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class Main {
    public static void main(String[] args) throws SQLException, ClassNotFoundException {

        //  HAZELCAST TEST FOR TIME OF FETCHING 100K NUMBERS
        Random newRandom = new Random();

        HazelcastInstance hazelcastInstance = Hazelcast.newHazelcastInstance();

        IList<Integer> randomNumberList = hazelcastInstance.getList("randomNumberList");

        long startTimeInsert = System.currentTimeMillis();
        for (int i = 0; i < 20000; i++) {
            randomNumberList.add(newRandom.nextInt(500));
        }
        long endTimeInsert = System.currentTimeMillis();

        long startTimeGet = System.currentTimeMillis();
        List<Integer> cachedNumbers = new ArrayList<>();
        for (int i = 0; i < 20000; i++) {
            cachedNumbers.add(randomNumberList.get(i));
        }

        long endTimeGet = System.currentTimeMillis();

        randomNumberList.destroy();
        hazelcastInstance.shutdown();

        // ORACLE DB SQL TEST FOR TIME OF FETCHING 100K NUMBERS
        Connection con = DriverManager.getConnection("jdbc:oracle:thin:@localhost:1521/XE", "c##devnami",
                "demo1234");
        String query = "DELETE FROM DEMO"; // deletes existing table data.

        Statement st = con.createStatement();
        st.executeUpdate(query);

        long startTimeOracleInsert = System.currentTimeMillis();
        int randomValue;
        for (int i = 0; i < 20000; i++) {
            randomValue = newRandom.nextInt(500);
            st.executeUpdate("INSERT INTO DEMO(NUMBERS, ID) VALUES(" + randomValue + " ," + i + ")");
        }
        long endTimeOracleInsert = System.currentTimeMillis();

        long startTimeOracleSelect = System.currentTimeMillis();

        ResultSet rs;
        for (int i = 0; i < 20000; i++) {
            rs = st.executeQuery("SELECT NUMBERS FROM DEMO WHERE ID = " + i + "");
            rs.next();
        }
        long endTimeOracleSelect = System.currentTimeMillis();


        System.out.println("The runtime for insert with Hazelcast is: " + (endTimeInsert - startTimeInsert));
        System.out.println("The runtime for select with Hazelcast is:: " + (endTimeGet - startTimeGet));
        System.out.println("The runtime for insert with Oracle  is: " + (endTimeOracleInsert - startTimeOracleInsert));
        System.out.println("The runtime for select with Oracle is: " + (endTimeOracleSelect - startTimeOracleSelect));
    }

}
