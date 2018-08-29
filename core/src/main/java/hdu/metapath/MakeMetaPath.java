package hdu.metapath;

import com.google.common.collect.BiMap;
import net.librec.math.structure.DenseMatrix;
import net.librec.math.structure.SparseMatrix;

import java.io.IOException;

/**
 * @Author: Skye
 * @Date: 21:37 2018/5/31
 * @Description:
 */
public interface  MakeMetaPath {

    void processData() throws IOException;
    DenseMatrix getPreferenceMatrix();
    BiMap<String, Integer> getUserIds();
    BiMap<String, Integer> getItemIds();
}
