package banking;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.Random;

class Account {

    private String cardNumber;
    private String pin;

    //Create a new account
    public Account(Connection con) throws SQLException {
        Random random = new Random();
        int[] cardNum = {4, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0};
        StringBuffer cardNumber = new StringBuffer();

        boolean isContained = true;
        do {
            //Generating a card number
            for (int i = 6; i < 15; i++) {
                cardNum[i] = random.nextInt(9);
            }
            cardNum[15] = toGenerateCheckSum(Arrays.copyOf(cardNum, cardNum.length));
            for (int i = 0; i < 16; i++) {
                cardNumber.append(cardNum[i]);
            }

            //Checking the presence of a card in the database
            String cardsInDBQuery = "SELECT number FROM card WHERE number =" + cardNumber.toString();
            ResultSet resultSetCardsInDB = con.createStatement().executeQuery(cardsInDBQuery);
            if (resultSetCardsInDB.next()) {
                cardNum[15] = 0;
                cardNumber.delete(0, 16);
            } else {
                isContained = false;
            }
            resultSetCardsInDB.close();
        } while (isContained);

        //Generating a pin (0000-9999)
        String pin = String.valueOf(random.nextInt(10000 - 1000 + 1) + 1000);

        con.createStatement().executeUpdate("INSERT INTO card (number, pin) VALUES ('"
                + cardNumber.toString() +
                "', '" + pin + "')");

        this.cardNumber = cardNumber.toString();
        this.pin = pin;
    }
    //Generation of Check Sum
    public static int toGenerateCheckSum(int[] arr) {
        int sum = 0;
        for (int i = 0; i < 15; i++) {
            if ((i + 1) % 2 != 0) {
                arr[i] = arr[i] * 2;
            }
            if (arr[i] > 9) {
                arr[i] = arr[i] - 9;
            }
            sum += arr[i];
        }
        return sum % 10 == 0 ? 0 : (sum / 10 + 1) * 10 - sum;
    }

    //Checking if the card number satisfies the Luhn algorithm
    public static boolean isPassedLuhnAlgorithm(String cardNumber) {
        int[] num = new int[16];
        int t = 0;
        for (String s : cardNumber.split("")) {
            num[t++] = Integer.parseInt(s);
        }
        if (toGenerateCheckSum(num) != num[15]) {
            return false;
        }
        return true;
    }

    public String getCardNumber() {
        return this.cardNumber;
    }

    public String getPin() {
        return this.pin;
    }

    @Override
    public String toString() {
        return "Account{" +
                "cardNumber='" + cardNumber + '\'' +
                ", pin=" + pin +
                '}';
    }
}
