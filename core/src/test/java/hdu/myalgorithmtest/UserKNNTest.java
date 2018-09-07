package hdu.myalgorithmtest;

import net.librec.BaseTestCase;
import net.librec.common.LibrecException;
import net.librec.conf.Configuration;
import net.librec.job.RecommenderJob;
import org.junit.Before;
import org.junit.Test;

import java.io.IOException;

/**
 * @Author: Skye
 * @Date: 8:45 2018/9/6
 * @Description:
 */
public class UserKNNTest extends BaseTestCase {

    @Override
    @Before
    public void setUp() throws Exception {
        super.setUp();
    }

    /**
     * test the whole rating process of UserKNN recommendation
     *
     * @throws ClassNotFoundException
     * @throws LibrecException
     * @throws IOException
     */
    // @Ignore
    @Test
    public void testRecommender() throws ClassNotFoundException, LibrecException, IOException {
        Configuration.Resource resource = new Configuration.Resource("rec/skye/userknn-testranking-skye.properties");
        conf.addResource(resource);
        conf.set("rec.recommender.ranking.topn","10");

        String path  = "D:\\Works\\论文\\dataSet\\experimentData\\Foursquare\\";
        // build data model

        conf.set("dfs.data.dir", path);
        //conf.set("data.input.path","process/user_chekin_venue_count.txt");
        conf.set("data.input.path","process/小数据量/user_chekin_venue_count.txt");
        conf.set("data.model.splitter", "ratio");

//        // 这里需要传切分测试集与训练集之前的数据
//        conf.set("data.input.path",conf.get("hete.preference.name"));
//
//        // split train test
//        conf.set("data.model.splitter","testset");
//        conf.set("data.testset.path",conf.get("hete.test.name"));
        conf.set("rec.eval.enable", "true");
        RecommenderJob job = new RecommenderJob(conf);
        job.runJob();
    }
}
