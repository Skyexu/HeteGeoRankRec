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
        String test1 = "D:\\Works\\论文\\dataSet\\experimentData\\Foursquare\\process\\实验数据8_21\\result\\metapath_feature_result\\upcp";
        String test2 = "D:\\Works\\论文\\dataSet\\experimentData\\Foursquare\\process\\实验数据8_21\\result\\metapath_feature_result\\upcpup";
        String test3 = "D:\\Works\\论文\\dataSet\\experimentData\\Foursquare\\process\\实验数据8_21\\result\\metapath_feature_result\\upup";
        String test4 = "D:\\Works\\论文\\dataSet\\experimentData\\Foursquare\\process\\实验数据8_21\\result\\metapath_feature_result\\uup";
        String test5 = "D:\\Works\\论文\\dataSet\\experimentData\\Foursquare\\process\\实验数据8_21\\result\\metapath_feature_result\\up";
        List<String> inputFiles = new ArrayList<>();
        inputFiles.add(test1);
        inputFiles.add(test2);
        inputFiles.add(test3);
        inputFiles.add(test4);
        inputFiles.add(test5);

        CombineFeature combineFeature = new CombineFeature(inputFiles,458,93,5);

        combineFeature.process();

        DenseMatrix denseMatrix = combineFeature.getFeatureMatix();
        BiMap<String,Integer> map = combineFeature.getUservenueMapping();
    }
}
