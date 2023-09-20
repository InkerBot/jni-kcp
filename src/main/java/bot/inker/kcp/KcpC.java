package bot.inker.kcp;

import java.io.*;
import java.net.URL;
import java.nio.Buffer;

final class KcpC {
  static {
    try {
      File file = File.createTempFile("libnative-", ".dylib");
      file.deleteOnExit();
      try (OutputStream out = new FileOutputStream(file)) {
        URL url = KcpC.class.getClassLoader().getResource("libnative.dylib");
        if (url == null) {
          throw new FileNotFoundException("resource: libnative.dylib");
        }
        try (InputStream in = url.openStream()) {
          in.transferTo(out);
        }
      }
      System.load(file.getAbsolutePath());
    }catch (IOException e) {
      throw new UncheckedIOException(e);
    }
  }

  private KcpC() {
    throw new UnsupportedOperationException();
  }

  static native @NativeType("int (*)(const char *, int, ikcpcb *, void *)") long get_jni_output_method();

  static native @NativeType("void (*)(const char *, struct IKCPCB *, void *)") long get_jni_log_method();

  static native @NativeType("jobject") long new_global_reference(@NativeType("jobject") Object obj);

  static native void delete_global_reference(@NativeType("jobject") long obj);

  static native @NativeType("void*") long get_buffer_address(@NativeType("jobject") Buffer buffer);

  // create a new kcp control object, 'conv' must equal in two endpoint
  // from the same connection. 'user' will be passed to the output callback
  // output callback can be setup like this: 'kcp->output = my_udp_output'
  static native @NativeType("ikcpcb *") long ikcp_create(@NativeType("IUINT32") int conv, @NativeType("void *") long user);

  // release kcp control object
  static native void ikcp_release(@NativeType("ikcpcb *") long kcp);

  // set output callback, which will be invoked by kcp
  static native void ikcp_setoutput(@NativeType("ikcpcb *") long kcp, @NativeType("int (*)(const char *, int, ikcpcb *, void *)") long output);

  // set log callback, which will be invoked by kcp
  static native void ikcp_setlog(@NativeType("ikcpcb *") long kcp, @NativeType("void (*)(const char *, struct IKCPCB *, void *)") long log);

  // user/upper level recv: returns size, returns below zero for EAGAIN
  static native int ikcp_recv(@NativeType("ikcpcb *") long kcp, @NativeType("char *") long buffer, int len);

  // user/upper level send, returns below zero for error
  static native int ikcp_send(@NativeType("ikcpcb *") long kcp, @NativeType("const char *") long buffer, int len);

  // update state (call it repeatedly, every 10ms-100ms), or you can ask
  // ikcp_check when to call it again (without ikcp_input/_send calling).
  // 'current' - current timestamp in millisec.
  static native void ikcp_update(@NativeType("ikcpcb *") long kcp, @NativeType("IUINT32") int current);

  // Determine when should you invoke ikcp_update:
  // returns when you should invoke ikcp_update in millisec, if there
  // is no ikcp_input/_send calling. you can call ikcp_update in that
  // time, instead of call update repeatly.
  // Important to reduce unnacessary ikcp_update invoking. use it to
  // schedule ikcp_update (eg. implementing an epoll-like mechanism,
  // or optimize ikcp_update when handling massive kcp connections)
  static native @NativeType("IUINT32") int ikcp_check(@NativeType("const ikcpcb *") long kcp, @NativeType("IUINT32") int current);

  // when you received a low level packet (eg. UDP packet), call it
  static native int ikcp_input(@NativeType("ikcpcb *") long kcp, @NativeType("const char *") long data, long size);

  // flush pending data
  static native void ikcp_flush(@NativeType("ikcpcb *") long kcp);

  // check the size of next message in the recv queue
  static native int ikcp_peeksize(@NativeType("const ikcpcb *") long kcp);

  // change MTU size, default is 1400
  static native int ikcp_setmtu(@NativeType("ikcpcb *") long kcp, int mtu);

  // set maximum window size: sndwnd=32, rcvwnd=32 by default
  static native int ikcp_wndsize(@NativeType("ikcpcb *") long kcp, int sndwnd, int rcvwnd);

  // get how many packet is waiting to be sent
  static native int ikcp_waitsnd(@NativeType("const ikcpcb *") long kcp);

  // fastest: ikcp_nodelay(kcp, 1, 20, 2, 1)
  // nodelay: 0:disable(default), 1:enable
  // interval: internal update timer interval in millisec, default is 100ms
  // resend: 0:disable fast resend(default), 1:enable fast resend
  // nc: 0:normal congestion control(default), 1:disable congestion control
  static native int ikcp_nodelay(@NativeType("ikcpcb *") long kcp, int nodelay, int interval, int resend, int nc);

  // static native void ikcp_log(@NativeType("ikcpcb *") long kcp, int mask, const char *fmt, ...);

  // setup allocator
  static native void ikcp_allocator(@NativeType("void* (*)(size_t)") long new_malloc, @NativeType("void (*)(void*)") long new_free);

  // read conv
  static native @NativeType("IUINT32") int ikcp_getconv(@NativeType("const void *") long ptr);
}
