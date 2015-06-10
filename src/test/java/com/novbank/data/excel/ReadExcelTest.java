package com.novbank.data.excel;

import com.google.common.collect.ImmutableMap;
import com.novbank.data.AbstractHDFSTest;
import com.novbank.data.ExtFields;
import com.typesafe.config.ConfigFactory;
import org.apache.hadoop.fs.Path;
import org.junit.Test;
import org.kitesdk.morphline.api.Record;
import org.kitesdk.morphline.base.Fields;

/**
 * Created by hp on 2015/6/9.
 */
public class ReadExcelTest extends AbstractHDFSTest {
    @Test
    public void testReadExcel() throws Exception {
        String uploader = "张怡菁";
        morphline = createMorphline("test-morphlines/readExcel", ConfigFactory.parseMap(
                ImmutableMap.of("uploader", uploader)));
        String location = "/user/yjzhang/全球价值链/2015/06";
        String fileName =  ".文档.xlsx";
        Record record = new Record();
        //Path file = getDFS().makeQualified(new Path(location,fileName));
        record.put(ExtFields.SOURCE_FILE_LOCATION,location);
        record.put(ExtFields.SOURCE_FILE_NAME,fileName);
        //System.out.println(file.toString());
        record.put(Fields.ATTACHMENT_BODY, "hdfs://10.1.20.93:8020/user/yjzhang/全球价值链/2015/06/.文档.xlsx");
        morphline.process(record);
    }
}