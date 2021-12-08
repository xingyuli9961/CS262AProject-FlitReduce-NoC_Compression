import compresor_lib


# This is the original configuration of the frequent pattern compression algorithm proposed in the paper
def frequent_pattern_compression(file):
    print(f'Compressing {file}:')
    f = open("/Users/xingyuli/Desktop/Logs/spikeLogs/samples/" + file, 'r')
    lines = f.readlines()
    origin_length = len(lines)
    origin_flits = origin_length * 8
    print("The origin number of the flits is " + str(origin_flits))
    new_flits = 0

    for line in lines:
        tmp_data = eval(line[:-1])
        data = []
        for x in tmp_data:
            tmp = int(x, base=16)
            data.append(tmp >> 32)
            data.append((tmp << 32) >> 32)
        tmp_size, prefix, new_data = compresor_lib.frequent_pattern_compressor(data)
        if tmp_size >= 512:
            new_flits += 8
        else:
            if tmp_size % 64 == 0:
                new_flits += tmp_size // 64
            else:
                new_flits += 1 + tmp_size // 64

    print("The new number of the flits is " + str(new_flits))
    print("The compression rate in number of flits is " + str(new_flits / origin_flits))


if __name__ == '__main__':
    # filename = str(sys.argv[1])
    file_list = ['401.bzip2.sample', '450.soplex.sample', '470.lbm.sample', '429.mcf.sample', '458.sjeng.sample']
    for filename in file_list:
        frequent_pattern_compression(filename)
    print("Work Done.")