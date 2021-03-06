/* ***** BEGIN LICENSE BLOCK *****
 * Version: MPL 1.1/GPL 2.0/LGPL 2.1
 *
 * The contents of this file are subject to the Mozilla Public License Version
 * 1.1 (the "License"); you may not use this file except in compliance with
 * the License. You may obtain a copy of the License at
 * http://www.mozilla.org/MPL/
 *
 * Software distributed under the License is distributed on an "AS IS" basis,
 * WITHOUT WARRANTY OF ANY KIND, either express or implied. See the License
 * for the specific language governing rights and limitations under the
 * License.
 *
 * The Original Code is JTransforms.
 *
 * The Initial Developer of the Original Code is
 * Piotr Wendykier, Emory University.
 * Portions created by the Initial Developer are Copyright (C) 2007
 * the Initial Developer. All Rights Reserved.
 *
 * Alternatively, the contents of this file may be used under the terms of
 * either the GNU General Public License Version 2 or later (the "GPL"), or
 * the GNU Lesser General Public License Version 2.1 or later (the "LGPL"),
 * in which case the provisions of the GPL or the LGPL are applicable instead
 * of those above. If you wish to allow use of your version of this file only
 * under the terms of either the GPL or the LGPL, and not to allow others to
 * use your version of this file under the terms of the MPL, indicate your
 * decision by deleting the provisions above and replace them with the notice
 * and other provisions required by the GPL or the LGPL. If you do not delete
 * the provisions above, a recipient may use your version of this file under
 * the terms of any one of the MPL, the GPL or the LGPL.
 *
 * ***** END LICENSE BLOCK ***** */

package edu.emory.mathcs.jtransforms.fft;

import edu.emory.mathcs.utils.ConcurrencyUtils;
import edu.emory.mathcs.utils.IOUtils;

/**
 * Benchmark of single precision FFT's
 * 
 * @author Piotr Wendykier (piotr.wendykier@gmail.com)
 * 
 */
public class BenchmarkFloatFFT {

    private static int nthread = 2;

    private static int nsize = 6;

    private static int niter = 200;

    private static boolean doWarmup = true;

    private static int initialExponent1D = 17;

    private static int initialExponent2D = 7;

    private static int initialExponent3D = 2;

    private static boolean doScaling = false;

    private BenchmarkFloatFFT() {

    }

    public static void parseArguments(String[] args) {
        for (int i = 0; i < args.length; i++) {
            System.out.println("args[" + i + "]:" + args[i]);
        }

        if ((args == null) || (args.length != 10)) {
            System.out.println("Parameters: <number of threads> <THREADS_BEGIN_N_2D> <THREADS_BEGIN_N_3D> <number of iterations> <perform warm-up> <perform scaling> <number of sizes> <initial exponent for 1D transforms> <initial exponent for 2D transforms> <initial exponent for 3D transforms>");
            System.exit(-1);
        }
        nthread = Integer.parseInt(args[0]);
        ConcurrencyUtils.setThreadsBeginN_2D(Integer.parseInt(args[1]));
        ConcurrencyUtils.setThreadsBeginN_3D(Integer.parseInt(args[2]));
        niter = Integer.parseInt(args[3]);
        doWarmup = Boolean.parseBoolean(args[4]);
        doScaling = Boolean.parseBoolean(args[5]);
        nsize = Integer.parseInt(args[6]);
        initialExponent1D = Integer.parseInt(args[7]);
        initialExponent2D = Integer.parseInt(args[8]);
        initialExponent3D = Integer.parseInt(args[9]);
        ConcurrencyUtils.setNumberOfProcessors(nthread);
    }

