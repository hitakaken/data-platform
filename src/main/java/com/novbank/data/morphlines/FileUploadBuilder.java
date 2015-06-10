package com.novbank.data.morphlines;

import com.typesafe.config.Config;
import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.Path;
import org.kitesdk.morphline.api.Command;
import org.kitesdk.morphline.api.CommandBuilder;
import org.kitesdk.morphline.api.MorphlineCompilationException;
import org.kitesdk.morphline.api.MorphlineContext;
import org.kitesdk.morphline.base.AbstractCommand;

import java.io.File;
import java.io.IOException;
import java.util.*;

/**
 * Created by CaoKe on 2015/6/10.
 */
public class FileUploadBuilder implements CommandBuilder {
    public Collection<String> getNames() {
        return Collections.singletonList("downloadHdfsFile");
    }

    public Command build(Config config, Command parent, Command child, MorphlineContext context) {
        try {
            return new FileUploadBuilder.UploadLocalFile(this, config, parent, child, context);
        } catch (IOException var6) {
            throw new MorphlineCompilationException("Cannot compile", config, var6);
        }
    }

    private static final class UploadLocalFile extends AbstractCommand {
        private static final Set<String> DONE = new HashSet<String>();
        public UploadLocalFile(CommandBuilder builder, Config config, Command parent, Command child, MorphlineContext context) throws IOException {
            super(builder, config, parent, child, context);
            List uris = this.getConfigs().getStringList(config, "inputFiles", Collections.<String>emptyList());
            File dstRootDir = new File(this.getConfigs().getString(config, "outputDir", "."));
            Configuration conf = new Configuration();
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

        private void upload(List<String> uris, Configuration conf, File dstRootDir) throws IOException {

        }
    }
}
