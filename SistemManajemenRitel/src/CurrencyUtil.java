/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

/**
 *
 * @author JIC
 */
import java.text.NumberFormat;
import java.text.ParseException;
import java.util.Locale;

public class CurrencyUtil {

    private static final NumberFormat rupiahFormat = NumberFormat.getCurrencyInstance(new Locale("id", "ID"));

    public static String formatRupiah(double amount) {
        return rupiahFormat.format(amount);
    }

    public static double parseRupiah(String amount) {
        try {
            if (!amount.contains("Rp")) {
                return Double.parseDouble(amount.replaceAll("[^\\d.]", ""));
            }
            Number number = rupiahFormat.parse(amount);
            return number.doubleValue();
        } catch (ParseException e) {
            e.printStackTrace();
            return 0;
        }
    }
}
