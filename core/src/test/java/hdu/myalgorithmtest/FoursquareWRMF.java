package hdu.myalgorithmtest;

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
import net.librec.recommender.cf.ranking.WRMFRecommender;
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
 * @Date: 9:33 2018/4/18
 * @Description:
 */
public class FoursquareWRMF {
    protected static final Log LOG = LogFactory.getLog(FoursquareWRMF.class);

    public static void main(String[] args) throws LibrecException, IOException, ClassNotFoundException {
        LocalDateTime time = LocalDateTime.now();
        String timeString = time.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH_mm_ss"));

        String path  = "D:\\Works\\论文\\dataSet\\experimentData\\Foursquare\\";
        // build data model
        Configuration conf = new Configuration();
        conf.set("dfs.data.dir", path);
        conf.set("data.input.path","process/user_chekin_venue_count.txt");
        conf.set("dfs.result.dir",path+"result");


        String outputPath  =  conf.get("dfs.result.dir") + "/" + "user_chekin_venue_count" + timeString;
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
        conf.set("rec.recommender.ranking.topn", "5");
        conf.set("rec.wrmf.weight.coefficient", "4.0");
        conf.set("data.model.splitter","ratio");
        conf.set("data.splitter.trainset.ratio","0.8");

        Recommender recommender = new WRMFRecommender();
        recommender.setContext(context);

        // run recommender algorithm
        recommender.recommend(context);

        // evaluate the recommended result

        RecommenderEvaluator PRECISION = new PrecisionEvaluator();
        PRECISION.setTopN(5);
        System.out.println("PRECISION:" + recommender.evaluate(PRECISION));
        RecommenderEvaluator RECALL = new RecallEvaluator();
        RECALL.setTopN(5);
        System.out.println("RECALL:" + recommender.evaluate(RECALL));
        RecommenderEvaluator AP = new AveragePrecisionEvaluator();
        AP.setTopN(5);
        System.out.println("AP:" + recommender.evaluate(AP));
        RecommenderEvaluator Novelty = new NoveltyEvaluator();
        Novelty.setTopN(5);
        System.out.println("Novelty:" + recommender.evaluate(Novelty));

        List<RecommendedItem> recommendedList = recommender.getRecommendedList();

        saveResult(recommendedList,outputPath);
    }

    public static void  saveResult(List<RecommendedItem> recommendedList,String outputPath) throws LibrecException, IOException, ClassNotFoundException {
        if (recommendedList != null && recommendedList.size() > 0) {
            // make output path

            LOG.info("Result path is " + outputPath);
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
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
}
