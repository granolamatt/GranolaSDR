clear all
close all

file = fopen("test.out","r");
[val2, count] = fread(file,"char");
N = 1024;

fclose(file);

val = (val2(1:2:2*N) + j*val2(2:2:2*N))';
val(2:2:end) = -val(2:2:end);

m = zeros(1,N);
ff = fft(val);
ss = ff.*conj(ff);
plot(10*log10(ss))

