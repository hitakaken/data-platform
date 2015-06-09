package com.novbank.data.excel;

import com.typesafe.config.Config;
import org.apache.poi.ss.usermodel.Workbook;
import org.kitesdk.morphline.api.Command;
import org.kitesdk.morphline.api.CommandBuilder;
import org.kitesdk.morphline.api.MorphlineContext;
import org.kitesdk.morphline.api.Record;
import org.kitesdk.morphline.base.Validator;
import org.kitesdk.morphline.stdio.AbstractParser;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.Charset;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

/**
 * Created by hp on 2015/6/9.
 */
public class ReadExcelBuilder implements CommandBuilder {
    @Override
    public Collection<String> getNames() {
        return Collections.singletonList("readExcel");
    }

    @Override
    public Command build(Config config, Command parent, Command child, MorphlineContext context) {
        return new ReadExcel(this, config, parent, child, context);
    }

    private static final class ReadExcel extends AbstractParser {
        private final String separator;
        private final Charset charset;
        private final String schemaSheet;
        private final String dataSheet;
        private final List<String> columnNames;
        private final List<String> columnNameMap;
        private final List<String> columnSeparatorMap;
        private final List<String> columnTagMap;
        private final int ignoreRows;
        private final int rowAsHeader;
        private final boolean trim;
        private final boolean addEmptyStrings;
        private final int maxCharactersPerRecord;
        private final boolean ignoreTooLongRecords;

        public ReadExcel(CommandBuilder builder, Config config, Command parent, Command child, MorphlineContext context) {
            super(builder,config,parent,child,context);
            this.separator = getConfigs().getString(config, "separator", ",");
            this.schemaSheet = getConfigs().getString(config, "schema");
            this.dataSheet = getConfigs().getString(config, "data");
            this.columnNames = getConfigs().getStringList(config, "columns");
            this.columnNameMap = getConfigs().getStringList(config, "fields");
            this.columnSeparatorMap = getConfigs().getStringList(config, "separators");
            this.columnTagMap = getConfigs().getStringList(config, "tags");
            this.charset = getConfigs().getCharset(config, "charset", null);
            this.ignoreRows = getConfigs().getInt(config, "ignoreRowNum", 0);
            this.rowAsHeader = getConfigs().getInt(config, "rowAsHeader", 1);
            this.trim = getConfigs().getBoolean(config, "trim", true);
            this.addEmptyStrings = getConfigs().getBoolean(config, "addEmptyStrings", false);
            this.maxCharactersPerRecord = getConfigs().getInt(config, "maxCharactersPerRecord", 1000 * 1000);
            this.ignoreTooLongRecords = new Validator<OnMaxCharactersPerRecord>().validateEnum(
                    config,
                    getConfigs().getString(config, "onMaxCharactersPerRecord", OnMaxCharactersPerRecord.throwException.toString()),
                    OnMaxCharactersPerRecord.class) == OnMaxCharactersPerRecord.ignoreRecord;
            validateArguments();
        }

        @Override
        protected boolean doProcess(Record inputRecord, InputStream stream) throws IOException {
            Record template = inputRecord.copy();
            removeAttachments(template);
            Charset detectedCharset = detectCharset(inputRecord, charset);
            Workbook workbook = new ;
            BufferedReader reader = new BufferedReader(
                    new InputStreamReader(stream, detectedCharset), getBufferSize(stream));
            if (ignoreFirstLine) {
                reader.readLine();
            }

            while (true) {
                Record outputRecord = readNext(reader, template);
                if (outputRecord == null) {
                    break;
                }
                incrementNumRecords();

                // pass record to next command in chain:
                if (!getChild().process(outputRecord)) {
                    return false;
                }
            }
            return true;
        }

        private Record readNext(BufferedReader reader, Record template) throws IOException {
            while (true) {
                String line = reader.readLine();
                if (line == null) {
                    return null;
                }

                if (!QuotedCSVTokenizer.verifyRecordLength(
                        line.length(), maxCharactersPerRecord, line, ignoreTooLongRecords, LOG)) {
                    continue; // ignore
                }

                if (ignoreEmptyLines && isTrimmedLineEmpty(line)) {
                    continue; // ignore
                }

                if (commentPrefix.length() > 0 && line.startsWith(commentPrefix)) {
                    continue; // ignore
                }

                Record outputRecord = template.copy();
                if (!tokenizer.tokenizeLine(line, reader, outputRecord)) {
                    continue; // ignore
                }

                return outputRecord;
            }
        }

        private boolean isTrimmedLineEmpty(String line) {
//      return line.trim().length() == 0; // slow
            int len = line.length();
            for (int i = len; --i >= 0; ) {
                if (line.charAt(i) > ' ') {
                    return false;
                }
            }
            return true;
        }
    }

    private static enum OnMaxCharactersPerRecord {
        ignoreRecord,
        throwException,
    }
}
