package hdu.geomf;

import com.google.common.collect.*;
import hdu.metapath.MakeUPCPUP;
import hdu.util.FileUtil;
import net.librec.common.LibrecException;
import net.librec.conf.Configuration;
import net.librec.conf.Configured;
import net.librec.data.DataAppender;
import net.librec.math.structure.SparseMatrix;
import net.librec.util.StringUtil;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.HashMap;

/**
 * @Author: Skye
 * @Date: 17:02 2018/7/9
 * @Description: process and store geo appender data (地点经纬度文件、用户签到文件)
 */
public class GeoUPDataAppender extends Configured implements DataAppender {
    private static final Log LOG = LogFactory.getLog(GeoUPDataAppender.class);
    /** The path of the appender data file */
    private String poiDataPath;
    private String upDataPath;
    /** POI lat lon map */
    private HashMap<Integer,double[]> poiLocation = new HashMap<>();
    private SparseMatrix upTrainMatrix;
    /**
     * user Mapping Data
     */
    public BiMap<String, Integer> userMappingData;

    /**
     * item Mapping Data
     */
    public BiMap<String, Integer> itemMappingData;
    public GeoUPDataAppender(){this(null);}

    public GeoUPDataAppender(Configuration conf) {
        this.conf = conf;
    }
    @Override
    public void processData() throws IOException {
        if (conf != null && StringUtils.isNotBlank(conf.get("data.appender.poilatlon"))&&StringUtils.isNotBlank(conf.get("data.appender.up"))) {
            poiDataPath = conf.get("dfs.data.dir") + "/" + conf.get("data.appender.poilatlon");
            upDataPath = conf.get("dfs.data.dir") + "/" + conf.get("data.appender.up");
            readData(poiDataPath);
            readUPData(upDataPath);
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
        LOG.info(String.format("poi lat lon data: %s", StringUtil.last(inputDataPath, 38)));
        BufferedReader br = new BufferedReader(new InputStreamReader(new FileInputStream(inputDataPath), "UTF-8"));
        String line = null;
        while ((line = br.readLine()) != null) {
            String[] data = line.split("\t");
            if (data.length > 1){
                if (!itemMappingData.containsKey(data[0]))
                    continue;
                int itemIndex = itemMappingData.get(data[0]);
                poiLocation.put(itemIndex, new double[]{Double.parseDouble(data[1]),Double.parseDouble(data[2])});
            }

        }
        br.close();
    }
    private void readUPData(String inputDataPath) throws IOException {
        LOG.info(String.format("up data: %s", StringUtil.last(inputDataPath, 38)));
        // Table {row-id, col-id, count}
        Table<Integer, Integer, Integer> dataTable = HashBasedTable.create();
        // Map {col-id, multiple row-id}: used to fast build a rating matrix
        Multimap<Integer, Integer> colMap = HashMultimap.create();

        String[] content = FileUtil.read(inputDataPath,null);
        for (int i = 0; i < content.length; i++) {
            String line = content[i];
            String[] data = line.trim().split("[ \t,]+");
            String user = data[0];
            String item = data[1];
            int count = Double.valueOf(data[2]).intValue();
            if (!userMappingData.containsKey(user))
                throw new IOException("there is no user:" + user);
            int row = userMappingData.get(user);
            if (!itemMappingData.containsKey(item))
                throw new IOException("there is no item: " + item);
            int col =  itemMappingData.get(item) ;

            dataTable.put(row, col, count);
            colMap.put(col, row);
        }

        // build counting matrix
        upTrainMatrix = new SparseMatrix(userMappingData.size(), itemMappingData.size(), dataTable, colMap);
        SparseMatrix.reshape(upTrainMatrix);
        // release memory of data table
        dataTable = null;
        content = null;
    }
    public HashMap<Integer, double[]> getPoiLocation() {
        return poiLocation;
    }

    public SparseMatrix getUpTrainMatrix() {
        return upTrainMatrix;
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
