package hdu.metapath;

import hdu.util.Utils;
import net.librec.math.structure.DenseMatrix;

import java.io.IOException;

/**
 * @Author: Skye
 * @Date: 0:18 2018/7/8
 * @Description:
 */
public class MakeUUPTest {
    public static void main(String[] args) throws IOException {
        String path  = "D:\\Works\\论文\\dataSet\\experimentData\\Foursquare\\";
        String upFile = path + "process/小数据量/user_chekin_venue_count.txt";
        String uuFile = path + "process/小数据量/friendship.txt";
        MakeUUP uupMaker = new MakeUUP(upFile,uuFile);
        uupMaker.processData();
        DenseMatrix uupMatrix = uupMaker.getPreferenceMatrix();
        String outputPath = path + "process\\小数据量\\metapath\\"+"uup.txt";
        boolean done = Utils.saveDenseMatrix(uupMatrix,uupMaker.getUserIds(),uupMaker.getItemIds(),outputPath);
        if (done)
            System.out.println("Result path is " + outputPath);
    }
}
