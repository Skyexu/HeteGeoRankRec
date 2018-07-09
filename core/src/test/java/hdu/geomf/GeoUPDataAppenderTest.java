package hdu.geomf;

import net.librec.BaseTestCase;
import net.librec.common.LibrecException;
import net.librec.data.convertor.TextDataConvertor;
import net.librec.data.convertor.appender.SocialDataAppender;
import net.librec.util.DriverClassUtil;
import net.librec.util.ReflectionUtil;
import org.junit.Before;
import org.junit.Test;

import java.io.IOException;

import static org.junit.Assert.assertTrue;

/**
 * @Author: Skye
 * @Date: 20:32 2018/7/9
 * @Description:
 */
public class GeoUPDataAppenderTest extends BaseTestCase {
    @Before
    public void setUp() throws Exception {
        super.setUp();
        conf.set("data.appender.class", "geoup");
        conf.set("dfs.data.dir","D:\\Works\\论文\\dataSet\\experimentData\\Foursquare\\process");
        conf.set("data.input.path","小数据量\\metapath\\upcp.txt");
    }

    /**
     * Test the function of read file.
     *
     * @throws IOException
     * @throws ClassNotFoundException
     */
    @Test
    public void testReadFile() throws IOException, LibrecException, ClassNotFoundException {
        String inputPath = conf.get("dfs.data.dir") + "/" + conf.get("data.input.path");
        TextDataConvertor textDataConvertor = new TextDataConvertor(inputPath);
        textDataConvertor.processData();
        conf.set("data.appender.poilatlon", "小数据量\\venue_place_small.txt");
        conf.set("data.appender.up", "小数据量\\user_chekin_venue_count.txt");
        GeoUPDataAppender dataFeature = (GeoUPDataAppender) ReflectionUtil.newInstance(DriverClassUtil.getClass(conf.get("data.appender.class")), conf);
        dataFeature.setItemMappingData(textDataConvertor.getItemIds());
        dataFeature.setUserMappingData(textDataConvertor.getUserIds());
        dataFeature.processData();

        assertTrue(dataFeature.getPoiLocation().size()>0);

    }

    /**
     * Test the function of read directory.
     *
     * @throws IOException
     */
    @Test
    public void testReadDir() throws IOException, LibrecException {
        String inputPath = conf.get("dfs.data.dir") + "/" + conf.get("data.input.path");
        TextDataConvertor textDataConvertor = new TextDataConvertor(inputPath);
        textDataConvertor.processData();
        conf.set("data.appender.path", "test/test-append-dir");
        SocialDataAppender dataFeature = new SocialDataAppender(conf);
        dataFeature.setItemMappingData(textDataConvertor.getItemIds());
        dataFeature.processData();

        assertTrue(dataFeature.getUserAppender().numRows() == dataFeature.getUserAppender().numColumns());
        assertTrue(dataFeature.getUserAppender().numRows() <= textDataConvertor.getUserIds().size());
    }
}
