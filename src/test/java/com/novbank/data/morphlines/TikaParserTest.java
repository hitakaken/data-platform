package com.novbank.data.morphlines;

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
public class TikaParserTest extends AbstractMorphlineTest {
    @Test
    public void testParse() throws Exception{
        morphline = createMorphline("test-morphlines/parseByTika");
        File file = new File("M:\\ygyang\\201408-JETRO-JETRO Global Trade and Investment Report.pdf");
        System.out.println(file.exists());
        FileInputStream in = new FileInputStream(file);
        Record record = new Record();
        record.put(Fields.ATTACHMENT_BODY,in);
        morphline.process(record);
        in.close();
    }
}