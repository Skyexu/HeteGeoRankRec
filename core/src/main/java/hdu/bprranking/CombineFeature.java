package hdu.bprranking;

import com.google.common.collect.BiMap;
import com.google.common.collect.HashBiMap;
import hdu.util.FileUtil;
import net.librec.common.LibrecException;
import net.librec.math.structure.DenseMatrix;

import java.io.IOException;
import java.util.List;

/**
 * @Author: Skye
 * @Date: 16:26 2018/7/14
 * @Description: 读取每个元路径语义相似度矩阵计算出的偏好特征，将特征连接为一个向量
 *                  构造用户、兴趣点特征矩阵，形式为
 *                  userId_1,venueId_1,f1,f2,f3.....
 *                  userId_1,venueId_2,f1,f2,f3.....
 *                  userId_2,venueId_1,f1,f2,f3.....
 *                  userId_2,venueId_2,f1,f2,f3.....
 */
public class CombineFeature {
    private BiMap<String,Integer> uservenueMapping;
    private List<String> inputFiles;
    private DenseMatrix featureMatix;
    private int numUsers;
    private int numVenues;
    private int numFeatures;
    public CombineFeature(List<String> inputFiles,int numUsers,int numVenues,int numFeatures){
        this.inputFiles = inputFiles;
        this.numUsers = numUsers;
        this.numVenues = numVenues;
        this.numFeatures = numFeatures;
    }
    public void process() throws IOException, LibrecException {
        featureMatix = new DenseMatrix(numUsers * numVenues, numFeatures);
        this.uservenueMapping = HashBiMap.create();
        for (int i = 0; i < inputFiles.size(); i++) {
            String file = inputFiles.get(i);
            String[] content = FileUtil.read(file,null);
            if (content == null || content.length == 0 ){
                throw new IOException(file + "content is null");
            }
            if (content.length != numUsers * numVenues){
                throw new LibrecException("content length not right");
            }
            for (int j = 0; j < content.length; j++) {
                String line = content[j];
                String[] data = line.trim().split("\t");
                String user_venue = data[0]+ "_"+ data[1];
                double value = Double.parseDouble(data[2]);
                int row = uservenueMapping.containsKey(user_venue) ? uservenueMapping.get(user_venue) : uservenueMapping.size();
                uservenueMapping.put(user_venue, row);
                // 设置当前行(user venue)，第 i 个特征
                featureMatix.set(row,i,value);
            }
        }

    }

    public BiMap<String, Integer> getUservenueMapping() {
        return uservenueMapping;
    }

    public void setUservenueMapping(BiMap<String, Integer> uservenueMapping) {
        this.uservenueMapping = uservenueMapping;
    }

    public DenseMatrix getFeatureMatix() {
        return featureMatix;
    }

    public void setFeatureMatix(DenseMatrix featureMatix) {
        this.featureMatix = featureMatix;
    }
}
