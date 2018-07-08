package hdu.metapath;

import com.google.common.collect.*;
import hdu.util.FileUtil;
import net.librec.math.structure.DenseMatrix;
import net.librec.math.structure.DenseVector;
import net.librec.math.structure.SparseMatrix;
import net.librec.math.structure.SparseVector;
import net.librec.util.StringUtil;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.io.IOException;

/**
 * @Author: Skye
 * @Date: 15:25 2018/7/7
 * @Description:
 */
public class MakeUUP implements MakeMetaPath{
    private static final Log LOG = LogFactory.getLog(MakeUUP.class);

    private BiMap<String, Integer> userIds, itemIds;
    private int numUsers;
    private int numItems;
    private SparseMatrix UPMatrix;
    private String upInputDataPath;
    private String uuInputDataPath;
    private DenseMatrix preferenceMatrix;
    private DenseMatrix uuMatrix;

    public MakeUUP(String upInputDataPath,String uuInputDataPath){
        this.upInputDataPath = upInputDataPath;
        this.uuInputDataPath = uuInputDataPath;
    }
    @Override
    public void processData() throws IOException{
        readUPData(upInputDataPath);
        readUUData(uuInputDataPath);
        processPreferenceMatrix();
    }
    private void readUPData(String inputDataPath) throws IOException {
        LOG.info(String.format("Dataset: %s", StringUtil.last(inputDataPath, 38)));
        // Table {row-id, col-id, count}
        Table<Integer, Integer, Integer> dataTable = HashBasedTable.create();
        // Map {col-id, multiple row-id}: used to fast build a rating matrix
        Multimap<Integer, Integer> colMap = HashMultimap.create();
        if (this.userIds == null) {
            this.userIds = HashBiMap.create();
        }
        if (this.itemIds == null) {
            this.itemIds = HashBiMap.create();
        }
        String[] content = FileUtil.read(inputDataPath,null);
        for (int i = 0; i < content.length; i++) {
            String line = content[i];
            String[] data = line.trim().split("[ \t,]+");
            String user = data[0];
            String item = data[1];
            int count = Integer.parseInt(data[2]);
            int row = userIds.containsKey(user) ? userIds.get(user) : userIds.size();
            userIds.put(user, row);

            int col = itemIds.containsKey(item) ? itemIds.get(item) : itemIds.size();
            itemIds.put(item, col);

            dataTable.put(row, col, count);
            colMap.put(col, row);
        }
        int numRows = numUsers(), numCols = numItems();
        // build counting matrix
        UPMatrix = new SparseMatrix(numRows, numCols, dataTable, colMap);
        numUsers = numUsers();
        numItems = numItems();
        // release memory of data table
        dataTable = null;
    }
    private void readUUData(String uuInputDataPath) throws IOException {
        LOG.info(String.format("user friend Dataset: %s", StringUtil.last(uuInputDataPath, 38)));
        String[] content = FileUtil.read(uuInputDataPath,null);
        int userIndex,friendIndex;
        uuMatrix = new DenseMatrix(numUsers,numUsers);
        for (int i = 0; i < content.length; i++) {
            String line = content[i];
            String[] data = line.trim().split("[ \t,]+");
            String user = data[0];
            String friend = data[1];
            if (userIds.containsKey(user)){
                userIndex = userIds.get(user);
            }else {
                continue;
            }

            if (userIds.containsKey(friend)){
                friendIndex = userIds.get(friend);
            }else {
                continue;
            }

            uuMatrix.set(userIndex,friendIndex,1.0);
        }
    }
    private void processPreferenceMatrix(){

        preferenceMatrix = new DenseMatrix(numUsers, numItems);
        for (int userIdx = 0; userIdx < numUsers; userIdx++) {
            DenseVector userRow = uuMatrix.row(userIdx);
            for (int itemIndex = 0; itemIndex < numItems; itemIndex++) {
                SparseVector itemColumn = UPMatrix.column(itemIndex);
                double value =  userRow.inner(itemColumn);
                preferenceMatrix.set(userIdx,itemIndex,value);
            }
        }
    }

    @Override
    public DenseMatrix getPreferenceMatrix() {
        return this.preferenceMatrix;
    }

    public int numUsers() {
        return userIds.size();
    }

    public int numItems() {
        return itemIds.size();
    }

    public BiMap<String, Integer> getUserIds() {
        return userIds;
    }

    public BiMap<String, Integer> getItemIds() {
        return itemIds;
    }

    public SparseMatrix getUPMatrix() {
        return UPMatrix;
    }
}
