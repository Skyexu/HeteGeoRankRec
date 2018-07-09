package hdu.geomf;

import net.librec.common.LibrecException;
import net.librec.conf.Configuration;
import net.librec.data.model.TextDataModel;
import net.librec.eval.RecommenderEvaluator;
import net.librec.eval.ranking.AveragePrecisionEvaluator;
import net.librec.eval.ranking.NoveltyEvaluator;
import net.librec.eval.ranking.PrecisionEvaluator;
import net.librec.eval.ranking.RecallEvaluator;
import net.librec.recommender.Recommender;
import net.librec.recommender.RecommenderContext;
import net.librec.recommender.item.RecommendedItem;
import net.librec.util.FileUtil;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

/**
 * @Author: Skye
 * @Date: 22:05 2018/7/9
 * @Description:
 */
public class GeoMetaPathMFTest {
    protected static final Log LOG = LogFactory.getLog(GeoMetaPathMFTest.class);

    public static void main(String[] args) throws LibrecException, IOException, ClassNotFoundException {
        LocalDateTime time = LocalDateTime.now();
        String timeString = time.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH_mm_ss"));

        String path  = "D:\\Works\\论文\\dataSet\\experimentData\\Foursquare\\";
        // build data model
        Configuration conf = new Configuration();
        conf.set("dfs.data.dir", path);
        conf.set("data.input.path","process/小数据量/metapath/user_chekin_venue_count.txt");
        //conf.set("data.input.path","process/user_chekin_venue_count.txt");
        conf.set("dfs.result.dir",path+"result");


        String outputPath  =  conf.get("dfs.result.dir") + "/" + "user_chekin_venue_count" + timeString;

        conf.set("data.appender.class", "geoup");
        conf.set("data.appender.poilatlon", "process\\小数据量\\venue_place_small.txt");
        conf.set("data.appender.up", "process\\小数据量\\user_chekin_venue_count.txt");

        //conf.set("data.appender.path", "process/venue_lat_lon.txt");
        TextDataModel dataModel = new TextDataModel(conf);
        dataModel.buildDataModel();

        // build recommender context
        RecommenderContext context = new RecommenderContext(conf, dataModel);

        // build recommender
        conf.set("rec.iterator.maximum", "20");
        conf.set("rec.user.regularization", "0.01");
        conf.set("rec.item.regularization", "0.01");
        conf.set("rec.factor.number", "10");
        conf.set("rec.recommender.isranking", "true");
        conf.set("rec.recommender.ranking.topn", "10");
        conf.set("rec.wrmf.weight.coefficient", "4.0");
        conf.set("data.model.splitter","ratio");
        conf.set("data.splitter.trainset.ratio","0.8");

        // set power-law parameter
        conf.set("rec.geomf.powerlaw.a","3.52855505698074E-4");
        conf.set("rec.geomf.powerlaw.b","-0.5152437346326175");
        conf.set("rec.geomf.alpha","0.8");

        Recommender recommender = new GeograpicalMetaPathMFRecommender();
        recommender.setContext(context);

        // run recommender algorithm
        // 调用 recommend 方法后，会调用 setup 方法初始化 contex 等信息
        recommender.recommend(context);

        // evaluate the recommended result

        RecommenderEvaluator PRECISION = new PrecisionEvaluator();
        PRECISION.setTopN(10);
        System.out.println("PRECISION:" + recommender.evaluate(PRECISION));
        RecommenderEvaluator RECALL = new RecallEvaluator();
        RECALL.setTopN(10);
        System.out.println("RECALL:" + recommender.evaluate(RECALL));
        RecommenderEvaluator AP = new AveragePrecisionEvaluator();
        AP.setTopN(10);
        System.out.println("AP:" + recommender.evaluate(AP));
        RecommenderEvaluator Novelty = new NoveltyEvaluator();
        Novelty.setTopN(10);
        System.out.println("Novelty:" + recommender.evaluate(Novelty));

        List<RecommendedItem> recommendedList = recommender.getRecommendedList();

        saveResult(recommendedList,outputPath);
    }

    public static void  saveResult(List<RecommendedItem> recommendedList,String outputPath) throws LibrecException, IOException, ClassNotFoundException {
        if (recommendedList != null && recommendedList.size() > 0) {
            // make output path


            // convert itemList to string
            StringBuilder sb = new StringBuilder();
            for (RecommendedItem recItem : recommendedList) {
                String userId = recItem.getUserId();
                String itemId = recItem.getItemId();
                String value = String.valueOf(recItem.getValue());
                sb.append(userId).append(",").append(itemId).append(",").append(value).append("\n");
            }
            String resultData = sb.toString();
            // save resultData
            try {
                FileUtil.writeString(outputPath, resultData);
                LOG.info("Result path is " + outputPath);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
}
