package hdu.util;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @Author: Skye
 * @Date: 17:05 2018/5/16
 * @Description:
 */
public class UserHomeTest {
    public static void main(String[] args) throws CheckinException {
        String path  = "D:\\Works\\论文\\dataSet\\experimentData\\Foursquare\\";
        String userVenueLatLonFile = path + "process\\user_venue_count_lat_lon.txt";
        String outputUserHomeFile = path + "process\\user_home.txt";
        UserHomeUtil.getHomeLocationFromUserCheckin(userVenueLatLonFile,outputUserHomeFile);

    }


}
