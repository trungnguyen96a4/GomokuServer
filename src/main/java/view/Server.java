package view;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.util.Iterator;
import java.util.Set;

public class Server implements Runnable {

    private int port;
    private Selector selector;

    private Callback callback = null;

    public Server(int port) {
        this.port = port;
    }

    public void start() {
        new Thread(this).start();
    }

    public void setCallback(Callback callback) {
        this.callback = callback;
    }

    @Override
    public void run() {
        try {
            selector = Selector.open();
            ServerSocketChannel serverSocketChannel = ServerSocketChannel.open();
            serverSocketChannel.bind(new InetSocketAddress(port));
            serverSocketChannel.configureBlocking(false);
            serverSocketChannel.register(selector, SelectionKey.OP_ACCEPT);

            while (true) {
                selector.select();
                Set<SelectionKey> selectionKeys = selector.selectedKeys();
                Iterator<SelectionKey> iterator = selectionKeys.iterator();
                while (iterator.hasNext()) {
                    SelectionKey key = iterator.next();
                    if (key.isAcceptable()) {
                        callback.accept(serverSocketChannel, selector);
                    } else if (key.isReadable()) {
                        callback.receiveData(key);
                    }
                    iterator.remove();
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public interface Callback {
        void accept(ServerSocketChannel serverSocketChannel, Selector selector);

        void receiveData(SelectionKey key);
    }
}
