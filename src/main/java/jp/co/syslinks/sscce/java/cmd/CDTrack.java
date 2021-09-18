package jp.co.syslinks.sscce.java.cmd;

import java.math.BigDecimal;
import java.util.List;

public class CDTrack {

    public List<CDTrack_chapters> chapters;

    public static class CDTrack_chapters {
        public int id;
        public BigDecimal start_time;
        public BigDecimal end_time;
    }

}
