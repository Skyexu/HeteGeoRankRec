package hdu.myalgorithmtest;

import net.librec.conf.Configuration;
import net.librec.data.model.TextDataModel;
import net.librec.eval.RecommenderEvaluator;
import net.librec.eval.ranking.PrecisionEvaluator;
import net.librec.eval.rating.RMSEEvaluator;
import net.librec.recommender.Recommender;
import net.librec.recommender.RecommenderContext;
import net.librec.recommender.cf.rating.SVDPlusPlusRecommender;
import net.librec.similarity.PCCSimilarity;
import net.librec.similarity.RecommenderSimilarity;

/**
 * @Author: Skye
 * @Date: 18:25 2018/4/16
 * @Description:
 */
public class SVDTest {
    public static void main(String[] args) throws Exception{
        // build data model
        Configuration conf = new Configuration();
        conf.set("dfs.data.dir", System.getProperty("user.dir") +"/data");
        conf.set("data.input.path","/filmtrust/rating");
        TextDataModel dataModel = new TextDataModel(conf);
        dataModel.buildDataModel();

        // build recommender context
        RecommenderContext context = new RecommenderContext(conf, dataModel);

        // build similarity
        conf.set("rec.recommender.similarity.key" ,"item");
        RecommenderSimilarity similarity = new PCCSimilarity();
        similarity.buildSimilarityMatrix(dataModel);
        context.setSimilarity(similarity);

        // build recommender
//        rec.iterator.learnrate=0.01
//        rec.iterator.learnrate.maximum=0.01
//        rec.iterator.maximum=13
//        rec.user.regularization=0.01
//        rec.item.regularization=0.01
//        rec.impItem.regularization=0.001
//        rec.factor.number=10
//        rec.learnrate.bolddriver=false
//        rec.learnrate.decay=1.0
        conf.set("rec.iterator.learnrate", "0.01");
        conf.set("rec.iterator.learnrate.maximum", "0.01");
        conf.set("rec.iterator.maximum", "20");
        conf.set("rec.user.regularization", "0.01");
        conf.set("rec.item.regularization", "0.01");
        conf.set("rec.impItem.regularization", "0.001");
        conf.set("rec.factor.number", "10");
        conf.set("rec.learnrate.bolddriver", "false");
        conf.set("rec.learnrate.decay", "1.0");
        Recommender recommender = new SVDPlusPlusRecommender();
        recommender.setContext(context);

        // run recommender algorithm
        recommender.recommend(context);

        // evaluate the recommended result
        RecommenderEvaluator evaluator = new RMSEEvaluator();
        System.out.println("RMSE:" + recommender.evaluate(evaluator));
        RecommenderEvaluator PRECISION = new PrecisionEvaluator();
        PRECISION.setTopN(10);
        System.out.println("PRECISION:" + recommender.evaluate(PRECISION));
    }
}
