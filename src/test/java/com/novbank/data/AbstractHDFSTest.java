package com.novbank.data;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.security.UserGroupInformation;
import org.junit.Before;
import org.kitesdk.morphline.api.AbstractMorphlineTest;

import java.io.IOException;
import java.security.PrivilegedExceptionAction;

/**
 * Created by hp on 2015/6/10.
 */
public class AbstractHDFSTest extends AbstractMorphlineTest {
    private FileSystem dfs;

    protected FileSystem getDFS() {
        return dfs;
    }

    @Before
    public void setupFS() throws Exception {
        System.setProperty("hadoop.home.dir","D:\\Tools\\hadoop\\hadoop-2.6.0");
        final String user = "gvc";
        UserGroupInformation ugi = UserGroupInformation.createRemoteUser(user);
        ugi.doAs(new PrivilegedExceptionAction<Void>() {
            public Void run() throws Exception {
                Configuration conf = new Configuration();
                conf.set("fs.defaultFS", "hdfs://10.1.20.93:8020");
                conf.set("hadoop.job.ugi", user);
                dfs = FileSystem.get(conf);
                return null;
            }
        });
    }
}
