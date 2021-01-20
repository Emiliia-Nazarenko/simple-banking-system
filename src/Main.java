package banking;

import org.sqlite.SQLiteDataSource;
import java.sql.*;
import java.util.*;

public class Main {

    public static void main(String[] args) {
        String url = "jdbc:sqlite:" + args[1];
        SQLiteDataSource dataSource = new SQLiteDataSource();
        dataSource.setUrl(url);
        try {
            //Connection to DB
            Connection con = dataSource.getConnection();
            //Create a table in DB
            con.createStatement().executeUpdate("CREATE TABLE IF NOT EXISTS card(" +
                    "id INTEGER PRIMARY KEY AUTOINCREMENT," +
                    "number TEXT NOT NULL," +
                    "pin TEXT NOT NULL," +
                    "balance INTEGER DEFAULT 0)");
            Scanner sc = new Scanner(System.in);
            boolean flag = true;
            while (flag) {
                System.out.println("1. Create an account");
                System.out.println("2. Log into account");
                System.out.println("0. Exit");
                int chooser = sc.nextInt();
                sc.nextLine();
                System.out.println();
                switch (chooser) {
                    case 1:
                        //Create an account
                        Account user = new Account(con);
                        System.out.println("Your card has been created.");
                        System.out.println("Your card number:");
                        System.out.println(user.getCardNumber());
                        System.out.println("Your card PIN:");
                        System.out.println(user.getPin());
                        System.out.println();
                        break;
                    case 2:
                        //Log into account
                        System.out.println("Enter your card number:");
                        String cardNumberLogIn = sc.nextLine();
                        System.out.println("Enter your PIN:");
                        String pinLogIn = sc.nextLine();
                        String existedCardsInDBQuery = "SELECT number, pin FROM card WHERE number = " + cardNumberLogIn;
                        ResultSet resultSetCardsInDB = con.createStatement().executeQuery(existedCardsInDBQuery);
                        //Operations within the account
                        if (resultSetCardsInDB.next()) {
                            if (resultSetCardsInDB.getString("pin").equals(pinLogIn)) {
                                System.out.println();
                                System.out.println("You have successfully logged in!");
                                System.out.println();
                                flag = insideAccount(sc, con, cardNumberLogIn);
                                resultSetCardsInDB.close();
                                break;
                            }
                        }
                        resultSetCardsInDB.close();
                        System.out.println("Wrong card number or PIN!");
                        System.out.println();
                        break;
                    case 0:
                        //Exit
                        System.out.println("Bye!");
                        flag = false;
                        break;
                    default:
                        System.out.println("There is no such command!");
                        System.out.println();
                        break;
                }
            }
            sc.close();
            con.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public static boolean insideAccount(Scanner sc, Connection con, String cardNumberLogIn) throws SQLException {
        while (true) {
            //Operations within the account
            System.out.println("1. Balance");
            System.out.println("2. Add income");
            System.out.println("3. Do transfer");
            System.out.println("4. Close account");
            System.out.println("5. Log out");
            System.out.println("0. Exit");
            int chooserInsideAccount = sc.nextInt();
            System.out.println();
            String balanceOfCardQuery;
            ResultSet resultBalanceOfCard;
            switch (chooserInsideAccount) {
                case 1:
                    //Get a balance of an account
                    balanceOfCardQuery = "SELECT balance FROM card WHERE number =  " + cardNumberLogIn;
                    resultBalanceOfCard = con.createStatement().executeQuery(balanceOfCardQuery);
                    System.out.println("Balance: " + resultBalanceOfCard.getString("balance"));
                    resultBalanceOfCard.close();
                    System.out.println();
                    break;
                case 2:
                    //To put money into the account
                    System.out.println("Enter income:");
                    int income = sc.nextInt();
                    String incomeToAccQuery = "UPDATE card SET balance = balance + ? WHERE number = '" +
                            cardNumberLogIn + "'";
                    PreparedStatement preparedStatement = con.prepareStatement(incomeToAccQuery);
                    preparedStatement.setInt(1, income);
                    preparedStatement.executeUpdate();
                    System.out.println("Income was added!");
                    System.out.println();
                    break;
                case 3:
                    //Money transfer
                    System.out.println("Transfer.");
                    System.out.println("Enter card number:");
                    sc.nextLine();
                    String cardNumberForTransfer = sc.nextLine();
                    //Checking the card number for transfer
                    if (cardNumberLogIn.equals(cardNumberForTransfer)) {
                        System.out.println("You can't transfer money to the same account!");
                        break;
                    }
                    if (!Account.isPassedLuhnAlgorithm(cardNumberForTransfer)) {
                        System.out.println("Probably you made a mistake in the card number. Please try again!");
                        break;
                    }
                    String matchQuery = "SELECT number FROM card WHERE number =  '" + cardNumberForTransfer + "'";
                    ResultSet resultSet = con.createStatement().executeQuery(matchQuery);
                    if (!resultSet.next()) {
                        System.out.println("Such a card does not exist.");
                        System.out.println();
                        resultSet.close();
                        break;
                    }
                    resultSet.close();
                    //Transaction
                    System.out.println("Enter how much money you want to transfer:");
                    int amountOfMoney = sc.nextInt();
                    balanceOfCardQuery = "SELECT balance FROM card WHERE number =  " + cardNumberLogIn;
                    resultBalanceOfCard = con.createStatement().executeQuery(balanceOfCardQuery);
                    if (resultBalanceOfCard.next()) {
                        if (amountOfMoney > resultBalanceOfCard.getInt("balance")) {
                            System.out.println("Not enough money!");
                        } else {
                            String debit = "UPDATE card SET balance = balance - ? WHERE number = '" + cardNumberLogIn + "'";
                            PreparedStatement preparedStatement1 = con.prepareStatement(debit);
                            preparedStatement1.setInt(1, amountOfMoney);
                            preparedStatement1.executeUpdate();
                            String incoming = "UPDATE card SET balance = balance + ? WHERE number = '" + cardNumberForTransfer + "'";
                            PreparedStatement preparedStatement2 = con.prepareStatement(incoming);
                            preparedStatement2.setInt(1, amountOfMoney);
                            preparedStatement2.executeUpdate();
                            System.out.println("Success!");
                            System.out.println();
                        }
                    }
                    resultBalanceOfCard.close();
                    break;
                case 4:
                    //Account deleting
                    String deleteQuery = "DELETE FROM card WHERE number =" + cardNumberLogIn;
                    con.createStatement().execute(deleteQuery);
                    System.out.println("Your account was deleted successfully.");
                    System.out.println();
                    break;
                case 5:
                    //Log out
                    System.out.println("You have successfully logged out!");
                    System.out.println();
                    return true;
                case 0:
                    //Exit
                    System.out.println("Bye!");
                    return false;
                default:
                    System.out.println("There is no such command!");
                    System.out.println();
                    break;
            }
        }
    }
}

