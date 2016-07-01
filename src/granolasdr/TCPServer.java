/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package granolasdr;

import java.io.*;
import java.net.*;
import java.util.logging.Level;
import java.util.logging.Logger;

class TCPServer implements Runnable {

    private final ServerSocket rtlSocket;
    private final File sendFile;

    public TCPServer(File sendFile) throws IOException {
        rtlSocket = new ServerSocket(1234);
        this.sendFile = sendFile;
    }

    @Override
    public void run() {
        while (!Thread.interrupted()) {
            try {
                Socket clientSocket = rtlSocket.accept();
                OutputStream outToServer = new BufferedOutputStream(clientSocket.getOutputStream());
                BufferedInputStream sampleServer = new BufferedInputStream(clientSocket.getInputStream());
                startDumper(sampleServer);
                FileInputStream fin = new FileInputStream(sendFile);

                byte[] samples = new byte[8192];
                byte[] buffer = new byte[8192];
                int num;

                float inv = 1f;
                while (!Thread.interrupted()) {
                    
                    try {
                        num = fin.read(samples) >> 1;
                        if (num <= 0) {
                            System.out.println("Resetting the file");
                            fin.close();
                            fin = new FileInputStream(sendFile);
                            num = fin.read(samples) >> 1;
                        }
                    } catch (IOException ex) {
                        System.out.println("Resetting the file");
                        fin.close();
                        fin = new FileInputStream(sendFile);
                        num = fin.read(samples) >> 1;
                    }
                    for (int cnt = 0; cnt < num; cnt++) {
                        // Note that I stored samples with dc in center, move back to zero with inv for rtl
                        buffer[2 * cnt] = (byte) (((samples[2 * cnt] & 0xff) + 127.0f) * inv);
                        buffer[2 * cnt + 1] = (byte) (((samples[2 * cnt + 1] & 0xff) + 127.0f) * inv);
                        inv = -inv;
                    }
                    outToServer.write(buffer, 0, num);
                    try {
                        Thread.sleep(2);
                    } catch (InterruptedException ex) {
                        Logger.getLogger(TCPServer.class.getName()).log(Level.SEVERE, null, ex);
                    }
                }

            } catch (IOException ex) {
                Logger.getLogger(TCPServer.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
    }

    // Gets commands from socket and drops them on the floor.
    public void startDumper(final BufferedInputStream sampleServer) {
        Thread dumpthread = new Thread() {

            @Override
            public void run() {

                try {
                    while (!Thread.interrupted()) {
                        byte[] samples = new byte[8192];
                        int num = sampleServer.read(samples) >> 1;
                        if (num > 0) {
                        System.out.println("Got a command from client " + num + " bytes");
                        } else {
                            Thread.sleep(1000);
                        }
                    }
                } catch (IOException ex) {
                    Logger.getLogger(TCPServer.class.getName()).log(Level.SEVERE, null, ex);
                } catch (InterruptedException ex) {
                    Logger.getLogger(TCPServer.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
        };
        dumpthread.setName("Dump Thread");
        dumpthread.setDaemon(true);
        dumpthread.start();
    }

}
