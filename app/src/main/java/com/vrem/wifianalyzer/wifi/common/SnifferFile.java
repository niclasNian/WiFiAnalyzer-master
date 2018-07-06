package com.vrem.wifianalyzer.wifi.common;

public class SnifferFile {
    public Integer rowID;
    public String devID;
    public String file;
    public String essid;

    public SnifferFile(int rowID, String devID, String file, String essid) {
        this.rowID = rowID;
        this.devID = devID;
        this.file = file;
        this.essid = essid;
    }

    public SnifferFile() {
    }

    @Override
    public boolean equals(Object o) {
        if(this == o) //先检查是否其自反性，后比较other是否为空。这样效率高
            return true;
        if(o == null)
            return false;
        if( !(o instanceof SnifferFile))
            return false;

        final SnifferFile v = (SnifferFile) o;

        return rowID.equals(v.rowID);
    }

    @Override
    public int hashCode() {
        return rowID.hashCode();
    }
}
