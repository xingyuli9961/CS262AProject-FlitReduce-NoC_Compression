import compresor_lib


# Separate the 64 bytes cache line in 64bit words, and the offset is 1 byte.
def nodelta_compression1(file):
    print(f'Compressing {file}:')
    f = open("/Users/xingyuli/Desktop/Logs/spikeLogs/samples/" + file, 'r')
    lines = f.readlines()
    origin_length = len(lines)
    origin_flits = origin_length * 8
    print("The origin number of the flits is " + str(origin_flits))
    new_flits = 0

    for line in lines:
        tmp_data = eval(line[:-1])
        data = [int(x, base=16) for x in tmp_data]
        ok, base, compressed_offsets = compresor_lib.no_delta_compressor1(data)
        if not ok:
            new_flits += 8
        else:
            new_flits += 2

    print("The new number of the flits is " + str(new_flits))
    print("The compression rate in number of flits is " + str(new_flits / origin_flits))


# Separate the 64 bytes cache line in 64bit words, and the offset is 2 byte.
def nodelta_compression2(file):
    print(f'Compressing {file}:')
    f = open("/Users/xingyuli/Desktop/Logs/spikeLogs/samples/" + file, 'r')
    lines = f.readlines()
    origin_length = len(lines)
    origin_flits = origin_length * 8
    print("The origin number of the flits is " + str(origin_flits))
    new_flits = 0

    for line in lines:
        tmp_data = eval(line[:-1])
        data = [int(x, base=16) for x in tmp_data]
        ok, base, compressed_offsets = compresor_lib.no_delta_compressor2(data)
        if not ok:
            new_flits += 8
        else:
            new_flits += 3

    print("The new number of the flits is " + str(new_flits))
    print("The compression rate in number of flits is " + str(new_flits / origin_flits))


# Separate the 64 bytes cache line in 64bit words, and the offset is 1 byte.
def nodelta_compression3(file):
    print(f'Compressing {file}:')
    f = open("/Users/xingyuli/Desktop/Logs/spikeLogs/samples/" + file, 'r')
    lines = f.readlines()
    origin_length = len(lines)
    origin_flits = origin_length * 8
    print("The origin number of the flits is " + str(origin_flits))
    new_flits = 0

    for line in lines:
        tmp_data = eval(line[:-1])
        data = [int(x, base=16) for x in tmp_data]
        ok1, base1, compressed_offsets1 = compresor_lib.no_delta_compressor1(data[:4])
        ok2, base2, compressed_offsets2 = compresor_lib.no_delta_compressor1(data[4:])
        if ok1 and ok2:
            new_flits += 3
        elif ok1 and (not ok2):
            new_flits += 6
        elif (not ok1) and ok2:
            new_flits += 6
        else:
            new_flits += 8

    print("The new number of the flits is " + str(new_flits))
    print("The compression rate in number of flits is " + str(new_flits / origin_flits))


# Separate the 64 bytes cache line in 64bit words, and the offset is 1 byte.
def nodelta_compression4(file):
    print(f'Compressing {file}:')
    f = open("/Users/xingyuli/Desktop/Logs/spikeLogs/samples/" + file, 'r')
    lines = f.readlines()
    origin_length = len(lines)
    origin_flits = origin_length * 8
    print("The origin number of the flits is " + str(origin_flits))
    new_flits = 0

    for line in lines:
        tmp_data = eval(line[:-1])
        data = [int(x, base=16) for x in tmp_data]
        ok1, base1, compressed_offsets1 = compresor_lib.no_delta_compressor2(data[:4])
        ok2, base2, compressed_offsets2 = compresor_lib.no_delta_compressor2(data[4:])
        if ok1 and ok2:
            new_flits += 4
        elif ok1 and (not ok2):
            new_flits += 6
        elif (not ok1) and ok2:
            new_flits += 6
        else:
            new_flits += 8

    print("The new number of the flits is " + str(new_flits))
    print("The compression rate in number of flits is " + str(new_flits / origin_flits))


if __name__ == '__main__':
    # filename = str(sys.argv[1])
    file_list = ['401.bzip2.sample', '450.soplex.sample', '470.lbm.sample', '429.mcf.sample', '458.sjeng.sample']
    for filename in file_list:
        nodelta_compression4(filename)
    print("Work Done.")