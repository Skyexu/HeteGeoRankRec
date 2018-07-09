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
 * @Date: 18:01 2018/7/8
 * @Description: 计算元路径 U-P-C-P 的基于计数的相似度
 */
public class MakeUPCP implements MakeMetaPath{
    private static final Log LOG = LogFactory.getLog(MakeUPCP.class);

    private BiMap<String, Integer> userIds, itemIds,categoryIds;
    private int numUsers;
    private int numItems;
    private int numCategories;
    private SparseMatrix UPMatrix;
    private String upInputDataPath;
    private String pcInputDataPath; // venue category file path
    private DenseMatrix preferenceMatrix;
    private SparseMatrix PCMatrix;

    public MakeUPCP(String upInputDataPath,String pcInputDataPath){
        this.upInputDataPath = upInputDataPath;
        this.pcInputDataPath = pcInputDataPath;
    }
    @Override
    public void processData() throws IOException{
        readUPData(upInputDataPath);
        readVCData(pcInputDataPath);
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
    private void readVCData(String vcInputDataPath) throws IOException {
        LOG.info(String.format("venue category Dataset: %s", StringUtil.last(vcInputDataPath, 38)));
        String[] content = FileUtil.read(vcInputDataPath,null);
        Table<Integer, Integer, Integer> dataTable = HashBasedTable.create();
        // Map {col-id, multiple row-id}: used to fast build a rating matrix
        Multimap<Integer, Integer> colMap = HashMultimap.create();
        categoryIds = HashBiMap.create();

        for (int i = 0; i < content.length; i++) {
            String line = content[i];
            String[] data = line.trim().split("\t");
            String venue = data[0];
            String category = data[1];
            if (!itemIds.containsKey(venue))
                continue;
            int row = itemIds.get(venue);
            int col = categoryIds.containsKey(category) ? categoryIds.get(category) : categoryIds.size();
            categoryIds.put(category, col);

            dataTable.put(row, col, 1);
            colMap.put(col, row);
        }
        int numRows = numItems(), numCols = numCategorys();
        PCMatrix = new SparseMatrix(numRows, numCols, dataTable, colMap);
        numCategories = numCols;
        // release memory of data table
        dataTable = null;
    }
    private void processPreferenceMatrix(){

        preferenceMatrix = new DenseMatrix(numUsers, numItems);
        DenseMatrix upc = new DenseMatrix(numUsers,numCategories);
        for (int userIdx = 0; userIdx < numUsers; userIdx++) {
            SparseVector userRow = UPMatrix.row(userIdx);
            for (int cateIndex = 0; cateIndex < numCategories; cateIndex++) {
                SparseVector cateColumn = PCMatrix.column(cateIndex);
                double value =  userRow.inner(cateColumn);
                upc.set(userIdx,cateIndex,value);
            }
        }
        for (int userIdx = 0; userIdx < numUsers; userIdx++) {
            DenseVector userRow = upc.row(userIdx);
            for (int itemIndex = 0; itemIndex < numItems; itemIndex++) {
                SparseVector venueColumn = PCMatrix.row(itemIndex);
                double value =  userRow.inner(venueColumn);
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
    public int numCategorys() {
        return categoryIds.size();
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
