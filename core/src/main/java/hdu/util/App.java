package hdu.util;

public class App {

    public static void main(String[] args) {
        long start = System.currentTimeMillis();
        GeoHash geoHash = new GeoHash(10);
        System.out.println(new GeoHash(10).around(44.9999, 116.3967));
        System.out.println(new GeoHash(10).around(45.0001, 116.3967));
        System.out.println(DistanceUtil.distance(44.9999, 116.3967, 45.0001, 116.3967));
        System.out.println("waste time: " + (System.currentTimeMillis() - start));
        // long start = System.currentTimeMillis();
        // System.out.println(new GeoHash().encode(44.9999, 116.3967));
        // System.out.println(new GeoHash().encode(45.0001, 116.3967));
        // System.out.println("waste time: " + (System.currentTimeMillis() - start));
    }

}
