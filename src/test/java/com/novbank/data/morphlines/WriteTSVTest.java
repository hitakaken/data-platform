package com.novbank.data.morphlines;

import com.google.common.collect.ImmutableMap;
import com.typesafe.config.ConfigFactory;
import org.junit.Test;
import org.kitesdk.morphline.api.AbstractMorphlineTest;
import org.kitesdk.morphline.api.Record;
import org.kitesdk.morphline.base.Fields;

import java.io.File;
import java.io.FileInputStream;

import static org.junit.Assert.*;

/**
 * Created by CaoKe on 2015/6/10.
 */
public class WriteTSVTest extends AbstractMorphlineTest {
    @Test
    public void testWriteTSVl() throws Exception {
        String uploader = "张怡菁";
        morphline = createMorphline("test-morphlines/writeTSV", ConfigFactory.parseMap(
                ImmutableMap.of("uploader", uploader)));
        File file = new File("M:\\ygyang\\机构.xlsx");
        System.out.println(file.exists());
        FileInputStream in = new FileInputStream(file);
        Record record = new Record();
        record.put(Fields.ATTACHMENT_BODY,in);
        morphline.process(record);
        in.close();
    }
}