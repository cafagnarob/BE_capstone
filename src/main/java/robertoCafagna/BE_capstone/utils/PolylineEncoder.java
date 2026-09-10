package robertoCafagna.BE_capstone.utils;

import java.util.List;

public class PolylineEncoder {
    private PolylineEncoder() {
    }

    public static String encode(List<double[]> points) {
        StringBuilder result = new StringBuilder();
        long prevLat = 0, prevLng = 0;
        for (double[] point : points) {
            long lat = Math.round(point[0] * 1e5);
            long lng = Math.round(point[1] * 1e5);
            encodeValue(lat - prevLat, result);
            encodeValue(lng - prevLng, result);
            prevLat = lat;
            prevLng = lng;
        }
        return result.toString();
    }

    private static void encodeValue(long value, StringBuilder result) {
        long v = value < 0 ? ~(value << 1) : (value << 1);
        while (v >= 0x20) {
            result.append((char) ((0x20 | (v & 0x1f)) + 63));
            v >>= 5;
        }
        result.append((char) (v + 63));
    }
}
