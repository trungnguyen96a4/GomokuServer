import model.Request;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class ClientMain {

//    public static void main(String[] args) {
//        new Thread(() -> {
//            try {
//                SocketChannel socketChannel = SocketChannel.open(new InetSocketAddress("localhost", 6677));
//                socketChannel.configureBlocking(true);
//                Thread.sleep(3000);
//                String[] message = {"nguyen", "huu", Request.LOGOUT};
//                for(String item : message) {
//                    ByteBuffer buffer = ByteBuffer.wrap(item.getBytes());
//                    socketChannel.write(buffer);
//                    Thread.sleep(3000);
//                }
//                socketChannel.close();
//            } catch (IOException | InterruptedException e) {
//                e.printStackTrace();
//            }
//
//        }).start();
//    }

    public static void main(String[] args) {
        Map<String, String> map = new HashMap<>();
        map.put("1", "1");
        map.put("2", "2");
        System.out.println(map.size());
        map.remove("2");
        System.out.println(map.size());
        map.remove("2");
        System.out.println(map.size());
    }

}
