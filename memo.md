- https://packages.jetbrains.team/maven/p/ij/intellij-dependencies/org/jetbrains/jediterm/
- https://packages.jetbrains.team/maven/p/ij/intellij-dependencies/org/jetbrains/jediterm/jediterm-core/
- https://packages.jetbrains.team/maven/p/ij/intellij-dependencies/org/jetbrains/jediterm/jediterm-ui/

```xml
<repositories>
    <repository>
        <id>jetbrains-dependencies</id>
        <url>https://packages.jetbrains.team/maven/p/ij/intellij-dependencies</url>
    </repository>
</repositories>

<dependencies>
    <dependency>
        <groupId>org.jetbrains.jediterm</groupId>
        <artifactId>jediterm-core</artifactId>
        <version>3.64</version>
    </dependency>

    <dependency>
        <groupId>org.jetbrains.jediterm</groupId>
        <artifactId>jediterm-ui</artifactId>
        <version>3.64</version>
    </dependency>

    <dependency>
        <groupId>org.apache.sshd</groupId>
        <artifactId>sshd-core</artifactId>
        <version>2.14.0</version>
    </dependency>
</dependencies>
```

```java
import com.jediterm.terminal.Questioner;
import com.jediterm.terminal.TtyConnector;

import java.io.*;

public class DummyTtyConnector implements TtyConnector {
    private InputStream in;

    public DummyTtyConnector() {
        // デモ用：ターミナル起動時に表示させる文字列
        // 実際のエスケープシーケンス（\u001B[31m など）を含めれば色が付きます
        String welcomeMessage = "Welcome to JediTerm!\r\n$ ";
        this.in = new ByteArrayInputStream(welcomeMessage.getBytes());
    }

    @Override
    public boolean init(Questioner q) {
        return true; // 初期化成功
    }

    @Override
    public int read(char[] buf, int offset, int length) throws IOException {
        // JediTermの裏側のスレッドが、画面に表示する文字を求めてこのメソッドを呼び続けます。
        // ※本来のSSHクライアントなら、ここでSSHのInputStreamからデータを読み込みます。
        InputStreamReader reader = new InputStreamReader(in);
        int readBytes = reader.read(buf, offset, length);
        
        // 読み込むデータが無くなったら、少し待機してCPU負荷を下げる
        if (readBytes == -1) {
            try {
                Thread.sleep(50);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
            return 0;
        }
        return readBytes;
    }

    @Override
    public void write(byte[] bytes) throws IOException {
        // ユーザーがターミナル上でキーボードを叩くと、このメソッドが呼ばれます。
        // ※本来のSSHクライアントなら、ここでSSHのOutputStreamに打鍵データを送信します。
        System.out.println("User typed: " + new String(bytes));
    }

    @Override
    public void write(String string) throws IOException {
        write(string.getBytes());
    }

    @Override
    public void close() {
        // 接続終了時の処理
    }

    @Override
    public String getName() {
        return "Dummy Terminal";
    }

    @Override
    public boolean isConnected() {
        return true;
    }

    @Override
    public void waitFor() throws InterruptedException {
    }
}
```

```java
import com.jediterm.terminal.ui.JediTermWidget;
import com.jediterm.terminal.ui.settings.DefaultSettingsProvider;

import javax.swing.*;
import java.awt.*;

public class TerminalApp {
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            JFrame frame = new JFrame("JediTerm SSH Client Demo");
            frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
            frame.setSize(800, 600);

            // 1. ターミナルウィジェットの作成（フォントや色などの設定プロバイダを渡す）
            JediTermWidget terminal = new JediTermWidget(new DefaultSettingsProvider());

            // 2. 自作のTtyConnectorをターミナルにセット
            terminal.setTtyConnector(new DummyTtyConnector());

            // 3. ターミナルのIO（読み込み）スレッドを開始
            terminal.start();

            // 4. 通常のSwingコンポーネントと同じようにJFrameに追加
            frame.add(terminal, BorderLayout.CENTER);
            frame.setVisible(true);
        });
    }
}
```
