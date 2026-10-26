import socket
import ssl

proxy_host = '127.0.0.1'
proxy_port = 8080
target_host = 'example.com'
target_port = 443

print(f"[*] 正在通过代理 {proxy_host}:{proxy_port} 连接 {target_host}...")

# 1. 建立与 mitmproxy 的 TCP 连接
sock = socket.create_connection((proxy_host, proxy_port))

# 2. 发送 HTTP CONNECT 请求，要求 mitmproxy 建立通往目标的隧道
connect_req = f"CONNECT {target_host}:{target_port} HTTP/1.1\r\nHost: {target_host}\r\n\r\n"
sock.sendall(connect_req.encode())
proxy_resp = sock.recv(4096)

if b"200 Connection established" in proxy_resp or b"200 OK" in proxy_resp:
    print("[*] 代理隧道建立成功，准备进行 TLS 握手...")
    
    # 3. 创建 SSL 上下文（重点：暂时关闭校验，否则 Python 会直接报错不给我们证书）
    ctx = ssl.create_default_context()
    ctx.check_hostname = False
    ctx.verify_mode = ssl.CERT_NONE  # 忽略错误，强行获取证书

    # 将普通的 socket 包装为加密 socket
    secure_sock = ctx.wrap_socket(sock, server_hostname=target_host)
    
    # 4. 获取服务器返回的证书（二进制 DER 格式）
    der_cert = secure_sock.getpeercert(binary_form=True)
    
    if der_cert:
        print("\n--- 🕵️ 截获到服务器证书 ---")
        print(f"证书大小: {len(der_cert)} bytes")
        
        # 5. X.509 证书的文本信息是以明文（ASCII/UTF-8）保存在二进制数据中的
        # 我们直接对二进制数据进行关键字匹配搜索
        if b"eMudhra" in der_cert or b"emSign" in der_cert:
            print("⚠️ 确凿证据：在证书底层字节中发现了 eMudhra / emSign 签发痕迹！")
            print("说明流量正在被 mitmproxy --emudhra 伪造拦截。")
        else:
            print("✅ 未发现 eMudhra 痕迹。")
        print("----------------------------\n")
else:
    print("代理连接失败，请确认 mitmproxy 是否已在 8080 端口启动。")

sock.close()
