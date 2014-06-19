/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package granolasdr;

import edu.emory.mathcs.jtransforms.fft.FloatFFT_1D;
import java.io.*;
import java.net.*;
import java.nio.ByteBuffer;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.logging.Level;
import java.util.logging.Logger;

class TCPClient implements Runnable {

    private final OutputStream outToServer;
    private final BufferedInputStream sampleServer;
    private final LinkedBlockingQueue<byte[]> sampleBuffer;
    public static final int N = 512;
    private volatile int numDropped = 0;
    private final FloatFFT_1D fft = new FloatFFT_1D(N);
    private final float[] working = new float[2 * N];
    private final int bufferDepth = 1000;

    public static enum RxCommand {

        CenterFreq(1), SampleRate(2), GainMode(3), Gain(4), FreqCorrection(5), IFGain(6),
        TestMode(7), AGCMode(8), DirectSampling(9), OffsetTuning(10),
        rtlXtal(11), TunerXtal(12), TunerGain(13);
        private final byte command;

        private RxCommand(int cmd) {
            command = (byte) cmd;
        }
    }

    public TCPClient() throws IOException {
        this.sampleBuffer = new LinkedBlockingQueue<byte[]>();
        Socket clientSocket = new Socket("localhost", 1234);
        outToServer = new BufferedOutputStream(clientSocket.getOutputStream());
        sampleServer = new BufferedInputStream(clientSocket.getInputStream());
    }

    public double[] getSamplesMag() throws InterruptedException {
        byte[] data = sampleBuffer.take();
        for (int cnt = 0; cnt < 2 * N; cnt++) {
            working[cnt] = (float) data[cnt];
        }
        fft.complexForward(working);
        double[] ret = new double[N];
        for (int cnt = 0; cnt < N; cnt++) {
            double val = working[2 * cnt] * working[2 * cnt] + working[2 * cnt + 1] * working[2 * cnt + 1];
            if (val != 0) {
                ret[cnt] = 10 * Math.log10(val);
            }
        }
        return ret;
    }

    public byte[] getNextSamples() throws InterruptedException {
        byte[] data = sampleBuffer.take();
        return data;
    }

    public int getNumDropped() {
        int ret = numDropped;
        sampleBuffer.clear();
        numDropped = 0;
        return ret;
    }

    @Override
    public void run() {
        byte[] samples = new byte[8192];
        byte[] buffer = new byte[2 * N];
        int num;
        int last = 0;
        try {
            float inv = 1f;
            while (!Thread.interrupted()) {
                num = sampleServer.read(samples) >> 1;
                for (int cnt = 0; cnt < num; cnt++) {
                    buffer[last++] = (byte) (((samples[2 * cnt] & 0xff) - 127.0f) * inv);
                    buffer[last++] = (byte) (((samples[2 * cnt + 1] & 0xff) - 127.0f) * inv);
                    inv = -inv;
                    if (last >= 2 * N) {
                        last = 0;
                        if (sampleBuffer.size() < bufferDepth) {
                            sampleBuffer.add(buffer);
                            buffer = new byte[2 * N];
                        } else {
                            numDropped++;
//                            System.out.println("On the floor");
                        }
                    }
                }
            }
        } catch (IOException ex) {
            Logger.getLogger(TCPClient.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    public void sendCommand(RxCommand command, int value) throws IOException {
        byte[] cmdbuf = new byte[5];
        ByteBuffer cmd = ByteBuffer.wrap(cmdbuf);
        cmd.put(command.command);
        cmd.putInt(value);
        outToServer.write(cmd.array());
        outToServer.flush();
    }

}
