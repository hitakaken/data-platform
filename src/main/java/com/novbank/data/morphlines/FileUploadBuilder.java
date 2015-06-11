package com.novbank.data.morphlines;

import com.typesafe.config.Config;
import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.security.UserGroupInformation;
import org.kitesdk.morphline.api.Command;
import org.kitesdk.morphline.api.CommandBuilder;
import org.kitesdk.morphline.api.MorphlineCompilationException;
import org.kitesdk.morphline.api.MorphlineContext;
import org.kitesdk.morphline.base.AbstractCommand;

import java.io.File;
import java.io.IOException;
import java.security.PrivilegedExceptionAction;
import java.util.*;

/**
 * Created by CaoKe on 2015/6/10.
 */
public class FileUploadBuilder implements CommandBuilder {
    public Collection<String> getNames() {
        return Collections.singletonList("uploadFiles");
    }

    public Command build(Config config, Command parent, Command child, MorphlineContext context) {
        try {
            return new UploadFile(this, config, parent, child, context);
        } catch (IOException var6) {
            throw new MorphlineCompilationException("Cannot compile", config, var6);
        }
    }

    private static final class UploadFile extends AbstractCommand {
        private static final Set<String> DONE = new HashSet<String>();
        public UploadFile(CommandBuilder builder, Config config, Command parent, Command child, MorphlineContext context) throws IOException {
            super(builder, config, parent, child, context);
            List uris = this.getConfigs().getStringList(config, "inputFiles", Collections.<String>emptyList());
            Path dstRootDir = new Path(this.getConfigs().getString(config, "outputDir", "."));
            String user = this.getConfigs().getString(config, "user", null);
            String hdfs = this.getConfigs().getString(config, "hdfs", null);
            Configuration conf = new Configuration();
            if(hdfs!=null){
                conf.set("fs.defaultFS", hdfs);
            }
            if(user!=null){
                conf.set("hadoop.job.ugi", user);
            }
            String defaultFileSystemUri = this.getConfigs().getString(config, "fs", (String)null);
            if(defaultFileSystemUri != null) {
                FileSystem.setDefaultUri(conf, defaultFileSystemUri);
            }

            Iterator i$ = this.getConfigs().getStringList(config, "conf", Collections.<String>emptyList()).iterator();

            while(i$.hasNext()) {
                String value = (String)i$.next();
                conf.addResource(new Path(value));
            }

            this.validateArguments();
            this.upload(uris, conf, dstRootDir);
        }

        private void upload(List<String> uris, final Configuration conf, Path dstRootDir) throws IOException {
            final FileSystem[] fs = new FileSystem[1];
            if(System.getProperty("user.name").equals(conf.get("hadoop.job.ugi"))){
                fs[0] = dstRootDir.getFileSystem(conf);
            }else{
                UserGroupInformation ugi = UserGroupInformation.createRemoteUser(conf.get("hadoop.job.ugi"));
                try {
                    ugi.doAs(new PrivilegedExceptionAction<Void>() {
                        public Void run() throws Exception {
                            System.out.println(conf.get("fs.defaultFS"));
                            System.out.println(conf.get("hadoop.job.ugi"));
                            fs[0] = FileSystem.get(conf);
                            return null;
                        }
                    });
                } catch (InterruptedException e) {
                    //ignore
                }
            }

            for(String uri : uris){
                File root = new File(uri);
                fs[0].copyFromLocalFile(false, true, new Path(root.toString()), dstRootDir);
            }
        }
    }
}
