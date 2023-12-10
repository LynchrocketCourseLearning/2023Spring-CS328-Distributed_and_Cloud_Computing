import os
import time

files = ['mat_v1', 'mat_v2', 'mat_cache_v2', 'mat_v3', 'mat_cache_v3', 'mat_v4', 'mat_v5', 'mat_v6']
processes = [1, 2, 4, 8, 16, 32]
os.system('echo " " > ./logs/s_logs')
for f in files:
	for p in processes:
		if p == 1 and f == 'mat_v1':
			continue
		os.system(f'echo "file: {f}, process: {p}" >> ./logs/s_logs && mpirun --oversubscribe -np {p} ./build/{f} >> ./logs/s_logs && echo "\n" >> ./logs/s_logs')
