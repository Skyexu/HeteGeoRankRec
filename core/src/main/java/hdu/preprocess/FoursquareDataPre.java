package hdu.preprocess;

import hdu.util.FileUtil;
import hdu.util.LogUtil;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.format.DateTimeFormatter;
import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;

/**
 * @Author: Skye
 * @Date: 15:16 2018/4/17
 * @Description:  Foursquare 数据集预处理，文本格式切分转换
 */
public class FoursquareDataPre {

    public static void main(String[] args) throws ParseException {


        String path = "D:\\Works\\论文\\dataSet\\experimentData\\";
        String dataPath = path + "Foursquare\\checkin_venues.txt";
        String resultFilePath = path + "Foursquare\\process\\checkin_venues_pre.txt";

        LogUtil.printLog("Begin");

        // 读取输入文件
        String[] dataContent = FileUtil.read(dataPath, null);

        // 功能实现入口
        String[] resultContents = dataProprecess(dataContent);

        // 写入输出文件
        if (hasResults(resultContents)) {
            FileUtil.write(resultFilePath, resultContents, false);
        } else {
            FileUtil.write(resultFilePath, new String[] { "NA" }, false);
        }
        LogUtil.printLog("End");
    }
    private static String[] dataProprecess(String[] dataContent) throws ParseException {

        DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern("EEE MMM dd HH:mm:ss Z yyyy");
        SimpleDateFormat sf = new SimpleDateFormat("EEE MMM dd hh:mm:ss Z yyyy", Locale.ENGLISH);
        SimpleDateFormat sf2 = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.ENGLISH);
        sf2.setTimeZone(TimeZone.getTimeZone("GMT"));

        String[] result = new String[dataContent.length];
        for (int i = 1 ; i < dataContent.length;i++) {
            StringBuilder line = new StringBuilder();
            String[] columns = dataContent[i].split("\t");
            //userID
            line.append(columns[0]).append("\t");
            //time
            Date date = sf.parse(columns[1]);
            line.append(sf2.format(date)).append("\t");
            // venueId
            line.append(columns[2]).append("\t");
            //venuelocation to lat lon  city country 先纬度，再经度
            String[] venueLocation = columns[3].substring(1,columns[3].length()-1).split(",");
            if (venueLocation.length < 5||venueLocation[0].equals(""))
                continue;
            System.out.println(i+" "+venueLocation.length+" "+" : "+venueLocation[0] + ","+venueLocation[1]);
            line.append(venueLocation[0]).append("\t").append(venueLocation[1]).append("\t")
                    .append(venueLocation[2]).append("\t").append(venueLocation[4]).append("\t");
            //venueCategory
            String venueCategory = columns[4].substring(1,columns[4].length()-1);
            line.append(venueCategory);
            result[i-1] = line.toString();
        }
        return result;
    }
    private static boolean hasResults(String[] resultContents) {
        if (resultContents == null) {
            return false;
        }
        for (String contents : resultContents) {
            if (contents != null && !contents.trim().isEmpty()) {
                return true;
            }
        }
        return false;
    }
}
