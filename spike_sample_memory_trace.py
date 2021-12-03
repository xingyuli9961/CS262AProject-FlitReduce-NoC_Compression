import sys
import random


def spike_sample_memory_trace(file):
    print(f'Hi, I am processing {file}')
    f = open("/Users/xingyuli/Desktop/Logs/spikeLogs/build.riscv/" + file, 'r')
    out = open("/Users/xingyuli/Desktop/Logs/spikeLogs/tests/" + file[:-3] + "sample", 'w')

    # Randomly sample 1,000 unit of data from the logs per 100,000 count
    loop_counter = 0
    sample_counter = 0
    loop_max = 100000
    sample_max = 1000
    # This is the physical address >> 6
    prev_addr = 0

    random.seed(3)
    start_index = random.randint(0, 90000)
    line = f.readline().strip()
    while line is not None:
        if (loop_counter >= start_index) and (loop_counter < loop_max) and (sample_counter < sample_max):
            line_list = line.split(" ")
            if line_list[0] != 'LoadTrace:':
                line = f.readline().strip()
                continue
            cur_addr = int(line_list[2], base=16) >> 6
            if cur_addr == prev_addr:
                line = f.readline().strip()
                continue
            data = []
            for i in range(8):
                tmp = (int(line_list[3 + 2 * i], base=16) << 32) | int(line_list[4 + 2 * i], base=16)
                tmp = hex(tmp)
                data.append(tmp)
            out.write(str(data) + "\n")
            prev_addr = cur_addr
            sample_counter += 1
        loop_counter += 1
        if loop_counter >= loop_max:
            loop_counter = 0
            sample_counter = 0
            start_index = random.randint(0, 90000)
        line = f.readline().strip()

    f.close()
    out.close()


if __name__ == '__main__':
    # filename = str(sys.argv[1])
    filename = "429.mcf.out"
    spike_sample_memory_trace(filename)
    print("Work Done.")