    public static void benchmarkComplexForward_1D(int init_exp) {
        int[] sizes = new int[nsize];
        double[] times = new double[nsize];
        float[] x;
        for (int i = 0; i < nsize; i++) {
            int exponent = init_exp + i;
            int N = (int) Math.pow(2, exponent);
            sizes[i] = N;
            System.out.println("Complex forward FFT 1D of size 2^" + exponent);
            FloatFFT_1D fft = new FloatFFT_1D(N);
            x = new float[2 * N];
            if (doWarmup) { // call the transform twice to warm up
                IOUtils.fillMatrix_1D(2 * N, x);
                fft.complexForward(x);
                IOUtils.fillMatrix_1D(2 * N, x);
                fft.complexForward(x);
            }
            float av_time = 0;
            long elapsedTime = 0;
            for (int j = 0; j < niter; j++) {
                IOUtils.fillMatrix_1D(2 * N, x);
                elapsedTime = System.nanoTime();
                fft.complexForward(x);
                elapsedTime = System.nanoTime() - elapsedTime;
                av_time = av_time + elapsedTime;
            }
            times[i] = (double) av_time / 1000000.0 / (double) niter;
            System.out.println("\tAverage execution time: " + String.format("%.2f", av_time / 1000000.0 / (float) niter) + " msec");
            x = null;
            fft = null;
            System.gc();
        }
        IOUtils.writeFFTBenchmarkResultsToFile("benchmarkFloatComplexForwardFFT_1D.txt", nthread, niter, doWarmup, doScaling, sizes, times);

    }

    public static void benchmarkRealForward_1D(int init_exp) {
        int[] sizes = new int[nsize];
        double[] times = new double[nsize];
        float[] x;
        for (int i = 0; i < nsize; i++) {
            int exponent = init_exp + i;
            int N = (int) Math.pow(2, exponent);
            sizes[i] = N;
            System.out.println("Real forward FFT 1D of size 2^" + exponent);
            FloatFFT_1D fft = new FloatFFT_1D(N);
            x = new float[2 * N];
            if (doWarmup) { // call the transform twice to warm up
                IOUtils.fillMatrix_1D(N, x);
                fft.realForwardFull(x);
                IOUtils.fillMatrix_1D(N, x);
                fft.realForwardFull(x);
            }
            float av_time = 0;
            long elapsedTime = 0;
            for (int j = 0; j < niter; j++) {
                IOUtils.fillMatrix_1D(N, x);
                elapsedTime = System.nanoTime();
                fft.realForwardFull(x);
                elapsedTime = System.nanoTime() - elapsedTime;
                av_time = av_time + elapsedTime;
            }
            times[i] = (double) av_time / 1000000.0 / (double) niter;
            System.out.println("\tAverage execution time: " + String.format("%.2f", av_time / 1000000.0 / (float) niter) + " msec");
            x = null;
            fft = null;
            System.gc();
        }
        IOUtils.writeFFTBenchmarkResultsToFile("benchmarkFloatRealForwardFFT_1D.txt", nthread, niter, doWarmup, doScaling, sizes, times);

    }

    public static void benchmarkComplexForward_2D_input_1D(int init_exp) {
        int[] sizes = new int[nsize];
        double[] times = new double[nsize];
        float[] x;
        for (int i = 0; i < nsize; i++) {
            int exponent = init_exp + i;
            int N = (int) Math.pow(2, exponent);
            sizes[i] = N;
            System.out.println("Complex forward FFT 2D (input 1D) of size 2^" + exponent + " x 2^" + exponent);
            FloatFFT_2D fft2 = new FloatFFT_2D(N, N);
            x = new float[N * 2 * N];
            if (doWarmup) { // call the transform twice to warm up
                IOUtils.fillMatrix_2D(N, 2 * N, x);
                fft2.complexForward(x);
                IOUtils.fillMatrix_2D(N, 2 * N, x);
                fft2.complexForward(x);
            }
            float av_time = 0;
            long elapsedTime = 0;
            for (int j = 0; j < niter; j++) {
                IOUtils.fillMatrix_2D(N, 2 * N, x);
                elapsedTime = System.nanoTime();
                fft2.complexForward(x);
                elapsedTime = System.nanoTime() - elapsedTime;
                av_time = av_time + elapsedTime;
            }
            times[i] = (double) av_time / 1000000.0 / (double) niter;
            System.out.println("\tAverage execution time: " + String.format("%.2f", av_time / 1000000.0 / (float) niter) + " msec");
            x = null;
            fft2 = null;
            System.gc();
        }
        IOUtils.writeFFTBenchmarkResultsToFile("benchmarkFloatComplexForwardFFT_2D_input_1D.txt", nthread, niter, doWarmup, doScaling, sizes, times);
    }

