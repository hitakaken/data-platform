package com.novbank.data.morphlines;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Sets;
import com.typesafe.config.ConfigFactory;
import org.junit.Test;
import org.kitesdk.morphline.api.Record;

import java.io.File;
import java.util.Set;

import static org.junit.Assert.*;

/**
 * Created by hp on 2015/6/11.
 */
public class UploadFileTest extends AbstractHDFSTest{
    @Test
    public void testUpload() throws Exception{
        Set<String> users = Sets.newHashSet("xtao", "ycwen", "ygyang", "yjzhang");
        //String user = "xtao";
        for(String user : users){
            File input =  new File("D:\\Workspace\\data\\全球价值链\\gvc\\数据\\" + user);
            String out = "hdfs://10.1.20.93:8020/user/gvc/数据/"+user;
            morphline = createMorphline("test-morphlines/uploadFile",
                    ConfigFactory.parseMap(
                            ImmutableMap.of(
                                    "outputDir", out,
                                    "inputFile", input.toString()
                            )));
            morphline.process(new Record());
        }
    }

}