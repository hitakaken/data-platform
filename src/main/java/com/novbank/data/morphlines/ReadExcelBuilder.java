package com.novbank.data.morphlines;

import com.google.common.base.Strings;
import com.google.common.collect.*;
import com.typesafe.config.Config;
import org.apache.commons.lang.NumberUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.poi.openxml4j.exceptions.InvalidFormatException;
import org.apache.poi.ss.usermodel.*;
import org.kitesdk.morphline.api.Command;
import org.kitesdk.morphline.api.CommandBuilder;
import org.kitesdk.morphline.api.MorphlineContext;
import org.kitesdk.morphline.api.Record;
import org.kitesdk.morphline.base.Validator;
import org.kitesdk.morphline.stdio.AbstractParser;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.Charset;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.regex.Pattern;

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
        /**
         * 文件编码
         */
        private final Charset charset;

        /**
         * 自定义Schema表名
         */
        private final String schemaSheetName;
        /**
         * 转换数据的表名
         */
        private final String dataSheetName;
        /**
         * 读取的列（为空，则读取所有在列名->字段名中有映射的列）
         */
        private List<String> columnNames;
        /**
         * 必填存在值的列名（否则则忽略）
         */
        private List<String> requiredColumnNames;
        /**
         * 列名 -> 字段名的映射
         */
        private List<String> columnNameList;
        /**
         * 列名对应类型，默认为文本
         */
        private List<String> columnTypeList;
        /**
         * 默认值
         */
        private List<String> defaultValues;
        /**
         * 列对应的分隔符（null则不分隔）
         */
        private List<String> columnSeparatorList;
        /**
         * 默认分隔符（null则不分隔）
         */
        private final String defaultSeparator;
        /**
         * 列对应值的tag
         */
        private List<String> columnTagList;
        /**
         * 自标题行开始，忽略的记录行
         */
        private final int ignoreRows;
        /**
         * 标题行所在的列，-1表示不使用标题行
         */
        private final int rowAsHeader;
        /**
         * 如果不使用标题行，则使用前缀+序号标识
         */
        private final String columnNumAsHeaderPrefix;
        /**
         * 是否去空格
         */
        private final boolean trim;
        /**
         * 是否添加空值
         */
        private final boolean addEmptyStrings;
        /**
         * 最大字符串长度
         */
        private final int maxCharactersPerRecord;
        /**
         * 是否忽略过长记录（未实现）
         */
        private final boolean ignoreTooLongRecords;
        /**
         * 日期格式
         */
        private final DateFormat dateFormat;
        /**
         * 时间格式
         */
        private final DateFormat timeFormat;
        /**
         * 日期时间格式
         */
        private final DateFormat dateTimeFormat;

        public ReadExcel(CommandBuilder builder, Config config, Command parent, Command child, MorphlineContext context) {
            super(builder,config,parent,child,context);
            this.schemaSheetName = getConfigs().getString(config, "schema",null);
            this.dataSheetName = getConfigs().getString(config, "data");
            this.columnNames = getConfigs().getStringList(config, "columns",null);
            this.requiredColumnNames = getConfigs().getStringList(config, "requires",null);
            this.defaultValues = getConfigs().getStringList(config, "defaultValues", null);
            this.columnTypeList = getConfigs().getStringList(config,"types",null);
            this.columnNameList = getConfigs().getStringList(config, "fields",null);
            this.columnSeparatorList = getConfigs().getStringList(config, "separators",null);
            this.defaultSeparator = getConfigs().getString(config, "defaultSeparator", null);
            this.columnTagList = getConfigs().getStringList(config, "tags",null);
            this.charset = getConfigs().getCharset(config, "charset", null);
            this.rowAsHeader = getConfigs().getInt(config, "rowAsHeader", 0);
            this.columnNumAsHeaderPrefix = getConfigs().getString(config, "columnNumAsHeaderPrefix", "col_");
            this.ignoreRows = getConfigs().getInt(config, "ignoreRows", 0);
            this.trim = getConfigs().getBoolean(config, "trim", true);
            this.addEmptyStrings = getConfigs().getBoolean(config, "addEmptyStrings", false);
            this.maxCharactersPerRecord = getConfigs().getInt(config, "maxCharactersPerRecord", 1000 * 1000);
            this.dateFormat = new SimpleDateFormat(getConfigs().getString(config,"dataFormat","yyyy-MM-dd"));
            this.timeFormat = new SimpleDateFormat(getConfigs().getString(config,"timeFormat","HH:mm:ss"));
            this.dateTimeFormat = new SimpleDateFormat(getConfigs().getString(config,"dateTimeFormat","yyyy-MM-dd HH:mm:ss"));
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
            //Charset detectedCharset = detectCharset(inputRecord, charset);
            try {
                Workbook workbook = WorkbookFactory.create(stream) ;
                //获取数据 Sheet
                Sheet dataSheet = workbook.getSheet(dataSheetName);
                //Sheet不存在中止
                if(dataSheet == null) {
                    return false;
                }
                //查找标题栏
                Row headerRow = rowAsHeader <0 ? null : dataSheet.getRow(rowAsHeader);
                //查找第一列
                Row dataRow = null;
                int rowNum = headerRow != null ? headerRow.getRowNum() + 1 + ignoreRows : ignoreRows;
                while(dataRow == null && rowNum<=dataSheet.getLastRowNum()){
                    dataRow = dataSheet.getRow(rowNum);
                    rowNum ++;
                }
                //未找到第一列，则返回
                if(dataRow == null) return false;
                //创建列序->列名 映射
                columnIndexToName = Maps.newHashMap();
                columnNameToIndex = Maps.newHashMap();
                for(Iterator<Cell> iterator = headerRow!=null?headerRow.cellIterator():dataRow.cellIterator(); iterator.hasNext();){
                    Cell cell = iterator.next();
                    String headerText = headerRow!=null?convertCellValueToString(cell) : columnNumAsHeaderPrefix + cell.getColumnIndex();
                    if(Strings.isNullOrEmpty(headerText)) continue;
                    headerText = headerText.trim();
                    if(Strings.isNullOrEmpty(headerText)) continue;
                    columnIndexToName.put(cell.getColumnIndex(),headerText);
                    columnNameToIndex.put(headerText,cell.getColumnIndex());
                }
                //解析 Schema
                Sheet schemaSheet = Strings.isNullOrEmpty(schemaSheetName)? null : workbook.getSheet(schemaSheetName);
                if( schemaSheet != null)  generateConfig(schemaSheet);
                //解析失败，返回
                if(!generateSchemaBySettings())
                    return false;
                //读取数据
                while(rowNum <= dataSheet.getLastRowNum()+1){
                    if(dataRow!=null){
                        Record outputRecord = readRow(dataRow, template);
                        System.out.println(outputRecord);
                        // pass record to next command in chain:
                        if(!getChild().process(outputRecord)){
                            return false;
                        }
                    }
                    dataRow =  dataSheet.getRow(rowNum);
                    rowNum++;
                }
            } catch (InvalidFormatException e) {
                e.printStackTrace();
            }
            return true;
        }

        private static final Set<String> SCHEMA_DISPLAY_COLUMN_NAMES = new HashSet<String>(){{add("显示");add("display");add("column");}};
        private static final Set<String> SCHEMA_DISPLAY_FIELD_NAMES = new HashSet<String>(){{add("字段");add("field");}};
        private static final Set<String> SCHEMA_DISPLAY_TYPE_NAMES = new HashSet<String>(){{add("类型");add("type");}};
        private static final Set<String> SCHEMA_DISPLAY_TAG_NAMES = new HashSet<String>(){{add("额外标签");add("标签");add("tag");}};
        private static final Set<String> SCHEMA_DISPLAY_SEPARATOR_NAMES = new HashSet<String>(){{add("分隔符");add("separator");}};
        private static final Set<String> SCHEMA_DISPLAY_PROPERTY_NAMES = new HashSet<String>(){{add("属性");add("property");}};
        private static final Set<String> SCHEMA_DISPLAY_DEFAULT_NAMES = new HashSet<String>(){{add("默认值");add("default");}};

        private void generateConfig(Sheet schemaSheet) {
            Row header = schemaSheet.getRow(0);
            int displayIndex = -1;
            int fieldIndex = -1;
            int typeIndex = -1;
            int tagIndex = -1;
            int separatorIndex = -1;
            int propertyIndex = -1;
            int defaultIndex = -1;
            //读取列序
            for(Iterator<Cell> iterator = header.cellIterator();iterator.hasNext();){
                Cell cell = iterator.next();
                String text = convertCellValueToString(cell);
                if(text == null) return;
                if(displayIndex<0 && SCHEMA_DISPLAY_COLUMN_NAMES.contains(text.toLowerCase())){
                    displayIndex = cell.getColumnIndex();
                }else if(fieldIndex<0 && SCHEMA_DISPLAY_FIELD_NAMES.contains(text.toLowerCase())){
                    fieldIndex = cell.getColumnIndex();
                }else if(typeIndex<0 && SCHEMA_DISPLAY_TYPE_NAMES.contains(text.toLowerCase())){
                    typeIndex = cell.getColumnIndex();
                }else if(tagIndex<0 && SCHEMA_DISPLAY_TAG_NAMES.contains(text.toLowerCase())){
                    tagIndex = cell.getColumnIndex();
                }else if(separatorIndex<0 && SCHEMA_DISPLAY_SEPARATOR_NAMES.contains(text.toLowerCase())){
                    separatorIndex = cell.getColumnIndex();
                }else if(propertyIndex<0 && SCHEMA_DISPLAY_PROPERTY_NAMES.contains(text.toLowerCase())){
                    propertyIndex = cell.getColumnIndex();
                }else if(defaultIndex<0 && SCHEMA_DISPLAY_DEFAULT_NAMES.contains(text.toLowerCase())){
                    defaultIndex = cell.getColumnIndex();
                }
            }
            if(displayIndex<0) return;
            List<String> columnNames = Lists.newArrayList();
            List<String> requiredColumnNames = Lists.newArrayList();
            List<String> columnNameList = Lists.newArrayList();
            List<String> columnTypeList = Lists.newArrayList();
            List<String> defaultValues = Lists.newArrayList();
            List<String> columnSeparatorList = Lists.newArrayList();
            List<String> columnTagList = Lists.newArrayList();
            for(int rowNum = 1; rowNum<= schemaSheet.getLastRowNum(); rowNum++){
                Row row = schemaSheet.getRow(rowNum);
                String display = convertCellValueToString(row.getCell(displayIndex));
                if(Strings.isNullOrEmpty(display)) continue;
                columnNames.add(display);
                //列名->字段映射
                columnNameList.add(display);
                columnNameList.add(convertCellValueToString(fieldIndex <0?null:row.getCell(fieldIndex)));
                //列类型
                String type = convertCellValueToString(typeIndex <0?null:row.getCell(typeIndex));
                columnTypeList.add(display);
                columnTypeList.add(type!=null?"文本":type);
                //默认值
                defaultValues.add(display);
                defaultValues.add(convertCellValueToString(defaultIndex <0?null:row.getCell(defaultIndex)));
                //属性
                String property = convertCellValueToString(tagIndex < 0 ? null : row.getCell(propertyIndex));
                if(property!=null && property.contains("必填"))
                    requiredColumnNames.add(display);
                //分隔符
                String separator = convertCellValueToString(separatorIndex < 0 ? null : row.getCell(separatorIndex));
                if(property!=null && property.contains("多值") && Strings.isNullOrEmpty(separator)) separator =defaultSeparator;
                columnSeparatorList.add(display);
                columnSeparatorList.add(separator);
                //标签
                columnTagList.add(display);
                columnTagList.add(convertCellValueToString(tagIndex < 0 ? null : row.getCell(tagIndex)));

                this.columnNames = columnNames;
                this.requiredColumnNames = requiredColumnNames;
                this.columnNameList = columnNameList;
                this.columnTypeList = columnTypeList;
                this.defaultValues = defaultValues;
                this.columnSeparatorList = columnSeparatorList;
                this.columnTagList = columnTagList;
            }
        }

        private boolean generateSchemaBySettings() {
            //校验必填列是否存在
            requires = Sets.newHashSet();
            if(requiredColumnNames!=null){
                for(String requireColumnName: requiredColumnNames){
                    if (!columnNameToIndex.containsKey(requireColumnName))
                        return false;
                    requires.add(columnNameToIndex.get(requireColumnName));
                }
            }
            //列名<-->字段映射
            columnNameToField = Maps.newHashMap();
            if(columnNameList == null && columnNames == null){
                columnNames = Lists.newArrayList();
                columnNames.addAll(columnNameToIndex.keySet());
            }
            if(columnNameList!=null){
                for(int i = 0; i<columnNameList.size()-1;i+=2){
                    String columnName = columnNameList.get(i);
                    if(!columnNameToIndex.containsKey(columnName)) continue;
                    String fieldName = i+1<columnNameList.size()?columnNameList.get(i+1):null;
                    if(fieldName == null) fieldName = columnName;
                    columnNameToField.put(columnName,fieldName);
                }
            }
            if(columnNames!=null){
                for(String columnName : columnNames){
                    if(!columnNameToField.containsKey(columnName) && columnNameToIndex.containsKey(columnName))
                        columnNameToField.put(columnName,columnName);
                }
            }
            for(int require : requires){
                if(!columnNameToField.containsKey(columnIndexToName.get(require)))
                    columnNameToField.put(columnIndexToName.get(require),columnIndexToName.get(require));
            }
            //列名<-->分隔符
            columnNameToSeparator = Maps.newHashMap();
            if(columnSeparatorList!=null){
                for(int i = 0; i<columnSeparatorList.size()-1;i+=2){
                    String columnName = columnSeparatorList.get(i);
                    if(!columnNameToField.containsKey(columnName)) continue;
                    String separator = i+1<columnSeparatorList.size()?columnSeparatorList.get(i+1):null;
                    if(separator != null)
                        columnNameToSeparator.put(columnName,Pattern.compile(separator));
                }
            }
            //列名<-->标签
            columnNameToTag = Maps.newHashMap();
            if(columnTagList!=null){
                for(int i = 0; i<columnTagList.size()-1;i+=2){
                    String columnName = columnTagList.get(i);
                    if(!columnNameToField.containsKey(columnName)) continue;
                    String tag = i+1<columnTagList.size()?columnTagList.get(i+1):null;
                    if(tag != null)
                        columnNameToTag.put(columnName,tag);
                }
            }
            //列名<-->默认值
            columnNameToDefaultValue = Maps.newHashMap();
            if(defaultValues!=null){
                for(int i = 0; i<defaultValues.size()-1;i+=2){
                    String columnName = defaultValues.get(i);
                    if(!columnNameToField.containsKey(columnName)) continue;
                    String defaultValue = i+1<defaultValues.size()?defaultValues.get(i+1):null;
                    if(defaultValue != null)
                        columnNameToDefaultValue.put(columnName,defaultValue);
                }
            }
            //列名<-->类型
            columnNameToType = Maps.newHashMap();
            if(columnTypeList!=null){
                for(int i = 0; i<columnTypeList.size()-1;i+=2){
                    String columnName = columnTypeList.get(i);
                    if(!columnNameToField.containsKey(columnName)) continue;
                    String types = i+1<columnTypeList.size()?columnTypeList.get(i+1):null;
                    if(types != null){
                        columnNameToType.put(columnName,Sets.newHashSet(types.split("[ ,，;；]+")));
                    }
                }
            }
            for ( String columnName : columnNameToField.keySet()){
                if(!columnNameToType.containsKey(columnName))
                    columnNameToType.put(columnName,Sets.newHashSet("文本"));
            }
            //清理不需要解析的列
            Set<String> removeColumnName = Sets.newHashSet();
            for(String columnName : columnNameToField.keySet()){
                if(!columnNameToField.containsKey(columnName))
                    removeColumnName.add(columnName);
            }
            for(String columnName : removeColumnName){
                columnIndexToName.remove(columnNameToIndex.remove(columnName));
            }
            return true;
        }

        private Map<Integer, String> columnIndexToName;
        private Map<String, Integer> columnNameToIndex;
        private Set<Integer> requires;
        private Map<String, String> columnNameToField;
        private Map<String, Pattern> columnNameToSeparator;
        private Map<String, String> columnNameToTag;
        private Map<String, Set<String>> columnNameToType;
        private Map<String, String> columnNameToDefaultValue;

        private Record readRow( Row row, Record template) throws IOException {
            //使用临时Map
            Multimap<String,Object> temp = ArrayListMultimap.create() ;
            //优先处理必填项
            for(Integer index : requires){
                String text = convertCellValueToString(row.getCell(index));
                //必填项不存在返回null
                if(text == null) return null;
                addValue(temp, index, text,  template);
            }
            for(String columnName : columnNameToField.keySet()){
                Integer index = columnNameToIndex.get(columnName);
                if(requires.contains(index)) continue;
                String text = convertCellValueToString(row.getCell(index));
                addValue(temp, index, text, template);
            }
            Record outputRecord = template.copy();
            for(String field : temp.keySet()){
                for(Object value : temp.get(field)){
                    outputRecord.put(field,value);
                }
            }
            return outputRecord;
        }

        private void addValue(final Multimap<String, Object> temp, Integer index, String text,Record template) {
            String columnName = columnIndexToName.get(index);
            String field = columnNameToField.get(columnName);
            String rawValue = Strings.isNullOrEmpty(text) && columnNameToDefaultValue.containsKey(columnName)?
                    columnNameToDefaultValue.get(columnName) : text;
            if(Strings.isNullOrEmpty(rawValue)) return;
            if(trim) rawValue = rawValue.trim();
            if(Strings.isNullOrEmpty(rawValue)) return;
            String[] rawValues = columnNameToSeparator.containsKey(columnName)?
                    columnNameToSeparator.get(columnName).split(rawValue) : new String[]{rawValue};
            String tag = columnNameToTag.containsKey(columnName)?columnNameToTag.get(columnName):null;
            Set<String> types = columnNameToType.containsKey(columnName)?columnNameToType.get(columnName):Sets.newHashSet("文本");
            for(String childValue : rawValues){
                if(trim) childValue = childValue.trim();
                if(Strings.isNullOrEmpty(childValue)) continue;
                //解析
                Object value = null;
                if(types.contains("文件") || types.contains("链接")  || types.contains("文件链接") || types.contains("网站链接")){
                    //补完相对路径
                    if(!childValue.contains("://") && !childValue.startsWith("/")&& template.getFields().containsKey(ExtFields.SOURCE_FILE_LOCATION)){
                        String location = (String) template.getFirstValue(ExtFields.SOURCE_FILE_LOCATION);
                        value = location.endsWith("/") ? location+childValue:location+"/"+childValue;
                    }else
                        value = childValue;
                }
                if(value == null && (types.contains("时间") || types.contains("日期"))){
                    String yearStr = "";
                    boolean yearEnd = false;
                    String monthStr = "";
                    boolean monthEnd = false;
                    String dayStr = "";
                    boolean dayEnd = false;
                    for(int i=0; i<childValue.length();i++){
                        if(Character.isDigit(childValue.charAt(i))){
                            if(!yearEnd && yearStr.length()<4){
                                yearStr = yearStr + childValue.charAt(i);
                                if(yearStr.length() == 4)
                                    yearEnd = true;
                            }else if(!monthEnd && monthStr.length()<2){
                                monthStr = monthStr + childValue.charAt(i);
                                if(monthStr.length() == 2)
                                    monthEnd = true;
                            }else if(!dayEnd && dayStr.length()<2){
                                dayStr = dayStr + childValue.charAt(i);
                                if(dayStr.length() == 2)
                                    dayEnd = true;
                            }
                        }else{
                            if(!yearEnd && yearStr.length()>1)
                                yearEnd = true;
                            else if(!monthEnd && monthStr.length()>0)
                                monthEnd = true;
                            else if(!dayEnd && dayStr.length()>0)
                                dayEnd = true;
                        }
                    }
                    if(yearStr.length()>1){
                        value = yearStr + (monthStr.length()>0?"-"+monthStr:"") + (dayStr.length()>0?"-"+dayStr:"") ;
                    }
                }
                if(value == null && (types.contains("数字") || types.contains("数值"))){
                    if(StringUtils.isNumeric(childValue))
                        value = NumberUtils.createNumber(childValue);
                }
                if(value == null && types.contains("文本")){
                    value=childValue;
                }
                if(value!=null)
                    temp.put(field,value);
            }
        }

        private String convertCellValueToString(Cell cell){
            if(cell == null) return null;
            switch (cell.getCellType()) {
                case Cell.CELL_TYPE_BLANK:
                    return null;
                case Cell.CELL_TYPE_BOOLEAN:
                    return cell.getBooleanCellValue()?"true":"false";
                case Cell.CELL_TYPE_ERROR:
                    //text = ErrorEval.getText(cell.getErrorCellValue());
                    return null;
                case Cell.CELL_TYPE_FORMULA:
                    break;
                case Cell.CELL_TYPE_NUMERIC:
                    if (DateUtil.isCellDateFormatted(cell)) {
                        return dateFormat.format(cell.getDateCellValue());
                    }
                    return String.valueOf(cell.getNumericCellValue());
                case Cell.CELL_TYPE_STRING:
                    return cell.getStringCellValue();
                default:
                    return null;
            }
            switch(cell.getCachedFormulaResultType()) {
                case Cell.CELL_TYPE_NUMERIC:
                    return String.valueOf(cell.getNumericCellValue());
                case Cell.CELL_TYPE_STRING:
                    return cell.getRichStringCellValue().getString();
            }
            return null;
        }
    }

    private static enum OnMaxCharactersPerRecord {
        ignoreRecord,
        throwException,
    }
}
