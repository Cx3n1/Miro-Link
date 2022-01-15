package miro.link.runnables;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.net.ServerSocket;
import java.net.Socket;

import static org.junit.jupiter.api.Assertions.*;

class ServerRunnableTest {

    private ServerRunnable serverRunnable;

    @BeforeEach
    void setUp() {
        serverRunnable = new ServerRunnable();
    }

}