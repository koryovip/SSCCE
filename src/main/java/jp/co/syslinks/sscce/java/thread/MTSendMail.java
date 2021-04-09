package jp.co.syslinks.sscce.java.thread;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Random;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
マルチスレッドメール送信模擬コード

CREATE TABLE T_MAIL (
    ID NUMBER NOT NULL,
    STATUS CHAR(1) NOT NULL,
    SENDER CHAR(10),
    SENT_TIME TIMESTAMP,
    CONSTRAINT T_MAIL_PK1 PRIMARY KEY(ID)
)

UPDATE T_MAIL SET STATUS = '1' , SENDER = NULL, SENT_TIME = NULL;
COMMIT;
 */
public class MTSendMail {

    private static final Logger logger = LoggerFactory.getLogger(MTSendMail.class);

    static private final int MAX = 4;

    /*
    static private final ThreadLocal<Connection> connPool = new ThreadLocal<>();
    static private final ThreadLocal<PreparedStatement> ps1Pool = new ThreadLocal<>();
    static private final ThreadLocal<PreparedStatement> ps2Pool = new ThreadLocal<>();
    */

    static private final Random random = new Random();
    static {
        random.setSeed(System.currentTimeMillis());
    }

    public static void main(String[] args) throws Exception {
        CountDownLatch start = new CountDownLatch(1);
        for (int ii = 0; ii < MAX; ii++) {
            new Thread(new SendMailThread(start), "sender-" + ii).start();
        }
        start.countDown();
    }

    public static final class SendMailThread implements Runnable {
        private final CountDownLatch startSignal;

        public SendMailThread(CountDownLatch startSignal) {
            this.startSignal = startSignal;
        }

        @Override
        public void run() {
            try {
                this.startSignal.await();
                doWork();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        private void doWork() throws Exception {
            Connection conn = null;
            PreparedStatement ps1 = null;
            PreparedStatement ps2 = null;
            try {
                conn = DriverManager.getConnection("jdbc:oracle:thin:@192.168.0.150:49161:xe", "system", "oracle");
                conn.setAutoCommit(false);
                // ヒント句の/*+FIRST_ROWS_1*/が必須！
                // FOR UPDATE: 行ロック
                // SKIP LOCKED: ロックされた行を無視
                ps1 = conn.prepareStatement("SELECT /*+FIRST_ROWS_1*/ ID FROM T_MAIL WHERE STATUS = ? ORDER BY ID ASC FOR UPDATE SKIP LOCKED");
                ps1.setMaxRows(1); // ★ ps.setMaxRows(1); も必須！
                ps1.setFetchSize(1);
                ps1.setString(1, "1"); // status = '1'
                Long id = null;
                while ((id = findId(ps1)) != null) {
                    {
                        // 模擬メール送信処理
                        TimeUnit.MILLISECONDS.sleep(100 + random.nextInt(100));
                    }
                    if (ps2 == null) {
                        ps2 = conn.prepareStatement("UPDATE T_MAIL SET SENDER = ? , STATUS = ?, SENT_TIME = SYSTIMESTAMP WHERE ID = ?");
                    }
                    ps2.clearParameters();
                    final String name = Thread.currentThread().getName();
                    ps2.setString(1, name);
                    ps2.setString(2, "2"); // status = '2'
                    ps2.setLong(3, id);
                    int updateCount = ps2.executeUpdate();
                    logger.debug("{}", String.format("%10s : % 3d : %d", name, id, updateCount));
                    conn.commit(); // 行単位コミット
                }
            } catch (Exception e) {
                e.printStackTrace();
                conn.rollback();
            } finally {
                this.close(ps2, ps1, conn);
            }
        }

        private Long findId(PreparedStatement ps1) throws SQLException {
            try (ResultSet rs = ps1.executeQuery()) {
                if (rs.next()) {
                    return rs.getLong("ID");
                }
                return null;
            }
        }

        private void close(PreparedStatement ps2, PreparedStatement ps1, Connection conn) throws SQLException {
            if (ps2 != null) {
                ps2.close();
            }
            if (ps1 != null) {
                ps1.close();
            }
            if (conn != null) {
                conn.close();
            }
        }
    }

    //    private static final Connection getConn() throws SQLException {
    //        Connection conn = connPool.get();
    //        if (conn == null) {
    //            conn = DriverManager.getConnection("jdbc:oracle:thin:@192.168.0.150:49161:xe", "system", "oracle");
    //            conn.setAutoCommit(false);
    //            connPool.set(conn);
    //            System.out.println(conn);
    //        }
    //        return conn;
    //    }
    //
    //    private static final PreparedStatement getPS1(Connection conn) throws SQLException {
    //        PreparedStatement ps = ps1Pool.get();
    //        if (ps == null) {
    //            ps = conn.prepareStatement("SELECT /*+FIRST_ROWS_1*/ ID FROM T_MAIL WHERE STATUS = ? ORDER BY ID DESC FOR UPDATE SKIP LOCKED");
    //            ps1Pool.set(ps);
    //            System.out.println("ps1" + ps);
    //        }
    //        return ps;
    //    }
    //
    //    private static final PreparedStatement getPS2(Connection conn) throws SQLException {
    //        PreparedStatement ps = ps2Pool.get();
    //        if (ps == null) {
    //            ps = conn.prepareStatement("UPDATE T_MAIL SET SENDER = ? , STATUS = ?, SENT_TIME = SYSTIMESTAMP WHERE ID = ?");
    //            ps2Pool.set(ps);
    //            System.out.println("ps2" + ps);
    //        }
    //        return ps;
    //    }
}