    public static void benchmarkComplexForward_2D_input_2D(int init_exp) {
        int[] sizes = new int[nsize];
        double[] times = new double[nsize];
        float[][] x;
        for (int i = 0; i < nsize; i++) {
            int exponent = init_exp + i;
            int N = (int) Math.pow(2, exponent);
            sizes[i] = N;
            System.out.println("Complex forward FFT 2D (input 2D) of size 2^" + exponent + " x 2^" + exponent);
            FloatFFT_2D fft2 = new FloatFFT_2D(N, N);
            x = new float[N][2 * N];
            if (doWarmup) { // call the transform twice to warm up
                IOUtils.fillMatrix_2D(N, 2 * N, x);
                fft2.complexForward(x);
                IOUtils.fillMatrix_2D(N, 2 * N, x);
                fft2.complexForward(x);
            }
            float av_time = 0;
            long elapsedTime = 0;
            for (int j = 0; j < niter; j++) {
                IOUtils.fillMatrix_2D(N, 2 * N, x);
                elapsedTime = System.nanoTime();
                fft2.complexForward(x);
                elapsedTime = System.nanoTime() - elapsedTime;
                av_time = av_time + elapsedTime;
            }
            times[i] = (double) av_time / 1000000.0 / (double) niter;
            System.out.println("\tAverage execution time: " + String.format("%.2f", av_time / 1000000.0 / (float) niter) + " msec");
            x = null;
            fft2 = null;
            System.gc();
        }
        IOUtils.writeFFTBenchmarkResultsToFile("benchmarkFloatComplexForwardFFT_2D_input_2D.txt", nthread, niter, doWarmup, doScaling, sizes, times);
    }

    public static void benchmarkRealForward_2D_input_1D(int init_exp) {
        int[] sizes = new int[nsize];
        double[] times = new double[nsize];
        float[] x;
        for (int i = 0; i < nsize; i++) {
            int exponent = init_exp + i;
            int N = (int) Math.pow(2, exponent);
            sizes[i] = N;
            System.out.println("Real forward FFT 2D (input 1D) of size 2^" + exponent + " x 2^" + exponent);
            FloatFFT_2D fft2 = new FloatFFT_2D(N, N);
            x = new float[N * 2 * N];
            if (doWarmup) { // call the transform twice to warm up
                IOUtils.fillMatrix_2D(N, N, x);
                fft2.realForwardFull(x);
                IOUtils.fillMatrix_2D(N, N, x);
                fft2.realForwardFull(x);
            }
            float av_time = 0;
            long elapsedTime = 0;
            for (int j = 0; j < niter; j++) {
                IOUtils.fillMatrix_2D(N, N, x);
                elapsedTime = System.nanoTime();
                fft2.realForwardFull(x);
                elapsedTime = System.nanoTime() - elapsedTime;
                av_time = av_time + elapsedTime;
            }
            times[i] = (double) av_time / 1000000.0 / (double) niter;
            System.out.println("\tAverage execution time: " + String.format("%.2f", av_time / 1000000.0 / (float) niter) + " msec");
            x = null;
            fft2 = null;
            System.gc();
        }
        IOUtils.writeFFTBenchmarkResultsToFile("benchmarkFloatRealForwardFFT_2D_input_1D.txt", nthread, niter, doWarmup, doScaling, sizes, times);
    }

    public static void benchmarkRealForward_2D_input_2D(int init_exp) {
        int[] sizes = new int[nsize];
        double[] times = new double[nsize];
        float[][] x;
        for (int i = 0; i < nsize; i++) {
            int exponent = init_exp + i;
            int N = (int) Math.pow(2, exponent);
            sizes[i] = N;
            System.out.println("Real forward FFT 2D (input 2D) of size 2^" + exponent + " x 2^" + exponent);
            FloatFFT_2D fft2 = new FloatFFT_2D(N, N);
            x = new float[N][2 * N];
            if (doWarmup) { // call the transform twice to warm up
                IOUtils.fillMatrix_2D(N, N, x);
                fft2.realForwardFull(x);
                IOUtils.fillMatrix_2D(N, N, x);
                fft2.realForwardFull(x);
            }
            float av_time = 0;
            long elapsedTime = 0;
            for (int j = 0; j < niter; j++) {
                IOUtils.fillMatrix_2D(N, N, x);
                elapsedTime = System.nanoTime();
                fft2.realForwardFull(x);
                elapsedTime = System.nanoTime() - elapsedTime;
                av_time = av_time + elapsedTime;
            }
            times[i] = (double) av_time / 1000000.0 / (double) niter;
            System.out.println("\tAverage execution time: " + String.format("%.2f", av_time / 1000000.0 / (float) niter) + " msec");
            x = null;
            fft2 = null;
            System.gc();
        }
        IOUtils.writeFFTBenchmarkResultsToFile("benchmarkFloatRealForwardFFT_2D_input_2D.txt", nthread, niter, doWarmup, doScaling, sizes, times);
    }

