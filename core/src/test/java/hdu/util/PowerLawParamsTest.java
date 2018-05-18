package hdu.util;

import java.io.File;

/**
 * @Author: Skye
 * @Date: 16:04 2018/5/17
 * @Description: 测试幂律分布参数计算
 */
public class PowerLawParamsTest {
    public  static final float zeroDistanceDefaultValue    = 1f;
    public static final String FILE_NAME_HOME_POWERLAW_PARAM = "UserHome_PowerLawParam";
    public static void main(String[] args) throws CheckinException {
        String path  = "D:\\Works\\论文\\dataSet\\experimentData\\Foursquare\\";
        String output = path + "recommendOut";
        String userHomeFile = path + "process\\user_home.txt";
        String trainRating = path + "process\\user_chekin_venue_count_t.txt";
        String locationLatLon = path + "process\\venue_lat_lon.txt";
        File userHomeLatLngFile = new File(userHomeFile);
        File trainRatingFile = new File(trainRating);
        File locationLatLngFile = new File(locationLatLon);
        File outputPath = new File(output);

        double[] ab = Utils.loadUserHomePowerLawParams(userHomeLatLngFile,
                trainRatingFile, locationLatLngFile, zeroDistanceDefaultValue,
                outputPath, FILE_NAME_HOME_POWERLAW_PARAM);
    }
}
