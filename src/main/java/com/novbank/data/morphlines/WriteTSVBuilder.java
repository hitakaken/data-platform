package com.novbank.data.morphlines;

import com.google.common.base.Charsets;
import com.google.common.collect.Lists;
import com.google.common.io.Files;
import com.typesafe.config.Config;
import org.apache.commons.lang.StringUtils;
import org.kitesdk.morphline.api.Command;
import org.kitesdk.morphline.api.CommandBuilder;
import org.kitesdk.morphline.api.MorphlineContext;
import org.kitesdk.morphline.api.Record;
import org.kitesdk.morphline.base.AbstractCommand;

import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

/**
 * Created by CaoKe on 2015/6/10.
 */
public class WriteTSVBuilder implements CommandBuilder {
    @Override
    public Collection<String> getNames() {
        return Collections.singletonList("writeTSV");
    }

    @Override
    public Command build(Config config, Command parent, Command child, MorphlineContext context) {
        return new WriteTSV(this, config, parent, child, context);
    }

    private static final class WriteTSV extends AbstractCommand {
        private final List<String> fields;
        private final String output;
        private final String joiner;
        private final Charset charset;
        protected WriteTSV(CommandBuilder builder, Config config, Command parent, Command child, MorphlineContext context) {
            super(builder, config, parent, child, context);
            this.fields = getConfigs().getStringList(config, "fields");
            this.output = getConfigs().getString(config, "output");
            this.joiner = getConfigs().getString(config, "joiner", "|");
            this.charset = getConfigs().getCharset(config, "charset", Charsets.UTF_8);
        }

        @Override
        protected boolean doProcess(Record record) {
            File file = new File(output);
            try {
                if(!file.exists()){
                    file.getParentFile().mkdirs();
                    Files.append(StringUtils.join(fields,"\t")+System.lineSeparator(),file,charset);
                }
                List<String> line = Lists.newArrayList();
                for(String field : fields){
                    if(record.get(field)!=null)
                        line.add(StringUtils.join(record.get(field), joiner));
                    else
                        line.add("");
                }
                Files.append(StringUtils.join(line,"\t")+System.lineSeparator(),file,charset);
            } catch (IOException e) {
                return false;
            }
            // pass record to next command in chain:
            if(!getChild().process(record)){
                return false;
            }
            return true;
        }
    }
}
