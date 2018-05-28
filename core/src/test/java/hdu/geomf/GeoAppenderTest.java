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
 * @Date: 2:35 2018/5/23
 * @Description:
 */
public class GeoAppenderTest extends BaseTestCase{
    @Before
    public void setUp() throws Exception {
        super.setUp();
        conf.set("data.appender.class", "geo");
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
        conf.set("data.appender.path", "geo/venue_lat_lon.txt");
        GeoDataAppender dataFeature = (GeoDataAppender) ReflectionUtil.newInstance(DriverClassUtil.getClass(conf.get("data.appender.class")), conf);
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
        dataFeature.setUserMappingData(textDataConvertor.getUserIds());
        dataFeature.processData();

        assertTrue(dataFeature.getUserAppender().numRows() == dataFeature.getUserAppender().numColumns());
        assertTrue(dataFeature.getUserAppender().numRows() <= textDataConvertor.getUserIds().size());
    }
}
