import os
import time

files = ['mat_v1', 'mat_v2', 'mat_cache_v2', 'mat_v3', 'mat_cache_v3', 'mat_v4', 'mat_v5', 'mat_v6']
processes = [1, 2, 4, 8, 16, 32]
os.system('echo " " > ./logs/d_logs')
for f in files:
	for p in processes:
		if p == 1 and f == 'mat_v1':
			continue
		os.system(f'echo "file: {f}, process: {p}" >> ./logs/d_logs && mpirun --oversubscribe -np {p} -N {1 if p//2==0 else p//2} --host 192.168.56.101,192.168.56.102 -mca btl_tcp_if_include 192.168.56.0/24 ./build/{f} >> ./logs/d_logs && echo "\n" >> ./logs/d_logs')
