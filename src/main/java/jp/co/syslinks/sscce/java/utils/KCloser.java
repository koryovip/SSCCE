package jp.co.syslinks.sscce.java;

import java.sql.SQLException;

public class KCloser {

    public static void close(java.sql.PreparedStatement ps) throws SQLException {
        if (ps != null) {
            ps.close();
        }
    }

    public static void close$(java.sql.PreparedStatement ps) {
        if (ps != null) {
            try {
                ps.close();
            } catch (SQLException ignore) {
            }
        }
    }

    public static void close(java.sql.Connection conn) throws SQLException {
        if (conn != null) {
            conn.close();
        }
    }

    public static void close$(java.sql.Connection conn) {
        if (conn != null) {
            try {
                conn.close();
            } catch (SQLException ignore) {
            }
        }
    }

    public static void close(java.sql.ResultSet rs) throws SQLException {
        if (rs != null) {
            rs.close();
        }
    }

    public static void close$(java.sql.ResultSet rs) {
        if (rs != null) {
            try {
                rs.close();
            } catch (SQLException ignore) {
            }
        }
    }
}