    public static void benchmarkComplexForward_3D_input_1D(int init_exp) {
        int[] sizes = new int[nsize];
        double[] times = new double[nsize];
        float[] x;
        for (int i = 0; i < nsize; i++) {
            int exponent = init_exp + i;
            int N = (int) Math.pow(2, exponent);
            sizes[i] = N;
            System.out.println("Complex forward FFT 3D (input 1D) of size 2^" + exponent + " x 2^" + exponent + " x 2^" + exponent);
            FloatFFT_3D fft3 = new FloatFFT_3D(N, N, N);
            x = new float[N * N * 2 * N];
            if (doWarmup) { // call the transform twice to warm up
                IOUtils.fillMatrix_3D(N, N, 2 * N, x);
                fft3.complexForward(x);
                IOUtils.fillMatrix_3D(N, N, 2 * N, x);
                fft3.complexForward(x);
            }
            float av_time = 0;
            long elapsedTime = 0;
            for (int j = 0; j < niter; j++) {
                IOUtils.fillMatrix_3D(N, N, 2 * N, x);
                elapsedTime = System.nanoTime();
                fft3.complexForward(x);
                elapsedTime = System.nanoTime() - elapsedTime;
                av_time = av_time + elapsedTime;
            }
            times[i] = (double) av_time / 1000000.0 / (double) niter;
            System.out.println("\tAverage execution time: " + String.format("%.2f", av_time / 1000000.0 / (float) niter) + " msec");
            x = null;
            fft3 = null;
            System.gc();
        }
        IOUtils.writeFFTBenchmarkResultsToFile("benchmarkFloatComplexForwardFFT_3D_input_1D.txt", nthread, niter, doWarmup, doScaling, sizes, times);
    }

    public static void benchmarkComplexForward_3D_input_3D(int init_exp) {
        int[] sizes = new int[nsize];
        double[] times = new double[nsize];
        float[][][] x;
        for (int i = 0; i < nsize; i++) {
            int exponent = init_exp + i;
            int N = (int) Math.pow(2, exponent);
            sizes[i] = N;
            System.out.println("Complex forward FFT 3D (input 3D) of size 2^" + exponent + " x 2^" + exponent + " x 2^" + exponent);
            FloatFFT_3D fft3 = new FloatFFT_3D(N, N, N);
            x = new float[N][N][2 * N];
            if (doWarmup) { // call the transform twice to warm up
                IOUtils.fillMatrix_3D(N, N, 2 * N, x);
                fft3.complexForward(x);
                IOUtils.fillMatrix_3D(N, N, 2 * N, x);
                fft3.complexForward(x);
            }
            float av_time = 0;
            long elapsedTime = 0;
            for (int j = 0; j < niter; j++) {
                IOUtils.fillMatrix_3D(N, N, 2 * N, x);
                elapsedTime = System.nanoTime();
                fft3.complexForward(x);
                elapsedTime = System.nanoTime() - elapsedTime;
                av_time = av_time + elapsedTime;
            }
            times[i] = (double) av_time / 1000000.0 / (double) niter;
            System.out.println("\tAverage execution time: " + String.format("%.2f", av_time / 1000000.0 / (float) niter) + " msec");
            x = null;
            fft3 = null;
            System.gc();
        }
        IOUtils.writeFFTBenchmarkResultsToFile("benchmarkFloatComplexForwardFFT_3D_input_3D.txt", nthread, niter, doWarmup, doScaling, sizes, times);
    }

