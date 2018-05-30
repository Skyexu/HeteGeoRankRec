package hdu.geomf;

import com.google.common.collect.BiMap;
import net.librec.conf.Configuration;
import net.librec.conf.Configured;
import net.librec.data.DataAppender;
import org.apache.commons.lang.StringUtils;
import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.HashMap;

/**
 * @Author: Skye
 * @Date: 1:50 2018/5/23
 * @Description:  precess and store geo appender data
 */
public class GeoDataAppender extends Configured implements DataAppender {
    /** The path of the appender data file */
    private String inputDataPath;
    /** POI lat lon map */
    private HashMap<Integer,double[]> poiLocation = new HashMap<>();
    /**
     * user Mapping Data
     */
    public BiMap<String, Integer> userMappingData;

    /**
     * item Mapping Data
     */
    public BiMap<String, Integer> itemMappingData;
    public GeoDataAppender(){this(null);}

    public GeoDataAppender(Configuration conf) {
        this.conf = conf;
    }
    @Override
    public void processData() throws IOException {
        if (conf != null && StringUtils.isNotBlank(conf.get("data.appender.path"))) {
            inputDataPath = conf.get("dfs.data.dir") + "/" + conf.get("data.appender.path");
            readData(inputDataPath);
        }
    }
    /**
     * Read data from the data file. Note that we didn't take care of the
     * duplicated lines.
     *
     * @param inputDataPath
     *            the path of the data file
     * @throws IOException if I/O error occurs during reading
     */
    private void readData(String inputDataPath) throws IOException {
        BufferedReader br = new BufferedReader(new InputStreamReader(new FileInputStream(inputDataPath), "UTF-8"));
        String line = null;
        while ((line = br.readLine()) != null) {
            String[] data = line.split("\t");
            if (data.length > 1){
                int itemIndex = itemMappingData.get(data[0]);
                poiLocation.put(itemIndex, new double[]{Double.parseDouble(data[1]),Double.parseDouble(data[2])});
            }

        }
        br.close();
    }

    public HashMap<Integer, double[]> getPoiLocation() {
        return poiLocation;
    }

    @Override
    public void setUserMappingData(BiMap<String, Integer> userMappingData) {
        this.userMappingData = userMappingData;
    }

    @Override
    public void setItemMappingData(BiMap<String, Integer> itemMappingData) {
        this.itemMappingData= itemMappingData;
    }
}
