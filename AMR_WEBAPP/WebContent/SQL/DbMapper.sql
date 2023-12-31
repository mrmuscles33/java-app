WITH COLS AS (
    SELECT TABLE_NAME, COLUMN_NAME, DATA_TYPE,
        LOWER(SUBSTR(REPLACE(INITCAP(COLUMN_NAME), '_', ''), 1, 1))||SUBSTR(REPLACE(INITCAP(COLUMN_NAME), '_', ''), 2) AS CAMEL_CASE,
        REPLACE(INITCAP(COLUMN_NAME), '_', '') AS METHOD_NAME,
        CASE WHEN DATA_TYPE = 'NUMBER' AND DATA_PRECISION IS NULL THEN 'int'
             WHEN DATA_TYPE = 'NUMBER' AND DATA_PRECISION IS NOT NULL THEN 'double'
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
) SELECT REPLACE(INITCAP(c.TABLE_NAME),'_','')||'.java' AS CLASS_NAME, 'package A_MODIFIER;'||
    'import java.util.Arrays;'||
    'import java.util.Map;'||
    'import java.util.HashMap;'||
    'import java.util.List;'||
    'import fr.amr.database.DbMapper;'||
    'import fr.amr.utils.NumberUtils;'||
    'import fr.amr.utils.StringUtils;'||
    '/** Generated on '||TO_CHAR(SYSDATE,'DD/MM/YYY HH24:MI:SS')||' **/'||
    'public class '||REPLACE(INITCAP(c.TABLE_NAME),'_','')||'SGBD extends DbMapper{'||
    REPLACE(XMLAGG(XMLELEMENT(E, ' private '||c.JAVA_TYPE||' '||c.CAMEL_CASE||';').EXTRACT('//text()') ORDER BY c.COLUMN_NAME).GETCLOBVAL(), CHR(38)||'quot;','"')||
    ' @Override public String getTable(){return  "'||c.TABLE_NAME||'";}'||
    ' @Override public List<String> getKeys(){return  Arrays.asList('||(SELECT LISTAGG('"'||c2.COLUMN_NAME||'"', ',') FROM COLS c2 WHERE c2.TABLE_NAME = c.TABLE_NAME AND c2.KEY = 1)||');}'||
    ' @Override protected List<Object> getKeysValues(){return  Arrays.asList('||(SELECT LISTAGG('this.'||c2.CAMEL_CASE, ',') FROM COLS c2 WHERE c2.TABLE_NAME = c.TABLE_NAME AND c2.KEY = 1)||');}'||
    ' @Override protected List<String> getColumns(){return  Arrays.asList('||REPLACE(RTRIM(XMLAGG(XMLELEMENT(E, '"'||c.COLUMN_NAME||'",').EXTRACT('//text()') ORDER BY c.COLUMN_NAME).GETCLOBVAL(),','), CHR(38)||'quot;','"')||');}'||
    ' @Override protected List<Object> getColumnsValues(){return  Arrays.asList('||REPLACE(RTRIM(XMLAGG(XMLELEMENT(E, 'this.'||c.CAMEL_CASE||',').EXTRACT('//text()') ORDER BY c.COLUMN_NAME).GETCLOBVAL(),','), CHR(38)||'quot;','"')||');}'||
    ' @Override protected List<String> getDateColumns(){return  Arrays.asList('||(SELECT LISTAGG('"'||c2.COLUMN_NAME||'"', ',') FROM COLS c2 WHERE c2.TABLE_NAME = c.TABLE_NAME AND c2.DATA_TYPE = 'DATE')||');}'||
    RTRIM(XMLAGG(XMLELEMENT(E, ' public '||c.JAVA_TYPE||' get'||c.METHOD_NAME||'(){return this.'||c.CAMEL_CASE||';}').EXTRACT('//text()') ORDER BY c.COLUMN_NAME).GETCLOBVAL(),',')||
    RTRIM(XMLAGG(XMLELEMENT(E, ' public void set'||c.METHOD_NAME||'('||c.JAVA_TYPE||' p'||c.METHOD_NAME||'){this.'||c.CAMEL_CASE||' = p'||c.METHOD_NAME||';}').EXTRACT('//text()') ORDER BY c.COLUMN_NAME).GETCLOBVAL(),',')||
    ' @Override public void fromMap(Map<String, ?> map) {if(map==null) return;'||REPLACE(XMLAGG(XMLELEMENT(E, ' this.set'||c.METHOD_NAME||'('||DECODE(c.JAVA_TYPE,'int','NumberUtils.toInt','double','NumberUtils.toDouble','StringUtils.toString')||'(map.get("'||c.COLUMN_NAME||'")))'||';').EXTRACT('//text()') ORDER BY c.COLUMN_NAME).GETCLOBVAL(), CHR(38)||'quot;','"')||'}'||
    ' @Override public Map<String, Object> toMap() {Map<String, Object> map = new HashMap<>();'||REPLACE(XMLAGG(XMLELEMENT(E, ' map.put("'||c.COLUMN_NAME||'", this.get'||c.METHOD_NAME||'());').EXTRACT('//text()') ORDER BY c.COLUMN_NAME).GETCLOBVAL(), CHR(38)||'quot;','"')||'return map;}'||
    '}' AS CODE
FROM COLS c
GROUP BY c.TABLE_NAME;