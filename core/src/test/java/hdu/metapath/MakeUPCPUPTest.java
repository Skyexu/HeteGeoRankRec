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
        String path  = "D:\\Works\\论文\\dataSet\\experimentData\\Foursquare\\process\\实验数据8_21\\";
        String upFile = path + "newTrain_uvc.txt";
        String pcFile = path + "venueId_categoryId.txt";
        MakeUPCPUP upcpupMaker = new MakeUPCPUP(upFile,pcFile);
        upcpupMaker.processData();
        DenseMatrix uupMatrix = upcpupMaker.getPreferenceMatrix();
        String outputPath = path + "metapath\\"+"upcpup.txt";
        boolean done = Utils.saveDenseMatrix(uupMatrix,upcpupMaker.getUserIds(),upcpupMaker.getItemIds(),outputPath);
        if (done)
            System.out.println("Result path is " + outputPath);
    }
}
