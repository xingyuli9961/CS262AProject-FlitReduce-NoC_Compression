def trace_compression(file):
    print(f'Hi, I am compressing {file}')
    f = open("/Users/xingyuli/Desktop/Logs/spikeLogs/samples/" + file, 'r')
    lines = f.readlines()
    origin_length = len(lines)
    print(origin_length)






if __name__ == '__main__':
    # filename = str(sys.argv[1])
    filename = "458.sjeng.sample"
    trace_compression(filename)
    print("Work Done.")