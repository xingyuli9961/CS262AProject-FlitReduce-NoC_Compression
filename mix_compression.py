import compresor_lib


def mix_compression(file):
    print(f'Compressing {file}:')
    f = open("/Users/xingyuli/Desktop/Logs/spikeLogs/samples/" + file, 'r')
    lines = f.readlines()
    origin_length = len(lines)
    origin_flits = origin_length * 8
    print("The origin number of the flits is " + str(origin_flits))
    new_flits = 0

    for line in lines:
        tmp_data = eval(line[:-1])
        data1 = [int(x, base=16) for x in tmp_data]
        base_flit, offset_flits, delta_flits, mask = compresor_lib.adapted_no_delta_compressor(data1)
        abnd_size = min(8, 1 + 1 + len(delta_flits))
        data2 = []
        for x in tmp_data:
            tmp = int(x, base=16)
            data2.append(tmp >> 48)
            data2.append((tmp << 16) >> 48)
            data2.append((tmp << 32) >> 48)
            data2.append((tmp << 48) >> 48)
        header, compressed_data = compresor_lib.zero_compressor(data2)
        data3 = []
        for x in tmp_data:
            tmp = int(x, base=16)
            data3.append(tmp >> 32)
            data3.append((tmp << 32) >> 32)
        fpc_size, fpc_prefix, fpc_data = compresor_lib.frequent_pattern_compressor(data3)

        z_size = 0
        if header == 0:
            z_size = 8
        else:
            if len(compressed_data) % 4 == 3:
                z_size = 2 + len(compressed_data) // 4
            else:
                z_size = 1 + len(compressed_data) // 4

        f_size = 0
        if fpc_size >= 512:
            f_size = 8
        else:
            if fpc_size % 64 == 0:
                f_size = fpc_size // 64
            else:
                f_size = 1 + fpc_size // 64

        new_flits += min(abnd_size, z_size, f_size)

    print("The new number of the flits is " + str(new_flits))
    print("The compression rate in number of flits is " + str(new_flits / origin_flits))


if __name__ == '__main__':
    # filename = str(sys.argv[1])
    file_list = ['401.bzip2.sample', '450.soplex.sample', '470.lbm.sample', '429.mcf.sample', '458.sjeng.sample']
    for filename in file_list:
        mix_compression(filename)
    print("Work Done.")







