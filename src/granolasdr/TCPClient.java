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
    private final LinkedBlockingQueue<float[]> sampleBuffer;
    public static final int N = 512;
    private final FloatFFT_1D fft = new FloatFFT_1D(N);

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
        this.sampleBuffer = new LinkedBlockingQueue<float[]>();
        Socket clientSocket = new Socket("localhost", 1234);
        outToServer = new BufferedOutputStream(clientSocket.getOutputStream());
        sampleServer = new BufferedInputStream(clientSocket.getInputStream());
    }

    public float[] getSamplesFreq() throws InterruptedException {
        float[] data = sampleBuffer.take();
        fft.complexForward(data);
        return data;
    }

    public double[] getSamplesMag() throws InterruptedException {
        float[] data = sampleBuffer.take();
        fft.complexForward(data);
        double[] ret = new double[N];
        for (int cnt = 0; cnt < N; cnt++) {
            double val = data[2 * cnt] * data[2 * cnt] + data[2 * cnt + 1] * data[2 * cnt + 1];
            if (val != 0) {
                ret[cnt] = 10 * Math.log10(val);
            }
        }
        return ret;
    }

    @Override
    public void run() {
        byte[] samples = new byte[8192];
        float[] buffer = new float[2 * N];
        int num;
        int last = 0;
        try {
            float inv = 1f;
            while (!Thread.interrupted()) {
                num = sampleServer.read(samples) >> 1;
                for (int cnt = 0; cnt < num; cnt++) {
                    buffer[last++] = ((samples[2*cnt] & 0xff) - 127.0f)*inv;
                    buffer[last++] = ((samples[2*cnt+1] & 0xff) - 127.0f)*inv;
                    inv = -inv;
                    if (last >= 2 * N) {
                        last = 0;
                        if (sampleBuffer.size() < 10) {
                            sampleBuffer.add(buffer);
                            buffer = new float[2 * N];
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
