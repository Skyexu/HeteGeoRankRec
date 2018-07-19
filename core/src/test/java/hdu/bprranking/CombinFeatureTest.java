package hdu.bprranking;

import com.google.common.collect.BiMap;
import net.librec.common.LibrecException;
import net.librec.math.structure.DenseMatrix;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * @Author: Skye
 * @Date: 21:24 2018/7/15
 * @Description:
 */
public class CombinFeatureTest {
    public static void main(String[] args) throws IOException, LibrecException {
        String test1 = "D:\\Works\\论文\\dataSet\\experimentData\\Foursquare\\result\\metapath_feature_result\\upcp2018-07-15 21_22_40";
        String test2 = "D:\\Works\\论文\\dataSet\\experimentData\\Foursquare\\result\\metapath_feature_result\\upup2018-07-14 16_16_36";
        List<String> inputFiles = new ArrayList<>();
        inputFiles.add(test1);
        inputFiles.add(test2);

        CombineFeature combineFeature = new CombineFeature(inputFiles,458,93,2);

        combineFeature.process();

        DenseMatrix denseMatrix = combineFeature.getFeatureMatix();
        BiMap<String,Integer> map = combineFeature.getUservenueMapping();
    }
}
