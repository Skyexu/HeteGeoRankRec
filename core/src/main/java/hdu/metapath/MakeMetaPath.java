package hdu.metapath;

import net.librec.math.structure.SparseMatrix;

import java.io.IOException;

/**
 * @Author: Skye
 * @Date: 21:37 2018/5/31
 * @Description:
 */
public interface  MakeMetaPath {

    public void processData() throws IOException;
    public SparseMatrix getPreferenceMatrix();
}
