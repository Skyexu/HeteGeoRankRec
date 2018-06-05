package hdu.metapath;

import net.librec.math.structure.DenseMatrix;
import net.librec.math.structure.SparseMatrix;

import java.io.IOException;

/**
 * @Author: Skye
 * @Date: 19:57 2018/6/5
 * @Description:
 */
public class MakeUPUPTest {
    public static void main(String[] args) throws IOException {
        String path  = "D:\\Works\\论文\\dataSet\\experimentData\\Foursquare\\";
        String upFile = path + "process/user_chekin_venue_count.txt";
        MakeUPUP upupMaker = new MakeUPUP(upFile);
        upupMaker.processData();
        DenseMatrix upupMatrix = upupMaker.getPreferenceMatrix();
    }
}
