# This program reads the memory trace log file generated by the qemu and generate samples of the data sequences of
# the communication between the core and the memory system. Contact Xingyu(xingyuli9961@berkeley.edu) for more
# information.

import sys
import random


def qemu_sample_memory_trace(file):
    print(f'Hi, I am processing {file}')
    f = open("/Users/xingyuli/Desktop/Logs/" + file, 'r')
    # set up return list
    read_array = []
    write_array = []
    # Randomly sample 2,000 unit of data from the logs per 100,000 count
    loop_counter = 0
    read_counter = 0
    write_counter = 0
    # loop_max = 100000
    # read_max = 1000
    # write_max = 1000
    loop_max = 10000000000
    read_max = 100
    write_max = 100

    random.seed(2)
    start_index = random.randint(0, 90000)
    print (start_index)

    line = f.readline().strip()
    while line != "":
        if loop_counter >= start_index:
            if read_counter < read_max or write_counter < write_max:
                line_list = line.split(" ")
                trace_type = line_list[0].split(":")[-1]
                if trace_type == "memory_region_ops_read" and read_counter < read_max:
                    read_array.append(line_list[8])
                    read_counter += 1
                elif trace_type == "memory_region_ops_write" and write_counter < write_max:
                    write_array.append(line_list[8])
                    write_counter += 1
        loop_counter += 1
        if loop_counter >= loop_max:
            loop_counter = 0
            read_counter = 0
            write_counter = 0
            start_index = random.randint(0, 90000)
        line = f.readline().strip()

    f.close()

    return read_array, write_array


if __name__ == '__main__':
    # filename = str(sys.argv[1])
    filename = "631.deepsjeng_s.txt"
    read_samples, write_samples = qemu_sample_memory_trace(filename)
    print(read_samples)
    print(write_samples)
    print("Work Done.")