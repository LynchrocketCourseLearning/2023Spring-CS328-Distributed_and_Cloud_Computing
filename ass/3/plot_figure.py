import os
if not os.path.exists('./img'):
    os.mkdir('./img')
    
import numpy as np
import pandas as pd
import matplotlib.pyplot as plt
plt.rcParams['font.sans-serif'] = ['SimHei']

r5 = pd.read_csv('./result/r5.csv', parse_dates=['start_time', 'end_time'])

section1 = r5[r5['section']=='学府路(前海段)']
section2 = r5[r5['section']=='工业九路']
section3 = r5[r5['section']=='华明路']

section1_plot_data = section1.groupby(section1['start_time'].dt.time, as_index=True).agg({'count':'sum'})
section1_plot_data = section1_plot_data.reset_index()
section1_plot_data['start_time'] = section1_plot_data['start_time'].apply(lambda x: str(x))

plt.figure(figsize=(42,12))
plt.bar(section1_plot_data['start_time'], section1_plot_data['count'], width=0.6)
plt.title('学府路(前海段)', fontsize=32)
plt.xticks(size=18)
plt.yticks(size=22)
plt.xlabel('Time', fontsize=28)
plt.ylabel('Berthage in use count', fontsize=28)
plt.savefig('./img/学府路(前海段).png', bbox_inches='tight')


section2_plot_data = section2.groupby(section2['start_time'].dt.time, as_index=True).agg({'count':'sum'})
section2_plot_data = section2_plot_data.reset_index()
section2_plot_data['start_time'] = section2_plot_data['start_time'].apply(lambda x: str(x))

plt.figure(figsize=(42,12))
plt.bar(section2_plot_data['start_time'], section2_plot_data['count'], width=0.6)
plt.title('工业九路', fontsize=32)
plt.xticks(size=18)
plt.yticks(size=22)
plt.xlabel('Time', fontsize=28)
plt.ylabel('Berthage in use count', fontsize=28)
plt.savefig('./img/工业九路.png', bbox_inches='tight')


section3_plot_data = section3.groupby(section3['start_time'].dt.time, as_index=True).agg({'count':'sum'})
section3_plot_data = section3_plot_data.reset_index()
section3_plot_data['start_time'] = section3_plot_data['start_time'].apply(lambda x: str(x))

plt.figure(figsize=(42,12))
plt.bar(section3_plot_data['start_time'], section3_plot_data['count'], width=0.6)
plt.title('华明路', fontsize=32)
plt.xticks(size=18)
plt.yticks(size=22)
plt.xlabel('Time', fontsize=28)
plt.ylabel('Berthage in use count', fontsize=28)
plt.savefig('./img/华明路.png', bbox_inches='tight')