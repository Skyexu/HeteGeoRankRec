package hdu.metapath;

import net.librec.math.structure.SparseMatrix;

/**
 * @Author: Skye
 * @Date: 21:37 2018/5/31
 * @Description:
 */
public interface  MakeMetaPath {

    public void processData();
    public SparseMatrix getPreferenceMatrix();
}
