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
        String path  = "D:\\Works\\论文\\dataSet\\experimentData\\Foursquare\\process\\实验数据8_21\\";
        String upFile = path + "newTrain_uvc.txt";
        String vcFile = path + "venueId_categoryId.txt";
        MakeUPCP upcpMaker = new MakeUPCP(upFile,vcFile);
        upcpMaker.processData();
        DenseMatrix uupMatrix = upcpMaker.getPreferenceMatrix();
        String outputPath = path + "metapath\\"+"upcp.txt";
        boolean done = Utils.saveDenseMatrix(uupMatrix,upcpMaker.getUserIds(),upcpMaker.getItemIds(),outputPath);
        if (done)
            System.out.println("Result path is " + outputPath);
    }
}
