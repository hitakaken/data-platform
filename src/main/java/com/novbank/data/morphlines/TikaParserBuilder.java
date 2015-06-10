package com.novbank.data.morphlines;

import com.typesafe.config.Config;
import org.apache.tika.Tika;
import org.apache.tika.exception.TikaException;
import org.kitesdk.morphline.api.Command;
import org.kitesdk.morphline.api.CommandBuilder;
import org.kitesdk.morphline.api.MorphlineContext;
import org.kitesdk.morphline.api.Record;
import org.kitesdk.morphline.stdio.AbstractParser;

import java.io.IOException;
import java.io.InputStream;
import java.util.Collection;
import java.util.Collections;

/**
 * Created by hp on 2015/6/10.
 */
public class TikaParserBuilder implements CommandBuilder {
    @Override
    public Collection<String> getNames() {
        return Collections.singletonList("parseByTika");
    }

    @Override
    public Command build(Config config, Command parent, Command child, MorphlineContext context) {
        return new TikaParser(this, config, parent, child, context);
    }

    private static final class TikaParser extends AbstractParser {
        private final int mode;  //0 String 1 XHTML
        private final String field;
        protected TikaParser(CommandBuilder builder, Config config, Command parent, Command child, MorphlineContext context) {
            super(builder, config, parent, child, context);
            this.mode = getConfigs().getString(config, "mode", "text").equalsIgnoreCase("text")?0:1;
            this.field = getConfigs().getString(config, "field");
            validateArguments();
        }

        @Override
        protected boolean doProcess(Record record, InputStream inputStream) throws IOException {
            Tika tika = new Tika();
            try {
                record.put(field, tika.parseToString(inputStream));
            } catch (TikaException e) {
                //ignore
                e.printStackTrace();
            }
            tika = null;
            if(!getChild().process(record)){
                return false;
            }
            return true;
        }
    }
}
