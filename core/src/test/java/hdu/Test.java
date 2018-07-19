package hdu;

import net.librec.math.algorithm.Maths;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;

/**
 * @Author: Skye
 * @Date: 15:41 2018/4/17
 * @Description:
 */
public class Test {
    public static void main(String[] args) throws ParseException {
        DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern("EEE MMM dd HH:mm:ss Z yyyy");
        SimpleDateFormat sf = new SimpleDateFormat("EEE MMM dd hh:mm:ss Z yyyy", Locale.ENGLISH);
        SimpleDateFormat sf2 = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.ENGLISH);
        sf2.setTimeZone(TimeZone.getTimeZone("GMT"));
        String date = "Sat Jul 30 20:15:24 +0000 2011";
        Date date2 = sf.parse(date);
        String date22 = sf2.format(date2);
        System.out.println(date22);
        String str = ",,Kingsburg,California,";
        String[] splitStr = str.split(",");
        if (splitStr[0].equals(""))
            System.out.println("yes");
        for (String s:
             splitStr) {
            System.out.println(s);
        }
        System.out.println(Maths.logistic(-1691.6019396234522));
        System.out.println(Math.exp(-1691.6019396234522) * Maths.logistic(-1691.6019396234522));
    }
}
