package bot.inker.kcp;

import java.io.Closeable;
import java.nio.ByteBuffer;

/**
 * IKCPCB
 */
@SuppressWarnings({"unused", "UnusedReturnValue"})
public final class IKcpcb implements Closeable, AutoCloseable {
  private boolean closed = false;
  private final long thisReference;
  private final long ptr;
  private IKcpcbOutput output;
  private IKcpcbLog log;

  /**
   * create a new kcp control object
   *
   * @param conv must equal in two endpoint from the same connection
   */
  public IKcpcb(int conv) {
    this.thisReference = KcpC.new_global_reference(this);
    this.ptr = KcpC.ikcp_create(conv, thisReference);
    KcpC.ikcp_setoutput(ptr, KcpC.get_jni_output_method());
  }

  @SuppressWarnings("unused") // invoke by native
  private int output(ByteBuffer buf, int len) {
    if (this.output != null) {
      return this.output.output(buf, len);
    }
    return 0;
  }

  @SuppressWarnings("unused") // invoke by native
  private void writelog(ByteBuffer log) {
    if (this.log != null) {
      byte[] messageBytes = new byte[log.remaining()];
      log.get(messageBytes);
      this.log.writelog(new String(messageBytes));
    }
  }

  @SuppressWarnings("unused") // invoke by native
  private void writelog(String log) {
    if (this.log != null) {
      this.log.writelog(log);
    }
  }

  public void setoutput(IKcpcbOutput output) {
    this.output = output;
    KcpC.ikcp_setoutput(ptr, KcpC.get_jni_output_method());
  }

  public void setlog(IKcpcbLog log) {
    this.log = log;
    KcpC.ikcp_setlog(ptr, KcpC.get_jni_log_method());
  }

  /**
   * user/upper level recv: returns size, returns below zero for EAGAIN
   */
  public int recv(ByteBuffer buffer, int len) {
    if (buffer.isDirect()) {
      return KcpC.ikcp_recv(ptr, KcpC.get_buffer_address(buffer), len);
    } else {
      writelog("heap buffer found");
      int sourcePosition = buffer.position();
      ByteBuffer directBuffer = ByteBuffer.allocateDirect(len);
      int r = KcpC.ikcp_recv(ptr, KcpC.get_buffer_address(buffer), len);
      buffer.put(directBuffer);
      buffer.position(sourcePosition);
      return r;
    }
  }

  /**
   * user/upper level recv: returns size, returns below zero for EAGAIN
   */
  public int recv(ByteBuffer buffer) {
    if (buffer.isDirect()) {
      return KcpC.ikcp_recv(ptr, KcpC.get_buffer_address(buffer), buffer.remaining());
    } else {
      writelog("heap buffer found");
      ByteBuffer directBuffer = ByteBuffer.allocateDirect(buffer.remaining());
      int r = KcpC.ikcp_recv(ptr, KcpC.get_buffer_address(buffer), buffer.remaining());
      buffer.put(buffer.position(), directBuffer, 0, buffer.remaining());
      return r;
    }
  }

  /**
   * user/upper level send, returns below zero for error
   */
  public int send(ByteBuffer buffer, int len) {
    if (buffer.isDirect()) {
      return KcpC.ikcp_send(ptr, KcpC.get_buffer_address(buffer), len);
    }else{
      writelog("heap buffer found");
      ByteBuffer directBuffer = ByteBuffer.allocateDirect(len);
      directBuffer.put(0, buffer, buffer.position(), len);
      buffer.position(buffer.position() + len);
      return KcpC.ikcp_send(ptr, KcpC.get_buffer_address(directBuffer), len);
    }
  }

  /**
   * user/upper level send, returns below zero for error
   */
  public int send(ByteBuffer buffer) {
    if (buffer.isDirect()) {
      return KcpC.ikcp_send(ptr, KcpC.get_buffer_address(buffer), buffer.remaining());
    }else{
      writelog("heap buffer found");
      ByteBuffer directBuffer = ByteBuffer.allocateDirect(buffer.remaining());
      directBuffer.put(0, buffer, buffer.position(), buffer.remaining());
      buffer.position(buffer.position() + buffer.remaining());
      return KcpC.ikcp_send(ptr, KcpC.get_buffer_address(directBuffer), directBuffer.remaining());
    }
  }

