package hdu.metapath;

import hdu.util.Utils;
import net.librec.math.structure.DenseMatrix;

import java.io.IOException;

/**
 * @Author: Skye
 * @Date: 21:51 2018/7/8
 * @Description:
 */
public class MakeUPCPTest {
    public static void main(String[] args) throws IOException {
        String path  = "D:\\Works\\论文\\dataSet\\experimentData\\Foursquare\\";
        String upFile = path + "process/小数据量/user_chekin_venue_count.txt";
        String pcFile = path + "process/小数据量/venueId_categoryId.txt";
        MakeUPCP upcpMaker = new MakeUPCP(upFile,pcFile);
        upcpMaker.processData();
        DenseMatrix uupMatrix = upcpMaker.getPreferenceMatrix();
        String outputPath = path + "process\\小数据量\\metapath\\"+"upcp.txt";
        boolean done = Utils.saveDenseMatrix(uupMatrix,upcpMaker.getUserIds(),upcpMaker.getItemIds(),outputPath);
        if (done)
            System.out.println("Result path is " + outputPath);
    }
}
