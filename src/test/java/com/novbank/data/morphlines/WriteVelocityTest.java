package com.novbank.data.morphlines;

import com.google.common.collect.Maps;
import org.junit.Test;
import org.kitesdk.morphline.api.AbstractMorphlineTest;
import org.kitesdk.morphline.api.Record;
import org.kitesdk.morphline.base.Fields;

import java.io.File;
import java.io.FileInputStream;
import java.nio.file.Files;
import java.util.Map;

import static org.junit.Assert.*;

/**
 * Created by hp on 2015/6/11.
 */
public class WriteVelocityTest extends AbstractMorphlineTest {
    @Test
    public void testWriteVelocity() throws Exception {
        morphline = createMorphline("test-morphlines/writeHTML");

        Map<String,Map<String,String>> users;
        File root;
        users = Maps.newHashMap();
        //陶翔
        users.put("xtao", Maps.<String, String>newHashMap());
        users.get("xtao").put("month","06");
        users.get("xtao").put("doc","文档TX.xlsx");
        users.get("xtao").put("name","陶翔");
        //温一村
        users.put("ycwen", Maps.<String, String>newHashMap());
        users.get("ycwen").put("month","01,03");
        users.get("ycwen").put("doc",".文档.xlsx");
        users.get("ycwen").put("org",".机构.xlsx");
        users.get("ycwen").put("name","温一村");
        //杨莺歌
        users.put("ygyang", Maps.<String, String>newHashMap());
        users.get("ygyang").put("month","06");
        users.get("ygyang").put("doc",".文档.xlsx");
        users.get("ygyang").put("org",".机构.xlsx");
        users.get("ygyang").put("name","杨莺歌");
        //张毅菁
        users.put("yjzhang", Maps.<String, String>newHashMap());
        users.get("yjzhang").put("month","06");
        users.get("yjzhang").put("doc",".文档.xlsx");
        users.get("yjzhang").put("org",".机构.xlsx");
        users.get("yjzhang").put("name","张毅菁");
        root = new File("D:\\Workspace\\data\\全球价值链\\gvc\\数据\\");
        File orgOutput = new File(root,"org.tsv");
        if(orgOutput.exists()) Files.delete(orgOutput.toPath());
        File docOutput = new File(root,"doc.tsv");
        if(docOutput.exists()) Files.delete(docOutput.toPath());
        for(String user : users.keySet()){
            for(String month : users.get(user).get("month").split(",")){
                if(users.get(user).containsKey("org")){
                    File file = new File(root,user + "/2015/"+month+"/"+users.get(user).get("org"));
                    if(file.exists()) {
                        FileInputStream in = new FileInputStream(file);
                        Record record = new Record();
                        record.put("uploader",user);
                        record.put("outputFile",orgOutput.toString());
                        record.put(Fields.ATTACHMENT_BODY,in);
                        morphline.process(record);
                        in.close();
                    }
                }
                if(users.get(user).containsKey("doc")){
                    File file = new File(root,user + "/2015/"+month+"/"+users.get(user).get("doc"));
                    if(file.exists()) {
                        FileInputStream in = new FileInputStream(file);
                        Record record = new Record();
                        record.put("uploader",user);
                        record.put("outputFile", docOutput.toString());
                        record.put("uploader_name",users.get(user).get("name"));
                        record.put(ExtFields.SOURCE_FILE_LOCATION,"/user/gvc/数据/"+user+"/2015/"+month+"/");
                        record.put(ExtFields.DOWNLOAD_ROOT,"/filebrowser/download/");
                        record.put(Fields.ATTACHMENT_BODY,in);

                        morphline.process(record);
                        in.close();
                    }
                }
            }

        }
    }
}