    public static void benchmarkRealForward_3D_input_1D(int init_exp) {
        int[] sizes = new int[nsize];
        double[] times = new double[nsize];
        float[] x;
        for (int i = 0; i < nsize; i++) {
            int exponent = init_exp + i;
            int N = (int) Math.pow(2, exponent);
            sizes[i] = N;
            System.out.println("Real forward FFT 3D (input 1D) of size 2^" + exponent + " x 2^" + exponent + " x 2^" + exponent);
            FloatFFT_3D fft3 = new FloatFFT_3D(N, N, N);
            x = new float[N * N * 2 * N];
            if (doWarmup) { // call the transform twice to warm up
                IOUtils.fillMatrix_3D(N, N, N, x);
                fft3.realForwardFull(x);
                IOUtils.fillMatrix_3D(N, N, N, x);
                fft3.realForwardFull(x);
            }
            float av_time = 0;
            long elapsedTime = 0;
            for (int j = 0; j < niter; j++) {
                IOUtils.fillMatrix_3D(N, N, N, x);
                elapsedTime = System.nanoTime();
                fft3.realForwardFull(x);
                elapsedTime = System.nanoTime() - elapsedTime;
                av_time = av_time + elapsedTime;
            }
            times[i] = (double) av_time / 1000000.0 / (double) niter;
            System.out.println("\tAverage execution time: " + String.format("%.2f", av_time / 1000000.0 / (float) niter) + " msec");
            x = null;
            fft3 = null;
            System.gc();
        }
        IOUtils.writeFFTBenchmarkResultsToFile("benchmarkFloatRealForwardFFT_3D_input_1D.txt", nthread, niter, doWarmup, doScaling, sizes, times);
    }

    public static void benchmarkRealForward_3D_input_3D(int init_exp) {
        int[] sizes = new int[nsize];
        double[] times = new double[nsize];
        float[][][] x;
        for (int i = 0; i < nsize; i++) {
            int exponent = init_exp + i;
            int N = (int) Math.pow(2, exponent);
            sizes[i] = N;
            System.out.println("Real forward FFT 3D (input 3D) of size 2^" + exponent + " x 2^" + exponent + " x 2^" + exponent);
            FloatFFT_3D fft3 = new FloatFFT_3D(N, N, N);
            x = new float[N][N][2 * N];
            if (doWarmup) { // call the transform twice to warm up
                IOUtils.fillMatrix_3D(N, N, N, x);
                fft3.realForwardFull(x);
                IOUtils.fillMatrix_3D(N, N, N, x);
                fft3.realForwardFull(x);
            }
            float av_time = 0;
            long elapsedTime = 0;
            for (int j = 0; j < niter; j++) {
                IOUtils.fillMatrix_3D(N, N, N, x);
                elapsedTime = System.nanoTime();
                fft3.realForwardFull(x);
                elapsedTime = System.nanoTime() - elapsedTime;
                av_time = av_time + elapsedTime;
            }
            times[i] = (double) av_time / 1000000.0 / (double) niter;
            System.out.println("\tAverage execution time: " + String.format("%.2f", av_time / 1000000.0 / (float) niter) + " msec");
            x = null;
            fft3 = null;
            System.gc();
        }
        IOUtils.writeFFTBenchmarkResultsToFile("benchmarkFloatRealForwardFFT_3D_input_3D.txt", nthread, niter, doWarmup, doScaling, sizes, times);
    }

    public static void main(String[] args) {
        parseArguments(args);
        benchmarkComplexForward_1D(initialExponent1D);
        benchmarkRealForward_1D(initialExponent1D);
        benchmarkComplexForward_2D_input_1D(initialExponent2D);
        benchmarkComplexForward_2D_input_2D(initialExponent2D);
        benchmarkRealForward_2D_input_1D(initialExponent2D);
        benchmarkRealForward_2D_input_2D(initialExponent2D);
        benchmarkComplexForward_3D_input_1D(initialExponent3D);
        benchmarkComplexForward_3D_input_3D(initialExponent3D);
        benchmarkRealForward_3D_input_1D(initialExponent3D);
        benchmarkRealForward_3D_input_3D(initialExponent3D);
        System.exit(0);

    }
}
