package com.novbank.data.file;

import com.google.common.collect.ImmutableMap;
import com.novbank.data.AbstractHDFSTest;
import com.typesafe.config.ConfigFactory;
import org.junit.Test;
import org.kitesdk.morphline.api.Record;

import java.io.File;

/**
 * Created by hp on 2015/6/10.
 */
public class DownloadHDFSTest extends AbstractHDFSTest {
    @Test
    public void testDownload() throws Exception {
        String user = "kcao";
        File out = new File("D:\\Workspace\\data\\"+user);
        morphline = createMorphline("test-morphlines/downloadHdfsFile",
                ConfigFactory.parseMap(
                        ImmutableMap.of("inputFile", "hdfs://10.1.20.93:8020/user/"+user+"/全球价值链", "outputDir", out.toString())));
        morphline.process(new Record());
    }


}
