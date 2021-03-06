#!/usr/bin/env python
# coding: utf-8

import matplotlib as mpl
import matplotlib.pyplot as plt
import numpy as np
import pandas as pd

lockThroughputs = np.array([[17523717.260, 23621735.866, 53329259.558, 47328586.548, 53415581.041, 56980842.990],
                            [15590099.303, 10762974.397, 8475885.328, 10191880.763, 52891861.677, 5881990.940],
                            [14942658.867, 9292536.929, 5576432.267, 10241944.163, 49535991.357, 4074534.897],
                            [14816880.575, 4340903.210, 4210607.829, 8185921.362, 47966844.255, 4426907.605]
                            ])

frame = pd.DataFrame(lockThroughputs)

frame.rename(index={0:'1 thread',1:'3 threads', 2:'5 threads', 3:'8 threads'},
             inplace=True)

frame.columns = ['AdaptiveBackoff', 'Q_CLH', 'CheckCheck', 'Q_MCS', 'SimpleBackOff', 'Vanilla']

frame


mpl.style.use('dark_background')

# Data for plotting
t = [1, 3, 5, 8]

adaptiveBackoff = [17523717.260, 15590099.303, 14942658.867, 14816880.575]
simpleBackoff = [53415581.041, 52891861.677, 49535991.357, 47966844.255]
vanilla = [56980842.990, 5881990.940, 4074534.897, 4426907.605]
checKCheck = [53329259.558, 8475885.328, 5576432.267, 4210607.829]
q_CLH = [23621735.866, 10762974.397, 9292536.929, 4340903.210]
q_MCS = [47328586.548, 10191880.763, 10241944.163, 8185921.362]

# ------------plotting the vanilla locks -------------------

fig1, ax1 = plt.subplots()

ax1.plot(t, vanilla, color='#33bbff', marker='o', ls='-', linewidth=1,
         markersize=10, label='Vanilla')

ax1.plot(t, checKCheck, color='#ff3333', marker='o', ls='--', linewidth=1,
         markersize=10, label='ChecKCheck')

ax1.set(xlabel='number of threads', ylabel='throughput (ops/sec)',
        title='Spin Locks Performance')

ax1.plot(t, adaptiveBackoff, color='#cc9966', marker='o', ls='-', linewidth=1,
         markersize=10, label='AdaptiveBackoff')

ax1.plot(t, simpleBackoff, color='#b8b894', marker='o', ls='--', linewidth=1,
         markersize=10, label='SimpleBackoff')

ax1.plot(t, q_CLH, color='#00e6ac', marker='o', ls='-', linewidth=1,
         markersize=10, label='Q_CLH')

ax1.plot(t, q_MCS, color='#668cff', marker='o', ls='--', linewidth=1,
         markersize=10, label='Q_MCS')

ax1.get_yaxis().get_major_formatter().set_scientific(False)
plt.grid(linestyle=':')
legend = ax1.legend(loc='center_right', shadow=False, fontsize='x-small')
plt.show()
