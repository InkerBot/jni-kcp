package bot.inker.kcp;

import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;

public class IKcpcbTest {
  @Test
  public void a() throws IOException {
    int recvLength;
    IKcpcb server = new IKcpcb(123);
    IKcpcb client = new IKcpcb(123);

    client.setlog(System.out::println);
    server.setlog(System.out::println);
    server.setoutput(client::input);
    client.setoutput(server::input);

    client.send(ByteBuffer.wrap("hello\n".getBytes(StandardCharsets.UTF_8)));

    for (int i = 0; i < 100; i++) {
      server.update(10 * i);
      client.update(10 * i);

      ByteBuffer buf = ByteBuffer.allocateDirect(4096);
      if ((recvLength = server.recv(buf)) > 0) {
        byte[] bytes = new byte[recvLength];
        buf.get(bytes);
        System.out.write(bytes);
      }
    }

    server.close();
    client.close();
  }
}
