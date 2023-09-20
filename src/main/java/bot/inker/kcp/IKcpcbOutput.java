package bot.inker.kcp;

import java.nio.ByteBuffer;

@FunctionalInterface
public interface IKcpcbOutput {
  int output(ByteBuffer buf, int len);
}
