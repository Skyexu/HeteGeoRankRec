package hdu.metapath;

import com.google.common.collect.BiMap;
import hdu.util.Utils;
import net.librec.math.structure.DenseMatrix;

import java.io.*;

/**
 * @Author: Skye
 * @Date: 0:18 2018/7/8
 * @Description:
 */
public class MakeUUPTest {
    public static void main(String[] args) throws IOException {
        String path  = "D:\\Works\\论文\\dataSet\\experimentData\\Foursquare\\process\\实验数据8_21\\";
        String upFile = path + "newTrain_uvc.txt";
        String uuFile = path + "friendship.txt";
        MakeUUP uupMaker = new MakeUUP(upFile,uuFile);
        uupMaker.processData();
        DenseMatrix uupMatrix = uupMaker.getPreferenceMatrix();
        String outputPath = path + "metapath\\"+"uup.txt";
        boolean done = saveDenseMatrix(uupMatrix,uupMaker.getUserIds(),uupMaker.getItemIds(),outputPath);
        if (done)
            System.out.println("Result path is " + outputPath);
    }
    public static boolean saveDenseMatrix(DenseMatrix matrix, BiMap<String, Integer> userIds, BiMap<String, Integer> itemIds, String outputFile) throws IOException {
        BiMap<Integer, String> inverseUserIds = userIds.inverse();
        BiMap<Integer, String> inverseItemIds = itemIds.inverse();
        File file = new File(outputFile);
        BufferedWriter writer = null;
        try {
            if (! file.getParentFile().exists())
                file.getParentFile().mkdirs();
            writer = new BufferedWriter(new OutputStreamWriter(
                    new FileOutputStream(file)));
            if (matrix != null) {
                String userId,itemId;
                int data;
                for (int i = 0; i < matrix.numRows(); i++) {
                    if (!inverseUserIds.containsKey(i))
                        continue;
                    userId = inverseUserIds.get(i);
                    for (int j = 0; j < matrix.numColumns(); j++) {
                        if (!inverseItemIds.containsKey(j))
                            continue;
                        itemId = inverseItemIds.get(j);
                        data = (int)matrix.get(i,j);
                        //if (data > 0)
                        writer.write(userId + "\t" + itemId + "\t" + data +"\n");
                    }
                }

            }
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        } finally {
            writer.close();
        }
        return true;
    }
}
