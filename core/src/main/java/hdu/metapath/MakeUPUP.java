package hdu.metapath;

import net.librec.math.structure.SparseMatrix;

/**
 * @Author: Skye
 * @Date: 21:12 2018/5/31
 * @Description:  计算元路径 U-P-U-P 的基于计数的相似度
 *
 */
public class MakeUPUP implements MakeMetaPath{
    private SparseMatrix UPMatrix;

    @Override
    public void processData() {

    }

    @Override
    public SparseMatrix getPreferenceMatrix() {
        return null;
    }
}
