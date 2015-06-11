package com.novbank.data.morphlines;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Sets;
import com.typesafe.config.ConfigFactory;
import org.junit.Test;
import org.kitesdk.morphline.api.Record;

import java.io.File;
import java.util.Set;

/**
 * Created by hp on 2015/6/10.
 */
public class DownloadHDFSTest extends AbstractHDFSTest {
    @Test
    public void testDownload() throws Exception {
        Set<String> users = Sets.newHashSet("xtao","ycwen","ygyang","yjzhang");
        for(String user : users){
            File out = new File("D:\\Workspace\\data\\全球价值链\\"+user);
            morphline = createMorphline("test-morphlines/downloadHdfsFile",
                    ConfigFactory.parseMap(
                            ImmutableMap.of("inputFile", "hdfs://10.1.20.93:8020/user/"+user+("ycwen".equals(user)?"/":"/全球价值链")+"/2015", "outputDir", out.toString())));
            morphline.process(new Record());
        }

    }


}
