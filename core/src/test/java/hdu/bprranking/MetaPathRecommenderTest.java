package hdu.bprranking;

import com.google.common.collect.BiMap;
import hdu.geomf.GeograpicalMetaPathMFRecommender;
import net.librec.common.LibrecException;
import net.librec.conf.Configuration;
import net.librec.data.model.TextDataModel;
import net.librec.math.structure.DenseMatrix;
import net.librec.recommender.Recommender;
import net.librec.recommender.RecommenderContext;

import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

/**
 * @Author: Skye
 * @Date: 22:32 2018/7/16
 * @Description:
 */
public class MetaPathRecommenderTest {
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




        LocalDateTime time = LocalDateTime.now();
        String timeString = time.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH_mm_ss"));

        String path  = "D:\\Works\\论文\\dataSet\\experimentData\\Foursquare\\";
        // build data model
        Configuration conf = new Configuration();
        conf.set("dfs.data.dir", path);
        //conf.set("data.input.path","process/小数据量/metapath/upcp.txt");
        conf.set("data.input.path","process/小数据量/user_chekin_venue_count.txt");
        conf.set("dfs.result.dir",path+"result");


        String outputPath  =  conf.get("dfs.result.dir") + "/" + "upup" + timeString;

        conf.set("data.appender.class", "geoup");
        conf.set("data.appender.poilatlon", "process\\小数据量\\venue_place_small.txt");
        conf.set("data.appender.up", "process\\小数据量\\user_chekin_venue_count.txt");

        //conf.set("data.appender.path", "process/venue_lat_lon.txt");
        TextDataModel dataModel = new TextDataModel(conf);
        dataModel.buildDataModel();

        // build recommender context
        RecommenderContext context = new RecommenderContext(conf, dataModel);

        // build recommender
        conf.set("rec.iterator.maximum", "40");
        conf.set("rec.metapathbpr.regularization", "0.01");
        conf.set("rec.recommender.isranking", "true");
        conf.set("rec.recommender.ranking.topn", "10");
        conf.set("data.model.splitter","ratio");
        conf.set("data.splitter.trainset.ratio","0.8");


        MetaPathBPRRecommender recommender = new MetaPathBPRRecommender(denseMatrix,map);
        recommender.setContext(context);

        // run recommender algorithm
        // 调用 recommend 方法后，会调用 setup 方法初始化 contex 等信息
        recommender.recommend(context);
    }
}
