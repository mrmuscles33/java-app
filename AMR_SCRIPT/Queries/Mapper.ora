WITH COLS AS (
    SELECT TABLE_NAME, COLUMN_NAME, DATA_TYPE,
        LOWER(SUBSTR(REPLACE(INITCAP(COLUMN_NAME), '_', ''), 1, 1))||SUBSTR(REPLACE(INITCAP(COLUMN_NAME), '_', ''), 2) AS CAMEL_CASE,
        REPLACE(INITCAP(COLUMN_NAME), '_', '') AS METHOD_NAME,
        CASE WHEN DATA_TYPE = 'NUMBER' AND DATA_PRECISION IS NULL THEN 'int'
             WHEN DATA_TYPE = 'NUMBER' AND DATA_PRECISION IS NOT NULL THEN 'double'
             WHEN DATA_TYPE = 'DATE' THEN 'LocalDateTime'
             ELSE 'String' END AS JAVA_TYPE,
        CASE WHEN EXISTS (
            SELECT 1
            FROM USER_CONSTRAINTS cons
            INNER JOIN USER_CONS_COLUMNS col ON col.CONSTRAINT_NAME = cons.CONSTRAINT_NAME
            WHERE cons.TABLE_NAME = utc.TABLE_NAME
            AND cons.CONSTRAINT_TYPE = 'P' AND col.COLUMN_NAME = utc.COLUMN_NAME
        ) THEN 1 ELSE 0 END AS KEY
    FROM USER_TAB_COLUMNS utc
    WHERE utc.TABLE_NAME LIKE '%%'
    ORDER BY COLUMN_ID
) SELECT REPLACE(INITCAP(c.TABLE_NAME),'_','')||'.java' AS CLASS_NAME,
    -- package
    'package A_MODIFIER;'||

    -- imports
    'import java.time.LocalDateTime;'||
    'import java.util.Arrays;'||
    'import java.util.Map;'||
    'import java.util.HashMap;'||
    'import java.util.List;'||

    'import fr.amr.database.DbMapper;'||
    'import fr.amr.utils.NumberUtils;'||
    'import fr.amr.utils.StringUtils;'||
    'import fr.amr.utils.DateUtils;'||

    -- date
    '/** Generated on '||TO_CHAR(SYSDATE,'DD/MM/YYY HH24:MI:SS')||' **/'||

    -- class
    'public class '||REPLACE(INITCAP(c.TABLE_NAME),'_','')||'Mapper extends DbMapper{'||

    -- privates attributes
    REPLACE(XMLAGG(XMLELEMENT(E,
        ' private '||c.JAVA_TYPE||' '||c.CAMEL_CASE||';'
    ).EXTRACT('//text()') ORDER BY c.COLUMN_NAME).GETCLOBVAL(), CHR(38)||'quot;','"')||

    -- getTable()
    ' @Override public String getTable(){'||
        'return  "'||c.TABLE_NAME||'";'||
    '}'||

    -- getKeys()
    ' @Override public List<String> getKeys(){'||
        'return  Arrays.asList('||(
            SELECT LISTAGG('"'||c2.COLUMN_NAME||'"', ',')
            FROM COLS c2
            WHERE c2.TABLE_NAME = c.TABLE_NAME
            AND c2.KEY = 1
        )||');'||
    '}'||

    -- getKeysValues()
    ' @Override protected List<Object> getKeysValues(){'||
        'return  Arrays.asList('||(
            SELECT LISTAGG('this.'||c2.CAMEL_CASE, ',')
            FROM COLS c2
            WHERE c2.TABLE_NAME = c.TABLE_NAME
            AND c2.KEY = 1
        )||');'||
    '}'||

    -- getColumns()
    ' @Override '||
    'protected List<String> getColumns(){'||
        'return  Arrays.asList('||
            REPLACE(RTRIM(XMLAGG(XMLELEMENT(E,
                '"'||c.COLUMN_NAME||'",'
            ).EXTRACT('//text()') ORDER BY c.COLUMN_NAME).GETCLOBVAL(),','), CHR(38)||'quot;','"')||');'||
    '}'||

    -- getColumnsValues()
    ' @Override '||
    'protected List<Object> getColumnsValues(){'||
        'return  Arrays.asList('||
            REPLACE(RTRIM(XMLAGG(XMLELEMENT(E,
                DECODE(c.JAVA_TYPE,
                'LocalDateTime', 'DateUtils.toString(this.'||c.CAMEL_CASE||', this.getDateFormat("'||c.COLUMN_NAME||'")),',
                                 'this.'||c.CAMEL_CASE||',')
            ).EXTRACT('//text()') ORDER BY c.COLUMN_NAME).GETCLOBVAL(),','), CHR(38)||'quot;','"')||');'||
    '}'||

    -- getDateColumns()
    ' @Override '||
    'protected List<String> getDateColumns(){'||
        'return  Arrays.asList('||(
            SELECT LISTAGG('"'||c2.COLUMN_NAME||'"', ',')
            FROM COLS c2
            WHERE c2.TABLE_NAME = c.TABLE_NAME
            AND c2.DATA_TYPE = 'DATE'
        )||');'||
    '}'||

    -- getters
    RTRIM(XMLAGG(XMLELEMENT(E,
        ' public '||c.JAVA_TYPE||' get'||c.METHOD_NAME||'(){'||
            'return this.'||c.CAMEL_CASE||';'||
        '}'
    ).EXTRACT('//text()') ORDER BY c.COLUMN_NAME).GETCLOBVAL(),',')||

    -- setters
    RTRIM(XMLAGG(XMLELEMENT(E,
        ' public void set'||c.METHOD_NAME||'('||c.JAVA_TYPE||' p'||c.METHOD_NAME||'){'||
            'this.'||c.CAMEL_CASE||' = p'||c.METHOD_NAME||';'||
        '}'
    ).EXTRACT('//text()') ORDER BY c.COLUMN_NAME).GETCLOBVAL(),',')||

    -- fromMap
    ' @Override '||
    'public void fromMap(Map<String, ?> map) {'||
        'if(map==null) return;'||
        REPLACE(XMLAGG(XMLELEMENT(E,
        'this.set'||c.METHOD_NAME||'('||
            DECODE(c.JAVA_TYPE,
            'int'           , '(NumberUtils.toInt(map.get("'||c.COLUMN_NAME||'")));',
            'double'        , 'NumberUtils.toDouble(map.get("'||c.COLUMN_NAME||'")));',
            'LocalDateTime' , 'DateUtils.toDateTime(StringUtils.toString(map.get("'||c.COLUMN_NAME||'")), this.getDateFormat("'||c.COLUMN_NAME||'")));',
                              'StringUtils.toString(map.get("'||c.COLUMN_NAME||'")));')
        ).EXTRACT('//text()') ORDER BY c.COLUMN_NAME).GETCLOBVAL(), CHR(38)||'quot;','"')||
    '}'||

    -- toMap()
    ' @Override '||
    'public Map<String, Object> toMap() {'||
        'Map<String, Object> map = new HashMap<>();'||
        REPLACE(XMLAGG(XMLELEMENT(E,
            DECODE(c.JAVA_TYPE,
            'LocalDateTime', 'map.put("'||c.COLUMN_NAME||'", DateUtils.toString(this.get'||c.METHOD_NAME||'(), this.getDateFormat("'||c.COLUMN_NAME||'")));',
                             'map.put("'||c.COLUMN_NAME||'", this.get'||c.METHOD_NAME||'());')
        ).EXTRACT('//text()') ORDER BY c.COLUMN_NAME).GETCLOBVAL(), CHR(38)||'quot;','"')||
        'return map;'||
    '}'||
'}' AS CODE
FROM COLS c
GROUP BY c.TABLE_NAME;