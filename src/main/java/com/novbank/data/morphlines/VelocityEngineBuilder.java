package com.novbank.data.morphlines;

import com.google.common.base.Charsets;
import com.google.common.collect.Lists;
import com.google.common.io.Files;
import com.typesafe.config.Config;
import org.apache.commons.lang.StringUtils;
import org.apache.velocity.VelocityContext;
import org.apache.velocity.app.VelocityEngine;
import org.kitesdk.morphline.api.Command;
import org.kitesdk.morphline.api.CommandBuilder;
import org.kitesdk.morphline.api.MorphlineContext;
import org.kitesdk.morphline.api.Record;
import org.kitesdk.morphline.base.AbstractCommand;
import org.kitesdk.morphline.base.FieldExpression;
import org.slf4j.helpers.FormattingTuple;
import org.slf4j.helpers.MessageFormatter;

import javax.xml.transform.Templates;
import java.io.File;
import java.io.IOException;
import java.io.StringWriter;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

/**
 * Created by hp on 2015/6/11.
 */
public class VelocityEngineBuilder implements CommandBuilder {
    protected VelocityEngine ve = new VelocityEngine();
    public VelocityEngineBuilder() {
        ve.setProperty("input.encoding","utf-8");
        ve.setProperty("output.encoding","utf-8");
        ve.setProperty("resource.loader","file");
        ve.setProperty("file.resource.loader.path","D:\\Workspace\\data\\全球价值链\\gvc\\site\\template");
        ve.init();
    }

    @Override
    public Collection<String> getNames() {
        return Collections.singletonList("velocity");
    }

    @Override
    public Command build(Config config, Command parent, Command child, MorphlineContext context) {
        return new WriteVelocity(this, config, parent, child, context);
    }

    private static final class WriteVelocity extends AbstractCommand {
        private final VelocityEngine ve;
        private final String template;
        private final String mode;
        private final String output;
        private final String suffix;
        private final String joiner;
        private final List<String> fields;

        protected WriteVelocity(CommandBuilder builder, Config config, Command parent, Command child, MorphlineContext context) {
            super(builder, config, parent, child, context);
            ve = ((VelocityEngineBuilder)builder).ve;
            this.template = getConfigs().getString(config, "template");
            this.mode = getConfigs().getString(config, "mode");
            this.output = getConfigs().getString(config, "output");
            this.suffix = getConfigs().getString(config, "suffix","");
            this.joiner = getConfigs().getString(config, "joiner","");
            this.fields = getConfigs().getStringList(config, "fields", Collections.<String>emptyList());
            validateArguments();
        }

        @Override
        protected boolean doProcess(Record record) {
            StringWriter writer = new StringWriter();
            VelocityContext context = new VelocityContext();
            context.put("record",record);
            ve.getTemplate(template).merge(context,writer);
            List<String> paths = Lists.newArrayList(output);
            for(String field : fields){
                if(record.get(field)==null || record.get(field).isEmpty())
                    return true;
                paths.add(record.getFirstValue(field).toString());
            }
            String path = StringUtils.join(paths,joiner) + suffix;
            if(mode.equalsIgnoreCase("file")){
                try {
                    File file = new File(path);
                    if(!file.getParentFile().exists()) file.getParentFile().mkdirs();
                    Files.write(writer.toString(),file, Charsets.UTF_8);
                } catch (IOException e) {
                    e.printStackTrace();
                    record.put("error","velocity");
                }
            }else{
                record.put(output,writer.toString());
            }
            System.out.println(writer.toString());
            if(!getChild().process(record)){
                return false;
            }
            return true;
        }
    }
}
