package fr.amr.database;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Map;
import java.util.HashMap;
import java.util.List;

import fr.amr.database.DbMapper;
import fr.amr.utils.NumberUtils;
import fr.amr.utils.StringUtils;
import fr.amr.utils.DateUtils;

/**
 * Generated on 31/12/023 18:03:07
 **/
public class TestMapper extends DbMapper {
    private LocalDateTime dt;
    private double id;
    private String label;

    @Override
    public String getTable() {
        return "TEST";
    }

    @Override
    public List<String> getKeys() {
        return Arrays.asList("ID");
    }

    @Override
    protected List<Object> getKeysValues() {
        return Arrays.asList(this.id);
    }

    @Override
    protected List<String> getColumns() {
        return Arrays.asList("DT", "ID", "LABEL");
    }

    @Override
    protected List<Object> getColumnsValues() {
        return Arrays.asList(DateUtils.toString(this.dt, DateUtils.formatJava(this.getDateFormat("DT"))), this.id, this.label);
    }

    @Override
    protected List<String> getDateColumns() {
        return Arrays.asList("DT");
    }

    public LocalDateTime getDt() {
        return this.dt;
    }

    public double getId() {
        return this.id;
    }

    public String getLabel() {
        return this.label;
    }

    public void setDt(LocalDateTime pDt) {
        this.dt = pDt;
    }

    public void setId(double pId) {
        this.id = pId;
    }

    public void setLabel(String pLabel) {
        this.label = pLabel;
    }

    @Override
    public void fromMap(Map<String, ?> map) {
        if (map == null) return;
        this.setDt(DateUtils.toDateTime(StringUtils.toString(map.get("DT")), DateUtils.formatJava(this.getDateFormat("DT"))));
        this.setId(NumberUtils.toDouble(map.get("ID")));
        this.setLabel(StringUtils.toString(map.get("LABEL")));
    }

    @Override
    public Map<String, Object> toMap() {
        Map<String, Object> map = new HashMap<>();
        map.put("DT", DateUtils.toString(this.getDt(), DateUtils.formatJava(this.getDateFormat("DT"))));
        map.put("ID", this.getId());
        map.put("LABEL", this.getLabel());
        return map;
    }
}