package hdu.metapath;

import hdu.util.Utils;
import net.librec.math.structure.DenseMatrix;

import java.io.IOException;

/**
 * @Author: Skye
 * @Date: 22:05 2018/7/8
 * @Description:
 */
public class MakeUPCPUPTest {
    public static void main(String[] args) throws IOException {
        String path  = "D:\\Works\\论文\\dataSet\\experimentData\\Foursquare\\";
        String upFile = path + "process/小数据量/user_chekin_venue_count.txt";
        String pcFile = path + "process/小数据量/venueId_category_small.txt";
        MakeUPCPUP upcpupMaker = new MakeUPCPUP(upFile,pcFile);
        upcpupMaker.processData();
        DenseMatrix uupMatrix = upcpupMaker.getPreferenceMatrix();
        String outputPath = path + "process\\小数据量\\metapath\\"+"upcpup.txt";
        boolean done = Utils.saveDenseMatrix(uupMatrix,upcpupMaker.getUserIds(),upcpupMaker.getItemIds(),outputPath);
        if (done)
            System.out.println("Result path is " + outputPath);
    }
}
