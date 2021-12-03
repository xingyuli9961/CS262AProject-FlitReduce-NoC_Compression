import compresor_lib


def zero_compression(file):
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
        new_flits += 1 + len(compressed_data)

    print("The new number of the flits is " + str(new_flits))
    print("The compression rate in number of flits is " + str(new_flits / origin_flits))


if __name__ == '__main__':
    # filename = str(sys.argv[1])
    file_list = ['401.bzip2.sample', '450.soplex.sample', '470.lbm.sample', '429.mcf.sample', '458.sjeng.sample']
    for filename in file_list:
        zero_compression(filename)
    print("Work Done.")