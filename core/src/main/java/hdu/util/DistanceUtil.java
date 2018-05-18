package hdu.util;

import java.math.BigDecimal;

public class DistanceUtil {
    private static final double EARTH_RADIUS = 6378.137;

    private static double rad(double d)
    {
        return d * Math.PI / 180.0;
    }

    /**
     *
     * 计算球面距离,单位（米）
     *
     * @param lat1 纬度 1
     * @param lon1 经度 1
     * @param lat2 纬度 2
     * @param lon2 经度 2
     * @return
     */
    public static Double distance(Double lat1,Double lon1,Double lat2, Double lon2){
        double radLat1 = rad(lat1);
        double radLat2 = rad(lat2);
        double a = radLat1 - radLat2;
        double b = rad(lon1) - rad(lon2);
        Double s = 2 * Math.asin(Math.sqrt(Math.pow(Math.sin(a/2),2)+Math.cos(radLat1)*Math.cos(radLat2)*Math.pow(Math.sin(b/2),2)));
        s = s * EARTH_RADIUS * 1000;
        s = new BigDecimal(s).setScale(6, BigDecimal.ROUND_HALF_UP).doubleValue();
        return s;
    }

    public static void main(String[] args) {
        System.out.println(DistanceUtil.distance(39.992907, 116.391728, 39.985336, 116.37736));
    }
}
