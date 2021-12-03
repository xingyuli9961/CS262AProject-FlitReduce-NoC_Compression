# Tile link has the space to indicate if the data is compressed or not, so we will send the original data is it's not
# compressed.
import dataclasses

import compresor_lib

# header is 8 bit
def zero_compression_64(file):
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
        header, compressed_data = compresor_lib.zero_compressor(data)
        if header == 0:
            new_flits += 8
        else:
            new_flits += 1 + len(compressed_data)

    print("The new number of the flits is " + str(new_flits))
    print("The compression rate in number of flits is " + str(new_flits / origin_flits))


# header is 16 bit
def zero_compression_32(file):
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
        header, compressed_data = compresor_lib.zero_compressor(data)
        if header == 0:
            new_flits += 8
        else:
            new_flits += 1 + len(compressed_data) // 2

    print("The new number of the flits is " + str(new_flits))
    print("The compression rate in number of flits is " + str(new_flits / origin_flits))


# header is 32 bit
def zero_compression_16(file):
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
            data.append(tmp >> 48)
            data.append((tmp << 16) >> 48)
            data.append((tmp << 32) >> 48)
            data.append((tmp << 48) >> 48)
        header, compressed_data = compresor_lib.zero_compressor(data)
        if header == 0:
            new_flits += 8
        else:
            if len(compressed_data) % 4 == 3:
                new_flits += 2 + len(compressed_data) // 4
            else:
                new_flits += 1 + len(compressed_data) // 4

    print("The new number of the flits is " + str(new_flits))
    print("The compression rate in number of flits is " + str(new_flits / origin_flits))


# header is 64 bit
def zero_compression_8(file):
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
            data.append(tmp >> 56)
            data.append((tmp << 8) >> 56)
            data.append((tmp << 16) >> 56)
            data.append((tmp << 24) >> 56)
            data.append((tmp << 32) >> 56)
            data.append((tmp << 40) >> 56)
            data.append((tmp << 48) >> 56)
            data.append((tmp << 56) >> 56)
        header, compressed_data = compresor_lib.zero_compressor(data)
        if header == 0:
            new_flits += 8
        else:
            if len(compressed_data) % 8 > 0:
                new_flits += 2 + len(compressed_data) // 8
            else:
                new_flits += 1 + len(compressed_data) // 8

    print("The new number of the flits is " + str(new_flits))
    print("The compression rate in number of flits is " + str(new_flits / origin_flits))


if __name__ == '__main__':
    # filename = str(sys.argv[1])
    file_list = ['401.bzip2.sample', '450.soplex.sample', '470.lbm.sample', '429.mcf.sample', '458.sjeng.sample']
    for filename in file_list:
        zero_compression_8(filename)
    print("Work Done.")