  /**
   * update state (call it repeatedly, every 10ms-100ms), or you can ask
   * ikcp_check when to call it again (without ikcp_input/_send calling).
   * 'current' - current timestamp in millisec.
   */
  public void update(int current) {
    KcpC.ikcp_update(ptr, current);
  }

  /**
   * Determine when should you invoke ikcp_update:
   * returns when you should invoke ikcp_update in millisec, if there
   * is no ikcp_input/_send calling. you can call ikcp_update in that
   * time, instead of call update repeatly.
   * Important to reduce unnacessary ikcp_update invoking. use it to
   * schedule ikcp_update (eg. implementing an epoll-like mechanism,
   * or optimize ikcp_update when handling massive kcp connections)
   */
  public int check(int current) {
    return KcpC.ikcp_check(ptr, current);
  }

  /**
   * when you received a low level packet (eg. UDP packet), call it
   */
  public int input(ByteBuffer data, long size) {
    if (data.isDirect()) {
      return KcpC.ikcp_input(ptr, KcpC.get_buffer_address(data), size);
    }else{
      writelog("heap buffer found");
      if(size > Integer.MAX_VALUE) {
        throw new OutOfMemoryError("input size > Integer.MAX_VALUE");
      }
      int len = (int) size;
      ByteBuffer directBuffer = ByteBuffer.allocateDirect(len);
      directBuffer.put(0, data, data.position(), len);
      data.position(data.position() + len);
      return KcpC.ikcp_input(ptr, KcpC.get_buffer_address(directBuffer), len);
    }
  }

  /**
   * when you received a low level packet (eg. UDP packet), call it
   */
  public int input(ByteBuffer data) {
    if (data.isDirect()) {
      return KcpC.ikcp_input(ptr, KcpC.get_buffer_address(data), data.remaining());
    }else{
      writelog("heap buffer found");
      ByteBuffer directBuffer = ByteBuffer.allocateDirect(data.remaining());
      directBuffer.put(0, data, data.position(), data.remaining());
      data.position(data.position() + data.remaining());
      return KcpC.ikcp_input(ptr, KcpC.get_buffer_address(directBuffer), directBuffer.remaining());
    }
  }

  /**
   * flush pending data
   */
  public void flush() {
    KcpC.ikcp_flush(ptr);
  }

  /**
   * check the size of next message in the recv queue
   */
  public int peeksize() {
    return KcpC.ikcp_peeksize(ptr);
  }

  /**
   * change MTU size, default is 1400
   */
  public int setmtu(int mtu) {
    return KcpC.ikcp_setmtu(ptr, mtu);
  }

  /**
   * set maximum window size: sndwnd=32, rcvwnd=32 by default
   */
  public int wndsize(int sndwnd, int rcvwnd) {
    return KcpC.ikcp_wndsize(ptr, sndwnd, rcvwnd);
  }

  /**
   * get how many packet is waiting to be sent
  */
  public int waitsnd() {
    return KcpC.ikcp_waitsnd(ptr);
  }

  /**
   * fastest: ikcp_nodelay(kcp, 1, 20, 2, 1)
   * nodelay: 0:disable(default), 1:enable
   * interval: internal update timer interval in millisec, default is 100ms
   * resend: 0:disable fast resend(default), 1:enable fast resend
   * nc: 0:normal congestion control(default), 1:disable congestion control
   */
  public int nodelay(int nodelay, int interval, int resend, int nc) {
    return KcpC.ikcp_nodelay(ptr, nodelay, interval, resend, nc);
  }

  /**
   * read conv
   */
  public int getconv() {
    return KcpC.ikcp_getconv(ptr);
  }

  /**
   * release kcp control object
   */
  @Override
  public void close() {
    if(!closed) {
      closed = true;
      KcpC.delete_global_reference(thisReference);
      KcpC.ikcp_release(ptr);
    }
  }

  @Override
  protected void finalize() {
    close();
  }
}
