package hdu.util;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 *
 * @Author: Skye
 * @Date: 15:38 2018/5/17
 * @Description:  compute user home location
 * via KDD 2011 Friendship and Mobility: User Movement In Location-Based Social Networks
 */
public class UserHomeUtil {
    public static void getHomeLocationFromUserCheckin(String userVenueLatLonFile,String outputUserHomeFile) throws CheckinException {
        // 经度为 20KM 的 GeoHash
        GeoHash geoHash = new GeoHash(4);
        // GeoHash lat lon
        Map<String,List<double[]>> geoHashLatLon = new HashMap<>();
        // User home lat lone
        Map<Integer,double[]> userHomeDicMap = new HashMap<>();

        // 读取用户签到过的地点及经纬度文件
        String[] userVenueLatLonStr =  FileUtil.read(userVenueLatLonFile,null);
        if (userVenueLatLonStr.length == 0 ){
            throw new CheckinException("userVenueLatLon file is null ");
        }
        // 假设数据已经按照 userId 排序
        int lastUserID = Integer.parseInt(Utils.parseLine(userVenueLatLonStr[0],5)[0]);
        for (String userStr :userVenueLatLonStr) {
            String array[] = Utils.parseLine(userStr,5);
            int userID  = Integer.parseInt(array[0]);
            int venueID = Integer.parseInt(array[1]);
            int ckCount = Integer.parseInt(array[2]);
            double lat = Double.parseDouble(array[3]);
            double lon = Double.parseDouble(array[4]);
            if (userID != lastUserID){
                // do compute last user home location
                double[] home = computeHomeLocation(geoHashLatLon);
                userHomeDicMap.put(lastUserID,home);
                geoHashLatLon = new HashMap<>();
                lastUserID = userID;
            }
            String geohashStr = geoHash.encode(lat,lon);
            if (geoHashLatLon.containsKey(geohashStr)){
                for (int i = 0; i < ckCount; i++) {
                    geoHashLatLon.get(geohashStr).add(new double[]{lat,lon});
                }
            }else {
                geoHashLatLon.put(geohashStr,new ArrayList<>());
                for (int i = 0; i < ckCount; i++) {
                    geoHashLatLon.get(geohashStr).add(new double[]{lat,lon});
                }
            }

        }
        // lat user
        double[] home = computeHomeLocation(geoHashLatLon);
        userHomeDicMap.put(lastUserID,home);

        // save user home
        List<String> userHomeList = new ArrayList<>();
        for (Map.Entry<Integer,double[]> map:userHomeDicMap.entrySet()) {
            StringBuilder stringBuilder = new StringBuilder();
            stringBuilder.append(map.getKey()).append(CheckinConstants.DELIMITER);
            double[] location = map.getValue();
            stringBuilder.append(location[0]).append(CheckinConstants.DELIMITER);
            stringBuilder.append(location[1]).append(CheckinConstants.DELIMITER);
            userHomeList.add(stringBuilder.toString());
        }
        FileUtil.write(outputUserHomeFile,userHomeList.toArray(new String[userHomeList.size()]),false);
        System.out.println("user home location saved at " + outputUserHomeFile);
    }

    /**
     * 计算 home location
     * @param geoHashLatLon
     * @return
     */
    public static double[] computeHomeLocation(Map<String,List<double[]>> geoHashLatLon ){
        // 获取次数最多的 geoHash 网格
        int maxCount = 0;
        String maxGeoHash = "";
        for (Map.Entry<String,List<double[]>> map:geoHashLatLon.entrySet()) {
            String geoHashStr = map.getKey();
            List<double[]> latLonList = map.getValue();
            int placeCount = latLonList.size();
            if (maxCount == 0){
                maxCount = latLonList.size();
                maxGeoHash = geoHashStr;
            }else {
                if (placeCount > maxCount){
                    maxCount = placeCount;
                    maxGeoHash = geoHashStr;
                }
            }
        }
        List<double[]> latLonList = geoHashLatLon.get(maxGeoHash);
        // 以网格内的平均签到经纬度来作为 home
        double aveLat = 0;
        double aveLon = 0;
        for (double[] location:
                latLonList) {
            aveLat += location[0];
            aveLon += location[1];
        }
        aveLat = aveLat/latLonList.size();
        aveLon = aveLon/latLonList.size();

        return new double[]{aveLat,aveLon};
    }
}
